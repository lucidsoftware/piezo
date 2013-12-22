package com.lucidchart.piezo.admin

import play.api.mvc._
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._

object RequestStatCollector extends EssentialFilter {
  implicit val logger = Logger(this.getClass())
  def apply(next: EssentialAction) = new EssentialAction {
    def apply(rh: RequestHeader) = {
      val start = System.currentTimeMillis

      def logTime(result: PlainResult): Result = {
        val time = System.currentTimeMillis - start
        logger.info(s"${rh.method} ${rh.uri} took ${time}ms and returned ${result.header.status}")
        result.withHeaders("Request-Time" -> time.toString)
      }

      next(rh).map {
        case plain: PlainResult => logTime(plain)
        case async: AsyncResult => async.transform(logTime)
      }
    }
  }
}
