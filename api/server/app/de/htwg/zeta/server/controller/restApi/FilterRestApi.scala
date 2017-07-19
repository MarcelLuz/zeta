package de.htwg.zeta.server.controller.restApi

import java.util.UUID

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.actions.SecuredRequest
import de.htwg.zeta.common.models.entity.Filter
import de.htwg.zeta.persistence.Persistence
import de.htwg.zeta.server.controller.restApi.format.FilterFormat
import de.htwg.zeta.server.util.auth.ZetaEnv
import grizzled.slf4j.Logging
import play.api.libs.json.JsArray
import play.api.mvc.AnyContent
import play.api.mvc.Controller
import play.api.mvc.Result
import play.api.mvc.Results
import scalaoauth2.provider.OAuth2ProviderActionBuilders.executionContext

/**
 * RESTful API for filter definitions
 */
class FilterRestApi() extends Controller with Logging {

  private val repo = Persistence.fullAccessRepository.filter

  /** Lists all filter.
   *
   * @param request The request
   * @return The result
   */
  def showForUser()(request: SecuredRequest[ZetaEnv, AnyContent]): Future[Result] = {
    getEntities.map(getJsonArray).recover {
      case e: Exception =>
        error("Exception while trying to read all `Filter` from DB", e)
        BadRequest(e.getMessage)
    }
  }

  private def getEntities: Future[List[Filter]] = {
    repo.readAllIds().flatMap(ids => {
      val list = ids.toList.map(repo.read)
      Future.sequence(list)
    })
  }

  private def getJsonArray(list: List[Filter]) = {
    val entities = list.filter(e => !e.deleted.getOrElse(false))
    val entries = entities.map(FilterFormat.writes)
    val json = JsArray(entries)
    Ok(json)
  }

  /**
   * Get a single Generator instance
   * @param id Identifier of Generator
   * @param request The request
   * @return The result
   */
  def get(id: UUID)(request: SecuredRequest[ZetaEnv, AnyContent]): Future[Result] = {
    val repo = Persistence.fullAccessRepository.filter
    repo.read(id).flatMap(entity => {
      Future(Ok(FilterFormat.writes(entity)))
    }).recover {
      case e: Exception =>
        error("Exception while trying to read a single `Filter` from DB", e)
        Results.BadRequest(e.getMessage)
    }
  }

  /**
   * Flag Filter as deleted
   * @param id Identifier of Filter
   * @param request The request
   * @return The result
   */
  def delete(id: UUID)(request: SecuredRequest[ZetaEnv, AnyContent]): Future[Result] = {
    flagAsDeleted(id).map(_ => Ok("")).recover {
      case e: Exception =>
        error("Exception while trying to flag `Filter` as deleted at DB", e)
        BadRequest(e.getMessage)
    }
  }

  private def flagAsDeleted(id: UUID): Future[Filter] = {
    val deleted = Some(true)
    repo.update(id, e => e.copy(deleted = deleted))
  }
}