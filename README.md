piezo
=====

System to operate and manage a [Quartz Scheduler](http://quartz-scheduler.org/documentation/quartz-2.2.x/quick-start) cluster.


##Worker
Worker is a process that runs a [Quartz Scheduler](http://quartz-scheduler.org/documentation/quartz-2.2.x/quick-start) instance.

Worker provides a java main() function for running a quartz scheduler as a daemon. It writes a PID file for help with start and stop (e.g. init.d) scripts. It handles the runtime shutdown event and graceful exits when it receives a shutdown signal (`ctrl-c/SIGINT`).

Worker also expands the set of tables that quartz uses with additional tables to track historical job execution data.

###Setup
1. Create a database. Piezo includes a [sample database creation script](worker/src/main/resources/create_database.sql)
2. Create the standard [job store](http://quartz-scheduler.org/documentation/quartz-2.2.x/tutorials/tutorial-lesson-09) tables.
3. Create the Piezo [job history tables](worker/src/main/resources/create_history_tables.sql).
4. Create the quartz scheduler config file with a data source pointing to the job store database.
5. Create your [Quartz scheduler library config file](http://quartz-scheduler.org/documentation/quartz-2.2.x/configuration/).
6. Run Piezo as specified below.

###Building
Run

```
make package
```
to build the worker jar.

###Configuration
####JVM properties
* `org.quartz.properties` - [Quartz scheduler library config file](http://quartz-scheduler.org/documentation/quartz-2.2.x/configuration/)
* `logback.configurationFile` - [Logback config file](http://logback.qos.ch/manual/configuration.html)
* `pidfile.path` - path to file where PID should be written on startup

###Running
A sample java command for running a single worker instance:

```
java -Dlogback.configurationFile=<path to logback config> -Dorg.quartz.properties=<path to quartz properties> -Dpidfile.path=<path to pid file> -Dnetworkaddress.cache.ttl=10 -Dnetworkaddress.cache.negative.ttl=10 -cp <path to jars>/* com.lucidchart.piezo.Worker
```

###Stats
Worker reports statistics to a [StatsD](https://github.com/etsy/statsd/) server if available.

It also stores historical job execution data in pair of database tables defined in [create_history_tables.sql](worker/src/main/resources/create_history_tables.sql). These tables should be added to the same datasource as the standard quartz tables.


##Admin
--Coming soon--

Web interface for viewing and managing the scheduled jobs.
