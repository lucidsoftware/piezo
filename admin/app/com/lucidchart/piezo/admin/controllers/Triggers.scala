package com.lucidchart.piezo.admin.controllers

import play.api._
import play.api.mvc._

object Triggers extends Controller {

   def index = Action { implicit request =>
     val triggers = List("foo", "bar")
     Ok(com.lucidchart.piezo.admin.views.html.triggers(triggers)(request))
   }

 }