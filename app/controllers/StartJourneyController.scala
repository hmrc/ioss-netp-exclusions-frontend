/*
 * Copyright 2026 HM Revenue & Customs
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

import controllers.actions.AuthenticatedControllerComponents
import models.UserAnswers
import pages.EmptyWaypoints
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.IossNumberQuery
import services.ClientDetailService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class StartJourneyController @Inject()(
                                        cc: AuthenticatedControllerComponents,
                                        clientDetailService: ClientDetailService
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(iossNumber: String): Action[AnyContent] = cc.identifyAndGetOptionalData.async {
    implicit request =>

      val originalAnswers = request.userAnswers.getOrElse(UserAnswers(request.userId))
      val updatedAnswersTry = originalAnswers.set(IossNumberQuery, iossNumber)

      updatedAnswersTry match {
        case scala.util.Success(updatedAnswers) =>
          val iossNumber: String = updatedAnswers.get(IossNumberQuery).getOrElse(throw new Exception("IOSS number can't be retrieved from user answers"))
          clientDetailService.checkIntermediaryHasClient(request.intermediaryNumber, iossNumber).flatMap {
            case None =>
              cc.sessionRepository.set(updatedAnswers).map { _ =>
                Redirect(routes.StopSellingGoodsController.onPageLoad(EmptyWaypoints))
              }

            case Some(result) =>
              result.toFuture
          }

        case scala.util.Failure(_) =>
          InternalServerError("Could not set IOSS number in session").toFuture
      }
  }
}
