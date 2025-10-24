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

package services

import base.SpecBase
import connectors.RegistrationConnector
import models.etmp.*
import models.etmp.display.{EtmpDisplayIntermediaryRegistration, IntermediaryRegistrationWrapper}
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.Results.InternalServerError
import testutils.RegistrationData.etmpDisplayRegistration
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}


class ClientDetailServiceSpec extends SpecBase with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]
  private val clientDetailService = new ClientDetailService(mockRegistrationConnector)()

  private val otherClientName = "Gordons Alive"
  private val otherIossNumber = "IM9000000000"

  override def beforeEach(): Unit = {
    reset(mockRegistrationConnector)
  }

  ".getClientName" - {

    "must return the client's name when found" in {

      val clientDetails = Seq(
        EtmpClientDetails(clientName = clientName, clientIossID = iossNumber, clientExcluded = false)
      )

      val response = Right(
        IntermediaryRegistrationWrapper(
          EtmpDisplayIntermediaryRegistration(clientDetails)
        )
      )

      implicit val request: DataRequest[_] =
        DataRequest(
          request = fakeRequest,
          userId = "id",
          userAnswers = emptyUserAnswers,
          iossNumber = iossNumber,
          displayNetpRegistration = etmpDisplayRegistration,
          intermediaryNumber = intermediaryNumber
        )

      when(mockRegistrationConnector.displayIntermediaryRegistration(eqTo(intermediaryNumber))(any()))
        .thenReturn(Future.successful(response))

      val result = clientDetailService.getClientName.futureValue

      result mustBe clientName
    }

    "must return 'Unknown client' when no matching client IOSS ID is found" in {

      val clientDetails = Seq(
        EtmpClientDetails(clientName = otherClientName, clientIossID = otherIossNumber, clientExcluded = false)
      )

      val response = Right(
        IntermediaryRegistrationWrapper(
          EtmpDisplayIntermediaryRegistration(clientDetails)
        )
      )

      implicit val request: DataRequest[_] =
        DataRequest(
          request = fakeRequest,
          userId = "id",
          userAnswers = emptyUserAnswers,
          iossNumber = iossNumber,
          displayNetpRegistration = etmpDisplayRegistration,
          intermediaryNumber = intermediaryNumber
        )

      when(mockRegistrationConnector.displayIntermediaryRegistration(eqTo(intermediaryNumber))(any()))
        .thenReturn(Future.successful(response))

      val result = clientDetailService.getClientName.futureValue

      result mustBe "Unknown client"

    }

    "must return 'Unknown client' when connector returns an error" in {

      val response = Left(InternalServerError)

      implicit val request: DataRequest[_] =
        DataRequest(
          request = fakeRequest,
          userId = "id",
          userAnswers = emptyUserAnswers,
          iossNumber = iossNumber,
          displayNetpRegistration = etmpDisplayRegistration,
          intermediaryNumber = intermediaryNumber
        )

      when(mockRegistrationConnector.displayIntermediaryRegistration(eqTo(intermediaryNumber))(any()))
        .thenReturn(Future.successful(response))

      val result = clientDetailService.getClientName.futureValue

      result mustBe "Unknown client"
    }

    "must return 'Unknown client' when connector throws an exception" in {

      implicit val request: DataRequest[_] =
        DataRequest(
          request = fakeRequest,
          userId = "id",
          userAnswers = emptyUserAnswers,
          iossNumber = iossNumber,
          displayNetpRegistration = etmpDisplayRegistration,
          intermediaryNumber = intermediaryNumber
        )

      when(mockRegistrationConnector.displayIntermediaryRegistration(eqTo(intermediaryNumber))(any()))
        .thenReturn(Future.failed(new RuntimeException("Failed to retrieve registration")))

      val result = clientDetailService.getClientName.futureValue

      result mustBe "Unknown client"
    }

  }
}
