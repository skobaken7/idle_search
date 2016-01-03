package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.iteratee._
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format._
import scala.concurrent.duration._
import scala.concurrent.Future
import java.util.UUID
import models._
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global
import utils._
import utils.Utils._
import play.api.i18n._
import javax.inject.Inject


class Application @Inject()(val messagesApi: MessagesApi) extends Controller with DAO with I18nSupport {
  
  val form : Form[Param] = Form(
    mapping(
      "name"   -> optional(text),
      "birth"  -> tuple("from" -> optional(date),   "to" -> optional(date)),
      "height" -> tuple("from" -> optional(number), "to" -> optional(number)),
      "weight" -> tuple("from" -> optional(number), "to" -> optional(number)),
      "bust"   -> tuple("from" -> optional(number), "to" -> optional(number)),
      "waist"  -> tuple("from" -> optional(number), "to" -> optional(number)),
      "hip"    -> tuple("from" -> optional(number), "to" -> optional(number)),
      "cup"    -> tuple("from" -> optional(char),   "to" -> optional(char))
    )(Param.apply _)(Param.unapply _)
  )
  
  def search = Action.async { implicit request => 
    val boundForm = form.bindFromRequest
    boundForm.fold(
      formWithErrors => Future.successful(BadRequest(views.html.search(formWithErrors, Seq[Idle]()))),
      param => Idles.find(param).map(idles => Ok(views.html.search(boundForm, idles)))
    )
  }

  def updateDB = Action {
    UpdateDBBatch.run
    Ok("updated")
  }
}
