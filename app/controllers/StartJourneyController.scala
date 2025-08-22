/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import pages.EmptyWaypoints
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.IossNumberQuery
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StartJourneyController @Inject()(
                                        sessionRepository: SessionRepository,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        val controllerComponents: MessagesControllerComponents,
                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(iossNumber: String): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>

      val originalAnswers = request.userAnswers.getOrElse(UserAnswers(request.userId))
      val updatedAnswersTry = originalAnswers.set(IossNumberQuery, iossNumber)

      updatedAnswersTry match {
        case scala.util.Success(updatedAnswers) =>
          sessionRepository.set(updatedAnswers).map { _ =>
            Redirect(routes.StopSellingGoodsController.onPageLoad(EmptyWaypoints))
          }

        case scala.util.Failure(_) =>
          Future.successful(InternalServerError("Could not set IOSS number in session"))
      }
  }
}
