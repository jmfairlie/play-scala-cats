package controllers

import play.api._
import play.api.mvc._
import play.api.Logger


  /**
   * This is needed cause couldn't find a way to serve user uploaded files
   * in Heroku. Something to do with the assets being compiled or something...
   */
class CatFileServer extends Controller {
  def serveUploadedFiles( file: String ) = Action {
    implicit request => {
      val dirPath = "uploads"
      val f = new java.io.File(s"$dirPath/$file")
      if (f.exists) {
      	Logger.info(s"serveUploadFiles: $dirPath/$file")
      	Ok.sendFile(f, inline = true)
      } else {
	Logger.error(s"serveUploadFiles: File not found: $dirPath/$file")
	NotFound(<h1>File not found</h1>)
      }
    }
  }

}


