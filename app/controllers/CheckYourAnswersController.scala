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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.AuthenticatedControllerComponents
import date.Dates
import logging.Logging
import models.audit.ExclusionAuditType
import models.etmp.EtmpExclusionReason
import models.{CheckMode, UserAnswers}
import pages.{CheckYourAnswersPage, EmptyWaypoints, LeaveSchemePage, StopSellingGoodsPage, Waypoint, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ClientDetailService, RegistrationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CompletionChecks
import viewmodels.govuk.summarylist.*
import views.html.CheckYourAnswersView
import utils.FutureSyntax.FutureOps
import viewmodels.checkAnswers.{StopSellingGoodsSummary, StoppedSellingGoodsDateSummary, StoppedUsingServiceDateSummary}

import scala.concurrent.ExecutionContext

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            cc: AuthenticatedControllerComponents,
                                            dates: Dates,
                                            view: CheckYourAnswersView,
                                            config: FrontendAppConfig,
                                            registrationService: RegistrationService,
                                            clientDetailService: ClientDetailService
                                          )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with CompletionChecks with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(): Action[AnyContent] = cc.identifyAndGetData.async {
    implicit request =>

      for {
        clientName <- clientDetailService.getClientName
      } yield {
        val thisPage = CheckYourAnswersPage
        val waypoints = EmptyWaypoints.setNextWaypoint(Waypoint(thisPage, CheckMode, CheckYourAnswersPage.urlFragment))

        val stopSellingGoodsSummaryRow = StopSellingGoodsSummary.row(request.userAnswers, waypoints, thisPage)
        val stoppedSellingGoodsDateRow = StoppedSellingGoodsDateSummary.row(request.userAnswers, waypoints, thisPage, dates)
        val stoppedUsingServiceDateRow = StoppedUsingServiceDateSummary.row(request.userAnswers, waypoints, thisPage, dates)

        val list = SummaryListViewModel(
          rows = Seq(
            stopSellingGoodsSummaryRow,
            stoppedSellingGoodsDateRow,
            stoppedUsingServiceDateRow
          ).flatten
        )

        val isValid = validate()
        Ok(view(waypoints, list, isValid, config.iossYourAccountUrl, clientName))
      }
  }

  def onSubmit(waypoints: Waypoints, incompletePrompt: Boolean): Action[AnyContent] = cc.identifyAndGetData.async {
    implicit request =>
      getFirstValidationErrorRedirect(waypoints) match {
        case Some(errorRedirect) => if (incompletePrompt) {
          errorRedirect.toFuture
        } else {
          Redirect(routes.CheckYourAnswersController.onPageLoad()).toFuture
        }
        case None =>

          val exclusionReason = determineExclusionReason(request.userAnswers)

          registrationService.amendRegistrationAndAudit(
            userId = request.userId,
            iossNumber = request.iossNumber,
            userAnswers = request.userAnswers,
            displayNetpRegistration = request.displayNetpRegistration,
            intermediaryNumber = request.intermediaryNumber,
            exclusionReason = Some(exclusionReason),
            exclusionAuditType = ExclusionAuditType.ExclusionRequestSubmitted
          ).map {
            case Right(_) =>
              Redirect(CheckYourAnswersPage.navigate(waypoints, request.userAnswers, request.userAnswers).route)
            case Left(e) =>
              logger.error(s"Failure to submit self exclusion ${e.body}")
              Redirect(routes.SubmissionFailureController.onPageLoad())
          }
      }
  }

  private def determineExclusionReason(userAnswers: UserAnswers): EtmpExclusionReason = {
    userAnswers.get(StopSellingGoodsPage) match {
      case Some(true) =>
        EtmpExclusionReason.NoLongerSupplies
      case Some(false) =>
        userAnswers.get(LeaveSchemePage) match {
          case Some(true) =>
            EtmpExclusionReason.VoluntarilyLeaves
          case Some(false) =>
            throw new Exception("User chose not to stop selling goods or leave scheme")
          case None =>
            throw new Exception("Expected answer for LeaveSchemePage when StopSellingGoodsPage = false")
        }
      case _ =>
        throw new Exception("Expected answer for Stopped Selling Goods page")
    }
  }
}
