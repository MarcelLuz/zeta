package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LogoutEvent
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models.User
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Controller
import utils.auth.DefaultEnv

/**
 * The basic application controller.
 *
 * @param messagesApi The Play messages API.
 * @param silhouette The Silhouette stack.
 * @param socialProviderRegistry The social provider registry.
 * @param webJarAssets The webjar assets implementation.
 */
class ApplicationController @Inject() (
    val messagesApi: MessagesApi,
    silhouette: Silhouette[DefaultEnv],
    socialProviderRegistry: SocialProviderRegistry,
    implicit val webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {

  /**
   * Handles the index action.
   *
   * @return The result to display.
   */
  def index: Action[AnyContent] = silhouette.SecuredAction { implicit request =>
    Ok(views.html.webpage.WebpageIndex(Some(request.identity)))
  }

  /**
   * Get the user id of the logged in user
   *
   * @return The user id
   */
  def user: Action[AnyContent] = silhouette.SecuredAction { implicit request =>
    Ok(User.getUserId(request.identity.loginInfo))
  }

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
  def signOut: Action[AnyContent] = silhouette.SecuredAction.async { implicit request =>
    val result = Redirect(routes.ApplicationController.index())
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }
}
