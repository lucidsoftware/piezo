package com.lucidchart.piezo.admin

import play.api.mvc._
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._

object RequestStatCollector extends EssentialFilter {
  implicit val logger = Logger(this.getClass())


  private def recordStats(request: RequestHeader, start: Long)(result: Result): Result = {
    val time = System.currentTimeMillis - start
    logger.info(s"${request.method} ${request.uri} took ${time}ms and returned ${result.header.status}")
    result
  }

  def apply(next: EssentialAction) = EssentialAction { request: RequestHeader =>
    val start = System.currentTimeMillis
    next(request).map { value =>
      recordStats(request, start)(value)
    }
  }
}
