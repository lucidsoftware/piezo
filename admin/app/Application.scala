package com.lucidchart.piezo.admin

import com.lucidchart.piezo.util.DummyClassGenerator
import com.softwaremill.macwire._
import play.api.ApplicationLoader.Context
import play.api._
import play.api.i18n._
import play.api.http._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.Mode
import play.api.routing.Router
import router.Routes
import scala.concurrent.Future
import com.lucidchart.piezo.admin.models._
import com.lucidchart.piezo.admin.controllers._
import com.lucidchart.piezo.WorkerSchedulerFactory
import _root_.controllers.AssetsComponents
/**
 * Application loader that wires up the application dependencies using Macwire
 */
class PiezoAdminApplicationLoader extends ApplicationLoader {
  def load(context: Context): Application = new PiezoAdminComponents(context).application
}

class PiezoAdminComponents(context: Context) extends BuiltInComponentsFromContext(context)   with I18nComponents  with AssetsComponents {

  lazy val schedulerFactory: WorkerSchedulerFactory = new WorkerSchedulerFactory()
  lazy val jobFormHelper: JobFormHelper = wire[JobFormHelper]

  lazy val triggers: Triggers = wire[Triggers]
  lazy val jobs: Jobs = wire[Jobs]
  lazy val healthCheck: HealthCheck = wire[HealthCheck]
  lazy val applicationController: ApplicationController = wire[ApplicationController]

  lazy val jobView: views.html.job = wire[views.html.job]

  override val httpFilters: Seq[EssentialFilter] = {
    val ec = controllerComponents.executionContext
    Seq(
      wire[RequestStatCollector]
    )
  }
  val logger = Logger("com.lucidchart.piezo.Global")


  override lazy val httpErrorHandler: HttpErrorHandler = new DefaultHttpErrorHandler(environment, configuration, sourceMapper, Some(router)) {
      /**
     * Invoked when a handler or resource is not found.
     *
     * @param request The request that no handler was found to handle.
     * @param message A message.
     */
    override protected def onNotFound(request: RequestHeader, message: String): Future[Result] = {
      logger.error("Request handler not found for URL: " + request.uri)
      Future.successful(NotFound(com.lucidchart.piezo.admin.views.html.errors.notfound(None)(request)))
    }

    override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
      logger.error("Error handling request for URL: " + request.uri, exception)
      if(environment.mode == Mode.Dev) {
        super.onServerError(request, exception)
      } else {
        Future.successful(InternalServerError(com.lucidchart.piezo.admin.views.html.errors.error(Option(exception.getMessage))(request)))
      }
    }
  }
  // set up logger
  LoggerConfigurator(context.environment.classLoader).foreach {
    _.configure(context.environment, context.initialConfiguration, Map.empty)
  }

  lazy val router: Router = {
    // add the prefix string in local scope for the Routes constructor
    val prefix: String = "/"
    wire[Routes]
  }
}
