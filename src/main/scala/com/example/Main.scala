package com.example

import zio.{ZIOAppDefault, Console, Clock}

object Main extends ZIOAppDefault {
  //noinspection TypeAnnotation
  override def run = Clock.currentDateTime
    .flatMap(dt => Console.printLine("Now is: " + dt))
}
