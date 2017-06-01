
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
import org.quartz.TriggerKey
/**/
object job extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template8[scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.JobKey]]],Option[org.quartz.JobDetail],Option[List[com.lucidchart.piezo.JobRecord]],Option[List[org.quartz.Trigger]],Option[String],List[TriggerKey],List[TriggerKey],play.api.mvc.Request[AnyContent],play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*5.2*/(
jobsByGroup: scala.collection.mutable.Buffer[(String, scala.collection.immutable.List[org.quartz.JobKey])],
currentJob: Option[org.quartz.JobDetail],
jobHistory: Option[List[com.lucidchart.piezo.JobRecord]],
triggers: Option[List[org.quartz.Trigger]],
errorMessage: Option[String] = None,
pausableTriggers: List[TriggerKey] = Nil,
resumableTriggers: List[TriggerKey] = Nil
)(
implicit
request: play.api.mvc.Request[AnyContent]
):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*16.2*/("""


"""),_display_(/*19.2*/com/*19.5*/.lucidchart.piezo.admin.views.html.jobsLayout(jobsByGroup, currentJob)/*19.75*/ {_display_(Seq[Any](format.raw/*19.77*/("""
    """),_display_(/*20.6*/if(!errorMessage.isEmpty)/*20.31*/ {_display_(Seq[Any](format.raw/*20.33*/("""
        """),format.raw/*21.9*/("""<h3 class="text-danger">"""),_display_(/*21.34*/errorMessage/*21.46*/.get),format.raw/*21.50*/("""</h3>
    """)))}),format.raw/*22.6*/("""
    """),_display_(/*23.6*/if(!currentJob.isEmpty)/*23.29*/ {_display_(Seq[Any](format.raw/*23.31*/("""
      """),format.raw/*24.7*/("""<h3 class="pull-left job-name">"""),_display_(/*24.39*/currentJob/*24.49*/.get.getKey.getGroup()),format.raw/*24.71*/(""" """),format.raw/*24.72*/("""&raquo; <span class="text-info">"""),_display_(/*24.105*/currentJob/*24.115*/.get.getKey.getName()),format.raw/*24.136*/("""</span></h3>
      <a class="piezo-button pull-right" data-toggle="tooltip" title="Duplicate job" href=""""),_display_(/*25.93*/{piezoRoutes.Jobs.getNewJobForm()}),format.raw/*25.127*/("""?templateGroup="""),_display_(/*25.143*/currentJob/*25.153*/.get.getKey.getGroup()),format.raw/*25.175*/("""&templateName="""),_display_(/*25.190*/currentJob/*25.200*/.get.getKey.getName()),format.raw/*25.221*/("""">
        <span class="glyphicon glyphicon-duplicate"></span>
      </a>
      <a class="piezo-button pull-right" href=""""),_display_(/*28.49*/piezoRoutes/*28.60*/.Jobs.getEditJobAction(currentJob.get.getKey.getGroup(), currentJob.get.getKey.getName())),format.raw/*28.149*/("""" data-toggle="tooltip" title="Edit job">
        <span class="glyphicon glyphicon-pencil"></span>
      </a>
      <a id="deleteJob" class="piezo-button pull-right" data-toggle="tooltip" title="Delete job" onclick="$('#deleteConfirm').show()">
        <span class="glyphicon glyphicon-remove"></span>
      </a>
      <a id="runJob" class="piezo-button pull-right" data-toggle="tooltip" title="Trigger job" onclick="$('#runConfirm').show()">
        <span class="glyphicon glyphicon-flash"></span>
      </a>
      """),_display_(/*37.8*/if(!pausableTriggers.isEmpty)/*37.37*/ {_display_(Seq[Any](format.raw/*37.39*/("""
        """),format.raw/*38.9*/("""<a id="pauseJob" class="piezo-button pull-right" data-toggle="tooltip" title="Pause job">
            <span class="glyphicon glyphicon-pause"></span>
        </a>
      """)))}/*41.9*/else/*41.14*/{_display_(Seq[Any](format.raw/*41.15*/("""
        """),_display_(/*42.10*/if(!resumableTriggers.isEmpty)/*42.40*/ {_display_(Seq[Any](format.raw/*42.42*/("""
            """),format.raw/*43.13*/("""<a id="resumeJob" class="piezo-button pull-right" data-toggle="tooltip" title="Resume job">
                <span class="glyphicon glyphicon-play"></span>
            </a>
        """)))}),format.raw/*46.10*/("""
      """)))}),format.raw/*47.8*/("""

      """),format.raw/*49.7*/("""<div class="clearfix"></div>

        <div id="errorGrowl" style="display:none;" class="alert alert-danger">
            <p id="errorMessage">Failed to delete job.</p>
        </div>

        <!-- Run Job Alerts -->
        <div id="runSuccess"  style="display:none;" class="alert alert-success">
          <p>The job has been triggered, and will run once a thread becomes available.</p>
        </div>
        <div id="runConfirm" style="display:none;" class="alert alert-warning fade in">
            <h4>Are you sure you want to run this job right now?</h4>
            <button type="button" class="btn btn-success" onclick="runJob()">Run</button>
            <button type="button" class="btn btn-default" onclick="$('#runConfirm').hide()">Cancel</button>
        </div>

        <!-- Delete Job Alerts -->
        <div id="deleteConfirm" style="display:none;" class="alert alert-warning fade in">
          <h4>This will permanently delete the job!</h4>
          <button type="button" class="btn btn-danger" onclick="deleteJob()">Delete</button>
          <button type="button" class="btn btn-default" onclick="$('#deleteConfirm').hide()">Cancel</button>
        </div>
        <script>
            var deleteConfirm = $('#deleteError');
            var deleteError = $('#deleteConfirm');
            var runConfirm = $('#runConfirm');
            var runSuccess = $('#runSuccess');
            var runError = $('#runError');

            function showErrorMessage(message) """),format.raw/*78.48*/("""{"""),format.raw/*78.49*/("""
                """),format.raw/*79.17*/("""$('#errorGrowl').show();
                $('#errorMessage').text(message);
                setTimeout(function() """),format.raw/*81.39*/("""{"""),format.raw/*81.40*/("""
                    """),format.raw/*82.21*/("""$('#errorGrowl').fadeOut(1000);
                """),format.raw/*83.17*/("""}"""),format.raw/*83.18*/(""", 5000);
            """),format.raw/*84.13*/("""}"""),format.raw/*84.14*/("""

            """),format.raw/*86.13*/("""function deleteJob() """),format.raw/*86.34*/("""{"""),format.raw/*86.35*/("""
                """),format.raw/*87.17*/("""deleteConfirm.hide();
                var jobUrl = """"),_display_(/*88.32*/piezoRoutes/*88.43*/.Jobs.deleteJob(currentJob.get.getKey.getGroup(), currentJob.get.getKey.getName())),format.raw/*88.125*/("""";
                var deleteRequest = new XMLHttpRequest();
                deleteRequest.onreadystatechange = function() """),format.raw/*90.63*/("""{"""),format.raw/*90.64*/("""
                    """),format.raw/*91.21*/("""if (deleteRequest.readyState === 4) """),format.raw/*91.57*/("""{"""),format.raw/*91.58*/("""
                        """),format.raw/*92.25*/("""if (deleteRequest.status === 200) """),format.raw/*92.59*/("""{"""),format.raw/*92.60*/("""
                            """),format.raw/*93.29*/("""window.location.assign(""""),_display_(/*93.54*/piezoRoutes/*93.65*/.Jobs.getIndex()),format.raw/*93.81*/("""");
                        """),format.raw/*94.25*/("""}"""),format.raw/*94.26*/(""" """),format.raw/*94.27*/("""else """),format.raw/*94.32*/("""{"""),format.raw/*94.33*/("""
                            """),format.raw/*95.29*/("""showErrorMessage('Failed to delete job.');
                        """),format.raw/*96.25*/("""}"""),format.raw/*96.26*/("""
                    """),format.raw/*97.21*/("""}"""),format.raw/*97.22*/("""
                """),format.raw/*98.17*/("""}"""),format.raw/*98.18*/(""";
                deleteRequest.open("DELETE", jobUrl, true);
                deleteRequest.send(null);
            """),format.raw/*101.13*/("""}"""),format.raw/*101.14*/("""

            """),format.raw/*103.13*/("""function runJob() """),format.raw/*103.31*/("""{"""),format.raw/*103.32*/("""
                """),format.raw/*104.17*/("""runConfirm.hide();
                var jobUrl = """"),_display_(/*105.32*/piezoRoutes/*105.43*/.Triggers.triggerJob(currentJob.get.getKey.getGroup(), currentJob.get.getKey.getName())),format.raw/*105.130*/("""";
                var runRequest = new XMLHttpRequest();
                runRequest.onreadystatechange = function() """),format.raw/*107.60*/("""{"""),format.raw/*107.61*/("""
                    """),format.raw/*108.21*/("""if (runRequest.readyState === 4) """),format.raw/*108.54*/("""{"""),format.raw/*108.55*/("""
                        """),format.raw/*109.25*/("""if (runRequest.status === 200) """),format.raw/*109.56*/("""{"""),format.raw/*109.57*/("""
                            """),format.raw/*110.29*/("""runSuccess.show();
                            setTimeout(function() """),format.raw/*111.51*/("""{"""),format.raw/*111.52*/("""
                                """),format.raw/*112.33*/("""runSuccess.fadeOut(1000);
                            """),format.raw/*113.29*/("""}"""),format.raw/*113.30*/(""", 5000);
                        """),format.raw/*114.25*/("""}"""),format.raw/*114.26*/(""" """),format.raw/*114.27*/("""else """),format.raw/*114.32*/("""{"""),format.raw/*114.33*/("""
                            """),format.raw/*115.29*/("""showErrorMessage('Failed to run job.');
                        """),format.raw/*116.25*/("""}"""),format.raw/*116.26*/("""
                    """),format.raw/*117.21*/("""}"""),format.raw/*117.22*/("""
                """),format.raw/*118.17*/("""}"""),format.raw/*118.18*/(""";
                runRequest.open("POST", jobUrl, true);
                runRequest.send(null);
            """),format.raw/*121.13*/("""}"""),format.raw/*121.14*/("""

            """),format.raw/*123.13*/("""function setTrigger(pause, triggerUrl) """),format.raw/*123.52*/("""{"""),format.raw/*123.53*/("""
                """),format.raw/*124.17*/("""$.ajax("""),format.raw/*124.24*/("""{"""),format.raw/*124.25*/("""
                    """),format.raw/*125.21*/("""url:triggerUrl,
                    type:'PATCH',
                    headers : """),format.raw/*127.31*/("""{"""),format.raw/*127.32*/("""
                        """),format.raw/*128.25*/("""'Accept' : 'application/json',
                        'Content-Type' : 'application/json'
                    """),format.raw/*130.21*/("""}"""),format.raw/*130.22*/(""",
                    data: JSON.stringify("""),format.raw/*131.42*/("""{"""),format.raw/*131.43*/(""""state": (pause? "PAUSED" : "NORMAL")"""),format.raw/*131.80*/("""}"""),format.raw/*131.81*/("""),
                    success: function(resp, status) """),format.raw/*132.53*/("""{"""),format.raw/*132.54*/("""
                        """),format.raw/*133.25*/("""window.location.reload();
                    """),format.raw/*134.21*/("""}"""),format.raw/*134.22*/(""",
                    error: function() """),format.raw/*135.39*/("""{"""),format.raw/*135.40*/("""
                        """),format.raw/*136.25*/("""showErrorMessage(pause? 'Failed to pause trigger.' : 'Failed to resume trigger.');
                    """),format.raw/*137.21*/("""}"""),format.raw/*137.22*/("""
                """),format.raw/*138.17*/("""}"""),format.raw/*138.18*/(""");
            """),format.raw/*139.13*/("""}"""),format.raw/*139.14*/("""
            """),format.raw/*140.13*/("""$('#pauseJob').click(function() """),format.raw/*140.45*/("""{"""),format.raw/*140.46*/("""
                """),_display_(/*141.18*/pausableTriggers/*141.34*/.map/*141.38*/ { triggerKey =>_display_(Seq[Any](format.raw/*141.54*/("""
                    """),format.raw/*142.21*/("""setTrigger(true, """"),_display_(/*142.40*/piezoRoutes/*142.51*/.Triggers.patchTrigger(triggerKey.getGroup(), triggerKey.getName())),format.raw/*142.118*/("""");
                """)))}),format.raw/*143.18*/("""
            """),format.raw/*144.13*/("""}"""),format.raw/*144.14*/(""");
            $('#resumeJob').click(function() """),format.raw/*145.46*/("""{"""),format.raw/*145.47*/("""
                """),_display_(/*146.18*/resumableTriggers/*146.35*/.map/*146.39*/ { triggerKey =>_display_(Seq[Any](format.raw/*146.55*/("""
                    """),format.raw/*147.21*/("""setTrigger(false, """"),_display_(/*147.41*/piezoRoutes/*147.52*/.Triggers.patchTrigger(triggerKey.getGroup(), triggerKey.getName())),format.raw/*147.119*/("""");
                """)))}),format.raw/*148.18*/("""
            """),format.raw/*149.13*/("""}"""),format.raw/*149.14*/(""");
        </script>
        <div class="clearfix"></div>
        <table class="table table-condensed table-fixed-first-col table-bordered table-striped table-hover">
            <tbody>
            <tr>
                <td class="text-right">Class</td>
                <td>"""),_display_(/*156.22*/currentJob/*156.32*/.get.getJobClass.getName()),format.raw/*156.58*/("""</td>
            </tr>
            <tr>
                <td class="text-right">Description</td>
                <td>
                    """),_display_(/*161.22*/if(currentJob.get.getDescription() != null)/*161.65*/ {_display_(_display_(/*161.68*/currentJob/*161.78*/.get.getDescription()))}/*161.101*/else/*161.106*/{}),format.raw/*161.108*/("""
                """),format.raw/*162.17*/("""</td>
            </tr>
            <tr>
                <td class="text-right small">Durable</td>
                <td class="small">"""),_display_(/*166.36*/currentJob/*166.46*/.get.isDurable()),format.raw/*166.62*/("""</td>
            </tr>
            <tr>
                <td class="text-right small">Persist job data after execution</td>
                <td class="small">"""),_display_(/*170.36*/currentJob/*170.46*/.get.isPersistJobDataAfterExecution()),format.raw/*170.83*/("""</td>
            </tr>
            <tr>
                <td class="text-right small">Concurrent execution disallowed</td>
                <td class="small">"""),_display_(/*174.36*/currentJob/*174.46*/.get.isConcurrentExectionDisallowed()),format.raw/*174.83*/("""</td>
            </tr>
            <tr>
                <td class="text-right small">Requests recovery</td>
                <td class="small">"""),_display_(/*178.36*/currentJob/*178.46*/.get.requestsRecovery()),format.raw/*178.69*/("""</td>
            </tr>
            </tbody>
        </table>

        <hr />

        <h4>Job Data Map</h4>
        <table class="table table-bordered table-striped table-condensed job-data table-hover">
            <thead>
            <tr>
                <th class="text-right">Key</th>
                <th>Value</th>
            </tr>
            </thead>
            <tbody>
            """),_display_(/*194.14*/currentJob/*194.24*/.get.getJobDataMap.getKeys.map/*194.54*/ { jobDataKey =>_display_(Seq[Any](format.raw/*194.70*/("""
                """),format.raw/*195.17*/("""<tr>
                    <td class="text-right">"""),_display_(/*196.45*/jobDataKey),format.raw/*196.55*/("""</td>
                    <td>"""),_display_(/*197.26*/currentJob/*197.36*/.get.getJobDataMap.get(jobDataKey)),format.raw/*197.70*/("""</td>
                </tr>
            """)))}),format.raw/*199.14*/("""
            """),format.raw/*200.13*/("""</tbody>
        </table>

        <hr />

        <h4 class="inline-header">Triggers</h4>
        <a class="piezo-button" href=""""),_display_(/*206.40*/{piezoRoutes.Triggers.getNewTriggerForm("cron")}),format.raw/*206.88*/("""?jobGroup="""),_display_(/*206.99*/currentJob/*206.109*/.get.getKey.getGroup()),format.raw/*206.131*/("""&jobName="""),_display_(/*206.141*/currentJob/*206.151*/.get.getKey.getName()),format.raw/*206.172*/("""" data-toggle="tooltip" title="Add trigger for this job"><span class="glyphicon glyphicon-plus"></span></a>
        <table class="table table-bordered table-striped table-condensed">
          <thead>
            <tr>
              <th class="text-right">Group</th>
              <th>Name</th>
            </tr>
          </thead>
          <tbody>
            """),_display_(/*215.14*/triggers/*215.22*/.get.map/*215.30*/ { trigger =>_display_(Seq[Any](format.raw/*215.43*/("""
              """),format.raw/*216.15*/("""<tr>
                <td class="text-right">"""),_display_(/*217.41*/trigger/*217.48*/.getKey.getGroup()),format.raw/*217.66*/("""</td>
                <td><a href=""""),_display_(/*218.31*/piezoRoutes/*218.42*/.Triggers.getTrigger(trigger.getKey.getGroup(), trigger.getKey.getName())),format.raw/*218.115*/("""">"""),_display_(/*218.118*/trigger/*218.125*/.getKey.getName()),format.raw/*218.142*/("""</a></td>
              </tr>
            """)))}),format.raw/*220.14*/("""
          """),format.raw/*221.11*/("""</tbody>
        </table>

        """),_display_(/*224.10*/if(jobHistory.isDefined)/*224.34*/ {_display_(Seq[Any](format.raw/*224.36*/("""
            """),format.raw/*225.13*/("""<h4>Job History</h4>
            <table class="table table-bordered table-striped table-hover table-condensed">
                <thead>
                <tr>
                    <th>Trigger Group</th>
                    <th>Trigger Name</th>
                    <th>Success</th>
                    <th>Start</th>
                    <th>Finish</th>
                </tr>
                </thead>
                <tbody>
                """),_display_(/*237.18*/jobHistory/*237.28*/.get.map/*237.36*/ { record =>_display_(Seq[Any](format.raw/*237.48*/("""
                """),format.raw/*238.17*/("""<tr>
                    <td>"""),_display_(/*239.26*/record/*239.32*/.trigger_group),format.raw/*239.46*/("""</td>
                    <td>"""),_display_(/*240.26*/record/*240.32*/.trigger_name),format.raw/*240.45*/("""</td>
                    <td class="column-success">"""),_display_(/*241.49*/record/*241.55*/.success),format.raw/*241.63*/("""</td>
                    <td>"""),_display_(/*242.26*/record/*242.32*/.start),format.raw/*242.38*/("""</td>
                    <td>"""),_display_(/*243.26*/record/*243.32*/.finish),format.raw/*243.39*/("""</td>
                </tr>
                """)))}),format.raw/*245.18*/("""
                """),format.raw/*246.17*/("""</tbody>
            </table>
        """)))}),format.raw/*248.10*/("""
    """)))}),format.raw/*249.6*/("""
""")))}))}
  }

  def render(jobsByGroup:scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.JobKey]]],currentJob:Option[org.quartz.JobDetail],jobHistory:Option[List[com.lucidchart.piezo.JobRecord]],triggers:Option[List[org.quartz.Trigger]],errorMessage:Option[String],pausableTriggers:List[TriggerKey],resumableTriggers:List[TriggerKey],request:play.api.mvc.Request[AnyContent]): play.twirl.api.HtmlFormat.Appendable = apply(jobsByGroup,currentJob,jobHistory,triggers,errorMessage,pausableTriggers,resumableTriggers)(request)

  def f:((scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.JobKey]]],Option[org.quartz.JobDetail],Option[List[com.lucidchart.piezo.JobRecord]],Option[List[org.quartz.Trigger]],Option[String],List[TriggerKey],List[TriggerKey]) => (play.api.mvc.Request[AnyContent]) => play.twirl.api.HtmlFormat.Appendable) = (jobsByGroup,currentJob,jobHistory,triggers,errorMessage,pausableTriggers,resumableTriggers) => (request) => apply(jobsByGroup,currentJob,jobHistory,triggers,errorMessage,pausableTriggers,resumableTriggers)(request)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed May 31 21:03:54 MDT 2017
                  SOURCE: /home/paul/lucid/piezo/admin/app/com/lucidchart/piezo/admin/views/job.scala.html
                  HASH: e83530a79cbbe08ec65ab98f7f00a90ecc55b8b7
                  MATRIX: 981->169|1499->599|1529->603|1540->606|1619->676|1659->678|1691->684|1725->709|1765->711|1801->720|1853->745|1874->757|1899->761|1940->772|1972->778|2004->801|2044->803|2078->810|2137->842|2156->852|2199->874|2228->875|2289->908|2309->918|2352->939|2484->1044|2540->1078|2584->1094|2604->1104|2648->1126|2691->1141|2711->1151|2754->1172|2903->1294|2923->1305|3034->1394|3577->1911|3615->1940|3655->1942|3691->1951|3879->2122|3892->2127|3931->2128|3968->2138|4007->2168|4047->2170|4088->2183|4300->2364|4338->2372|4373->2380|5880->3859|5909->3860|5954->3877|6095->3990|6124->3991|6173->4012|6249->4060|6278->4061|6327->4082|6356->4083|6398->4097|6447->4118|6476->4119|6521->4136|6601->4189|6621->4200|6725->4282|6876->4405|6905->4406|6954->4427|7018->4463|7047->4464|7100->4489|7162->4523|7191->4524|7248->4553|7300->4578|7320->4589|7357->4605|7413->4633|7442->4634|7471->4635|7504->4640|7533->4641|7590->4670|7685->4737|7714->4738|7763->4759|7792->4760|7837->4777|7866->4778|8011->4894|8041->4895|8084->4909|8131->4927|8161->4928|8207->4945|8285->4995|8306->5006|8416->5093|8562->5210|8592->5211|8642->5232|8704->5265|8734->5266|8788->5291|8848->5322|8878->5323|8936->5352|9034->5421|9064->5422|9126->5455|9209->5509|9239->5510|9301->5543|9331->5544|9361->5545|9395->5550|9425->5551|9483->5580|9576->5644|9606->5645|9656->5666|9686->5667|9732->5684|9762->5685|9899->5793|9929->5794|9972->5808|10040->5847|10070->5848|10116->5865|10152->5872|10182->5873|10232->5894|10341->5974|10371->5975|10425->6000|10565->6111|10595->6112|10667->6155|10697->6156|10763->6193|10793->6194|10877->6249|10907->6250|10961->6275|11036->6321|11066->6322|11135->6362|11165->6363|11219->6388|11351->6491|11381->6492|11427->6509|11457->6510|11501->6525|11531->6526|11573->6539|11634->6571|11664->6572|11710->6590|11736->6606|11750->6610|11805->6626|11855->6647|11902->6666|11923->6677|12013->6744|12066->6765|12108->6778|12138->6779|12215->6827|12245->6828|12291->6846|12318->6863|12332->6867|12387->6883|12437->6904|12485->6924|12506->6935|12596->7002|12649->7023|12691->7036|12721->7037|13024->7312|13044->7322|13092->7348|13259->7487|13312->7530|13344->7533|13364->7543|13399->7566|13414->7571|13439->7573|13485->7590|13647->7724|13667->7734|13705->7750|13892->7909|13912->7919|13971->7956|14157->8114|14177->8124|14236->8161|14408->8305|14428->8315|14473->8338|14894->8731|14914->8741|14954->8771|15009->8787|15055->8804|15132->8853|15164->8863|15223->8894|15243->8904|15299->8938|15372->8979|15414->8992|15572->9122|15642->9170|15681->9181|15702->9191|15747->9213|15786->9223|15807->9233|15851->9254|16241->9616|16259->9624|16277->9632|16329->9645|16373->9660|16446->9705|16463->9712|16503->9730|16567->9766|16588->9777|16684->9850|16716->9853|16734->9860|16774->9877|16849->9920|16889->9931|16953->9967|16987->9991|17028->9993|17070->10006|17536->10444|17556->10454|17574->10462|17625->10474|17671->10491|17729->10521|17745->10527|17781->10541|17840->10572|17856->10578|17891->10591|17973->10645|17989->10651|18019->10659|18078->10690|18094->10696|18122->10702|18181->10733|18197->10739|18226->10746|18303->10791|18349->10808|18420->10847|18457->10853
                  LINES: 22->5|36->16|39->19|39->19|39->19|39->19|40->20|40->20|40->20|41->21|41->21|41->21|41->21|42->22|43->23|43->23|43->23|44->24|44->24|44->24|44->24|44->24|44->24|44->24|44->24|45->25|45->25|45->25|45->25|45->25|45->25|45->25|45->25|48->28|48->28|48->28|57->37|57->37|57->37|58->38|61->41|61->41|61->41|62->42|62->42|62->42|63->43|66->46|67->47|69->49|98->78|98->78|99->79|101->81|101->81|102->82|103->83|103->83|104->84|104->84|106->86|106->86|106->86|107->87|108->88|108->88|108->88|110->90|110->90|111->91|111->91|111->91|112->92|112->92|112->92|113->93|113->93|113->93|113->93|114->94|114->94|114->94|114->94|114->94|115->95|116->96|116->96|117->97|117->97|118->98|118->98|121->101|121->101|123->103|123->103|123->103|124->104|125->105|125->105|125->105|127->107|127->107|128->108|128->108|128->108|129->109|129->109|129->109|130->110|131->111|131->111|132->112|133->113|133->113|134->114|134->114|134->114|134->114|134->114|135->115|136->116|136->116|137->117|137->117|138->118|138->118|141->121|141->121|143->123|143->123|143->123|144->124|144->124|144->124|145->125|147->127|147->127|148->128|150->130|150->130|151->131|151->131|151->131|151->131|152->132|152->132|153->133|154->134|154->134|155->135|155->135|156->136|157->137|157->137|158->138|158->138|159->139|159->139|160->140|160->140|160->140|161->141|161->141|161->141|161->141|162->142|162->142|162->142|162->142|163->143|164->144|164->144|165->145|165->145|166->146|166->146|166->146|166->146|167->147|167->147|167->147|167->147|168->148|169->149|169->149|176->156|176->156|176->156|181->161|181->161|181->161|181->161|181->161|181->161|181->161|182->162|186->166|186->166|186->166|190->170|190->170|190->170|194->174|194->174|194->174|198->178|198->178|198->178|214->194|214->194|214->194|214->194|215->195|216->196|216->196|217->197|217->197|217->197|219->199|220->200|226->206|226->206|226->206|226->206|226->206|226->206|226->206|226->206|235->215|235->215|235->215|235->215|236->216|237->217|237->217|237->217|238->218|238->218|238->218|238->218|238->218|238->218|240->220|241->221|244->224|244->224|244->224|245->225|257->237|257->237|257->237|257->237|258->238|259->239|259->239|259->239|260->240|260->240|260->240|261->241|261->241|261->241|262->242|262->242|262->242|263->243|263->243|263->243|265->245|266->246|268->248|269->249
                  -- GENERATED --
              */
          