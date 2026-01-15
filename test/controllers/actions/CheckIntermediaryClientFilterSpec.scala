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

package controllers.actions

import base.SpecBase
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages.CannotUseClientExcludedOrNotClientPage
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import services.ClientDetailService
import utils.FutureSyntax.FutureOps

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckIntermediaryClientFilterSpec extends SpecBase with BeforeAndAfterEach {

  private val mockClientDetailService: ClientDetailService = mock[ClientDetailService]

  class Harness extends CheckIntermediaryClientFilterImpl(mockClientDetailService) {
    def callFilter(request: DataRequest[_]): Future[Option[Result]] = filter(request)
  }

  override def beforeEach(): Unit = {
    Mockito.reset(mockClientDetailService)
  }

  "CheckIntermediaryClientFilter" - {

    "must return None and allow users to access the service when Client Detail Service invocation returns None" in {

      when(mockClientDetailService.checkIntermediaryHasClient(any(), any())(any())) thenReturn None.toFuture

      val request = DataRequest(FakeRequest(), userAnswersId, emptyUserAnswers, iossNumber, etmpDisplayRegistration, intermediaryNumber)
      val controller = new Harness()

      val result = controller.callFilter(request).futureValue
      result mustBe None
      verify(mockClientDetailService, times(1)).checkIntermediaryHasClient(eqTo(intermediaryNumber), eqTo(iossNumber))(any())
    }

    "must return a redirect result and deny users access to the service when Client Detail Service invocation returns None" in {

      lazy val redirectResult: Result = Redirect(CannotUseClientExcludedOrNotClientPage.route(waypoints).url)

      when(mockClientDetailService.checkIntermediaryHasClient(any(), any())(any())) thenReturn Some(redirectResult).toFuture

      val request = DataRequest(FakeRequest(), userAnswersId, emptyUserAnswers, iossNumber, etmpDisplayRegistration, intermediaryNumber)
      val controller = new Harness()

      val result = controller.callFilter(request).futureValue
      result mustBe Some(redirectResult)
      verify(mockClientDetailService, times(1)).checkIntermediaryHasClient(eqTo(intermediaryNumber), eqTo(iossNumber))(any())
    }
  }
}
