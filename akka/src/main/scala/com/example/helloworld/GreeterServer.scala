package com.example.helloworld

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.{Http, Http2, HttpConnectionContext}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}

object GreeterServer {

  def main(args: Array[String]): Unit = {
    // important to enable HTTP/2 in ActorSystem's config
    val conf = ConfigFactory.defaultApplication()
    val system: ActorSystem = ActorSystem("HelloWorld", conf)
    new GreeterServer(system).run()
  }
}

class GreeterServer(system: ActorSystem) {

  def run(): Future[Http.ServerBinding] = {
    implicit val sys = system
    implicit val ec: ExecutionContext = sys.dispatcher

    val service: HttpRequest => Future[HttpResponse] =
      GreeterServiceHandler(new GreeterServiceImpl())

    val bound = Http2().bindAndHandleAsync(
      service,
      interface = "0.0.0.0",
      port = 8080,
      connectionContext = HttpConnectionContext()
    )

    bound.foreach { binding =>
      println(s"gRPC server bound to: ${binding.localAddress}")
    }

    bound
  }
}
