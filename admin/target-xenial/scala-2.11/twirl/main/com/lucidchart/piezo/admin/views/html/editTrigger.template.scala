
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
object editTrigger extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template8[scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.TriggerKey]]],Form[scala.Tuple3[org.quartz.Trigger, com.lucidchart.piezo.TriggerMonitoringPriority.Value, Int]],play.api.mvc.Call,Boolean,Boolean,Option[String],List[String],play.api.mvc.Request[AnyContent],play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(
triggersByGroup: scala.collection.mutable.Buffer[(String, scala.collection.immutable.List[org.quartz.TriggerKey])],
triggerForm: Form[(org.quartz.Trigger, com.lucidchart.piezo.TriggerMonitoringPriority.Value, Int)],
formAction: play.api.mvc.Call,
existing: Boolean,
isTemplate: Boolean,
errorMessage: Option[String] = None,
scripts: List[String] = List[String]("js/jobData.js", "js/typeAhead.js")
)(
implicit
request: play.api.mvc.Request[AnyContent]
):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import com.lucidchart.piezo.TriggerMonitoringPriority
import com.lucidchart.piezo.admin.controllers.{routes=>piezoRoutes}
import com.lucidchart.piezo.admin.views
import java.net.URLEncoder
import org.quartz._
import com.lucidchart.piezo.admin.views.FormHelpers._

