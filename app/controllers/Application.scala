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
      "birth"  -> tuple("birth_from"  -> optional(date),   "birth_to"  -> optional(date)),
      "height" -> tuple("height_from" -> optional(number), "height_to" -> optional(number)),
      "weight" -> tuple("weight_from" -> optional(number), "weight_to" -> optional(number)),
      "bust"   -> tuple("bust_from"   -> optional(number), "bust_to"   -> optional(number)),
      "waist"  -> tuple("waist_from"  -> optional(number), "waist_to"  -> optional(number)),
      "hip"    -> tuple("hip_from"    -> optional(number), "hip_to"    -> optional(number)),
      "cup"    -> tuple("cup_from"    -> optional(char),   "cup_to"    -> optional(char))
    )(Param.apply _)(Param.unapply _)
  )
  
  def search = Action.async { implicit request => 
    form.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.search(formWithErrors, List[Idle]()))),
      userData => Future.successful(Ok(views.html.search(form, List[Idle]())))
    )
  }

  def updateDB = Action {
    UpdateDBBatch.run
    Ok("updated")
  }
}
