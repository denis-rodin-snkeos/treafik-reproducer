package com.example.helloworld

import akka.NotUsed
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.duration._

class GreeterServiceImpl() extends GreeterService {

  override def sayHelloForever(in: HelloRequest): Source[HelloReply, NotUsed] = {
    Source.tick(100.millis, 1.second, HelloReply(s"Hello, ${in.name}")).mapMaterializedValue(_ => NotUsed).alsoTo(Sink.foreach(println))
  }
}
