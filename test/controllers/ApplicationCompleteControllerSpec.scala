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
import date.Today
import models.requests.DataRequest
import org.mockito.Mockito.when
import pages.{LeaveSchemePage, StopSellingGoodsPage, StoppedSellingGoodsDatePage, StoppedUsingServiceDatePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ClientDetailService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ApplicationCompleteView

import java.time.LocalDate
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class ApplicationCompleteControllerSpec extends SpecBase {

  val today: LocalDate = LocalDate.of(2024, 1, 25)
  val mockToday: Today = mock[Today]
  when(mockToday.date).thenReturn(today)

  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]

  private val mockClientDetailService: ClientDetailService = new ClientDetailService(mockRegistrationConnector) {
    override def getClientName(implicit request: DataRequest[_], hc: HeaderCarrier): Future[String] =
      Future.successful(clientName)
  }

  "ApplicationComplete Controller" - {

    "when someone stops selling goods" - {

      "must return OK with the leave date being end of the month (31st Jan) " +
        "when stopping at least 15 days prior to the end of the month (16th Jan)" in {

        val stoppedSellingGoodsDate = LocalDate.of(2024, 1, 16)

        val userAnswers = emptyUserAnswers
          .set(StopSellingGoodsPage, true).success.get
          .set(StoppedSellingGoodsDatePage, stoppedSellingGoodsDate).success.get

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Today].toInstance(mockToday),
            bind[ClientDetailService].toInstance(mockClientDetailService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ApplicationCompleteView]

          val config = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK
          val maxChangeDate = "1 February 2024"
          val vatReturnDate = "January 2024"

          contentAsString(result) mustEqual view(
            config.iossYourAccountUrl,
            clientName,
            maxChangeDate,
            vatReturnDate
          )(request, messages(application)).toString
        }
      }

      "must return OK with the leave date being end of the month (31st Jan) " +
        "when stopping after today (26th Jan)" in {

        val stoppedSellingGoodsDate = LocalDate.of(2024, 1, 26)

        val userAnswers = emptyUserAnswers
          .set(StopSellingGoodsPage, true).success.get
          .set(StoppedSellingGoodsDatePage, stoppedSellingGoodsDate).success.get

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Today].toInstance(mockToday),
            bind[ClientDetailService].toInstance(mockClientDetailService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ApplicationCompleteView]

          val config = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK
          val maxChangeDate = "1 February 2024"
          val vatReturnDate = "January 2024"

          contentAsString(result) mustEqual view(
            config.iossYourAccountUrl,
            clientName,
            maxChangeDate,
            vatReturnDate
          )(request, messages(application)).toString
        }
      }
    }

    "when someone stops using the service" - {

      "must return OK with the leave date being 1st day of next month (1st Feb) " +
        "when stopping at least 15 days prior to the end of the month (16th Jan)" in {

        val stoppedUsingServiceDate = LocalDate.of(2024, 1, 16)

        val userAnswers = emptyUserAnswers
          .set(StopSellingGoodsPage, false).success.get
          .set(LeaveSchemePage, true).success.get
          .set(StoppedUsingServiceDatePage, stoppedUsingServiceDate).success.get

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Today].toInstance(mockToday),
            bind[ClientDetailService].toInstance(mockClientDetailService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ApplicationCompleteView]

          val config = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK
          val maxChangeDate = "1 February 2024"
          val vatReturnDate = "January 2024"
          contentAsString(result) mustEqual view(
            config.iossYourAccountUrl,
            clientName,
            maxChangeDate,
            vatReturnDate
          )(request, messages(application)).toString
        }
      }

      "must return OK with the leave date being 1st day of the following month (1st March) " +
        "when stopping less than 15 days prior to the end of the month (17th Jan)" in {

        val stoppedUsingServiceDate = LocalDate.of(2024, 1, 17)

        val userAnswers = emptyUserAnswers
          .set(StopSellingGoodsPage, false).success.get
          .set(LeaveSchemePage, true).success.get
          .set(StoppedUsingServiceDatePage, stoppedUsingServiceDate).success.get

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Today].toInstance(mockToday),
            bind[ClientDetailService].toInstance(mockClientDetailService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ApplicationCompleteView]

          val config = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK
          val maxChangeDate = "1 March 2024"
          val vatReturnDate = "January 2024"
          contentAsString(result) mustEqual view(
            config.iossYourAccountUrl,
            clientName,
            maxChangeDate,
            vatReturnDate
          )(request, messages(application)).toString
        }
      }
    }

    "must redirect to JourneyRecoveryController when data is missing" in {

      val userAnswers = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

