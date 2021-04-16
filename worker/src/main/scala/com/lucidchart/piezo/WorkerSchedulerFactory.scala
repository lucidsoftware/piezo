package com.lucidchart.piezo

import org.quartz.impl.StdSchedulerFactory
import java.util.Properties

/**
  */
class WorkerSchedulerFactory extends StdSchedulerFactory {
  var props: Properties = null

  override def initialize(props: Properties): Unit = {
    this.props = props
    super.initialize(props)
  }
}
