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

import controllers.actions.*
import date.Dates
import forms.StoppedSellingGoodsDateFormProvider
import pages.{StoppedSellingGoodsDatePage, Waypoints}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import views.html.StoppedSellingGoodsDateView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StoppedSellingGoodsDateController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   cc: AuthenticatedControllerComponents,
                                                   dates: Dates,
                                                   formProvider: StoppedSellingGoodsDateFormProvider,
                                                   view: StoppedSellingGoodsDateView
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = cc.identifyAndGetDataAndCheckIntermediaryClient {
    implicit request =>

      val commencementDate = LocalDate.parse("2025-06-03") //todo get schemeDetails from registrationConnector for commencement date when viewReg is implemented

      val form: Form[LocalDate] = formProvider(dates.today.date, commencementDate)
      val preparedForm = request.userAnswers.get(StoppedSellingGoodsDatePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, dates.dateHint, waypoints))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = cc.identifyAndGetDataAndCheckIntermediaryClient.async {
    implicit request =>

      val commencementDate = LocalDate.parse("2025-06-03") //todo get schemeDetails from registrationConnector for commencement date when viewReg is implemented

      val form: Form[LocalDate] = formProvider(dates.today.date, commencementDate)

      form.bindFromRequest().fold(
        formWithErrors =>
          BadRequest(view(formWithErrors, dates.dateHint, waypoints)).toFuture,

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(StoppedSellingGoodsDatePage, value))
            _ <- cc.sessionRepository.set(updatedAnswers)
          } yield Redirect(StoppedSellingGoodsDatePage.navigate(waypoints, updatedAnswers, updatedAnswers).url)
      )
  }
}
