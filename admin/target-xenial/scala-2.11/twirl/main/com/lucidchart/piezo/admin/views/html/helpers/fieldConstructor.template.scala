
package com.lucidchart.piezo.admin.views.html.helpers

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
object fieldConstructor extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template1[helper.FieldElements,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(elements: helper.FieldElements):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*1.34*/("""

"""),format.raw/*3.1*/("""<div class="form-group """),_display_(/*3.25*/elements/*3.33*/.args.get('_class)),format.raw/*3.51*/(""" """),_display_(/*3.53*/if(elements.hasErrors)/*3.75*/ {_display_(Seq[Any](format.raw/*3.77*/("""has-error""")))}),format.raw/*3.87*/("""">
    <label for=""""),_display_(/*4.18*/elements/*4.26*/.id),format.raw/*4.29*/("""" class="piezo-label """),_display_(/*4.51*/elements/*4.59*/.args.get('labelClass)),format.raw/*4.81*/("""">"""),_display_(/*4.84*/elements/*4.92*/.label),format.raw/*4.98*/("""</label>
    <div class="input """),_display_(/*5.24*/elements/*5.32*/.args.get('inputDivClass)),format.raw/*5.57*/("""">
        """),_display_(/*6.10*/elements/*6.18*/.input),format.raw/*6.24*/("""
        """),format.raw/*7.9*/("""<span class="errors">"""),_display_(/*7.31*/elements/*7.39*/.errors.mkString(", ")),format.raw/*7.61*/("""</span>
    </div>
</div>
"""))}
  }

  def render(elements:helper.FieldElements): play.twirl.api.HtmlFormat.Appendable = apply(elements)

  def f:((helper.FieldElements) => play.twirl.api.HtmlFormat.Appendable) = (elements) => apply(elements)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed May 31 21:03:54 MDT 2017
                  SOURCE: /home/paul/lucid/piezo/admin/app/com/lucidchart/piezo/admin/views/helpers/fieldConstructor.scala.html
                  HASH: 4a3d67efc9980edff6af63b3cdfb79b054ef9794
                  MATRIX: 565->1|685->33|713->35|763->59|779->67|817->85|845->87|875->109|914->111|954->121|1000->141|1016->149|1039->152|1087->174|1103->182|1145->204|1174->207|1190->215|1216->221|1274->253|1290->261|1335->286|1373->298|1389->306|1415->312|1450->321|1498->343|1514->351|1556->373
                  LINES: 19->1|22->1|24->3|24->3|24->3|24->3|24->3|24->3|24->3|24->3|25->4|25->4|25->4|25->4|25->4|25->4|25->4|25->4|25->4|26->5|26->5|26->5|27->6|27->6|27->6|28->7|28->7|28->7|28->7
                  -- GENERATED --
              */
          