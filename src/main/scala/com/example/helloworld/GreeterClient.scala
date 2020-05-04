package com.example.helloworld

import akka.NotUsed
import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.scaladsl.Source

object GreeterClient {

  def main(args: Array[String]): Unit = {
    implicit val sys = ActorSystem("HelloWorldClient")
    import sys.dispatcher

    val client = GreeterServiceClient(GrpcClientSettings.fromConfig("helloworld.GreeterService"))

      println(s"Performing streaming requests")

      val responseStream: Source[HelloReply, NotUsed] =
        client.sayHelloForever(HelloRequest("Traefik")).take(3).drop(2)

    for {
      _ <- 1 to 10
    } {
      for {
        _ <- responseStream.runForeach(reply => println(s"got streaming reply: ${reply.message}"))
        _ <- responseStream.runForeach(reply => println(s"got streaming reply: ${reply.message}"))
        _ <- responseStream.runForeach(reply => println(s"got streaming reply: ${reply.message}"))
        _ <- responseStream.runForeach(reply => println(s"got streaming reply: ${reply.message}"))
        _ <- responseStream.runForeach(reply => println(s"got streaming reply: ${reply.message}"))
        _ <- responseStream.runForeach(reply => println(s"got streaming reply: ${reply.message}"))
      } yield {}
    }
  }
}
