import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import com.example.helloworld.{GreeterServiceGrpc, HelloReply, HelloRequest}
import com.github.phisgr.gatling.grpc.Predef._
import com.github.phisgr.gatling.grpc.action.GrpcCallActionBuilder
import com.github.phisgr.gatling.grpc.protocol.GrpcProtocol
import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioBuilder
import io.grpc._
import io.grpc.stub.{ClientCallStreamObserver, ClientCalls, ClientResponseObserver}

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.util.Try

class HelloSimulation extends Simulation {

  val config: Config = ConfigFactory.defaultApplication()

  val grpcConf: GrpcProtocol =
    grpc(ManagedChannelBuilder.forAddress("localhost", 80).usePlaintext())

  def scn(name: String): ScenarioBuilder = {
    val request = exec(helloRequest(name)).pause(1.second)

    // scenario(name).during(30.seconds)(request)
    scenario(name).repeat(10)(request)
  }

  // this will create 1 channel per scn() call and per injected user
  setUp {
    scn("Traefik").inject(atOnceUsers(100))
  }.protocols(grpcConf)

  private def helloRequest(name: String): GrpcCallActionBuilder[HelloRequest, HelloReply] =
    streamingCall(name).payload(HelloRequest(name = "Traefik"))

  def streamingCall(name: String): HeadOfServerStreamingCallWithMethod[HelloRequest, HelloReply] =
    new HeadOfServerStreamingCallWithMethod(s"greet $name", GreeterServiceGrpc.METHOD_SAY_HELLO_FOREVER)
}

class FirstResponseObserver[Req, Res] extends ClientResponseObserver[Req, Res] {

  val promise: Promise[Res] = Promise[Res]()

  private var lastRes: Option[Res] = Option.empty

  private var requestStream: ClientCallStreamObserver[Req] = _

  override def onNext(value: Res): Unit = {
    lastRes = Option(value)
    promise.tryComplete(Try(lastRes.get))
    requestStream.cancel("Done", null)
  }

  override def onError(t: Throwable): Unit = {
    Metrics.terminatedCalls.incrementAndGet()
    promise.tryFailure(t)
    requestStream.cancel("Error", t)
  }

  override def onCompleted(): Unit = {
    Metrics.terminatedCalls.incrementAndGet()
    val _ = promise.tryComplete(Try(lastRes.get))
  }

  override def beforeStart(requestStream: ClientCallStreamObserver[Req]): Unit = {
    Metrics.startedCalls.incrementAndGet()
    this.requestStream = requestStream
  }
}

trait RpcCallBuilder[Req, Res] {
  def payload(req: Expression[Req]): GrpcCallActionBuilder[Req, Res]
}

class HeadOfServerStreamingCallWithMethod[Req, Res](requestName: Expression[String], method: MethodDescriptor[Req, Res]) extends RpcCallBuilder[Req, Res] {
  private val f: Channel => Req => Future[Res] = { channel: Channel => request: Req =>
    Metrics.channels.put(channel, ())
    val call = channel.newCall(method, CallOptions.DEFAULT)
    val mso  = new FirstResponseObserver[Req, Res]
    ClientCalls.asyncServerStreamingCall(call, request, mso)
    mso.promise.future
  }

  def payload(req: Expression[Req]): GrpcCallActionBuilder[Req, Res] = GrpcCallActionBuilder(requestName, f, req, headers = Nil)
}

object Metrics {
  val channels: ConcurrentHashMap[Channel, Unit] = new ConcurrentHashMap[Channel, Unit]()
  val startedCalls: AtomicInteger                = new AtomicInteger(0)
  val terminatedCalls: AtomicInteger             = new AtomicInteger(0)
}
