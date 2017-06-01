
package com.lucidchart.piezo.admin.views.html.errors

import play.twirl.api._
import play.twirl.api.TemplateMagic._

import play.api.templates.PlayMagic._
import models._
import controllers._
import play.api.i18n._
import play.api.mvc._
import play.api.data._
import views.html._

/**/
object error extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template2[Option[String],RequestHeader,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(
errorMessage: Option[String] = None
)(
implicit
request: RequestHeader
):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import com.lucidchart.piezo.admin.views

Seq[Any](format.raw/*6.2*/("""
"""),format.raw/*8.1*/("""
"""),_display_(/*9.2*/com/*9.5*/.lucidchart.piezo.admin.views.html.main("Piezo Error")/*9.59*/ {_display_(Seq[Any](format.raw/*9.61*/("""
  """),format.raw/*10.3*/("""<div>
    <h1 class="text-danger">Error</h1>
    <p class="large">"""),_display_(/*12.23*/errorMessage/*12.35*/.getOrElse("Unknown Error")),format.raw/*12.62*/("""</p>
  </div>
""")))}))}
  }

  def render(errorMessage:Option[String],request:RequestHeader): play.twirl.api.HtmlFormat.Appendable = apply(errorMessage)(request)

  def f:((Option[String]) => (RequestHeader) => play.twirl.api.HtmlFormat.Appendable) = (errorMessage) => (request) => apply(errorMessage)(request)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed May 31 21:03:54 MDT 2017
                  SOURCE: /home/paul/lucid/piezo/admin/app/com/lucidchart/piezo/admin/views/errors/error.scala.html
                  HASH: 93a1e9d1ae8c66285439e15d5b5c22c9bfd1b34a
                  MATRIX: 561->1|761->75|788->117|815->119|825->122|887->176|926->178|956->181|1050->248|1071->260|1119->287
                  LINES: 19->1|27->6|28->8|29->9|29->9|29->9|29->9|30->10|32->12|32->12|32->12
                  -- GENERATED --
              */
          