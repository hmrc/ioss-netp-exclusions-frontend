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

import config.FrontendAppConfig
import controllers.actions.*
import date.Dates
import models.requests.DataRequest
import pages.{StopSellingGoodsPage, StoppedSellingGoodsDatePage, StoppedUsingServiceDatePage}

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ApplicationCompleteView


class ApplicationCompleteController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: ApplicationCompleteView,
                                        config: FrontendAppConfig,
                                        dates: Dates,
                                    ) extends FrontendBaseController with I18nSupport {

  private val clientName = "There is no Try Ltd"

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.get(StopSellingGoodsPage).flatMap { stopSellingGoods =>
        if (stopSellingGoods) {
          onStopSellingGoods()
        } else {
          onStopUsingService()
        }
      }.getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
  }

  private def onStopSellingGoods()(implicit request: DataRequest[_]): Option[Result] = {

    request.userAnswers.get(StoppedSellingGoodsDatePage).map { date =>
      val leaveDate = dates.getLeaveDateWhenStoppedSellingGoods

      Ok(view(
        config.iossYourAccountUrl,
        clientName,
        dates.formatter.format(leaveDate.minusDays(1)),
      ))

    }
  }

  private def onStopUsingService()(implicit request: DataRequest[_]): Option[Result] = {
    request.userAnswers.get(StoppedUsingServiceDatePage).map { stoppedUsingServiceDate =>

      val leaveDate = dates.getLeaveDateWhenStoppedUsingService(stoppedUsingServiceDate)
      Ok(view(
        config.iossYourAccountUrl,
        clientName,
        dates.formatter.format(leaveDate.minusDays(1))
      ))
    }
  }
}
