package com.lucidchart.piezo.admin

import play.api.mvc.*
import play.api.Logging
import scala.concurrent.ExecutionContext

class RequestStatCollector(ec: ExecutionContext) extends EssentialFilter with Logging {

  private def recordStats(request: RequestHeader, start: Long)(result: Result): Result = {
    val time = System.currentTimeMillis - start
    logger.info(s"${request.method} ${request.uri} took ${time}ms and returned ${result.header.status}")
    result
  }

  def apply(next: EssentialAction): EssentialAction = EssentialAction { (request: RequestHeader) =>
    val start = System.currentTimeMillis
    next(request).map { value =>
      recordStats(request, start)(value)
    }(ec)
  }
}
