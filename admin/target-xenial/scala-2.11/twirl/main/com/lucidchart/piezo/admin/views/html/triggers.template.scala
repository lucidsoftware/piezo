
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
object triggers extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template6[scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.TriggerKey]]],Option[org.quartz.Trigger],List[org.quartz.Trigger],org.quartz.SchedulerMetaData,Option[String],play.api.mvc.Request[AnyContent],play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(
triggersByGroup: scala.collection.mutable.Buffer[(String, scala.collection.immutable.List[org.quartz.TriggerKey])],
currentTrigger: Option[org.quartz.Trigger],
upcomingTriggers: List[org.quartz.Trigger],
schedulerMetadata: org.quartz.SchedulerMetaData,
errorMessage: Option[String] = None
)(
implicit
request: play.api.mvc.Request[AnyContent]
):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import com.lucidchart.piezo.admin.controllers.{routes=>piezoRoutes}
import com.lucidchart.piezo.admin.views
import java.net.URLEncoder
import java.util.Date
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime

Seq[Any](format.raw/*10.2*/("""
"""),format.raw/*17.1*/("""
"""),_display_(/*18.2*/com/*18.5*/.lucidchart.piezo.admin.views.html.triggersLayout(triggersByGroup, currentTrigger)/*18.87*/ {_display_(Seq[Any](format.raw/*18.89*/("""
    """),format.raw/*19.5*/("""<h3>Select a trigger</h3>
    <table class="table table-condensed table-fixed-first-col table-bordered table-striped table-hover">
        <tbody>
        <tr>
            <td class="text-right">Scheduler name</td>
            <td>"""),_display_(/*24.18*/schedulerMetadata/*24.35*/.getSchedulerName()),format.raw/*24.54*/("""</td>
        </tr>
        <tr>
            <td class="text-right">Total triggers</td>
            <td>"""),_display_(/*28.18*/{triggersByGroup.foldLeft(0)((a, b) => a + b._2.length)}),format.raw/*28.74*/("""</td>
        </tr>
        </tbody>
    </table>

    <h4>Upcoming Triggers</h4>
    <table class="table table-bordered table-striped table-hover table-condensed table-fixed-layout">
        <thead>
        <tr>
            <th class="column-time">Next Fire Time</th>
            <th class="column-time">Following Fire Time</th>
            <th class="column-long-content">Trigger Group</th>
            <th class="column-long-content">Trigger Name</th>
            <th class="column-long-content">Job Group</th>
            <th class="column-long-content">Job Name</th>

        </tr>
        </thead>
        <tbody>
        """),_display_(/*47.10*/defining(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))/*47.68*/ { dtf =>_display_(Seq[Any](format.raw/*47.77*/("""
        """),_display_(/*48.10*/defining(new Date())/*48.30*/ { now =>_display_(Seq[Any](format.raw/*48.39*/("""
        """),_display_(/*49.10*/upcomingTriggers/*49.26*/.map/*49.30*/ { trigger =>_display_(Seq[Any](format.raw/*49.43*/("""
        """),format.raw/*50.9*/("""<tr>
            <td>"""),_display_(/*51.18*/dtf/*51.21*/.print(new DateTime(trigger.getFireTimeAfter(now)))),format.raw/*51.72*/("""</td>
            <td>"""),_display_(/*52.18*/dtf/*52.21*/.print(new DateTime(trigger.getFireTimeAfter(trigger.getFireTimeAfter(now))))),format.raw/*52.98*/("""</td>
            <td>"""),_display_(/*53.18*/trigger/*53.25*/.getKey.getGroup),format.raw/*53.41*/("""</td>
            <td>"""),_display_(/*54.18*/trigger/*54.25*/.getKey.getName),format.raw/*54.40*/("""</td>
            <td>"""),_display_(/*55.18*/trigger/*55.25*/.getJobKey.getGroup),format.raw/*55.44*/("""</td>
            <td>"""),_display_(/*56.18*/trigger/*56.25*/.getJobKey.getName),format.raw/*56.43*/("""</td>

        </tr>
        """)))}),format.raw/*59.10*/("""
        """)))}),format.raw/*60.10*/("""
        """)))}),format.raw/*61.10*/("""
        """),format.raw/*62.9*/("""</tbody>
    </table>
""")))}))}
  }

  def render(triggersByGroup:scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.TriggerKey]]],currentTrigger:Option[org.quartz.Trigger],upcomingTriggers:List[org.quartz.Trigger],schedulerMetadata:org.quartz.SchedulerMetaData,errorMessage:Option[String],request:play.api.mvc.Request[AnyContent]): play.twirl.api.HtmlFormat.Appendable = apply(triggersByGroup,currentTrigger,upcomingTriggers,schedulerMetadata,errorMessage)(request)

  def f:((scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.TriggerKey]]],Option[org.quartz.Trigger],List[org.quartz.Trigger],org.quartz.SchedulerMetaData,Option[String]) => (play.api.mvc.Request[AnyContent]) => play.twirl.api.HtmlFormat.Appendable) = (triggersByGroup,currentTrigger,upcomingTriggers,schedulerMetadata,errorMessage) => (request) => apply(triggersByGroup,currentTrigger,upcomingTriggers,schedulerMetadata,errorMessage)(request)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed May 31 21:03:53 MDT 2017
                  SOURCE: /home/paul/lucid/piezo/admin/app/com/lucidchart/piezo/admin/views/triggers.scala.html
                  HASH: 02532bd966128bdb23b7f4100edf5d34f28dca1a
                  MATRIX: 767->1|1430->347|1458->584|1486->586|1497->589|1588->671|1628->673|1660->678|1919->910|1945->927|1985->946|2117->1051|2194->1107|2850->1736|2917->1794|2964->1803|3001->1813|3030->1833|3077->1842|3114->1852|3139->1868|3152->1872|3203->1885|3239->1894|3288->1916|3300->1919|3372->1970|3422->1993|3434->1996|3532->2073|3582->2096|3598->2103|3635->2119|3685->2142|3701->2149|3737->2164|3787->2187|3803->2194|3843->2213|3893->2236|3909->2243|3948->2261|4009->2291|4050->2301|4091->2311|4127->2320
                  LINES: 19->1|36->10|37->17|38->18|38->18|38->18|38->18|39->19|44->24|44->24|44->24|48->28|48->28|67->47|67->47|67->47|68->48|68->48|68->48|69->49|69->49|69->49|69->49|70->50|71->51|71->51|71->51|72->52|72->52|72->52|73->53|73->53|73->53|74->54|74->54|74->54|75->55|75->55|75->55|76->56|76->56|76->56|79->59|80->60|81->61|82->62
                  -- GENERATED --
              */
          