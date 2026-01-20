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

import config.FrontendAppConfig
import controllers.actions.*
import date.Dates
import models.requests.DataRequest
import pages.{StopSellingGoodsPage, StoppedSellingGoodsDatePage, StoppedUsingServiceDatePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.ClientDetailService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ApplicationCompleteView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApplicationCompleteController @Inject()(
                                               override val messagesApi: MessagesApi,
                                               cc: AuthenticatedControllerComponents,
                                               view: ApplicationCompleteView,
                                               config: FrontendAppConfig,
                                               dates: Dates,
                                               clientDetailService: ClientDetailService
                                             )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad: Action[AnyContent] = cc.identifyAndGetData.async {
    implicit request =>
      request.userAnswers.get(StopSellingGoodsPage) match {
        case Some(true) => onStopSellingGoods()
        case Some(false) => onStopUsingService()
        case None => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
  }

  private def onStopSellingGoods()(implicit request: DataRequest[_]): Future[Result] = {

    for {
      clientName <- clientDetailService.getClientName
    } yield {
      request.userAnswers.get(StoppedSellingGoodsDatePage).map { date =>
        val leaveDate = dates.getLeaveDateWhenStoppedSellingGoods
        val vatReturnDate = date

        Ok(view(
          config.iossYourAccountUrl,
          clientName,
          dates.formatter.format(leaveDate),
          dates.monthFormatter.format(vatReturnDate)
        ))
      }.getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  private def onStopUsingService()(implicit request: DataRequest[_]): Future[Result] = {

    for {
      clientName <- clientDetailService.getClientName
    } yield {
      request.userAnswers.get(StoppedUsingServiceDatePage).map { stoppedUsingServiceDate =>

        val leaveDate = dates.getLeaveDateWhenStoppedUsingService(stoppedUsingServiceDate)
        val vatReturnDate = dates.getVatReturnMonthWhenStoppedUsingService(stoppedUsingServiceDate)

        Ok(view(
          config.iossYourAccountUrl,
          clientName,
          dates.formatter.format(leaveDate),
          vatReturnDate
        ))
      }.getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
    }
  }
}
