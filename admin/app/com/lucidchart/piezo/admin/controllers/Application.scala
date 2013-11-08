package com.lucidchart.piezo.admin.controllers

import play.api._
import play.api.mvc._
import controllers.Assets

object Application extends Controller {
  def favicon = Assets.at("/public/img", "favicon.ico")
  
  def index = Action { implicit request =>
    Ok(com.lucidchart.piezo.admin.views.html.index()(request))
  }
  
}