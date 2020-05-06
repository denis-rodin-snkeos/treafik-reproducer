package com.example.helloworld

import akka.NotUsed
import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.AbruptStageTerminationException
import akka.stream.scaladsl.Source

import scala.concurrent.Future
import scala.util.Failure

object GreeterClient {

  def main(args: Array[String]): Unit = {
    implicit val sys = ActorSystem("HelloWorldClient")
    import sys.dispatcher

    val client = GreeterServiceClient(GrpcClientSettings.fromConfig("helloworld.GreeterService"))

    println(s"Performing streaming requests")

    def responseStream(clientId: Int, requestId: Int): Source[HelloReply, NotUsed] =
      client.sayHelloForever(HelloRequest(s"akka-grpc-$clientId-$requestId")).take(1)

    // for 100 concurrent clients
    Future.traverse((1 to 100).toList) { clientId =>
      // perform 10 requests in a row
      val r = for {
        _ <- responseStream(clientId, 1).runForeach(reply => println(s"got streaming reply: ${reply.message}"))
        _ <- responseStream(clientId, 2).runForeach(reply => println(s"got streaming reply: ${reply.message}"))
        _ <- responseStream(clientId, 3).runForeach(reply => println(s"got streaming reply: ${reply.message}"))
        _ <- responseStream(clientId, 4).runForeach(reply => println(s"got streaming reply: ${reply.message}"))
        _ <- responseStream(clientId, 5).runForeach(reply => println(s"got streaming reply: ${reply.message}"))
        _ <- responseStream(clientId, 6).runForeach(reply => println(s"got streaming reply: ${reply.message}"))
        _ <- responseStream(clientId, 7).runForeach(reply => println(s"got streaming reply: ${reply.message}"))
        _ <- responseStream(clientId, 8).runForeach(reply => println(s"got streaming reply: ${reply.message}"))
        _ <- responseStream(clientId, 9).runForeach(reply => println(s"got streaming reply: ${reply.message}"))
        _ <- responseStream(clientId, 10).runForeach(reply => println(s"got streaming reply: ${reply.message}"))
      } yield {}
      r.andThen {
         case Failure(_: AbruptStageTerminationException) => // ignore: caused by client closed by a previous error
         case Failure(exception) => println(s"Error at $clientId: ${exception}")
      }
    }.andThen {
      case _ =>
        println("Done performing streaming requests")
        client.close()
    }.andThen {
      case _ => sys.terminate()
    }
  }
}
