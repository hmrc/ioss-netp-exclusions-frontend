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

import connectors.RegistrationConnector
import connectors.RegistrationConnectorHttpParser.AmendRegistrationResultResponse
import models.UserAnswers
import models.etmp.EtmpExclusionReason.*
import models.etmp.EtmpMessageType.IOSSIntAmendClient
import models.etmp.{EtmpAdministration, EtmpEuRegistrationDetails, EtmpExclusionReason, EtmpSchemeDetails, TaxRefTraderID, TraderId, VatNumberTraderId}
import models.etmp.amend.{EtmpAmendCustomerIdentification, EtmpAmendRegistrationChangeLog, EtmpAmendRegistrationRequest, EtmpExclusionDetails}
import models.etmp.display.{EtmpDisplayEuRegistrationDetails, EtmpDisplayRegistration, EtmpDisplaySchemeDetails}
import pages.{StoppedSellingGoodsDatePage, StoppedUsingServiceDatePage}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.Future

class RegistrationService @Inject()(clock: Clock, registrationConnector: RegistrationConnector) {

  def amendRegistration(
                         answers: UserAnswers,
                         exclusionReason: Option[EtmpExclusionReason],
                         iossNumber: String,
                        registration: EtmpDisplayRegistration
                       )(implicit hc: HeaderCarrier): Future[AmendRegistrationResultResponse] = {

    registrationConnector.amend(buildEtmpAmendRegistrationRequest(
      answers, exclusionReason, registration, iossNumber
    ))
  }


  private def buildEtmpAmendRegistrationRequest(
                                                 answers: UserAnswers,
                                                 exclusionReason: Option[EtmpExclusionReason],
                                                 registration: EtmpDisplayRegistration,
                                                 iossNumber: String
                                               ): EtmpAmendRegistrationRequest = {

    EtmpAmendRegistrationRequest(
      administration = EtmpAdministration(messageType = IOSSIntAmendClient),
      changeLog = EtmpAmendRegistrationChangeLog(
        tradingNames = false,
        fixedEstablishments = false,
        contactDetails = false,
        bankDetails = false,
        reRegistration = exclusionReason.isEmpty,
        otherAddress = false
      ),
      exclusionDetails = exclusionReason.map(getExclusionDetailsForType(_, answers)),
      customerIdentification = EtmpAmendCustomerIdentification(iossNumber),
      tradingNames = registration.tradingNames,
      intermediaryDetails = registration.intermediaryDetails,
      otherAddress = registration.otherAddress,
      schemeDetails = buildSchemeDetailsFromDisplay(registration.schemeDetails),
      bankDetails = registration.bankDetails
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
      nonCompliantPayments = etmpDisplaySchemeDetails.nonCompliantPayments
    )
  }

  private def getExclusionDetailsForType(exclusionReason: EtmpExclusionReason, answers: UserAnswers): EtmpExclusionDetails = {
    exclusionReason match {
      case NoLongerSupplies => getExclusionDetailsForNoLongerSupplies(answers)
      case VoluntarilyLeaves => getExclusionDetailsForVoluntarilyLeaves(answers)
      case Reversal => getExclusionDetailsForReversal
      case _ => throw new Exception("Exclusion reason not valid")
    }
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

  private def getExclusionDetailsForVoluntarilyLeaves(answers: UserAnswers): EtmpExclusionDetails = {

    val stoppedUsingServiceDate = answers.get(StoppedUsingServiceDatePage).getOrElse(throw new Exception("No stopped using service date provided"))

    EtmpExclusionDetails(
      revertExclusion = false,
      noLongerSupplyGoods = false,
      noLongerEligible = false,
      partyType = "NETP",
      exclusionRequestDate = Some(stoppedUsingServiceDate),
      identificationValidityDate = None,
      intExclusionRequestDate = None,
      newMemberState = None,
      establishedMemberState = None
    )
  }

  private def getExclusionDetailsForNoLongerSupplies(answers: UserAnswers): EtmpExclusionDetails = {

    val stoppedSellingGoodsDate = answers.get(StoppedSellingGoodsDatePage).getOrElse(throw new Exception("No stopped selling goods date provided"))

    EtmpExclusionDetails(
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
  }



  private def getExclusionDetailsForReversal: EtmpExclusionDetails = {
    EtmpExclusionDetails(
      revertExclusion = true,
      noLongerSupplyGoods = false,
      noLongerEligible = false,
      partyType = "NETP",
      exclusionRequestDate = Some(LocalDate.now(clock)),
      identificationValidityDate = None,
      intExclusionRequestDate = None,
      newMemberState = None,
      establishedMemberState = None
    )
  }
}
