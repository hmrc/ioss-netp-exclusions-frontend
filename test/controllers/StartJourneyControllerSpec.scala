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

import base.SpecBase
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages.CannotUseClientExcludedOrNotClientPage
import play.api.inject.bind
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ClientDetailService
import utils.FutureSyntax.FutureOps

class StartJourneyControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val mockClientDetailService: ClientDetailService = mock[ClientDetailService]

  private val iossNumber: String = "IM9001234567"

  override def beforeEach(): Unit = {
    Mockito.reset(mockClientDetailService)
  }
  
  "Start Journey Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockClientDetailService.checkIntermediaryHasClient(any(), any())(any())) thenReturn None.toFuture

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[ClientDetailService].toInstance(mockClientDetailService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.StartJourneyController.onPageLoad(iossNumber).url)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER

        redirectLocation(result).value `mustBe` routes.StopSellingGoodsController.onPageLoad(waypoints).url
        verify(mockClientDetailService, times(1)).checkIntermediaryHasClient(any(), eqTo(iossNumber))(any())
      }
    }

    "must redirect to Cannot Use Client Excluded Or Not Client page when Client Detail Service returns a redirect result " in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[ClientDetailService].toInstance(mockClientDetailService))
        .build()

      running(application) {
        val redirectResult: Result = Redirect(CannotUseClientExcludedOrNotClientPage.route(waypoints).url)

        when(mockClientDetailService.checkIntermediaryHasClient(any(), any())(any())) thenReturn Some(redirectResult).toFuture

        val request = FakeRequest(GET, routes.StartJourneyController.onPageLoad(iossNumber).url)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER

        redirectLocation(result).value `mustBe` CannotUseClientExcludedOrNotClientPage.route(waypoints).url
        verify(mockClientDetailService, times(1)).checkIntermediaryHasClient(any(), eqTo(iossNumber))(any())
      }
    }
  }
}
