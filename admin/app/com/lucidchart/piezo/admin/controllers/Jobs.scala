package com.lucidchart.piezo.admin.controllers

import play.api._
import play.api.mvc._

object Jobs extends Controller {

   def index = Action { implicit request =>
     val jobs = List("foo", "bar")
     Ok(com.lucidchart.piezo.admin.views.html.jobs(jobs)(request))
   }

 }