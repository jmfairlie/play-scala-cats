package controllers

import play.api._
import play.api.mvc._
import play.api.i18n._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import models._
import dal._
import play.api.Logger


import scala.concurrent.{ ExecutionContext, Future }

import javax.inject._

class CatController @Inject() (repo: CatRepository, val messagesApi: MessagesApi)
                                 (implicit ec: ExecutionContext) extends Controller with I18nSupport{

  /**
   * The mapping for the create cat form.
   */
  val catForm: Form[CreateCatForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "color" -> nonEmptyText,
      "breed" -> number.verifying(min(0), max(100)),
      "gender" -> number.verifying(min(0), max(1))
    )(CreateCatForm.apply)(CreateCatForm.unapply)
  }
  
    /**
   * The mapping for the edit cat form.
   */
  val editForm: Form[EditCatForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "color" -> nonEmptyText,
      "breed" -> number.verifying(min(0), max(100)),
      "gender" -> number.verifying(min(0), max(1)),
      "filename" -> optional(text)
    )(EditCatForm.apply)(EditCatForm.unapply)
  }

  /**
   * The index action.
   */
  def index = Action {
    Ok(views.html.index(catForm))
  }

  /**
   * The add person action.
   *
   * This is asynchronous, since we're invoking the asynchronous methods on PersonRepository.
   */
  def addCat = Action.async(parse.multipartFormData) { implicit request =>
    
    val filename : String = uploadFile(request)

    catForm.bindFromRequest.fold(
     
      errorForm => {
        Future.successful(Ok(views.html.index(errorForm)))
      },
     
      cat => {
        repo.create(cat.name, cat.color, cat.breed, cat.gender, filename).map { _ =>
          Redirect(routes.CatController.index)
        }
      }
    )
    
  }
  
  
  def editCat(id: Long) = Action.async
  {
      repo.edit(id).map {
          
          cat => Ok(views.html.edit(editForm.fill(EditCatForm(cat.id, cat.name, cat.color, cat.breed, cat.gender, Option(cat.filename))), id))
      }
  }
  
    def saveCat(id: Long) = Action.async(parse.multipartFormData) { implicit request =>
      
      val filename : String = uploadFile(request)
     
      editForm.bindFromRequest.fold(
     
      errorForm => {
        Future.successful(Ok(views.html.edit(errorForm, id)))
      },
      
      cat => {
          repo.update(cat.id, cat.name, cat.color, cat.breed, cat.gender, filename).map { _ =>
          // If successful, we simply redirect to the index page.
          Redirect(routes.CatController.index)
        }
      }
    )
     
  }
  
  /**
   * Delete a kitten, synchronously.
   */
  def deleteCat(id: Long) = Action 
  {
    repo.delete(id)
    Redirect(routes.CatController.index)
  } 

  /**
   * A REST endpoint that gets all the people as JSON.
   */
  def getCats = Action.async {
  	repo.list().map { clowder =>
      Ok(Json.toJson(clowder))
    }
  }
  
  def uploadFile(request:Request[MultipartFormData[libs.Files.TemporaryFile]]): String =
  {
    var filename : String = ""
    request.body.file("filename").map 
    { 
        picture =>
        import java.io.File
        filename = picture.filename
        Logger.info(s"filename from request body: $filename")
        val contentType = picture.contentType
        val f = new File(s"public/uploads/$filename")
        if (!f.exists)
        {
            val dir = new File("public/uploads")
            if (!dir.exists)
            {
                dir.mkdirs
            }
            picture.ref.moveTo(f)
            Logger.info(s"uploaded file $f")
        }
        else
        {
            Logger.info(s"filename already exists $f")
        }
        Ok("File uploaded")
    }
    return filename
  }
}




case class CreateCatForm(name: String, color: String, breed: Int, gender: Int)

case class EditCatForm(id: Long, name: String, color: String, breed: Int, gender: Int, filename: Option[String])
