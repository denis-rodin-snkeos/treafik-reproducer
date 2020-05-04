package com.example.helloworld

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Span

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class GreeterServiceImplSpec
  extends Matchers
  with WordSpecLike
  with BeforeAndAfterAll
  with ScalaFutures {

  implicit val patience = PatienceConfig(5.seconds, Span(100, org.scalatest.time.Millis))

  implicit val system = ActorSystem("HelloWorldServer")
  val service = new GreeterServiceImpl()

  override def afterAll: Unit = {
    Await.ready(system.terminate(), 5.seconds)
  }

  "GreeterServiceImpl" should {
    "reply to single request" in {
      val reply = service.sayHelloForever(HelloRequest("Bob")).toMat(Sink.head)(Keep.right).run()
      reply.futureValue should ===(HelloReply("Hello, Bob"))
    }
  }
}
