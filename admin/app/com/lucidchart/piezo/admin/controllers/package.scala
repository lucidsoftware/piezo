package com.lucidchart.piezo.admin

import play.api.Logger

/**
  */
package object controllers {
  def logExceptions[T](value: => T)(implicit logger: Logger): T =
  {
    try {
      value
    }
    catch {
      case t: Throwable =>
        logger.error("Caught exception initializing class", t)
        throw t
    }
  }
}
