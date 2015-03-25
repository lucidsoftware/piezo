package com.lucidchart.piezo

object StatsD extends com.lucidchart.util.statsd.StatsD("applications.piezo.worker", multiMetrics = false)
