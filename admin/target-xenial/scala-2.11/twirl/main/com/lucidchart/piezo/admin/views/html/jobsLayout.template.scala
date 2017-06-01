
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
object jobsLayout extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template5[scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.JobKey]]],Option[org.quartz.JobDetail],List[String],Html,play.api.mvc.Request[AnyContent],play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(
jobsByGroup: scala.collection.mutable.Buffer[(String, scala.collection.immutable.List[org.quartz.JobKey])],
currentJob: Option[org.quartz.JobDetail],
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
"""),_display_(/*15.2*/com/*15.5*/.lucidchart.piezo.admin.views.html.main("Piezo Jobs", scripts)/*15.67*/ {_display_(Seq[Any](format.raw/*15.69*/("""
"""),format.raw/*16.1*/("""<div class="row">
    <div class="col-md-3">
        <h3>Job groups</h3>
        <div class="panel-group jobs-list" id="accordion">
            """),_display_(/*20.14*/jobsByGroup/*20.25*/.map/*20.29*/ { jobGroup =>_display_(Seq[Any](format.raw/*20.43*/("""
            """),format.raw/*21.13*/("""<div class="panel panel-default">
                <div class="panel-heading">
                    <h5 class="title">
                        <a data-toggle="collapse" data-parent="#accordion" href="#collapse"""),_display_(/*24.92*/{jobGroup._1.replaceAll("\\W+", "_")}),format.raw/*24.129*/("""">
                            """),_display_(/*25.30*/jobGroup/*25.38*/._1),format.raw/*25.41*/("""
                        """),format.raw/*26.25*/("""</a>
                    </h5>
                </div>
                <div id="collapse"""),_display_(/*29.35*/{jobGroup._1.replaceAll("\\W+", "_")}),format.raw/*29.72*/(""""
                """),_display_(/*30.18*/if(!currentJob.isEmpty && jobGroup._1 == currentJob.get.getKey().getGroup())/*30.94*/ {_display_(Seq[Any](format.raw/*30.96*/("""
                    """),format.raw/*31.21*/("""class="panel-collapse collapse in">
                """)))}/*32.19*/else/*32.24*/{_display_(Seq[Any](format.raw/*32.25*/("""
                    """),format.raw/*33.21*/("""class="panel-collapse collapse ">
                """)))}),format.raw/*34.18*/("""
                    """),format.raw/*35.21*/("""<div class="panel-body">
                        <div class="list-group">
                        """),_display_(/*37.26*/jobGroup/*37.34*/._2.map/*37.41*/ { jobKey =>_display_(Seq[Any](format.raw/*37.53*/("""
                            """),format.raw/*38.29*/("""<a href=""""),_display_(/*38.39*/piezoRoutes/*38.50*/.Jobs.getJob(jobKey.getGroup(), jobKey.getName())),format.raw/*38.99*/(""""
                            """),_display_(/*39.30*/if(!currentJob.isEmpty && jobGroup._1 == currentJob.get.getKey.getGroup() && jobKey.getName() == currentJob.get.getKey.getName())/*39.159*/ {_display_(Seq[Any](format.raw/*39.161*/("""
                                """),format.raw/*40.33*/("""class="list-group-item active"
                            """)))}/*41.31*/else/*41.36*/{_display_(Seq[Any](format.raw/*41.37*/("""
                                """),format.raw/*42.33*/("""class="list-group-item"
                            """)))}),format.raw/*43.30*/("""
                            """),format.raw/*44.29*/("""data-toggle="tooltip" data-placement="auto right"title=""""),_display_(/*44.86*/jobKey/*44.92*/.getName()),format.raw/*44.102*/("""">
                                """),_display_(/*45.34*/jobKey/*45.40*/.getName()),format.raw/*45.50*/("""
                            """),format.raw/*46.29*/("""</a>
                        """)))}),format.raw/*47.26*/("""
                        """),format.raw/*48.25*/("""</div>
                    </div>
                </div>
            </div>
            """)))}),format.raw/*52.14*/("""
        """),format.raw/*53.9*/("""</div>
    </div>
    <div class="col-md-9">
      <a class="piezo-button pull-right" href=""""),_display_(/*56.49*/piezoRoutes/*56.60*/.Jobs.getNewJobForm()),format.raw/*56.81*/("""" data-toggle="tooltip" title="Add job"><span class="glyphicon glyphicon-plus"></span></a>
      """),_display_(/*57.8*/detailsContent),format.raw/*57.22*/("""
    """),format.raw/*58.5*/("""</div>
</div>
""")))}))}
  }

  def render(jobsByGroup:scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.JobKey]]],currentJob:Option[org.quartz.JobDetail],scripts:List[String],detailsContent:Html,request:play.api.mvc.Request[AnyContent]): play.twirl.api.HtmlFormat.Appendable = apply(jobsByGroup,currentJob,scripts)(detailsContent)(request)

  def f:((scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.JobKey]]],Option[org.quartz.JobDetail],List[String]) => (Html) => (play.api.mvc.Request[AnyContent]) => play.twirl.api.HtmlFormat.Appendable) = (jobsByGroup,currentJob,scripts) => (detailsContent) => (request) => apply(jobsByGroup,currentJob,scripts)(detailsContent)(request)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed May 31 21:03:53 MDT 2017
                  SOURCE: /home/paul/lucid/piezo/admin/app/com/lucidchart/piezo/admin/views/jobsLayout.scala.html
                  HASH: ede607f39a65fdde088d10a157a3d292328cfe41
                  MATRIX: 716->1|1208->271|1236->410|1264->412|1275->415|1346->477|1386->479|1414->480|1586->625|1606->636|1619->640|1671->654|1712->667|1947->875|2006->912|2065->944|2082->952|2106->955|2159->980|2274->1068|2332->1105|2378->1124|2463->1200|2503->1202|2552->1223|2624->1277|2637->1282|2676->1283|2725->1304|2807->1355|2856->1376|2982->1475|2999->1483|3015->1490|3065->1502|3122->1531|3159->1541|3179->1552|3249->1601|3307->1632|3446->1761|3487->1763|3548->1796|3627->1857|3640->1862|3679->1863|3740->1896|3824->1949|3881->1978|3965->2035|3980->2041|4012->2051|4075->2087|4090->2093|4121->2103|4178->2132|4239->2162|4292->2187|4412->2276|4448->2285|4568->2378|4588->2389|4630->2410|4754->2508|4789->2522|4821->2527
                  LINES: 19->1|33->10|34->14|35->15|35->15|35->15|35->15|36->16|40->20|40->20|40->20|40->20|41->21|44->24|44->24|45->25|45->25|45->25|46->26|49->29|49->29|50->30|50->30|50->30|51->31|52->32|52->32|52->32|53->33|54->34|55->35|57->37|57->37|57->37|57->37|58->38|58->38|58->38|58->38|59->39|59->39|59->39|60->40|61->41|61->41|61->41|62->42|63->43|64->44|64->44|64->44|64->44|65->45|65->45|65->45|66->46|67->47|68->48|72->52|73->53|76->56|76->56|76->56|77->57|77->57|78->58
                  -- GENERATED --
              */
          