Seq[Any](format.raw/*12.2*/("""

"""),format.raw/*20.1*/("""
"""),_display_(/*21.2*/com/*21.5*/.lucidchart.piezo.admin.views.html.triggersLayout(triggersByGroup, None, scripts)/*21.86*/ {_display_(Seq[Any](format.raw/*21.88*/("""
  """),_display_(/*22.4*/if(!errorMessage.isEmpty)/*22.29*/ {_display_(Seq[Any](format.raw/*22.31*/("""
    """),format.raw/*23.5*/("""<h3 class="text-danger">"""),_display_(/*23.30*/errorMessage/*23.42*/.get),format.raw/*23.46*/("""</h3>
  """)))}),format.raw/*24.4*/("""

  """),_display_(/*26.4*/if(existing)/*26.16*/ {_display_(Seq[Any](format.raw/*26.18*/("""
    """),format.raw/*27.5*/("""<h3>Edit Trigger</h3>
  """)))}/*28.5*/else/*28.10*/{_display_(Seq[Any](format.raw/*28.11*/("""
    """),format.raw/*29.5*/("""<h3>New Trigger</h3>
  """)))}),format.raw/*30.4*/("""

  """),format.raw/*32.3*/("""<div class="form-horizontal">
      <div class="form-group">
          <div class="col-sm-2">
              <select class="form-control col-sm-2" onchange="window.location = this.options[this.selectedIndex].value;" """),_display_(/*35.123*/if(isTemplate)/*35.137*/{_display_(Seq[Any](format.raw/*35.138*/("""disabled""")))}),format.raw/*35.147*/(""" """),format.raw/*35.148*/(""">
                  <option value=""""),_display_(/*36.35*/piezoRoutes/*36.46*/.Triggers.getNewTriggerForm("cron")),format.raw/*36.81*/("""?"""),_display_(/*36.83*/request/*36.90*/.rawQueryString),format.raw/*36.105*/("""" """),_display_(/*36.108*/if(triggerForm.data.get("triggerType")==Some("cron"))/*36.161*/ {_display_(Seq[Any](format.raw/*36.163*/("""selected""")))}/*36.173*/else/*36.178*/{}),format.raw/*36.180*/(""">Cron</option>
                  <option value=""""),_display_(/*37.35*/piezoRoutes/*37.46*/.Triggers.getNewTriggerForm("simple")),format.raw/*37.83*/("""?"""),_display_(/*37.85*/request/*37.92*/.rawQueryString),format.raw/*37.107*/("""" """),_display_(/*37.110*/if(triggerForm.data.get("triggerType")==Some("simple"))/*37.165*/ {_display_(Seq[Any](format.raw/*37.167*/("""selected""")))}/*37.177*/else/*37.182*/{}),format.raw/*37.184*/(""">Simple</option>
              </select>
          </div>
          <div class="col-sm-2">
              """),_display_(/*41.16*/if(triggerForm.data.get("triggerType").getOrElse("") == "simple")/*41.81*/ {_display_(Seq[Any](format.raw/*41.83*/("""
              """),format.raw/*42.15*/("""<a class="piezo-button" href="http://quartz-scheduler.org/documentation/quartz-2.x/tutorials/tutorial-lesson-05" target="_blank"><span class="glyphicon glyphicon-question-sign"/></a>
              """)))}/*43.17*/else/*43.22*/{_display_(Seq[Any](format.raw/*43.23*/("""
              """),format.raw/*44.15*/("""<a class="piezo-button" href="http://quartz-scheduler.org/documentation/quartz-2.x/tutorials/tutorial-lesson-06" target="_blank"><span class="glyphicon glyphicon-question-sign"/></a>
              """)))}),format.raw/*45.16*/("""
          """),format.raw/*46.11*/("""</div>
      </div>
  </div>
  <br/>

  <h4 class="text-danger">"""),_display_(/*51.28*/triggerForm/*51.39*/.errors.filter(_.key == "").map(_.message).mkString(", ")),format.raw/*51.96*/("""</h4>
  <form role="form" action=""""),_display_(/*52.30*/formAction),format.raw/*52.40*/("""" method="POST">
    <div class="form-horizontal">
    """),_display_(/*54.6*/defining(if(existing) {'readonly} else {'none})/*54.53*/ { newEditOnly =>_display_(Seq[Any](format.raw/*54.70*/("""
    """),_display_(/*55.6*/helper/*55.12*/.input(triggerForm("group"), '_label -> "Group", 'labelClass -> "col-sm-2 text-right", 'inputDivClass -> "col-sm-4", 'placeholder -> "Group", 'value-> triggerForm.data.get("group").getOrElse(""), newEditOnly -> None)/*55.228*/ { (id, name, value, args) =>_display_(Seq[Any](format.raw/*55.257*/("""
      """),format.raw/*56.7*/("""<input type="text" class="trigger-group-type-ahead form-control form-inline-control" name=""""),_display_(/*56.99*/name),format.raw/*56.103*/("""" id=""""),_display_(/*56.110*/id),format.raw/*56.112*/("""" """),_display_(/*56.115*/toHtmlArgs(args)),format.raw/*56.131*/(""">
    """)))}),format.raw/*57.6*/("""
    """),_display_(/*58.6*/helper/*58.12*/.input(triggerForm("name"), '_label -> "Name", 'labelClass -> "col-sm-2 text-right", 'inputDivClass -> "col-sm-4", 'placeholder -> "Name", 'value-> triggerForm.data.get("name").getOrElse(""), newEditOnly -> None)/*58.224*/ { (id, name, value, args) =>_display_(Seq[Any](format.raw/*58.253*/("""
      """),format.raw/*59.7*/("""<input type="text" class="form-control form-inline-control " name=""""),_display_(/*59.75*/name),format.raw/*59.79*/("""" id=""""),_display_(/*59.86*/id),format.raw/*59.88*/("""" """),_display_(/*59.91*/toHtmlArgs(args)),format.raw/*59.107*/(""">
    """)))}),format.raw/*60.6*/("""
    """),_display_(/*61.6*/helper/*61.12*/.input(triggerForm("jobGroup"), '_label -> "Job group", 'labelClass -> "col-sm-2 text-right", 'inputDivClass -> "col-sm-4", 'placeholder -> "Job group", 'value-> triggerForm.data.get("jobGroup").getOrElse(""), newEditOnly -> None)/*61.242*/ { (id, name, value, args) =>_display_(Seq[Any](format.raw/*61.271*/("""
      """),format.raw/*62.7*/("""<input type="text" class="job-group-type-ahead form-control form-inline-control " name=""""),_display_(/*62.96*/name),format.raw/*62.100*/("""" id=""""),_display_(/*62.107*/id),format.raw/*62.109*/("""" """),_display_(/*62.112*/toHtmlArgs(args)),format.raw/*62.128*/(""">
    """)))}),format.raw/*63.6*/("""
    """),_display_(/*64.6*/helper/*64.12*/.input(triggerForm("jobName"), '_label -> "Job name", 'labelClass -> "col-sm-2 text-right", 'inputDivClass -> "col-sm-4", 'placeholder -> "Job name", 'value-> triggerForm.data.get("jobName").getOrElse(""), newEditOnly -> None)/*64.238*/ { (id, name, value, args) =>_display_(Seq[Any](format.raw/*64.267*/("""
      """),format.raw/*65.7*/("""<input type="text" class="job-name-type-ahead form-control form-inline-control " name=""""),_display_(/*65.95*/name),format.raw/*65.99*/("""" id=""""),_display_(/*65.106*/id),format.raw/*65.108*/("""" """),_display_(/*65.111*/toHtmlArgs(args)),format.raw/*65.127*/(""">
    """)))}),format.raw/*66.6*/("""
    """)))}),format.raw/*67.6*/("""
    """),_display_(/*68.6*/helper/*68.12*/.select(triggerForm("triggerMonitoringPriority"), TriggerMonitoringPriority.values.toList.map(tp => tp.toString -> tp.toString), '_label -> "Monitoring Priority", 'labelClass -> "col-sm-2 text-right", 'inputDivClass -> "col-sm-4", 'class -> "form-control", 'value -> triggerForm.data.get("triggerMonitoringPriority").getOrElse(TriggerMonitoringPriority.Off), 'placeholder -> TriggerMonitoringPriority.Off)),format.raw/*68.417*/("""
    """),_display_(/*69.6*/helper/*69.12*/.input(triggerForm("triggerMaxErrorTime"), '_label -> "Monitoring - Max Seconds Between Successes", 'labelClass -> "col-sm-2 text-right", 'inputDivClass -> "col-sm-4", 'placeholder -> "", 'value -> triggerForm.data.get("triggerMaxErrorTime").getOrElse(300))/*69.269*/ { (id, name, value, args) =>_display_(Seq[Any](format.raw/*69.298*/("""
      """),format.raw/*70.7*/("""<input type="number" class="form-control form-inline-control " name=""""),_display_(/*70.77*/name),format.raw/*70.81*/("""" id=""""),_display_(/*70.88*/id),format.raw/*70.90*/("""" """),_display_(/*70.93*/toHtmlArgs(args)),format.raw/*70.109*/(""">
    """)))}),format.raw/*71.6*/("""

    """),_display_(/*73.6*/helper/*73.12*/.input(triggerForm("description"), '_label -> "Description", 'labelClass -> "col-sm-2 text-right", 'inputDivClass -> "col-sm-10", 'placeholder -> "Description", 'value-> triggerForm.data.get("description").getOrElse(""))/*73.232*/ { (id, name, value, args) =>_display_(Seq[Any](format.raw/*73.261*/("""
      """),format.raw/*74.7*/("""<input type="text" class="form-control form-inline-control " name=""""),_display_(/*74.75*/name),format.raw/*74.79*/("""" id=""""),_display_(/*74.86*/id),format.raw/*74.88*/("""" """),_display_(/*74.91*/toHtmlArgs(args)),format.raw/*74.107*/(""">
    """)))}),format.raw/*75.6*/("""
    """),format.raw/*76.5*/("""<input type="hidden" name="triggerType" id="triggerType" value=""""),_display_(/*76.70*/triggerForm/*76.81*/.data.get("triggerType")),format.raw/*76.105*/(""""/>
    </div>
    <div class="clearfix"></div>
    <div class="form-horizontal">
    """),_display_(/*80.6*/if(triggerForm.data.get("triggerType").getOrElse("") == "simple")/*80.71*/ {_display_(Seq[Any](format.raw/*80.73*/("""
      """),_display_(/*81.8*/helper/*81.14*/.input(triggerForm("simple.repeatCount"), '_label -> "Repeat count", 'labelClass -> "col-sm-2 text-right", '_class -> "form-horizontal-inline", 'inputDivClass -> "col-sm-2", 'placeholder -> "Repeat count", 'value-> triggerForm.data.get("simple.repeatCount").getOrElse(""))/*81.286*/ { (id, name, value, args) =>_display_(Seq[Any](format.raw/*81.315*/("""
        """),format.raw/*82.9*/("""<input type="text" class="form-control form-inline-control " name=""""),_display_(/*82.77*/name),format.raw/*82.81*/("""" id=""""),_display_(/*82.88*/id),format.raw/*82.90*/("""" """),_display_(/*82.93*/toHtmlArgs(args)),format.raw/*82.109*/(""">
      """)))}),format.raw/*83.8*/("""
      """),_display_(/*84.8*/helper/*84.14*/.input(triggerForm("simple.repeatInterval"), '_label -> "Repeat interval (seconds)", 'labelClass -> "col-sm-2 text-right", '_class -> "form-horizontal-inline", 'inputDivClass -> "col-sm-2", 'placeholder -> "Repeat interval (seconds)", 'value-> triggerForm.data.get("simple.repeatInterval").getOrElse(""))/*84.318*/ { (id, name, value, args) =>_display_(Seq[Any](format.raw/*84.347*/("""
        """),format.raw/*85.9*/("""<input type="text" class="form-control form-inline-control" name=""""),_display_(/*85.76*/name),format.raw/*85.80*/("""" id=""""),_display_(/*85.87*/id),format.raw/*85.89*/("""" """),_display_(/*85.92*/toHtmlArgs(args)),format.raw/*85.108*/(""">
      """)))}),format.raw/*86.8*/("""
    """)))}/*87.7*/else/*87.12*/{_display_(Seq[Any](format.raw/*87.13*/("""
      """),_display_(/*88.8*/helper/*88.14*/.input(triggerForm("cron.cronExpression"), '_label -> "Cron Expression", 'labelClass -> "col-sm-2 text-right", '_class -> "form-horizontal-inline", 'inputDivClass -> "col-sm-4", 'placeholder -> "Cron expression", 'value-> triggerForm.data.get("cron.cronExpression").getOrElse(""))/*88.294*/ { (id, name, value, args) =>_display_(Seq[Any](format.raw/*88.323*/("""
          """),format.raw/*89.11*/("""<input type="text" class="form-control form-inline-control" name=""""),_display_(/*89.78*/name),format.raw/*89.82*/("""" id=""""),_display_(/*89.89*/id),format.raw/*89.91*/("""" """),_display_(/*89.94*/toHtmlArgs(args)),format.raw/*89.110*/(""">
      """)))}),format.raw/*90.8*/("""
    """)))}),format.raw/*91.6*/("""

    """),format.raw/*93.5*/("""<h4>Job Data Map</h4>

    <div class="job-data-map">
      """),_display_(/*96.8*/helper/*96.14*/.repeat(triggerForm("job-data-map"), min = triggerForm("job-data-map").indexes.length + 1)/*96.104*/ { dataMap =>_display_(Seq[Any](format.raw/*96.117*/("""

        """),_display_(/*98.10*/dataMap("key")/*98.24*/.value.map/*98.34*/ { _ =>_display_(Seq[Any](format.raw/*98.41*/("""
          """),_display_(/*99.12*/dataMap("value")/*99.28*/.value.map/*99.38*/ { _ =>_display_(Seq[Any](format.raw/*99.45*/("""
            """),format.raw/*100.13*/("""<div class="job-data-delete text-right"><a href="#">delete</a></div>
          """)))}),format.raw/*101.12*/("""
        """)))}),format.raw/*102.10*/("""

        """),_display_(/*104.10*/helper/*104.16*/.inputText(dataMap("key"), '_label -> "Key", 'labelClass -> "col-sm-2 text-right", 'inputDivClass -> "col-sm-4", 'placeholder -> "Key", 'class -> "job-data-key form-control form-inline-control")),format.raw/*104.210*/("""
        """),_display_(/*105.10*/helper/*105.16*/.inputText(dataMap("value"), '_label -> "Value", 'labelClass -> "col-sm-2 text-right", 'inputDivClass -> "col-sm-4", 'placeholder -> "Value", 'class -> "job-data-value form-control form-inline-control")),format.raw/*105.218*/("""

      """)))}),format.raw/*107.8*/("""

      """),format.raw/*109.7*/("""<div class="job-data-add text-right"><a href="#">add</a></div>
    </div>

    </div>
    <div class="clearfix"></div>
    <button type="submit" class="btn btn-default submit-btn">"""),_display_(/*114.63*/if(existing)/*114.75*/ {_display_(Seq[Any](format.raw/*114.77*/("""Save""")))}/*114.83*/else/*114.88*/{_display_(Seq[Any](format.raw/*114.89*/("""Create""")))}),format.raw/*114.96*/("""</button>
    <button type="button" class="btn btn-default submit-btn" onclick="history.back();" value="Cancel">Cancel</button>
  </form>
""")))}))}
  }

  def render(triggersByGroup:scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.TriggerKey]]],triggerForm:Form[scala.Tuple3[org.quartz.Trigger, com.lucidchart.piezo.TriggerMonitoringPriority.Value, Int]],formAction:play.api.mvc.Call,existing:Boolean,isTemplate:Boolean,errorMessage:Option[String],scripts:List[String],request:play.api.mvc.Request[AnyContent]): play.twirl.api.HtmlFormat.Appendable = apply(triggersByGroup,triggerForm,formAction,existing,isTemplate,errorMessage,scripts)(request)

  def f:((scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.TriggerKey]]],Form[scala.Tuple3[org.quartz.Trigger, com.lucidchart.piezo.TriggerMonitoringPriority.Value, Int]],play.api.mvc.Call,Boolean,Boolean,Option[String],List[String]) => (play.api.mvc.Request[AnyContent]) => play.twirl.api.HtmlFormat.Appendable) = (triggersByGroup,triggerForm,formAction,existing,isTemplate,errorMessage,scripts) => (request) => apply(triggersByGroup,triggerForm,formAction,existing,isTemplate,errorMessage,scripts)(request)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed May 31 21:03:53 MDT 2017
                  SOURCE: /home/paul/lucid/piezo/admin/app/com/lucidchart/piezo/admin/views/editTrigger.scala.html
                  HASH: 9feafbb69153c14b565c229b675e181c03310b25
                  MATRIX: 834->1|1638->455|1667->726|1695->728|1706->731|1796->812|1836->814|1866->818|1900->843|1940->845|1972->850|2024->875|2045->887|2070->891|2109->900|2140->905|2161->917|2201->919|2233->924|2276->950|2289->955|2328->956|2360->961|2414->985|2445->989|2689->1205|2713->1219|2753->1220|2794->1229|2824->1230|2887->1266|2907->1277|2963->1312|2992->1314|3008->1321|3045->1336|3076->1339|3139->1392|3180->1394|3209->1404|3223->1409|3247->1411|3323->1460|3343->1471|3401->1508|3430->1510|3446->1517|3483->1532|3514->1535|3579->1590|3620->1592|3649->1602|3663->1607|3687->1609|3820->1715|3894->1780|3934->1782|3977->1797|4194->1996|4207->2001|4246->2002|4289->2017|4518->2215|4557->2226|4649->2291|4669->2302|4747->2359|4809->2394|4840->2404|4922->2460|4978->2507|5033->2524|5065->2530|5080->2536|5306->2752|5374->2781|5408->2788|5527->2880|5553->2884|5588->2891|5612->2893|5643->2896|5681->2912|5718->2919|5750->2925|5765->2931|5987->3143|6055->3172|6089->3179|6184->3247|6209->3251|6243->3258|6266->3260|6296->3263|6334->3279|6371->3286|6403->3292|6418->3298|6658->3528|6726->3557|6760->3564|6876->3653|6902->3657|6937->3664|6961->3666|6992->3669|7030->3685|7067->3692|7099->3698|7114->3704|7350->3930|7418->3959|7452->3966|7567->4054|7592->4058|7627->4065|7651->4067|7682->4070|7720->4086|7757->4093|7793->4099|7825->4105|7840->4111|8267->4516|8299->4522|8314->4528|8581->4785|8649->4814|8683->4821|8780->4891|8805->4895|8839->4902|8862->4904|8892->4907|8930->4923|8967->4930|9000->4937|9015->4943|9245->5163|9313->5192|9347->5199|9442->5267|9467->5271|9501->5278|9524->5280|9554->5283|9592->5299|9629->5306|9661->5311|9753->5376|9773->5387|9819->5411|9932->5498|10006->5563|10046->5565|10080->5573|10095->5579|10377->5851|10445->5880|10481->5889|10576->5957|10601->5961|10635->5968|10658->5970|10688->5973|10726->5989|10765->5998|10799->6006|10814->6012|11128->6316|11196->6345|11232->6354|11326->6421|11351->6425|11385->6432|11408->6434|11438->6437|11476->6453|11515->6462|11539->6469|11552->6474|11591->6475|11625->6483|11640->6489|11930->6769|11998->6798|12037->6809|12131->6876|12156->6880|12190->6887|12213->6889|12243->6892|12281->6908|12320->6917|12356->6923|12389->6929|12476->6990|12491->6996|12591->7086|12643->7099|12681->7110|12704->7124|12723->7134|12768->7141|12807->7153|12832->7169|12851->7179|12896->7186|12938->7199|13050->7279|13092->7289|13131->7300|13147->7306|13364->7500|13402->7510|13418->7516|13643->7718|13683->7727|13719->7735|13928->7916|13950->7928|13991->7930|14016->7936|14030->7941|14070->7942|14109->7949
                  LINES: 19->1|38->12|40->20|41->21|41->21|41->21|41->21|42->22|42->22|42->22|43->23|43->23|43->23|43->23|44->24|46->26|46->26|46->26|47->27|48->28|48->28|48->28|49->29|50->30|52->32|55->35|55->35|55->35|55->35|55->35|56->36|56->36|56->36|56->36|56->36|56->36|56->36|56->36|56->36|56->36|56->36|56->36|57->37|57->37|57->37|57->37|57->37|57->37|57->37|57->37|57->37|57->37|57->37|57->37|61->41|61->41|61->41|62->42|63->43|63->43|63->43|64->44|65->45|66->46|71->51|71->51|71->51|72->52|72->52|74->54|74->54|74->54|75->55|75->55|75->55|75->55|76->56|76->56|76->56|76->56|76->56|76->56|76->56|77->57|78->58|78->58|78->58|78->58|79->59|79->59|79->59|79->59|79->59|79->59|79->59|80->60|81->61|81->61|81->61|81->61|82->62|82->62|82->62|82->62|82->62|82->62|82->62|83->63|84->64|84->64|84->64|84->64|85->65|85->65|85->65|85->65|85->65|85->65|85->65|86->66|87->67|88->68|88->68|88->68|89->69|89->69|89->69|89->69|90->70|90->70|90->70|90->70|90->70|90->70|90->70|91->71|93->73|93->73|93->73|93->73|94->74|94->74|94->74|94->74|94->74|94->74|94->74|95->75|96->76|96->76|96->76|96->76|100->80|100->80|100->80|101->81|101->81|101->81|101->81|102->82|102->82|102->82|102->82|102->82|102->82|102->82|103->83|104->84|104->84|104->84|104->84|105->85|105->85|105->85|105->85|105->85|105->85|105->85|106->86|107->87|107->87|107->87|108->88|108->88|108->88|108->88|109->89|109->89|109->89|109->89|109->89|109->89|109->89|110->90|111->91|113->93|116->96|116->96|116->96|116->96|118->98|118->98|118->98|118->98|119->99|119->99|119->99|119->99|120->100|121->101|122->102|124->104|124->104|124->104|125->105|125->105|125->105|127->107|129->109|134->114|134->114|134->114|134->114|134->114|134->114|134->114
                  -- GENERATED --
              */
          