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

import base.SpecBase
import config.FrontendAppConfig
import connectors.RegistrationConnector
import date.{Dates, Today}
import models.CheckMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.*
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.checkAnswers.{StopSellingGoodsSummary, StoppedSellingGoodsDateSummary, StoppedUsingServiceDateSummary}
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  private val today: LocalDate = LocalDate.now
  private val mockToday: Today = mock[Today]
  when(mockToday.date).thenReturn(today)
  private val date: Dates = new Dates(mockToday)
  private val answers = emptyUserAnswers
    .set(StopSellingGoodsPage, true).success.value
    .set(StoppedSellingGoodsDatePage, today).success.value

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val waypoints = EmptyWaypoints.setNextWaypoint(Waypoint(CheckYourAnswersPage, CheckMode, CheckYourAnswersPage.urlFragment))
        val list = SummaryListViewModel(
          Seq(
            StopSellingGoodsSummary.row(answers, waypoints, CheckYourAnswersPage),
            StoppedSellingGoodsDateSummary.row(answers, waypoints, CheckYourAnswersPage, date),
            StoppedUsingServiceDateSummary.row(answers, waypoints, CheckYourAnswersPage, date)
          ).flatten
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          waypoints,
          list,
          isValid = true,
          appConfig.iossYourAccountUrl,
          clientName
        )(request, messages(application)).toString
      }
    }

    "must include StopSellingGoodsSummary row in the summary list when data is present" in {
      val answersWithStopSellingGoods = answers
        .set(StopSellingGoodsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(answersWithStopSellingGoods))
        .build()

      running(application) {
        implicit val msgs: Messages = messages(application)
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val waypoints = EmptyWaypoints.setNextWaypoint(Waypoint(CheckYourAnswersPage, CheckMode, CheckYourAnswersPage.urlFragment))
        val list = SummaryListViewModel(
          Seq(
            StopSellingGoodsSummary.row(answersWithStopSellingGoods, waypoints, CheckYourAnswersPage),
            StoppedSellingGoodsDateSummary.row(answersWithStopSellingGoods, waypoints, CheckYourAnswersPage, date),
            StoppedUsingServiceDateSummary.row(answersWithStopSellingGoods, waypoints, CheckYourAnswersPage, date)
          ).flatten
        )

        status(result) mustBe OK
        contentAsString(result) mustBe view(
          waypoints,
          list,
          isValid = true,
          appConfig.iossYourAccountUrl,
          clientName)(request, msgs).toString

        val stopSellingGoodsRow = StopSellingGoodsSummary.row(answersWithStopSellingGoods, waypoints, CheckYourAnswersPage)
        stopSellingGoodsRow mustBe defined

        val actualValue = stopSellingGoodsRow.value.value.content.asInstanceOf[Text].value

        val expectedValue = msgs("site.yes")

        actualValue mustBe expectedValue
      }
    }

    "must include StopSellingGoodsSummary row with 'site.no' when StopSellingGoodsPage is false" in {
      val answersWithStopSellingGoodsNo = answers
        .set(StopSellingGoodsPage, false).success.value
        .set(LeaveSchemePage, true).success.value
        .set(StoppedUsingServiceDatePage, today).success.value

      val application = applicationBuilder(userAnswers = Some(answersWithStopSellingGoodsNo))
        .build()

      running(application) {
        implicit val msgs: Messages = messages(application)
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val waypoints = EmptyWaypoints.setNextWaypoint(Waypoint(CheckYourAnswersPage, CheckMode, CheckYourAnswersPage.urlFragment))
        val list = SummaryListViewModel(
          Seq(
            StopSellingGoodsSummary.row(answersWithStopSellingGoodsNo, waypoints, CheckYourAnswersPage),
            StoppedSellingGoodsDateSummary.row(answersWithStopSellingGoodsNo, waypoints, CheckYourAnswersPage, date),
            StoppedUsingServiceDateSummary.row(answersWithStopSellingGoodsNo, waypoints, CheckYourAnswersPage, date)
          ).flatten
        )

        status(result) mustBe OK
        contentAsString(result) mustBe view(
          waypoints,
          list,
          isValid = true,
          appConfig.iossYourAccountUrl,
          clientName
        )(request, msgs).toString

        val stopSellingGoodsRow = StopSellingGoodsSummary.row(answersWithStopSellingGoodsNo, waypoints, CheckYourAnswersPage)
        stopSellingGoodsRow mustBe defined

        val actualValue = stopSellingGoodsRow.value.value.content.asInstanceOf[Text].value

        val expectedValue = msgs("site.no")

        actualValue mustBe expectedValue
      }
    }

    "must include StoppedSellingGoodsDateSummary row in the summary list when data is present" in {
      val testDate = LocalDate.of(2023, 12, 31)
      val answersWithStoppedSellingGoodsDate = answers
        .set(StoppedSellingGoodsDatePage, testDate).success.value

      val application = applicationBuilder(userAnswers = Some(answersWithStoppedSellingGoodsDate))
        .build()

      running(application) {
        implicit val msgs: Messages = messages(application)
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val waypoints = EmptyWaypoints.setNextWaypoint(Waypoint(CheckYourAnswersPage, CheckMode, CheckYourAnswersPage.urlFragment))
        val dates = application.injector.instanceOf[Dates]
        val list = SummaryListViewModel(
          Seq(
            StopSellingGoodsSummary.row(answersWithStoppedSellingGoodsDate, waypoints, CheckYourAnswersPage),
            StoppedSellingGoodsDateSummary.row(answersWithStoppedSellingGoodsDate, waypoints, CheckYourAnswersPage, dates),
            StoppedUsingServiceDateSummary.row(answersWithStoppedSellingGoodsDate, waypoints, CheckYourAnswersPage, dates)
          ).flatten
        )

        status(result) mustBe OK
        contentAsString(result) mustBe view(
          waypoints,
          list,
          isValid = true,
          appConfig.iossYourAccountUrl,
          clientName
        )(request, msgs).toString

        val stoppedSellingGoodsDateRow = StoppedSellingGoodsDateSummary.row(answersWithStoppedSellingGoodsDate, waypoints, CheckYourAnswersPage, dates)
        stoppedSellingGoodsDateRow mustBe defined

        val actualValue = stoppedSellingGoodsDateRow.value.value.content.asInstanceOf[Text].value
        val expectedValue = dates.formatter.format(testDate)

        actualValue mustBe expectedValue
      }
    }

    "must include StoppedUsingServiceDateSummary row in the summary list when data is present" in {
      val testDate = LocalDate.of(2023, 12, 31)
      val answersWithStoppedUsingServiceDate = answers
        .set(StoppedUsingServiceDatePage, testDate).success.value

      val application = applicationBuilder(userAnswers = Some(answersWithStoppedUsingServiceDate))
        .build()

      running(application) {
        implicit val msgs: Messages = messages(application)
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val waypoints = EmptyWaypoints.setNextWaypoint(Waypoint(CheckYourAnswersPage, CheckMode, CheckYourAnswersPage.urlFragment))
        val dates = application.injector.instanceOf[Dates]
        val list = SummaryListViewModel(
          Seq(
            StopSellingGoodsSummary.row(answersWithStoppedUsingServiceDate, waypoints, CheckYourAnswersPage),
            StoppedSellingGoodsDateSummary.row(answersWithStoppedUsingServiceDate, waypoints, CheckYourAnswersPage, dates),
            StoppedUsingServiceDateSummary.row(answersWithStoppedUsingServiceDate, waypoints, CheckYourAnswersPage, dates)
          ).flatten
        )

        status(result) mustBe OK
        contentAsString(result) mustBe view(
          waypoints,
          list,
          isValid = true,
          appConfig.iossYourAccountUrl,
          clientName
        )(request, msgs).toString

        val stoppedUsingServiceDateRow = StoppedUsingServiceDateSummary.row(answersWithStoppedUsingServiceDate, waypoints, CheckYourAnswersPage, dates)
        stoppedUsingServiceDateRow mustBe defined

        val actualValue = stoppedUsingServiceDateRow.value.value.content.asInstanceOf[Text].value
        val expectedValue = dates.formatter.format(testDate)

        actualValue mustBe expectedValue
      }
    }
  }

  ".onSubmit" - {

    "must redirect to the correct page" in {

      val mockRegistrationConnector = mock[RegistrationConnector]
      
      when(mockRegistrationConnector.amend(any())(any())) thenReturn
        Future.successful(Right(()))


      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(waypoints, incompletePrompt = false).url)
        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe ApplicationCompletePage.route(waypoints).url
      }
    }

    "when the user has not answered all necessary data" - {
      "the user is redirected when the incomplete prompt is shown" - {
        "to the Eu Country page when the EU country is missing" in {
          val userAnswers = answers.remove(StoppedSellingGoodsDatePage).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(waypoints, incompletePrompt = true).url)
            val result = route(application, request).value

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe controllers.routes.StoppedSellingGoodsDateController.onPageLoad(waypoints).url
          }
        }
      }
    }
  }
}
