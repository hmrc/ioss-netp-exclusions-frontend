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

package services

import base.SpecBase
import connectors.RegistrationConnector
import models.etmp.*
import models.etmp.EtmpExclusionReason.{NoLongerSupplies, VoluntarilyLeaves}
import models.etmp.amend.{EtmpAmendCustomerIdentification, EtmpAmendRegistrationChangeLog, EtmpExclusionDetails}
import models.etmp.display.{EtmpDisplayEuRegistrationDetails, EtmpDisplaySchemeDetails}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.{StoppedSellingGoodsDatePage, StoppedUsingServiceDatePage}
import play.api.test.Helpers.running
import testutils.RegistrationData.{etmpAmendRegistrationRequest, etmpDisplayRegistration}
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global


class RegistrationServiceSpec extends SpecBase with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]
  private val mockAuditService: AuditService = mock[AuditService]
  private val registrationService = new RegistrationService(stubClockAtArbitraryDate, mockRegistrationConnector, mockAuditService)

  private val expectedChangeLog = EtmpAmendRegistrationChangeLog(
    tradingNames = false,
    fixedEstablishments = false,
    contactDetails = false,
    bankDetails = false,
    reRegistration = false,
    otherAddress = false
  )

  override def beforeEach(): Unit = {
    reset(mockRegistrationConnector)
  }

  private def buildExpectedAmendRequest(etmpChangeLog: EtmpAmendRegistrationChangeLog, etmpExclusionsDetails: EtmpExclusionDetails) = {
    etmpAmendRegistrationRequest.copy(
      changeLog = etmpChangeLog,
      exclusionDetails = Some(etmpExclusionsDetails),
      customerIdentification = EtmpAmendCustomerIdentification(iossNumber),
      tradingNames = etmpDisplayRegistration.tradingNames,
      schemeDetails = buildSchemeDetailsFromDisplay(etmpDisplayRegistration.schemeDetails),
      otherAddress = etmpDisplayRegistration.otherAddress
    )
  }

  private def buildSchemeDetailsFromDisplay(etmpDisplaySchemeDetails: EtmpDisplaySchemeDetails): EtmpSchemeDetails = {
    EtmpSchemeDetails(
      commencementDate = etmpDisplaySchemeDetails.commencementDate,
      euRegistrationDetails = etmpDisplaySchemeDetails.euRegistrationDetails.map(buildEuRegistrationDetails),
      previousEURegistrationDetails = etmpDisplaySchemeDetails.previousEURegistrationDetails,
      websites = Some(etmpDisplaySchemeDetails.websites),
      contactName = etmpDisplaySchemeDetails.contactName,
      businessTelephoneNumber = etmpDisplaySchemeDetails.businessTelephoneNumber,
      businessEmailId = etmpDisplaySchemeDetails.businessEmailId,
      nonCompliantReturns = etmpDisplaySchemeDetails.nonCompliantReturns,
      nonCompliantPayments = etmpDisplaySchemeDetails.nonCompliantPayments,
    )
  }

  private def buildEuRegistrationDetails(euDisplayRegistrationDetails: EtmpDisplayEuRegistrationDetails): EtmpEuRegistrationDetails = {
    EtmpEuRegistrationDetails(
      countryOfRegistration = euDisplayRegistrationDetails.issuedBy,
      traderId = buildTraderId(euDisplayRegistrationDetails.vatNumber, euDisplayRegistrationDetails.taxIdentificationNumber),
      tradingName = euDisplayRegistrationDetails.fixedEstablishmentTradingName,
      fixedEstablishmentAddressLine1 = euDisplayRegistrationDetails.fixedEstablishmentAddressLine1,
      fixedEstablishmentAddressLine2 = euDisplayRegistrationDetails.fixedEstablishmentAddressLine2,
      townOrCity = euDisplayRegistrationDetails.townOrCity,
      regionOrState = euDisplayRegistrationDetails.regionOrState,
      postcode = euDisplayRegistrationDetails.postcode
    )
  }

  private def buildTraderId(maybeVatNumber: Option[String], maybeTaxIdentificationNumber: Option[String]): TraderId = {
    (maybeVatNumber, maybeTaxIdentificationNumber) match {
      case (Some(vatNumber), _) => VatNumberTraderId(vatNumber)
      case (_, Some(taxIdentificationNumber)) => TaxRefTraderID(taxIdentificationNumber)
      case _ => throw new IllegalStateException("Neither vat number nor tax id were provided")
    }
  }

  ".amendRegistration" - {

    "when voluntarily leaves" - {

      "must create registration request and return a successful ETMP enrolment response" in {

        val amendRegistrationResponse =
          Right(())

        val stoppedUsingServiceDate = LocalDate.of(2023, 10, 4)

        val expectedExclusionDetails = EtmpExclusionDetails(
          revertExclusion = false,
          noLongerSupplyGoods = false,
          noLongerEligible = false,
          partyType = "NETP",
          exclusionRequestDate = Some(stoppedUsingServiceDate),
          identificationValidityDate = Some(stoppedUsingServiceDate),
          intExclusionRequestDate = None,
          newMemberState = None,
          establishedMemberState = None
        )

        val userAnswers = emptyUserAnswers
          .set(StoppedUsingServiceDatePage, stoppedUsingServiceDate).success.value

        val expectedAmendedRegistrationRequest = buildExpectedAmendRequest(expectedChangeLog, expectedExclusionDetails)

        when(mockRegistrationConnector.amend(any())(any())) thenReturn amendRegistrationResponse.toFuture

        val app = applicationBuilder()
          .build()

        running(app) {

          registrationService.amendRegistration(
            answers = userAnswers,
            exclusionReason = Some(VoluntarilyLeaves),
            iossNumber = iossNumber,
            registration = etmpDisplayRegistration
          ).futureValue mustBe amendRegistrationResponse
          verify(mockRegistrationConnector, times(1)).amend(eqTo(expectedAmendedRegistrationRequest))(any())
        }

      }
    }

    "when no longer supplying goods" - {

      "must create registration request and return a successful ETMP enrolment response" in {

        val amendRegistrationResponse =
          Right(())

        val stoppedSellingGoodsDate = LocalDate.of(2023, 10, 4)

        val expectedExclusionDetails = EtmpExclusionDetails(
          revertExclusion = false,
          noLongerSupplyGoods = true,
          noLongerEligible = false,
          partyType = "NETP",
          exclusionRequestDate = Some(stoppedSellingGoodsDate),
          identificationValidityDate = None,
          intExclusionRequestDate = None,
          newMemberState = None,
          establishedMemberState = None
        )

        val userAnswers = emptyUserAnswers
          .set(StoppedSellingGoodsDatePage, stoppedSellingGoodsDate).success.value

        val expectedAmendedRegistrationRequest = buildExpectedAmendRequest(expectedChangeLog, expectedExclusionDetails)

        when(mockRegistrationConnector.amend(any())(any())) thenReturn amendRegistrationResponse.toFuture

        val app = applicationBuilder()
          .build()

        running(app) {

          registrationService.amendRegistration(
            answers = userAnswers,
            exclusionReason = Some(NoLongerSupplies),
            iossNumber = iossNumber,
            registration = etmpDisplayRegistration
          ).futureValue mustBe amendRegistrationResponse
          verify(mockRegistrationConnector, times(1)).amend(eqTo(expectedAmendedRegistrationRequest))(any())
        }

      }
    }

  }
}
