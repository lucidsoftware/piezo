package com.lucidchart.piezo.admin.controllers

import play.api._
import play.api.mvc._

class ApplicationController(cc: ControllerComponents) extends AbstractController(cc) {

  def index = Action { implicit request =>
    Ok(com.lucidchart.piezo.admin.views.html.index()(request))
  }

}
