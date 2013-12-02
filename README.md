Piezo
=====

System to operate and manage a [Quartz Scheduler](http://quartz-scheduler.org/documentation/quartz-2.2.x/quick-start) cluster.


##Worker
Worker is a process that runs a [Quartz Scheduler](http://quartz-scheduler.org/documentation/quartz-2.2.x/quick-start) instance.

Worker provides a java main() function for running a quartz scheduler as a daemon. It writes a PID file for help with start and stop (e.g. init.d) scripts. It handles the runtime shutdown event and graceful exits when it receives a shutdown signal (`ctrl-c/SIGINT`).

Worker also expands the set of tables that quartz uses with additional tables to track historical job execution data.

###Setup
1. Create a database. Piezo includes a [sample database creation script](worker/src/main/resources/create_database.sql)
2. Create the standard [job store](http://quartz-scheduler.org/documentation/quartz-2.2.x/tutorials/tutorial-lesson-09) using ONE of the following methods:
    1. Use the sample script included as [worker/src/main/resources/tables_mysql.sql](worker/src/main/resources/tables_mysql.sql).
    2. See the [quartz job store documentation](http://quartz-scheduler.org/documentation/quartz-2.2.x/tutorials/tutorial-lesson-09) for the complete set of options.
    3. From the documentation:
        "JDBCJobStore works with nearly any database, it has been used widely with Oracle, PostgreSQL, MySQL, MS SQLServer, HSQLDB, and DB2. To use JDBCJobStore, you must first create a set of database tables for Quartz to use. You can find table-creation SQL scripts in the 'docs/dbTables' directory of the Quartz distribution. If there is not already a script for your database type, just look at one of the existing ones, and modify it in any way necessary for your DB."
3. Create the Piezo [job history](worker/src/main/resources/create_history_tables.sql) tables.
4. Create your quartz scheduler [job initialization file](http://quartz-scheduler.org/documentation/quartz-2.2.x/cookbook/JobInitPlugin) (see [sample dev.xml](/worker/src/main/resources/dev.xml)).
5. Create your [Quartz scheduler library config file](http://quartz-scheduler.org/documentation/quartz-2.2.x/configuration/) with a data source pointing to the job store database just created  (see [sample quartz.properties](/worker/src/main/resources/quartz.properties)).
6. Run Piezo as specified in [Running](#running).

###Building
You must have [sbt](http://www.scala-sbt.org/) version 0.12.0 or higher to build the worker project.

To compile the project run

`make compile`.

To package the project into a jar run

`make package`.

To collect all dependencies into a single folder (target/staged) run

`make stage`

###Configuration
####JVM properties
* `org.quartz.properties` - [Quartz scheduler library config file](http://quartz-scheduler.org/documentation/quartz-2.2.x/configuration/)
* `logback.configurationFile` - [Logback config file](http://logback.qos.ch/manual/configuration.html)
* `pidfile.path` - path to file where PID should be written on startup

###Running
The project includes a sample script for running a worker process. It launches with the [sample quartz.properties file](worker/src/main/resources/quartz.properties) included in the project.

```
./worker/src/main/resources/run.sh
```

Here also is a sample java command for running a single worker instance:

```
java -Dlogback.configurationFile=<path to logback config> -Dorg.quartz.properties=<path to quartz properties> -Dpidfile.path=<path to pid file> -Dnetworkaddress.cache.ttl=10 -Dnetworkaddress.cache.negative.ttl=10 -cp <path to jars> com.lucidchart.piezo.Worker
```

###Stats
Worker reports statistics to a [StatsD](https://github.com/etsy/statsd/) server if available.

It also stores historical job execution data in a pair of database tables defined in [create_history_tables.sql](worker/src/main/resources/create_history_tables.sql). These tables should be added to the same datasource as the standard quartz tables.


##Admin

Web interface for viewing and managing the scheduled jobs.

###Setup
1. Follow the steps for the Worker [Setup](#setup) above.

###Building
You must have [play](http://www.playframework.com/) version 2.1.1 or higher to build the admin project.

To compile the project run

`play compile`.

To collect all dependencies into a single folder (target/staged) run

`make stage`

###Configuration
####JVM properties
* `org.quartz.properties` - [Quartz scheduler library config file](http://quartz-scheduler.org/documentation/quartz-2.2.x/configuration/)
* `logback.configurationFile` - [Logback config file](http://logback.qos.ch/manual/configuration.html)
* `pidfile.path` - path to file where PID should be written on startup
* `http.port[s]` - [Play Framework production configuration](http://www.playframework.com/documentation/2.1.1/ProductionConfiguration)

###Running
The project includes a sample script for running a worker process. It launches with the [sample quartz.properties file](worker/src/main/resources/quartz.properties) included in the project.

```
./admin/bin/run.sh
```
After running the above script, go to http://localhost:8001/ to view the admin tool.

Here also is a sample java command for running a single admin instance:

```
java -Dlogback.configurationFile=<path to logback config> -Dorg.quartz.properties=<path to quartz properties> -Dpidfile.path=<path to pid file> -Dnetworkaddress.cache.ttl=10 -Dnetworkaddress.cache.negative.ttl=10 -Dhttp.port=<port> -cp <path to jars> play.core.server.NettyServer
```

