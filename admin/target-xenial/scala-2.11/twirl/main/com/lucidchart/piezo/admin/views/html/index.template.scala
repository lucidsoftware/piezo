
package com.lucidchart.piezo.admin.views.html

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
object index extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template1[play.api.mvc.Request[AnyContent],play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(
)(
implicit
request: play.api.mvc.Request[AnyContent]
):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import com.lucidchart.piezo.admin.controllers.{routes=>piezoRoutes}
import com.lucidchart.piezo.admin.views

Seq[Any](format.raw/*5.2*/("""
"""),format.raw/*8.1*/("""
"""),_display_(/*9.2*/com/*9.5*/.lucidchart.piezo.admin.views.html.main("Piezo Admin Home")/*9.64*/ {_display_(Seq[Any](format.raw/*9.66*/("""
"""),format.raw/*10.1*/("""<div class="clearfix" style="text-align: center; margin: 15px 0;">
    <img src=""""),_display_(/*11.16*/routes/*11.22*/.Assets.at("img/PiezoLogo.png")),format.raw/*11.53*/("""">
</div>
<div class="well">
    <a href="https://github.com/lucidsoftware/piezo">Piezo</a>
    was created by
    <a href="https://www.lucidchart.com/" target="_blank">Lucid Software, Inc.</a>
    to provide management tools for quartz scheduler clusters.
</div>
""")))}))}
  }

  def render(request:play.api.mvc.Request[AnyContent]): play.twirl.api.HtmlFormat.Appendable = apply()(request)

  def f:(() => (play.api.mvc.Request[AnyContent]) => play.twirl.api.HtmlFormat.Appendable) = () => (request) => apply()(request)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed May 31 21:03:53 MDT 2017
                  SOURCE: /home/paul/lucid/piezo/admin/app/com/lucidchart/piezo/admin/views/index.scala.html
                  HASH: ea55023d26a9f65962b83603d6aed81ca72dfbbc
                  MATRIX: 558->1|809->58|836->169|863->171|873->174|940->233|979->235|1007->236|1116->318|1131->324|1183->355
                  LINES: 19->1|27->5|28->8|29->9|29->9|29->9|29->9|30->10|31->11|31->11|31->11
                  -- GENERATED --
              */
          