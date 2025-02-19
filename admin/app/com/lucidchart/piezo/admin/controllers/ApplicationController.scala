package com.lucidchart.piezo.admin.controllers

import play.api.*
import play.api.mvc.*

class ApplicationController(cc: ControllerComponents) extends AbstractController(cc) {

  def index: Action[AnyContent] = Action { implicit request =>
    Ok(com.lucidchart.piezo.admin.views.html.index()(request))
  }

}
