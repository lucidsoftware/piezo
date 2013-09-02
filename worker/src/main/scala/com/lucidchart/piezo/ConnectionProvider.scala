package com.lucidchart.piezo

import org.quartz.utils.PoolingConnectionProvider
import java.util.Properties
import org.slf4j.LoggerFactory
import java.io._

class ConnectionProvider(props: Properties) {
  private val provider = new PoolingConnectionProvider(
    props.getProperty("org.quartz.dataSource.lucidJobs.driver"),
    props.getProperty("org.quartz.dataSource.lucidJobs.URL"),
    props.getProperty("org.quartz.dataSource.lucidJobs.user"),
    props.getProperty("org.quartz.dataSource.lucidJobs.password"),
    props.getProperty("org.quartz.dataSource.lucidJobs.maxConnections").toInt,
    props.getProperty("org.quartz.dataSource.lucidJobs.validationQuery")
  )

  def getConnection = provider.getConnection
}
