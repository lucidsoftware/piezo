
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
object notfound extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template2[Option[String],RequestHeader,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(
message: Option[String] = None
)(
implicit
request: RequestHeader
):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import com.lucidchart.piezo.admin.views

Seq[Any](format.raw/*6.2*/("""
"""),format.raw/*8.1*/("""
"""),_display_(/*9.2*/com/*9.5*/.lucidchart.piezo.admin.views.html.main("Piezo Error")/*9.59*/ {_display_(Seq[Any](format.raw/*9.61*/("""
"""),format.raw/*10.1*/("""<div>
  <h1 class="text-danger">Page Not Found</h1>
  <p class="large">"""),_display_(/*12.21*/message/*12.28*/.getOrElse("The page you requested could not be located.")),format.raw/*12.86*/("""</p>
</div>
""")))}))}
  }

  def render(message:Option[String],request:RequestHeader): play.twirl.api.HtmlFormat.Appendable = apply(message)(request)

  def f:((Option[String]) => (RequestHeader) => play.twirl.api.HtmlFormat.Appendable) = (message) => (request) => apply(message)(request)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed May 31 21:03:54 MDT 2017
                  SOURCE: /home/paul/lucid/piezo/admin/app/com/lucidchart/piezo/admin/views/errors/notfound.scala.html
                  HASH: 75ffff96da03fd2f9e463266fff2af7aabbf738e
                  MATRIX: 564->1|759->70|786->112|813->114|823->117|885->171|924->173|952->174|1051->246|1067->253|1146->311
                  LINES: 19->1|27->6|28->8|29->9|29->9|29->9|29->9|30->10|32->12|32->12|32->12
                  -- GENERATED --
              */
          