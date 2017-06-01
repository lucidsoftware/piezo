
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
object editJob extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template8[scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.JobKey]]],Form[org.quartz.JobDetail],String,play.api.mvc.Call,Boolean,Option[String],List[String],play.api.mvc.Request[AnyContent],play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(
jobsByGroup: scala.collection.mutable.Buffer[(String, scala.collection.immutable.List[org.quartz.JobKey])],
jobForm: Form[org.quartz.JobDetail],
submitValue: String,
formAction: play.api.mvc.Call,
existing: Boolean,
errorMessage: Option[String] = None,
scripts: List[String] = List[String]("js/jobData.js", "js/typeAhead.js")
)(
implicit
request: play.api.mvc.Request[AnyContent]
):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import com.lucidchart.piezo.admin.controllers.{routes=>piezoRoutes}
import com.lucidchart.piezo.admin.views
import org.quartz._
import com.lucidchart.piezo.admin.views.FormHelpers._

Seq[Any](format.raw/*12.2*/("""

"""),format.raw/*18.1*/("""
"""),_display_(/*19.2*/com/*19.5*/.lucidchart.piezo.admin.views.html.jobsLayout(jobsByGroup, None, scripts)/*19.78*/ {_display_(Seq[Any](format.raw/*19.80*/("""
  """),_display_(/*20.4*/if(!errorMessage.isEmpty)/*20.29*/ {_display_(Seq[Any](format.raw/*20.31*/("""
    """),format.raw/*21.5*/("""<h3 class="text-danger">"""),_display_(/*21.30*/errorMessage/*21.42*/.get),format.raw/*21.46*/("""</h3>
  """)))}),format.raw/*22.4*/("""

  """),_display_(/*24.4*/if(existing)/*24.16*/ {_display_(Seq[Any](format.raw/*24.18*/("""
    """),format.raw/*25.5*/("""<h3>Edit Job</h3>
  """)))}/*26.5*/else/*26.10*/{_display_(Seq[Any](format.raw/*26.11*/("""
    """),format.raw/*27.5*/("""<h3>New Job</h3>
  """)))}),format.raw/*28.4*/("""

  """),format.raw/*30.3*/("""<h4 class="text-danger">"""),_display_(/*30.28*/jobForm/*30.35*/.errors.filter(_.key == "").map(_.message).mkString(", ")),format.raw/*30.92*/("""</h4>
  <form role="form" action=""""),_display_(/*31.30*/formAction),format.raw/*31.40*/("""" method="POST">
    <div class="form-horizontal">
      """),_display_(/*33.8*/defining(if(existing) {'readonly} else {'none})/*33.55*/ { newEditOnly =>_display_(Seq[Any](format.raw/*33.72*/("""

        """),_display_(/*35.10*/helper/*35.16*/.inputText(jobForm("group"), '_label -> "Group", 'labelClass -> "col-sm-3 text-right", 'inputDivClass -> "col-sm-4", 'placeholder -> "Group", newEditOnly -> None, 'class -> "job-group-type-ahead form-control form-inline-control")),format.raw/*35.245*/("""
        """),_display_(/*36.10*/helper/*36.16*/.inputText(jobForm("name"), '_label -> "Name", 'labelClass -> "col-sm-3 text-right", 'inputDivClass -> "col-sm-4", 'placeholder -> "Name", newEditOnly -> None, 'class -> "form-control form-inline-control")),format.raw/*36.221*/("""
        """),_display_(/*37.10*/helper/*37.16*/.inputText(jobForm("class"), '_label -> "Class", 'labelClass -> "col-sm-3 text-right", 'inputDivClass -> "col-sm-4", 'placeholder -> "Name", 'class -> "form-control form-inline-control")),format.raw/*37.202*/("""
        """),_display_(/*38.10*/helper/*38.16*/.inputText(jobForm("description"), '_label -> "Description", 'labelClass -> "col-sm-3 text-right", 'inputDivClass -> "col-sm-4", 'placeholder -> "Description", 'class -> "form-control form-inline-control")),format.raw/*38.221*/("""
        """),_display_(/*39.10*/helper/*39.16*/.checkbox(jobForm("durable"), '_label -> "Durable", 'labelClass -> "col-sm-3 text-right", 'inputDivClass -> "col-sm-4", 'readonly -> None, 'class -> "form-inline-control", 'checked -> true, 'disabled -> true, 'id -> "durable-placeholder")),format.raw/*39.254*/("""

        """),format.raw/*41.9*/("""<input type="hidden" id="durable" name="durable" value="true" readonly="true" />

        """),_display_(/*43.10*/helper/*43.16*/.checkbox(jobForm("requests-recovery"), '_label -> "Requests recovery", 'labelClass -> "col-sm-3 text-right", 'inputDivClass -> "col-sm-4", 'class -> "form-inline-control")),format.raw/*43.188*/("""

        """),format.raw/*45.9*/("""<h4>Job Data Map</h4>

        <div class="job-data-map">
          """),_display_(/*48.12*/helper/*48.18*/.repeat(jobForm("job-data-map"), min = jobForm("job-data-map").indexes.length + 1)/*48.100*/ { dataMap =>_display_(Seq[Any](format.raw/*48.113*/("""

            """),_display_(/*50.14*/dataMap("key")/*50.28*/.value.map/*50.38*/ { _ =>_display_(Seq[Any](format.raw/*50.45*/("""
              """),_display_(/*51.16*/dataMap("value")/*51.32*/.value.map/*51.42*/ { _ =>_display_(Seq[Any](format.raw/*51.49*/("""
                """),format.raw/*52.17*/("""<div class="job-data-delete text-right"><a href="#">delete</a></div>
              """)))}),format.raw/*53.16*/("""
            """)))}),format.raw/*54.14*/("""

            """),_display_(/*56.14*/helper/*56.20*/.inputText(dataMap("key"), '_label -> "Key", 'labelClass -> "col-sm-3 text-right", 'inputDivClass -> "col-sm-4", 'placeholder -> "Key", 'class -> "job-data-key form-control form-inline-control")),format.raw/*56.214*/("""
            """),_display_(/*57.14*/helper/*57.20*/.inputText(dataMap("value"), '_label -> "Value", 'labelClass -> "col-sm-3 text-right", 'inputDivClass -> "col-sm-4", 'placeholder -> "Value", 'class -> "job-data-value form-control form-inline-control")),format.raw/*57.222*/("""

          """)))}),format.raw/*59.12*/("""

          """),format.raw/*61.11*/("""<div class="job-data-add text-right"><a href="#">add</a></div>
        </div>
      """)))}),format.raw/*63.8*/("""

    """),format.raw/*65.5*/("""</div>

    <button type="submit" class="btn btn-default submit-btn">"""),_display_(/*67.63*/submitValue),format.raw/*67.74*/("""</button>
    <button type="button" class="btn btn-default submit-btn" onclick="history.back();" value="Cancel">Cancel</button>
  </form>
""")))}),format.raw/*70.2*/("""
"""))}
  }

  def render(jobsByGroup:scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.JobKey]]],jobForm:Form[org.quartz.JobDetail],submitValue:String,formAction:play.api.mvc.Call,existing:Boolean,errorMessage:Option[String],scripts:List[String],request:play.api.mvc.Request[AnyContent]): play.twirl.api.HtmlFormat.Appendable = apply(jobsByGroup,jobForm,submitValue,formAction,existing,errorMessage,scripts)(request)

  def f:((scala.collection.mutable.Buffer[scala.Tuple2[String, scala.collection.immutable.List[org.quartz.JobKey]]],Form[org.quartz.JobDetail],String,play.api.mvc.Call,Boolean,Option[String],List[String]) => (play.api.mvc.Request[AnyContent]) => play.twirl.api.HtmlFormat.Appendable) = (jobsByGroup,jobForm,submitValue,formAction,existing,errorMessage,scripts) => (request) => apply(jobsByGroup,jobForm,submitValue,formAction,existing,errorMessage,scripts)(request)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed May 31 21:03:54 MDT 2017
                  SOURCE: /home/paul/lucid/piezo/admin/app/com/lucidchart/piezo/admin/views/editJob.scala.html
                  HASH: aeabee787105a438768f73c8db18e253acd6e831
                  MATRIX: 754->1|1406->384|1435->572|1463->574|1474->577|1556->650|1596->652|1626->656|1660->681|1700->683|1732->688|1784->713|1805->725|1830->729|1869->738|1900->743|1921->755|1961->757|1993->762|2032->784|2045->789|2084->790|2116->795|2166->815|2197->819|2249->844|2265->851|2343->908|2405->943|2436->953|2520->1011|2576->1058|2631->1075|2669->1086|2684->1092|2935->1321|2972->1331|2987->1337|3214->1542|3251->1552|3266->1558|3474->1744|3511->1754|3526->1760|3753->1965|3790->1975|3805->1981|4065->2219|4102->2229|4220->2320|4235->2326|4429->2498|4466->2508|4562->2577|4577->2583|4669->2665|4721->2678|4763->2693|4786->2707|4805->2717|4850->2724|4893->2740|4918->2756|4937->2766|4982->2773|5027->2790|5142->2874|5187->2888|5229->2903|5244->2909|5460->3103|5501->3117|5516->3123|5740->3325|5784->3338|5824->3350|5939->3435|5972->3441|6069->3511|6101->3522|6270->3661
                  LINES: 19->1|36->12|38->18|39->19|39->19|39->19|39->19|40->20|40->20|40->20|41->21|41->21|41->21|41->21|42->22|44->24|44->24|44->24|45->25|46->26|46->26|46->26|47->27|48->28|50->30|50->30|50->30|50->30|51->31|51->31|53->33|53->33|53->33|55->35|55->35|55->35|56->36|56->36|56->36|57->37|57->37|57->37|58->38|58->38|58->38|59->39|59->39|59->39|61->41|63->43|63->43|63->43|65->45|68->48|68->48|68->48|68->48|70->50|70->50|70->50|70->50|71->51|71->51|71->51|71->51|72->52|73->53|74->54|76->56|76->56|76->56|77->57|77->57|77->57|79->59|81->61|83->63|85->65|87->67|87->67|90->70
                  -- GENERATED --
              */
          