package com.lucidchart.piezo.admin.controllers

import play.api.Logging

trait ErrorLogging { self: Logging =>
  def logExceptions[T](value: => T): T = {
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
