package com.lucidchart.piezo

import scala.beans.BeanProperty
import java.sql.Connection

/**
 * Custom ConnectionProvider that is compatible with the configuration of a custom ConnectionProvider, using Java Beans.
 * Note that unlike our `ConnectionProvider` class, it is pretty mutable, because Java.
 *
 * It ultimately wraps the ConnectionProvider
 *
 * WARNING: This is intedned for the properties to all be set, and then `initialize` called to set everything up, after
 * which the properties should be left unchanged.
 */
class BeanConnectionProvider extends org.quartz.utils.ConnectionProvider {

  @BeanProperty
  var URL: String = null

  @BeanProperty
  var driver: String = null

  @BeanProperty
  var user: String = null

  @BeanProperty
  var password: String = null

  @BeanProperty
  var maxConnections: Int = -1

  @BeanProperty
  var validationQuery: String = ""

  @BeanProperty
  var supportIPFailover: Boolean = false

  @BeanProperty
  var causeFailoverEveryConnection: Boolean = false

  private var provider: ConnectionProvider = null

  override def initialize(): Unit = {
    provider = new ConnectionProvider(
      URL,
      driver,
      user,
      password,
      maxConnections,
      validationQuery,
      supportIPFailover,
      causeFailoverEveryConnection,
    )
  }

  override def getConnection(): Connection = provider.getConnection()

  override def shutdown(): Unit = {
    if (provider != null) {
      provider.shutdown()
    }
  }
}
