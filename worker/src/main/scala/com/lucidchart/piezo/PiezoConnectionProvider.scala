package com.lucidchart.piezo

import java.net.UnknownHostException
import java.sql.{Connection, SQLTransientConnectionException}
import java.util.concurrent.TimeUnit
import org.quartz.utils.HikariCpPoolingConnectionProvider
import org.slf4j.LoggerFactory
import scala.annotation.tailrec
import org.slf4j.Logger

private[piezo] class PiezoConnectionProvider(
  url: String,
  driver: String,
  user: String,
  password: String,
  maxConnections: Int = 10,
  validationQuery: String = "SELECT 0",
  supportIPFailover: Boolean = false,
  // Intended to be used only for tests. This mocks an IP failover every time a connection is retreived
  causeFailoverEveryConnection: Boolean = false,
) {

  private class Pool(val ip: String, val connectionProvider: HikariCpPoolingConnectionProvider) {

    def this(ip: String) = this(ip, createNewConnectionProvider())

    logger.info(s"Initialized Db connection pool for ${jdbcURL}")
    // Hikari takes about a second to add connections to the connection pool
    // We are now going to warm-up connectionPool(with timelimit of 2500ms)
    warmUpCP(connectionProvider)
  }

  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  private def jdbcURL = url
  // Removes "jdbc:mysql://" prefix and ":{port}..." suffix
  private val dataSourceHostname = if (jdbcURL != null) jdbcURL.replace("jdbc:mysql://", "").split(":")(0) else null

  // Time (in milliseconds) that the in-memory cache will retain the ip address
  private val cachedIpTTL: Long = 1000
  private val getIpNumRetries = 10
  // Class for storing the ip address of the host, along with an expiration date
  private case class CachedIpWithExpiration(ip: String, expiration: Long)
  // Cache for ip address and its expiration for a host
  @volatile
  private var cachedIpWithExpiration: Option[CachedIpWithExpiration] = None

  @volatile
  private var pool: Pool = new Pool(getIP)

  def createNewConnectionProvider(): HikariCpPoolingConnectionProvider = {
    new HikariCpPoolingConnectionProvider(
      driver,
      jdbcURL,
      user,
      password,
      maxConnections,
      validationQuery,
    )
  }

  /**
   * HikariCP connection pools don't automatically close when IP addresses for a hostname change. This function returns
   * True, iff at least one of the following conditions is met:
   *   - IP addresses have changed for the CNAME record used for DNS lookup
   *   - causeFailoverEveryConnection is set to "true", which is used for testing failover functionality
   *
   * @param pool
   *   the connection pool currently being used
   * @param dnsIP
   *   the IP returned when performing a DNS lookup
   * @return
   */
  private def hasIpAddressChanged(pool: Pool, dnsIP: String): Boolean = {
    causeFailoverEveryConnection || pool.ip != dnsIP
  }

  @tailrec
  private def retryGettingIp(n: Int)(fn: => String): String = {
    try {
      return fn
    } catch {
      // Failed to resolve it from JVM
      case e: UnknownHostException if n > 1 =>
    }
    // Wait 10 milliseconds between retries
    Thread.sleep(10)
    retryGettingIp(n - 1)(fn)
  }

  def _getIp: String = {
    retryGettingIp(getIpNumRetries) {
      // Get the ip address of the hostname. The result is cached in the JVM
      val ip = java.net.InetAddress.getByName(dataSourceHostname).getHostAddress
      cachedIpWithExpiration = Some(CachedIpWithExpiration(ip, System.currentTimeMillis() + cachedIpTTL))
      ip
    }
  }

  def getIP: String = {
    synchronized {
      cachedIpWithExpiration
        .map { cachedValue =>
          if (System.currentTimeMillis() > cachedValue.expiration) {
            _getIp
          } else {
            cachedValue.ip
          }
        }
        .getOrElse(_getIp)
    }
  }

  def getConnection(): Connection = {
    if (supportIPFailover && dataSourceHostname != null) {
      // If the IP has changed, then we know a failover has occurred, and we need to create a new hikari config
      val newIP: String = getIP
      if (hasIpAddressChanged(pool, newIP)) {
        // A failover has occurred, so we evict connections softly. New connectons look up the new IP address
        logger.info(s"IP Address has changed for ${jdbcURL}: ${pool.ip} -> ${newIP}. Attempt replacing pool...")
        val optionalOldPool = synchronized {
          val oldPool = pool
          // check if another thread updated the pool
          if (hasIpAddressChanged(pool, newIP)) {
            logger.info(s"Replacing pool for ${jdbcURL}...")
            pool = new Pool(newIP)
            Some(oldPool)
          } else {
            // already up to date
            logger.info(s"Pool already replaced for ${jdbcURL}")
            None
          }
        }

        // Clean up old pool so we don't leak connections to the old server
        optionalOldPool.foreach { oldPool =>
          logger.info(s"Closing DB connection pool for ${jdbcURL} for failover (${oldPool.ip} -> ${pool.ip})")
          oldPool.connectionProvider.shutdown()
        }
      }
    }
    pool.connectionProvider.getConnection()
  }

  def shutdown(): Unit = {
    logger.info(s"Shutting down connection pool for ${jdbcURL}")
    pool.connectionProvider.shutdown()
  }

  private def warmUpCP(connectionPool: HikariCpPoolingConnectionProvider): Unit = {
    var testConn: Connection = null
    val start = System.currentTimeMillis
    while (testConn == null && (System.currentTimeMillis - start) < 2500) {
      try {
        testConn = connectionPool.getConnection()
      } catch {
        case _: SQLTransientConnectionException => { TimeUnit.MILLISECONDS.sleep(100) } // do nothing
      }
    }
    if (testConn != null) {
      testConn.close()
    }
  }
}
