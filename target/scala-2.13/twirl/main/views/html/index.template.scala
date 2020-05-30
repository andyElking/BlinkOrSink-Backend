
package views.html

import _root_.play.twirl.api.TwirlFeatureImports._
import _root_.play.twirl.api.TwirlHelperImports._
import _root_.play.twirl.api.Html
import _root_.play.twirl.api.JavaScript
import _root_.play.twirl.api.Txt
import _root_.play.twirl.api.Xml
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import play.api.mvc._
import play.api.data._

object index extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template2[String,AssetsFinder,play.twirl.api.HtmlFormat.Appendable] {

  /*
 * This template takes a two arguments, a String containing a
 * message to display and an AssetsFinder to locate static assets.
 */
  def apply/*5.2*/(message: String)(implicit assetsFinder: AssetsFinder):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*6.1*/("""
"""),format.raw/*11.4*/("""

"""),_display_(/*13.2*/main(title = "PPEEPP")/*13.24*/ {_display_(Seq[Any](format.raw/*13.26*/("""

    """),_display_(/*15.6*/welcome(message + "plup", style = "scala")),format.raw/*15.48*/("""

    """),format.raw/*20.8*/("""


""")))}),format.raw/*23.2*/("""
"""))
      }
    }
  }

  def render(message:String,assetsFinder:AssetsFinder): play.twirl.api.HtmlFormat.Appendable = apply(message)(assetsFinder)

  def f:((String) => (AssetsFinder) => play.twirl.api.HtmlFormat.Appendable) = (message) => (assetsFinder) => apply(message)(assetsFinder)

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: 2020-05-28T15:44:36.384178
                  SOURCE: /home/andy/sbt Projects/play starter/play-samples-play-scala-starter-example/app/views/index.scala.html
                  HASH: c0f28825489f8dc62c5978f780412b33d5686eb5
                  MATRIX: 873->137|1021->192|1049->387|1078->390|1109->412|1149->414|1182->421|1245->463|1278->592|1312->596
                  LINES: 24->5|29->6|30->11|32->13|32->13|32->13|34->15|34->15|36->20|39->23
                  -- GENERATED --
              */
          