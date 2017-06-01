
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
import com.lucidchart.piezo.TriggerMonitoringPriority
import com.lucidchart.piezo.admin.controllers.{routes=>piezoRoutes}
import com.lucidchart.piezo.admin.views
import java.net.URLEncoder
import org.quartz._
import org.quartz.Trigger.TriggerState
/**/
object trigger extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template8[scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.TriggerKey]]],Option[org.quartz.Trigger],Option[List[com.lucidchart.piezo.TriggerRecord]],Option[String],Option[com.lucidchart.piezo.TriggerMonitoringPriority.Value],Integer,Option[TriggerState],play.api.mvc.Request[AnyContent],play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*7.2*/(
triggersByGroup: scala.collection.mutable.Buffer[(String, scala.collection.immutable.List[org.quartz.TriggerKey])],
currentTrigger: Option[org.quartz.Trigger],
triggerHistory: Option[List[com.lucidchart.piezo.TriggerRecord]],
errorMessage: Option[String] = None,
triggerMonitoringPriority: Option[com.lucidchart.piezo.TriggerMonitoringPriority.Value] = None,
triggerMaxErrorTime: Integer = 300,
triggerState: Option[TriggerState] = None
)(
implicit
request: play.api.mvc.Request[AnyContent]
):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {

Seq[Any](format.raw/*18.2*/("""

"""),_display_(/*20.2*/com/*20.5*/.lucidchart.piezo.admin.views.html.triggersLayout(triggersByGroup, currentTrigger)/*20.87*/ {_display_(Seq[Any](format.raw/*20.89*/("""
    """),_display_(/*21.6*/if(!errorMessage.isEmpty)/*21.31*/ {_display_(Seq[Any](format.raw/*21.33*/("""
        """),format.raw/*22.9*/("""<h3 class="text-danger">"""),_display_(/*22.34*/errorMessage/*22.46*/.get),format.raw/*22.50*/("""</h3>
    """)))}),format.raw/*23.6*/("""
    """),_display_(/*24.6*/if(!currentTrigger.isEmpty)/*24.33*/ {_display_(Seq[Any](format.raw/*24.35*/("""
        """),format.raw/*25.9*/("""<h3 class="pull-left">"""),_display_(/*25.32*/currentTrigger/*25.46*/.get.getKey.getGroup()),format.raw/*25.68*/(""" """),format.raw/*25.69*/("""&raquo; <span class="text-info">"""),_display_(/*25.102*/currentTrigger/*25.116*/.get.getKey.getName()),format.raw/*25.137*/("""</span></h3>
        <a class="piezo-button pull-right" data-toggle="tooltip" title="Duplicate trigger" href=""""),_display_(/*26.99*/{piezoRoutes.Triggers.getNewTriggerForm(
            currentTrigger.get match {
                case c: CronTrigger => "cron"
                case s: SimpleTrigger => "simple"
            }
            )}),format.raw/*31.15*/("""?templateGroup="""),_display_(/*31.31*/currentTrigger/*31.45*/.get.getKey.getGroup()),format.raw/*31.67*/("""&templateName="""),_display_(/*31.82*/currentTrigger/*31.96*/.get.getKey.getName()),format.raw/*31.117*/("""">
            <span class="glyphicon glyphicon-duplicate"></span>
        </a>
        <a class="piezo-button pull-right" href=""""),_display_(/*34.51*/piezoRoutes/*34.62*/.Triggers.getEditTriggerAction(currentTrigger.get.getKey.getGroup(), currentTrigger.get.getKey.getName())),format.raw/*34.167*/("""" data-toggle="tooltip" title="Edit trigger">
            <span class="glyphicon glyphicon-pencil"></span>
        </a>
        <a id="deleteTrigger" class="piezo-button pull-right" data-toggle="tooltip" title="Delete trigger" onclick="$('#deleteConfirm').show()">
            <span class="glyphicon glyphicon-remove"></span>
        </a>

        """),_display_(/*41.10*/if(triggerState.contains(TriggerState.PAUSED))/*41.56*/ {_display_(Seq[Any](format.raw/*41.58*/("""
            """),format.raw/*42.13*/("""<a id="resumeTrigger" class="piezo-button pull-right" data-toggle="tooltip" title="Resume trigger">
                <span class="glyphicon glyphicon-play"></span>
            </a>
        """)))}/*45.11*/else/*45.16*/{_display_(Seq[Any](format.raw/*45.17*/("""
            """),format.raw/*46.13*/("""<a id="pauseTrigger" class="piezo-button pull-right" data-toggle="tooltip" title="Pause trigger">
                <span class="glyphicon glyphicon-pause"></span>
            </a>
        """)))}),format.raw/*49.10*/("""
        """),format.raw/*50.9*/("""<div class="clearfix"></div>
        <div id="errorGrowl" style="display:none;" class="alert alert-danger">
            <p id="errorMessage"></p>
        </div>

        <div id="deleteConfirm" style="display:none;" class="alert alert-warning fade in">
          <h4>This will permanently delete the trigger!</h4>
            <button type="button" class="btn btn-danger" onclick="deleteTrigger()">Delete</button>
            <button type="button" class="btn btn-default" onclick="$('#deleteConfirm').hide()">Cancel</button>
        </div>
        <script>

            function showErrorMessage(message) """),format.raw/*62.48*/("""{"""),format.raw/*62.49*/("""
                """),format.raw/*63.17*/("""$('#errorGrowl').show();
                $('#errorMessage').text(message);
                setTimeout(function() """),format.raw/*65.39*/("""{"""),format.raw/*65.40*/("""
                    """),format.raw/*66.21*/("""$('#errorGrowl').fadeOut(1000);
                """),format.raw/*67.17*/("""}"""),format.raw/*67.18*/(""", 5000);
            """),format.raw/*68.13*/("""}"""),format.raw/*68.14*/("""

            """),format.raw/*70.13*/("""function deleteTrigger() """),format.raw/*70.38*/("""{"""),format.raw/*70.39*/("""
                """),format.raw/*71.17*/("""var triggerUrl = """"),_display_(/*71.36*/piezoRoutes/*71.47*/.Triggers.deleteTrigger(currentTrigger.get.getKey.getGroup(), currentTrigger.get.getKey.getName())),format.raw/*71.145*/("""";
                var deleteRequest = new XMLHttpRequest();
                deleteRequest.onreadystatechange = function() """),format.raw/*73.63*/("""{"""),format.raw/*73.64*/("""
                    """),format.raw/*74.21*/("""if (deleteRequest.readyState === 4) """),format.raw/*74.57*/("""{"""),format.raw/*74.58*/("""
                        """),format.raw/*75.25*/("""if (deleteRequest.status === 200) """),format.raw/*75.59*/("""{"""),format.raw/*75.60*/("""
                            """),format.raw/*76.29*/("""window.location.assign(""""),_display_(/*76.54*/piezoRoutes/*76.65*/.Triggers.getIndex()),format.raw/*76.85*/("""");
                        """),format.raw/*77.25*/("""}"""),format.raw/*77.26*/(""" """),format.raw/*77.27*/("""else """),format.raw/*77.32*/("""{"""),format.raw/*77.33*/("""
                            """),format.raw/*78.29*/("""showErrorMessage('Failed to delete trigger.');
                        """),format.raw/*79.25*/("""}"""),format.raw/*79.26*/("""
                    """),format.raw/*80.21*/("""}"""),format.raw/*80.22*/("""
                """),format.raw/*81.17*/("""}"""),format.raw/*81.18*/(""";
                deleteRequest.open("DELETE", triggerUrl, true);
                deleteRequest.send(null);
            """),format.raw/*84.13*/("""}"""),format.raw/*84.14*/("""

            """),format.raw/*86.13*/("""function setTriggerState(pause) """),format.raw/*86.45*/("""{"""),format.raw/*86.46*/(""";
                $.ajax("""),format.raw/*87.24*/("""{"""),format.raw/*87.25*/("""
                    """),format.raw/*88.21*/("""url:""""),_display_(/*88.27*/piezoRoutes/*88.38*/.Triggers.patchTrigger(currentTrigger.get.getKey.getGroup(), currentTrigger.get.getKey.getName())),format.raw/*88.135*/("""",
                    type:'PATCH',
                    headers : """),format.raw/*90.31*/("""{"""),format.raw/*90.32*/("""
                        """),format.raw/*91.25*/("""'Accept' : 'application/json',
                        'Content-Type' : 'application/json'
                    """),format.raw/*93.21*/("""}"""),format.raw/*93.22*/(""",
                    data: JSON.stringify("""),format.raw/*94.42*/("""{"""),format.raw/*94.43*/(""""state": (pause? "PAUSED" : "NORMAL")"""),format.raw/*94.80*/("""}"""),format.raw/*94.81*/("""),
                    success: function(resp, status) """),format.raw/*95.53*/("""{"""),format.raw/*95.54*/("""
                        """),format.raw/*96.25*/("""window.location.reload();
                    """),format.raw/*97.21*/("""}"""),format.raw/*97.22*/(""",
                    error: function() """),format.raw/*98.39*/("""{"""),format.raw/*98.40*/("""
                        """),format.raw/*99.25*/("""showErrorMessage(pause? 'Failed to pause trigger.' : 'Failed to resume trigger.');
                    """),format.raw/*100.21*/("""}"""),format.raw/*100.22*/("""
                """),format.raw/*101.17*/("""}"""),format.raw/*101.18*/(""");
            """),format.raw/*102.13*/("""}"""),format.raw/*102.14*/("""
            """),format.raw/*103.13*/("""$('#pauseTrigger').click(setTriggerState.bind(this,true));
            $('#resumeTrigger').click(setTriggerState.bind(this,false));
        </script>
        <table class="table table-condensed table-bordered table-striped table-hover">
            <tbody>
            <tr>
                <td class="text-right">Class name:</td>
                <td>"""),_display_(/*110.22*/currentTrigger/*110.36*/.get.getClass()),format.raw/*110.51*/("""</td>
            </tr>
            <tr>
                <td class="text-right">Trigger group:</td>
                <td>"""),_display_(/*114.22*/currentTrigger/*114.36*/.get.getKey.getGroup()),format.raw/*114.58*/("""</td>
            </tr>
            <tr>
                <td class="text-right">Trigger name:</td>
                <td>"""),_display_(/*118.22*/currentTrigger/*118.36*/.get.getKey.getName()),format.raw/*118.57*/("""</td>
            </tr>
            """),_display_(/*120.14*/if(currentTrigger.get.getJobKey() != null)/*120.56*/ {_display_(Seq[Any](format.raw/*120.58*/("""
            """),format.raw/*121.13*/("""<tr>
                <td class="text-right">Job group:</td>
                <td>"""),_display_(/*123.22*/currentTrigger/*123.36*/.get.getJobKey.getGroup()),format.raw/*123.61*/("""</td>
            </tr>
            <tr>
                <td class="text-right">Job name:</td>
                <td><a href=""""),_display_(/*127.31*/piezoRoutes/*127.42*/.Jobs.getJob(currentTrigger.get.getJobKey.getGroup(), currentTrigger.get.getJobKey.getName())),format.raw/*127.135*/("""">"""),_display_(/*127.138*/currentTrigger/*127.152*/.get.getJobKey.getName()),format.raw/*127.176*/("""</a></td>
            </tr>
            """)))}),format.raw/*129.14*/("""
            """),_display_(/*130.14*/currentTrigger/*130.28*/.get/*130.32*/ match/*130.38*/ {/*131.17*/case c: CronTrigger =>/*131.39*/ {_display_(Seq[Any](format.raw/*131.41*/("""
                    """),format.raw/*132.21*/("""<tr>
                        <td class="text-right">Cron expression:</td>
                        <td>"""),_display_(/*134.30*/c/*134.31*/.getCronExpression()),format.raw/*134.51*/("""</td>
                    </tr>
                    <tr>
                        <td class="text-right">Expression summary:</td>
                        <td>"""),_display_(/*138.30*/c/*138.31*/.getExpressionSummary()),format.raw/*138.54*/("""</td>
                    </tr>
                    <tr>
                        <td class="text-right">Time zone:</td>
                        <td>"""),_display_(/*142.30*/c/*142.31*/.getTimeZone().getDisplayName()),format.raw/*142.62*/("""</td>
                    </tr>
                """)))}/*145.17*/case s: SimpleTrigger =>/*145.41*/ {_display_(Seq[Any](format.raw/*145.43*/("""
                    """),format.raw/*146.21*/("""<tr>
                        <td class="text-right">Repeat count:</td>
                        <td>"""),_display_(/*148.30*/s/*148.31*/.getRepeatCount()),format.raw/*148.48*/("""</td>
                    </tr>
                    <tr>
                        <td class="text-right">Repeat interval (seconds):</td>
                        <td>"""),_display_(/*152.30*/{s.getRepeatInterval() / 1000}),format.raw/*152.60*/("""</td>
                    </tr>
                    <tr>
                        <td class="text-right">Times triggered:</td>
                        <td>"""),_display_(/*156.30*/s/*156.31*/.getTimesTriggered()),format.raw/*156.51*/("""</td>
                    </tr>
                """)))}/*159.17*/case _ =>/*159.26*/ {_display_(Seq[Any](format.raw/*159.28*/(""" """)))}}),format.raw/*160.14*/("""
            """),_display_(/*161.14*/triggerMonitoringPriority/*161.39*/.map/*161.43*/ { triggerMonitoringPriority =>_display_(Seq[Any](format.raw/*161.74*/("""
            """),format.raw/*162.13*/("""<tr>
                <td class="text-right">Monitoring priority:</td>
                <td>"""),_display_(/*164.22*/triggerMonitoringPriority),format.raw/*164.47*/("""</td>
            </tr>
            <tr>
                <td class="text-right">Monitoring - max seconds between successes:</td>
                <td>"""),_display_(/*168.22*/triggerMaxErrorTime),format.raw/*168.41*/(""" """),format.raw/*168.42*/("""seconds</td>
            </tr>
            """)))}),format.raw/*170.14*/("""
            """),format.raw/*171.13*/("""<tr>
                <td class="text-right">Description:</td>
                <td>
                    """),_display_(/*174.22*/if(currentTrigger.get.getDescription() != null)/*174.69*/ {_display_(_display_(/*174.72*/currentTrigger/*174.86*/.get.getDescription()))}/*174.109*/else/*174.114*/{}),format.raw/*174.116*/("""
                """),format.raw/*175.17*/("""</td>
            </tr>
            <tr>
                <td class="text-right small">State:</td>
                <td class="small">"""),_display_(/*179.36*/triggerState/*179.48*/.map(_.toString)),format.raw/*179.64*/("""</td>
            </tr>
            <tr>
                <td class="text-right small">End time:</td>
                <td class="small">"""),_display_(/*183.36*/currentTrigger/*183.50*/.get.getEndTime()),format.raw/*183.67*/("""</td>
            </tr>
            <tr>
                <td class="text-right small">Final fire time:</td>
                <td class="small">"""),_display_(/*187.36*/currentTrigger/*187.50*/.get.getFinalFireTime()),format.raw/*187.73*/("""</td>
            </tr>
            <tr>
                <td class="text-right small">Misfire instruction:</td>
                <td class="small">"""),_display_(/*191.36*/currentTrigger/*191.50*/.get.getMisfireInstruction()),format.raw/*191.78*/("""</td>
            </tr>
            <tr>
                <td class="text-right small">Next fire time:</td>
                <td class="small">"""),_display_(/*195.36*/currentTrigger/*195.50*/.get.getNextFireTime()),format.raw/*195.72*/("""</td>
            </tr>
            <tr>
                <td class="text-right small">Previous fire time:</td>
                <td class="small">"""),_display_(/*199.36*/currentTrigger/*199.50*/.get.getPreviousFireTime()),format.raw/*199.76*/("""</td>
            </tr>
            <tr>
                <td class="text-right small">Start time:</td>
                <td class="small">"""),_display_(/*203.36*/currentTrigger/*203.50*/.get.getStartTime()),format.raw/*203.69*/("""</td>
            </tr>
            </tbody>
        </table>

        <hr />

        <h4>Trigger Data Map</h4>
        <table class="table table-bordered table-striped table-hover table-condensed">
            <thead>
                <tr>
                    <th class="text-right">Key</th>
                    <th>Value</th>
                </tr>
            </thead>
            <tbody>
            """),_display_(/*219.14*/currentTrigger/*219.28*/.get.getJobDataMap.getKeys.map/*219.58*/ { triggerDataKey =>_display_(Seq[Any](format.raw/*219.78*/("""
                """),format.raw/*220.17*/("""<tr>
                    <td class="text-right">"""),_display_(/*221.45*/triggerDataKey),format.raw/*221.59*/("""</td>
                    <td>"""),_display_(/*222.26*/currentTrigger/*222.40*/.get.getJobDataMap.getString(triggerDataKey)),format.raw/*222.84*/("""</td>
                </tr>
            """)))}),format.raw/*224.14*/("""
            """),format.raw/*225.13*/("""</tbody>
        </table>

        <hr />

        """),_display_(/*230.10*/if(triggerHistory.isDefined)/*230.38*/ {_display_(Seq[Any](format.raw/*230.40*/("""
            """),format.raw/*231.13*/("""<h4>Trigger History</h4>
            <table class="table table-bordered table-striped table-hover table-condensed">
                <thead>
                <tr>
                    <th>Scheduled Start</th>
                    <th>Actual Start</th>
                    <th>Finish</th>
                    <th>Misfire</th>
                </tr>
                </thead>
                <tbody>
                """),_display_(/*242.18*/triggerHistory/*242.32*/.get.map/*242.40*/ { record =>_display_(Seq[Any](format.raw/*242.52*/("""
                    """),format.raw/*243.21*/("""<tr>
                        <td>"""),_display_(/*244.30*/record/*244.36*/.scheduled_start),format.raw/*244.52*/("""</td>
                        <td>"""),_display_(/*245.30*/record/*245.36*/.actual_start),format.raw/*245.49*/("""</td>
                        <td>"""),_display_(/*246.30*/record/*246.36*/.finish),format.raw/*246.43*/("""</td>
                        <td>"""),_display_(/*247.30*/record/*247.36*/.misfire),format.raw/*247.44*/("""</td>
                    </tr>
                """)))}),format.raw/*249.18*/("""
                """),format.raw/*250.17*/("""</tbody>
            </table>
        """)))}),format.raw/*252.10*/("""
    """)))}),format.raw/*253.6*/("""
""")))}))}
  }

  def render(triggersByGroup:scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.TriggerKey]]],currentTrigger:Option[org.quartz.Trigger],triggerHistory:Option[List[com.lucidchart.piezo.TriggerRecord]],errorMessage:Option[String],triggerMonitoringPriority:Option[com.lucidchart.piezo.TriggerMonitoringPriority.Value],triggerMaxErrorTime:Integer,triggerState:Option[TriggerState],request:play.api.mvc.Request[AnyContent]): play.twirl.api.HtmlFormat.Appendable = apply(triggersByGroup,currentTrigger,triggerHistory,errorMessage,triggerMonitoringPriority,triggerMaxErrorTime,triggerState)(request)

  def f:((scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.TriggerKey]]],Option[org.quartz.Trigger],Option[List[com.lucidchart.piezo.TriggerRecord]],Option[String],Option[com.lucidchart.piezo.TriggerMonitoringPriority.Value],Integer,Option[TriggerState]) => (play.api.mvc.Request[AnyContent]) => play.twirl.api.HtmlFormat.Appendable) = (triggersByGroup,currentTrigger,triggerHistory,errorMessage,triggerMonitoringPriority,triggerMaxErrorTime,triggerState) => (request) => apply(triggersByGroup,currentTrigger,triggerHistory,errorMessage,triggerMonitoringPriority,triggerMaxErrorTime,triggerState)(request)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed May 31 21:03:53 MDT 2017
                  SOURCE: /home/paul/lucid/piezo/admin/app/com/lucidchart/piezo/admin/views/trigger.scala.html
                  HASH: 5a7f61484cf56f9f5c5280a70a7178dc22a0bad7
                  MATRIX: 1098->255|1680->749|1709->752|1720->755|1811->837|1851->839|1883->845|1917->870|1957->872|1993->881|2045->906|2066->918|2091->922|2132->933|2164->939|2200->966|2240->968|2276->977|2326->1000|2349->1014|2392->1036|2421->1037|2482->1070|2506->1084|2549->1105|2687->1216|2912->1420|2955->1436|2978->1450|3021->1472|3063->1487|3086->1501|3129->1522|3286->1652|3306->1663|3433->1768|3809->2117|3864->2163|3904->2165|3945->2178|4153->2368|4166->2373|4205->2374|4246->2387|4465->2575|4501->2584|5133->3188|5162->3189|5207->3206|5348->3319|5377->3320|5426->3341|5502->3389|5531->3390|5580->3411|5609->3412|5651->3426|5704->3451|5733->3452|5778->3469|5824->3488|5844->3499|5964->3597|6115->3720|6144->3721|6193->3742|6257->3778|6286->3779|6339->3804|6401->3838|6430->3839|6487->3868|6539->3893|6559->3904|6600->3924|6656->3952|6685->3953|6714->3954|6747->3959|6776->3960|6833->3989|6932->4060|6961->4061|7010->4082|7039->4083|7084->4100|7113->4101|7261->4221|7290->4222|7332->4236|7392->4268|7421->4269|7474->4294|7503->4295|7552->4316|7585->4322|7605->4333|7724->4430|7819->4497|7848->4498|7901->4523|8040->4634|8069->4635|8140->4678|8169->4679|8234->4716|8263->4717|8346->4772|8375->4773|8428->4798|8502->4844|8531->4845|8599->4885|8628->4886|8681->4911|8813->5014|8843->5015|8889->5032|8919->5033|8963->5048|8993->5049|9035->5062|9414->5413|9438->5427|9475->5442|9624->5563|9648->5577|9692->5599|9840->5719|9864->5733|9907->5754|9972->5791|10024->5833|10065->5835|10107->5848|10216->5929|10240->5943|10287->5968|10440->6093|10461->6104|10577->6197|10609->6200|10634->6214|10681->6238|10754->6279|10796->6293|10820->6307|10834->6311|10850->6317|10862->6336|10894->6358|10935->6360|10985->6381|11116->6484|11127->6485|11169->6505|11355->6663|11366->6664|11411->6687|11588->6836|11599->6837|11652->6868|11721->6934|11755->6958|11796->6960|11846->6981|11974->7081|11985->7082|12024->7099|12217->7264|12269->7294|12452->7449|12463->7450|12505->7470|12574->7536|12593->7545|12634->7547|12669->7563|12711->7577|12746->7602|12760->7606|12830->7637|12872->7650|12991->7741|13038->7766|13216->7916|13257->7935|13287->7936|13363->7980|13405->7993|13537->8097|13594->8144|13626->8147|13650->8161|13685->8184|13700->8189|13725->8191|13771->8208|13932->8341|13954->8353|13992->8369|14156->8505|14180->8519|14219->8536|14390->8679|14414->8693|14459->8716|14634->8863|14658->8877|14708->8905|14878->9047|14902->9061|14946->9083|15120->9229|15144->9243|15192->9269|15358->9407|15382->9421|15423->9440|15855->9844|15879->9858|15919->9888|15978->9908|16024->9925|16101->9974|16137->9988|16196->10019|16220->10033|16286->10077|16359->10118|16401->10131|16481->10183|16519->10211|16560->10213|16602->10226|17039->10635|17063->10649|17081->10657|17132->10669|17182->10690|17244->10724|17260->10730|17298->10746|17361->10781|17377->10787|17412->10800|17475->10835|17491->10841|17520->10848|17583->10883|17599->10889|17629->10897|17710->10946|17756->10963|17827->11002|17864->11008
                  LINES: 24->7|38->18|40->20|40->20|40->20|40->20|41->21|41->21|41->21|42->22|42->22|42->22|42->22|43->23|44->24|44->24|44->24|45->25|45->25|45->25|45->25|45->25|45->25|45->25|45->25|46->26|51->31|51->31|51->31|51->31|51->31|51->31|51->31|54->34|54->34|54->34|61->41|61->41|61->41|62->42|65->45|65->45|65->45|66->46|69->49|70->50|82->62|82->62|83->63|85->65|85->65|86->66|87->67|87->67|88->68|88->68|90->70|90->70|90->70|91->71|91->71|91->71|91->71|93->73|93->73|94->74|94->74|94->74|95->75|95->75|95->75|96->76|96->76|96->76|96->76|97->77|97->77|97->77|97->77|97->77|98->78|99->79|99->79|100->80|100->80|101->81|101->81|104->84|104->84|106->86|106->86|106->86|107->87|107->87|108->88|108->88|108->88|108->88|110->90|110->90|111->91|113->93|113->93|114->94|114->94|114->94|114->94|115->95|115->95|116->96|117->97|117->97|118->98|118->98|119->99|120->100|120->100|121->101|121->101|122->102|122->102|123->103|130->110|130->110|130->110|134->114|134->114|134->114|138->118|138->118|138->118|140->120|140->120|140->120|141->121|143->123|143->123|143->123|147->127|147->127|147->127|147->127|147->127|147->127|149->129|150->130|150->130|150->130|150->130|150->131|150->131|150->131|151->132|153->134|153->134|153->134|157->138|157->138|157->138|161->142|161->142|161->142|163->145|163->145|163->145|164->146|166->148|166->148|166->148|170->152|170->152|174->156|174->156|174->156|176->159|176->159|176->159|176->160|177->161|177->161|177->161|177->161|178->162|180->164|180->164|184->168|184->168|184->168|186->170|187->171|190->174|190->174|190->174|190->174|190->174|190->174|190->174|191->175|195->179|195->179|195->179|199->183|199->183|199->183|203->187|203->187|203->187|207->191|207->191|207->191|211->195|211->195|211->195|215->199|215->199|215->199|219->203|219->203|219->203|235->219|235->219|235->219|235->219|236->220|237->221|237->221|238->222|238->222|238->222|240->224|241->225|246->230|246->230|246->230|247->231|258->242|258->242|258->242|258->242|259->243|260->244|260->244|260->244|261->245|261->245|261->245|262->246|262->246|262->246|263->247|263->247|263->247|265->249|266->250|268->252|269->253
                  -- GENERATED --
              */
          