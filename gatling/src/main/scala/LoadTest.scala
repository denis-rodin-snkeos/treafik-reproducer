import io.gatling.app.Gatling

import scala.collection._

object LoadTest {
   def main(args: Array[String]): Unit = {
    Gatling.fromMap(
      mutable.Map(
        "gatling.core.directory.results" -> "/tmp",
        "gatling.core.simulationClass"   -> "HelloSimulation"
      )
    )
    println(s"""Metric:
           |Channels: ${Metrics.channels.size()}
           |calls started: ${Metrics.startedCalls.get()}
           |calls terminated: ${Metrics.terminatedCalls.get()}
           |""".stripMargin)
  }
}
