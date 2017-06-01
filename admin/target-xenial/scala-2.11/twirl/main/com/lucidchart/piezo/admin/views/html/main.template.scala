
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
object main extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template5[String,List[String],List[String],Html,RequestHeader,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(
title: String,
scripts: List[String] = List(),
styles: List[String] = List()
)(
content: Html
)(
implicit request: RequestHeader
):play.twirl.api.HtmlFormat.Appendable = {
      _display_ {import com.lucidchart.piezo.admin.controllers.{routes=>piezoRoutes}

Seq[Any](format.raw/*9.2*/("""
"""),format.raw/*11.1*/("""
"""),format.raw/*12.1*/("""<!DOCTYPE HTML>
<html>
<head>
    <title>"""),_display_(/*15.13*/if(!title.isEmpty)/*15.31*/{_display_(Seq[Any](format.raw/*15.32*/(""" """),_display_(/*15.34*/title),format.raw/*15.39*/(""" """),format.raw/*15.40*/("""| """)))}),format.raw/*15.43*/(""" """),format.raw/*15.44*/("""Piezo</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link rel="shortcut icon" href="/favicon.ico" type="image/png">

    <link rel="stylesheet" type="text/css" href=""""),_display_(/*21.51*/routes/*21.57*/.Assets.at("jquery-ui-1.10.3/core/themes/base/minified/jquery-ui.min.css")),format.raw/*21.131*/("""" />
    <link rel="stylesheet" type="text/css" href=""""),_display_(/*22.51*/routes/*22.57*/.Assets.at("bootstrap-3.3.6/css/bootstrap.min.css")),format.raw/*22.108*/("""" />
    <link rel="stylesheet" type="text/css" href=""""),_display_(/*23.51*/routes/*23.57*/.Assets.at("stylesheets/main.css")),format.raw/*23.91*/("""" />

    """),_display_(/*25.6*/for(style <- styles) yield /*25.26*/ {_display_(Seq[Any](format.raw/*25.28*/("""
    """),format.raw/*26.5*/("""<link rel="stylesheet" type="text/css" href=""""),_display_(/*26.51*/routes/*26.57*/.Assets.at(style)),format.raw/*26.74*/("""" />
    """)))}),format.raw/*27.6*/("""

    """),format.raw/*29.5*/("""<script type="text/javascript" src=""""),_display_(/*29.42*/routes/*29.48*/.Assets.at("js/jquery-2.0.3.min.js")),format.raw/*29.84*/(""""></script>
    <script type="text/javascript" src=""""),_display_(/*30.42*/routes/*30.48*/.Assets.at("jquery-ui-1.10.3/core/ui/minified/jquery-ui.min.js")),format.raw/*30.112*/(""""></script>
    <script type="text/javascript" src=""""),_display_(/*31.42*/routes/*31.48*/.Assets.at("bootstrap-3.3.6/js/bootstrap.min.js")),format.raw/*31.97*/(""""></script>

    """),_display_(/*33.6*/for(script <- scripts) yield /*33.28*/ {_display_(Seq[Any](format.raw/*33.30*/("""
      """),format.raw/*34.7*/("""<script type="text/javascript" src=""""),_display_(/*34.44*/routes/*34.50*/.Assets.at(script)),format.raw/*34.68*/(""""></script>
    """)))}),format.raw/*35.6*/("""

"""),format.raw/*37.1*/("""</head>
<body>

<nav class="navbar navbar-default navbar-fixed-top" role="navigation">
    <div class="container">
    <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand piezoicon" href="/"><span class="glyphicon glyphicon-flash"></span>&nbsp;Piezo</a>
    </div>

    <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
        <ul class="nav navbar-nav">
            <li><a href="/jobs">Jobs</a></li>
            <li><a href="/triggers">Triggers</a></li>
        </ul>
    </div>
    </div>
</nav>

<script type="text/javascript">
var url = window.location;
// Will only work if string in href matches with location
$('ul.nav a[href="'+ url +'"]').parent().addClass('active');

// Will also work for relative and absolute hrefs
$('ul.nav a').filter(function() """),format.raw/*67.33*/("""{"""),format.raw/*67.34*/("""
    """),format.raw/*68.5*/("""return this.href == url;
"""),format.raw/*69.1*/("""}"""),format.raw/*69.2*/(""").parent().addClass('active');
</script>

<div class="container">
    <div class="flash-container alert-success">
        """),_display_(/*74.10*/if(!request.flash.isEmpty)/*74.36*/ {_display_(Seq[Any](format.raw/*74.38*/("""
        """),format.raw/*75.9*/("""<div id="flash-page" class=""""),_display_(/*75.38*/request/*75.45*/.flash("class")),format.raw/*75.60*/("""">
        """),_display_(/*76.10*/request/*76.17*/.flash("message")),format.raw/*76.34*/("""
    """),format.raw/*77.5*/("""</div>
    <script type="text/javascript">
        $(document).ready(function() """),format.raw/*79.38*/("""{"""),format.raw/*79.39*/("""
            """),format.raw/*80.13*/("""setTimeout(function() """),format.raw/*80.35*/("""{"""),format.raw/*80.36*/("""
                """),format.raw/*81.17*/("""var element = $('#flash-page');
                element.fadeTo(2000, 0, function() """),format.raw/*82.52*/("""{"""),format.raw/*82.53*/("""
                    """),format.raw/*83.21*/("""element.slideUp(400, function() """),format.raw/*83.53*/("""{"""),format.raw/*83.54*/("""
                        """),format.raw/*84.25*/("""element.remove();
                    """),format.raw/*85.21*/("""}"""),format.raw/*85.22*/(""");
                """),format.raw/*86.17*/("""}"""),format.raw/*86.18*/(""");
            """),format.raw/*87.13*/("""}"""),format.raw/*87.14*/(""", 3000);
        """),format.raw/*88.9*/("""}"""),format.raw/*88.10*/(""");
    </script>
    """)))}),format.raw/*90.6*/("""
"""),format.raw/*91.1*/("""</div>

"""),_display_(/*93.2*/content),format.raw/*93.9*/("""

"""),format.raw/*95.1*/("""<hr />
<footer>
    <p class="text-muted pull-left">&copy; Apache License Version 2.0</p>
    <div class="text-muted pull-right">Created by&nbsp;&nbsp;&nbsp;<img src=""""),_display_(/*98.79*/routes/*98.85*/.Assets.at("img/LucidSoftwareBlueLogo_167x32.png")),format.raw/*98.135*/(""""></div>
</footer>
</div>

"""),_display_(/*102.2*/for(script <- scripts) yield /*102.24*/ {_display_(Seq[Any](format.raw/*102.26*/("""
"""),format.raw/*103.1*/("""<script type="text/javascript" src=""""),_display_(/*103.38*/routes/*103.44*/.Assets.at(script)),format.raw/*103.62*/(""""></script>
""")))}),format.raw/*104.2*/("""
"""),format.raw/*105.1*/("""</body>
</html>"""))}
  }

  def render(title:String,scripts:List[String],styles:List[String],content:Html,request:RequestHeader): play.twirl.api.HtmlFormat.Appendable = apply(title,scripts,styles)(content)(request)

  def f:((String,List[String],List[String]) => (Html) => (RequestHeader) => play.twirl.api.HtmlFormat.Appendable) = (title,scripts,styles) => (content) => (request) => apply(title,scripts,styles)(content)(request)

  def ref: this.type = this

}
              /*
                  -- GENERATED --
                  DATE: Wed May 31 21:03:53 MDT 2017
                  SOURCE: /home/paul/lucid/piezo/admin/app/com/lucidchart/piezo/admin/views/main.scala.html
                  HASH: a74fb6e02972c070a040e1482ff972fb06aa659e
                  MATRIX: 576->1|862->133|890->203|918->204|987->246|1014->264|1053->265|1082->267|1108->272|1137->273|1171->276|1200->277|1463->513|1478->519|1574->593|1656->648|1671->654|1744->705|1826->760|1841->766|1896->800|1933->811|1969->831|2009->833|2041->838|2114->884|2129->890|2167->907|2207->917|2240->923|2304->960|2319->966|2376->1002|2456->1055|2471->1061|2557->1125|2637->1178|2652->1184|2722->1233|2766->1251|2804->1273|2844->1275|2878->1282|2942->1319|2957->1325|2996->1343|3043->1360|3072->1362|4217->2479|4246->2480|4278->2485|4330->2510|4358->2511|4508->2634|4543->2660|4583->2662|4619->2671|4675->2700|4691->2707|4727->2722|4766->2734|4782->2741|4820->2758|4852->2763|4960->2843|4989->2844|5030->2857|5080->2879|5109->2880|5154->2897|5265->2980|5294->2981|5343->3002|5403->3034|5432->3035|5485->3060|5551->3098|5580->3099|5627->3118|5656->3119|5699->3134|5728->3135|5772->3152|5801->3153|5853->3175|5881->3176|5916->3185|5943->3192|5972->3194|6167->3362|6182->3368|6254->3418|6309->3446|6348->3468|6389->3470|6418->3471|6483->3508|6499->3514|6539->3532|6583->3545|6612->3546
                  LINES: 19->1|30->9|31->11|32->12|35->15|35->15|35->15|35->15|35->15|35->15|35->15|35->15|41->21|41->21|41->21|42->22|42->22|42->22|43->23|43->23|43->23|45->25|45->25|45->25|46->26|46->26|46->26|46->26|47->27|49->29|49->29|49->29|49->29|50->30|50->30|50->30|51->31|51->31|51->31|53->33|53->33|53->33|54->34|54->34|54->34|54->34|55->35|57->37|87->67|87->67|88->68|89->69|89->69|94->74|94->74|94->74|95->75|95->75|95->75|95->75|96->76|96->76|96->76|97->77|99->79|99->79|100->80|100->80|100->80|101->81|102->82|102->82|103->83|103->83|103->83|104->84|105->85|105->85|106->86|106->86|107->87|107->87|108->88|108->88|110->90|111->91|113->93|113->93|115->95|118->98|118->98|118->98|122->102|122->102|122->102|123->103|123->103|123->103|123->103|124->104|125->105
                  -- GENERATED --
              */
          