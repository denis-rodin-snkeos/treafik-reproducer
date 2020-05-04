package com.example.helloworld

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.scaladsl.Source

import scala.concurrent.Future
import scala.util.{Failure, Success}

object GreeterClient {

  def main(args: Array[String]): Unit = {
    implicit val sys = ActorSystem("HelloWorldClient")
    import sys.dispatcher

    val client = GreeterServiceClient(GrpcClientSettings.fromConfig("helloworld.GreeterService"))

      println(s"Performing streaming requests")

      val responseStream: Source[HelloReply, NotUsed] =
        client.sayHelloForever(HelloRequest("Traefik")).take(1)
      val done: Future[Done] =
        responseStream.runForeach(reply => println(s"got streaming reply: ${reply.message}"))

      done.onComplete {
        case Success(_) =>
          println("streaming done")
        case Failure(e) =>
          println(s"Error streaming: $e")
      }
    }
}
