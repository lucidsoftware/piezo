
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
import com.lucidchart.piezo.admin.controllers.{routes=>piezoRoutes}
import com.lucidchart.piezo.admin.views
import java.net.URLEncoder
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import org.quartz.{JobKey, SchedulerMetaData}
/**/
object jobs extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template7[scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.JobKey]]],Option[org.quartz.JobDetail],Option[List[com.lucidchart.piezo.JobRecord]],List[JobKey],SchedulerMetaData,Option[String],play.api.mvc.Request[AnyContent],play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*7.2*/(
jobsByGroup: scala.collection.mutable.Buffer[(String, scala.collection.immutable.List[org.quartz.JobKey])],
currentJob: Option[org.quartz.JobDetail],
jobsHistory: Option[List[com.lucidchart.piezo.JobRecord]],
untriggeredJobs: List[JobKey],
schedulerMetadata: SchedulerMetaData,
errorMessage: Option[String] = None
)(
implicit request: play.api.mvc.Request[AnyContent]
):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*16.2*/("""

"""),_display_(/*18.2*/com/*18.5*/.lucidchart.piezo.admin.views.html.jobsLayout(jobsByGroup, currentJob)/*18.75*/ {_display_(Seq[Any](format.raw/*18.77*/("""
"""),format.raw/*19.1*/("""<h3>Select a job</h3>
<table class="table table-condensed table-fixed-first-col table-bordered table-striped table-hover">
    <tbody>
    <tr>
        <td class="text-right">Scheduler name</td>
        <td>"""),_display_(/*24.14*/schedulerMetadata/*24.31*/.getSchedulerName()),format.raw/*24.50*/("""</td>
    </tr>
    <tr>
        <td class="text-right">Total jobs</td>
        <td>"""),_display_(/*28.14*/{jobsByGroup.foldLeft(0)((a, b) => a + b._2.length)}),format.raw/*28.66*/("""</td>
    </tr>
    </tbody>
</table>

    """),_display_(/*33.6*/if(jobsHistory.isDefined)/*33.31*/ {_display_(Seq[Any](format.raw/*33.33*/("""
    """),format.raw/*34.5*/("""<h4>Jobs History</h4>
    <table class="table table-bordered table-striped table-hover table-condensed table-fixed-layout">
        <thead>
        <tr>
            <th class="column-time">Most Recent Start</th>
            <th class="column-time">Finish</th>
            <th class="column-long-content">Job Group</th>
            <th class="column-long-content">Job Name</th>
            <th class="column-long-content">Trigger Group</th>
            <th class="column-long-content">Trigger Name</th>
            <th class="column-success">Success</th>

        </tr>
        </thead>
        <tbody>
        """),_display_(/*49.10*/defining(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))/*49.68*/ { dtf =>_display_(Seq[Any](format.raw/*49.77*/("""
        """),_display_(/*50.10*/jobsHistory/*50.21*/.get.map/*50.29*/ { record =>_display_(Seq[Any](format.raw/*50.41*/("""
        """),format.raw/*51.9*/("""<tr>
            <td>"""),_display_(/*52.18*/dtf/*52.21*/.print(new DateTime(record.start))),format.raw/*52.55*/("""</td>
            <td>"""),_display_(/*53.18*/dtf/*53.21*/.print(new DateTime(record.finish))),format.raw/*53.56*/("""</td>
            <td>"""),_display_(/*54.18*/record/*54.24*/.group),format.raw/*54.30*/("""</td>
            <td><a href=""""),_display_(/*55.27*/piezoRoutes/*55.38*/.Jobs.getJob(record.group, record.name)),format.raw/*55.77*/("""">"""),_display_(/*55.80*/record/*55.86*/.name),format.raw/*55.91*/("""</a></td>
            <td>"""),_display_(/*56.18*/record/*56.24*/.trigger_group),format.raw/*56.38*/("""</td>
            <td>"""),_display_(/*57.18*/record/*57.24*/.trigger_name),format.raw/*57.37*/("""</td>
            <td>"""),_display_(/*58.18*/record/*58.24*/.success),format.raw/*58.32*/("""</td>

        </tr>
        """)))}),format.raw/*61.10*/("""
        """)))}),format.raw/*62.10*/("""
        """),format.raw/*63.9*/("""</tbody>
    </table>
    """)))}),format.raw/*65.6*/("""
    """),_display_(/*66.6*/if(!untriggeredJobs.isEmpty)/*66.34*/ {_display_(Seq[Any](format.raw/*66.36*/("""
    """),format.raw/*67.5*/("""<h4>Untriggered Jobs</h4>
    <table class="table table-bordered table-striped table-hover table-condensed">
        <thead>
        <tr>
            <th>Job Group</th>
            <th>Job Name</th>
        </tr>
        </thead>
        <tbody>
        """),_display_(/*76.10*/untriggeredJobs/*76.25*/.map/*76.29*/ { job =>_display_(Seq[Any](format.raw/*76.38*/("""
        """),format.raw/*77.9*/("""<tr>
            <td>"""),_display_(/*78.18*/job/*78.21*/.getGroup),format.raw/*78.30*/("""</td>
            <td><a href=""""),_display_(/*79.27*/piezoRoutes/*79.38*/.Jobs.getJob(job.getGroup, job.getName)),format.raw/*79.77*/("""">"""),_display_(/*79.80*/job/*79.83*/.getName),format.raw/*79.91*/("""</a></td>
        </tr>
        """)))}),format.raw/*81.10*/("""
        """),format.raw/*82.9*/("""</tbody>
    </table>
    """)))}),format.raw/*84.6*/("""
""")))}))}
  }

  def render(jobsByGroup:scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.JobKey]]],currentJob:Option[org.quartz.JobDetail],jobsHistory:Option[List[com.lucidchart.piezo.JobRecord]],untriggeredJobs:List[JobKey],schedulerMetadata:SchedulerMetaData,errorMessage:Option[String],request:play.api.mvc.Request[AnyContent]): play.twirl.api.HtmlFormat.Appendable = apply(jobsByGroup,currentJob,jobsHistory,untriggeredJobs,schedulerMetadata,errorMessage)(request)

  def f:((scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.JobKey]]],Option[org.quartz.JobDetail],Option[List[com.lucidchart.piezo.JobRecord]],List[JobKey],SchedulerMetaData,Option[String]) => (play.api.mvc.Request[AnyContent]) => play.twirl.api.HtmlFormat.Appendable) = (jobsByGroup,currentJob,jobsHistory,untriggeredJobs,schedulerMetadata,errorMessage) => (request) => apply(jobsByGroup,currentJob,jobsHistory,untriggeredJobs,schedulerMetadata,errorMessage)(request)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed May 31 21:03:53 MDT 2017
                  SOURCE: /home/paul/lucid/piezo/admin/app/com/lucidchart/piezo/admin/views/jobs.scala.html
                  HASH: d0fd8da477b05054771fba5cb895970bfa1549cf
                  MATRIX: 1036->261|1495->632|1524->635|1535->638|1614->708|1654->710|1682->711|1917->919|1943->936|1983->955|2095->1040|2168->1092|2238->1136|2272->1161|2312->1163|2344->1168|2982->1779|3049->1837|3096->1846|3133->1856|3153->1867|3170->1875|3220->1887|3256->1896|3305->1918|3317->1921|3372->1955|3422->1978|3434->1981|3490->2016|3540->2039|3555->2045|3582->2051|3641->2083|3661->2094|3721->2133|3751->2136|3766->2142|3792->2147|3846->2174|3861->2180|3896->2194|3946->2217|3961->2223|3995->2236|4045->2259|4060->2265|4089->2273|4150->2303|4191->2313|4227->2322|4284->2349|4316->2355|4353->2383|4393->2385|4425->2390|4707->2645|4731->2660|4744->2664|4791->2673|4827->2682|4876->2704|4888->2707|4918->2716|4977->2748|4997->2759|5057->2798|5087->2801|5099->2804|5128->2812|5192->2845|5228->2854|5285->2881
                  LINES: 24->7|36->16|38->18|38->18|38->18|38->18|39->19|44->24|44->24|44->24|48->28|48->28|53->33|53->33|53->33|54->34|69->49|69->49|69->49|70->50|70->50|70->50|70->50|71->51|72->52|72->52|72->52|73->53|73->53|73->53|74->54|74->54|74->54|75->55|75->55|75->55|75->55|75->55|75->55|76->56|76->56|76->56|77->57|77->57|77->57|78->58|78->58|78->58|81->61|82->62|83->63|85->65|86->66|86->66|86->66|87->67|96->76|96->76|96->76|96->76|97->77|98->78|98->78|98->78|99->79|99->79|99->79|99->79|99->79|99->79|101->81|102->82|104->84
                  -- GENERATED --
              */
          