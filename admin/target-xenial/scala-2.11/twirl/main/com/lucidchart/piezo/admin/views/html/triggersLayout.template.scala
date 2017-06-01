
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
object triggersLayout extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template5[scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.TriggerKey]]],Option[org.quartz.Trigger],List[String],Html,play.api.mvc.Request[AnyContent],play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(
triggersByGroup: scala.collection.mutable.Buffer[(String, scala.collection.immutable.List[org.quartz.TriggerKey])],
currentTrigger: Option[org.quartz.Trigger],
scripts: List[String] = List[String]()
)(
detailsContent: Html
)(
implicit
request: play.api.mvc.Request[AnyContent]
):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import com.lucidchart.piezo.admin.controllers.{routes=>piezoRoutes}
import com.lucidchart.piezo.admin.views
import java.net.URLEncoder

Seq[Any](format.raw/*10.2*/("""
"""),format.raw/*14.1*/("""
"""),_display_(/*15.2*/com/*15.5*/.lucidchart.piezo.admin.views.html.main("Piezo Triggers", scripts)/*15.71*/ {_display_(Seq[Any](format.raw/*15.73*/("""
"""),format.raw/*16.1*/("""<div class="row">
    <div class="col-md-3">
        <h3>Trigger groups</h3>
        <div class="panel-group jobs-list" id="accordion">
            """),_display_(/*20.14*/triggersByGroup/*20.29*/.map/*20.33*/ { triggerGroup =>_display_(Seq[Any](format.raw/*20.51*/("""
            """),format.raw/*21.13*/("""<div class="panel panel-default">
                <div class="panel-heading">
                    <h5 class="title">
                        <a data-toggle="collapse" data-parent="#accordion" href="#collapse"""),_display_(/*24.92*/{triggerGroup._1.replaceAll("\\W+", "_")}),format.raw/*24.133*/("""">
                            """),_display_(/*25.30*/triggerGroup/*25.42*/._1),format.raw/*25.45*/("""
                        """),format.raw/*26.25*/("""</a>
                    </h5>
                </div>
                <div id="collapse"""),_display_(/*29.35*/{triggerGroup._1.replaceAll("\\W+", "_")}),format.raw/*29.76*/(""""
                """),_display_(/*30.18*/if(!currentTrigger.isEmpty && triggerGroup._1 == currentTrigger.get.getKey().getGroup())/*30.106*/ {_display_(Seq[Any](format.raw/*30.108*/("""
                """),format.raw/*31.17*/("""class="panel-collapse collapse in">
                """)))}/*32.19*/else/*32.24*/{_display_(Seq[Any](format.raw/*32.25*/("""
                """),format.raw/*33.17*/("""class="panel-collapse collapse ">
                """)))}),format.raw/*34.18*/("""
                """),format.raw/*35.17*/("""<div class="panel-body">
                    <div class="list-group">
                        """),_display_(/*37.26*/triggerGroup/*37.38*/._2.map/*37.45*/ { triggerKey =>_display_(Seq[Any](format.raw/*37.61*/("""
                        """),format.raw/*38.25*/("""<a href=""""),_display_(/*38.35*/piezoRoutes/*38.46*/.Triggers.getTrigger(triggerKey.getGroup(), triggerKey.getName())),format.raw/*38.111*/(""""
                        """),_display_(/*39.26*/if(!currentTrigger.isEmpty && triggerGroup._1 == currentTrigger.get.getKey.getGroup() && triggerKey.getName() == currentTrigger.get.getKey.getName())/*39.175*/ {_display_(Seq[Any](format.raw/*39.177*/("""
                        """),format.raw/*40.25*/("""class="list-group-item active"
                        """)))}/*41.27*/else/*41.32*/{_display_(Seq[Any](format.raw/*41.33*/("""
                        """),format.raw/*42.25*/("""class="list-group-item"
                        """)))}),format.raw/*43.26*/("""
                        """),format.raw/*44.25*/("""data-toggle="tooltip" data-placement="auto right"title=""""),_display_(/*44.82*/triggerKey/*44.92*/.getName()),format.raw/*44.102*/("""">
                        """),_display_(/*45.26*/triggerKey/*45.36*/.getName()),format.raw/*45.46*/("""
                        """),format.raw/*46.25*/("""</a>
                        """)))}),format.raw/*47.26*/("""
                    """),format.raw/*48.21*/("""</div>
                </div>
            </div>
        </div>
        """)))}),format.raw/*52.10*/("""
    """),format.raw/*53.5*/("""</div>
</div>
<div class="col-md-9">
    <a class="piezo-button pull-right" href=""""),_display_(/*56.47*/piezoRoutes/*56.58*/.Triggers.getNewTriggerForm("cron")),format.raw/*56.93*/("""" data-toggle="tooltip" title="Add trigger"><span class="glyphicon glyphicon-plus"></span></a>
    """),_display_(/*57.6*/detailsContent),format.raw/*57.20*/("""
"""),format.raw/*58.1*/("""</div>
</div>
""")))}))}
  }

  def render(triggersByGroup:scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.TriggerKey]]],currentTrigger:Option[org.quartz.Trigger],scripts:List[String],detailsContent:Html,request:play.api.mvc.Request[AnyContent]): play.twirl.api.HtmlFormat.Appendable = apply(triggersByGroup,currentTrigger,scripts)(detailsContent)(request)

  def f:((scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.TriggerKey]]],Option[org.quartz.Trigger],List[String]) => (Html) => (play.api.mvc.Request[AnyContent]) => play.twirl.api.HtmlFormat.Appendable) = (triggersByGroup,currentTrigger,scripts) => (detailsContent) => (request) => apply(triggersByGroup,currentTrigger,scripts)(detailsContent)(request)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed May 31 21:03:53 MDT 2017
                  SOURCE: /home/paul/lucid/piezo/admin/app/com/lucidchart/piezo/admin/views/triggersLayout.scala.html
                  HASH: 1b8b9b3267b2134e6ddd5cfa9b6483cb118f6d04
                  MATRIX: 722->1|1224->281|1252->420|1280->422|1291->425|1366->491|1406->493|1434->494|1610->643|1634->658|1647->662|1703->680|1744->693|1979->901|2042->942|2101->974|2122->986|2146->989|2199->1014|2314->1102|2376->1143|2422->1162|2520->1250|2561->1252|2606->1269|2678->1323|2691->1328|2730->1329|2775->1346|2857->1397|2902->1414|3024->1509|3045->1521|3061->1528|3115->1544|3168->1569|3205->1579|3225->1590|3312->1655|3366->1682|3525->1831|3566->1833|3619->1858|3694->1915|3707->1920|3746->1921|3799->1946|3879->1995|3932->2020|4016->2077|4035->2087|4067->2097|4122->2125|4141->2135|4172->2145|4225->2170|4286->2200|4335->2221|4439->2294|4471->2299|4581->2382|4601->2393|4657->2428|4783->2528|4818->2542|4846->2543
                  LINES: 19->1|33->10|34->14|35->15|35->15|35->15|35->15|36->16|40->20|40->20|40->20|40->20|41->21|44->24|44->24|45->25|45->25|45->25|46->26|49->29|49->29|50->30|50->30|50->30|51->31|52->32|52->32|52->32|53->33|54->34|55->35|57->37|57->37|57->37|57->37|58->38|58->38|58->38|58->38|59->39|59->39|59->39|60->40|61->41|61->41|61->41|62->42|63->43|64->44|64->44|64->44|64->44|65->45|65->45|65->45|66->46|67->47|68->48|72->52|73->53|76->56|76->56|76->56|77->57|77->57|78->58
                  -- GENERATED --
              */
          