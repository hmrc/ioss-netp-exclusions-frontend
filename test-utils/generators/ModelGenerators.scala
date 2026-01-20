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

package generators

import models.{Bic, Country, CountryWithValidationDetails, Iban, IntermediaryDetails}
import models.etmp.*
import models.etmp.amend.EtmpAmendRegistrationChangeLog
import models.etmp.display.{EtmpDisplayCustomerIdentification, EtmpDisplayEuRegistrationDetails, EtmpDisplayIntermediaryRegistration, EtmpDisplayRegistration, EtmpDisplaySchemeDetails}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.domain.Vrn

import java.time.{LocalDate, LocalDateTime}

trait ModelGenerators {

  private val maxEuTaxReferenceLength: Int = 20
  
  implicit lazy val arbitraryCountry: Arbitrary[Country] = {
    Arbitrary {
      Gen.oneOf(Country.euCountries)
    }
  }

  implicit lazy val arbitraryEtmpExclusion: Arbitrary[EtmpExclusion] = {
    Arbitrary {
      for {
        exclusionReason <- Gen.oneOf(EtmpExclusionReason.values)
        effectiveDate <- arbitrary[LocalDate]
        decisionDate <- arbitrary[LocalDate]
        quarantine <- arbitrary[Boolean]
      } yield {
        EtmpExclusion(
          exclusionReason = exclusionReason,
          effectiveDate = effectiveDate,
          decisionDate = decisionDate,
          quarantine = quarantine
        )
      }
    }
  }
  
  implicit lazy val arbitraryEtmpAdministration: Arbitrary[EtmpAdministration] = {
    Arbitrary {
      for {
        messageType <- Gen.oneOf(EtmpMessageType.values)
      } yield EtmpAdministration(messageType, "IOSSIntAmendClient")
    }
  }

  implicit lazy val arbitraryBic: Arbitrary[Bic] = {
    val asciiCodeForA = 65
    val asciiCodeForN = 78
    val asciiCodeForP = 80
    val asciiCodeForZ = 90

    Arbitrary {
      for {
        firstChars <- Gen.listOfN(6, Gen.alphaUpperChar).map(_.mkString)
        char7 <- Gen.oneOf(Gen.alphaUpperChar, Gen.choose(2, 9).map(_.toString.head))
        char8 <- Gen.oneOf(
          Gen.choose(asciiCodeForA, asciiCodeForN).map(_.toChar),
          Gen.choose(asciiCodeForP, asciiCodeForZ).map(_.toChar),
          Gen.choose(0, 9).map(_.toString.head)
        )
        lastChars <- Gen.option(Gen.listOfN(3, Gen.oneOf(Gen.alphaUpperChar, Gen.numChar)).map(_.mkString))
      } yield Bic(s"$firstChars$char7$char8${lastChars.getOrElse("")}").get
    }
  }
  
  implicit lazy val arbitraryIban: Arbitrary[Iban] = {
    Arbitrary {
      Gen.oneOf(
        "GB94BARC10201530093459",
        "GB33BUKB20201555555555",
        "DE29100100100987654321",
        "GB24BKEN10000031510604",
        "GB27BOFI90212729823529",
        "GB17BOFS80055100813796",
        "GB92BARC20005275849855",
        "GB66CITI18500812098709",
        "GB15CLYD82663220400952",
        "GB26MIDL40051512345674",
        "GB76LOYD30949301273801",
        "GB25NWBK60080600724890",
        "GB60NAIA07011610909132",
        "GB29RBOS83040210126939",
        "GB79ABBY09012603367219",
        "GB21SCBL60910417068859",
        "GB42CPBK08005470328725"
      ).map(v => Iban(v).toOption.get)
    }
  }


  implicit lazy val arbitraryEtmpBankDetails: Arbitrary[EtmpBankDetails] = {
    Arbitrary {
      for {
        accountName <- arbitrary[String]
        bic <- arbitraryBic.arbitrary
        iban <- arbitraryIban.arbitrary
      } yield {
        EtmpBankDetails(
          accountName = accountName,
          bic = Some(bic),
          iban = iban
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpAdminUse: Arbitrary[EtmpAdminUse] = {
    Arbitrary {
      for {
        changeDate <- arbitrary[LocalDateTime]
      } yield EtmpAdminUse(changeDate = Some(changeDate))
    }
  }

  implicit lazy val arbitraryEtmpClientDetails: Arbitrary[EtmpClientDetails] = {
    Arbitrary {
      for {
        clientName <- Gen.alphaStr
        clientIossID <- Gen.alphaNumStr
        clientExcluded <- arbitrary[Boolean]
      } yield {
        EtmpClientDetails(
          clientName = clientName,
          clientIossID = clientIossID,
          clientExcluded = clientExcluded
        )
      }
    }
  }

  implicit lazy val arbitraryVrn: Arbitrary[Vrn] = {
    Arbitrary {
      for {
        chars <- Gen.listOfN(9, Gen.numChar)
      } yield Vrn(chars.mkString(""))
    }
  }

  implicit val arbitraryIntermediaryDetails: Arbitrary[IntermediaryDetails] = {
    Arbitrary {
      for {
        intermediaryNumber <- Gen.alphaNumStr
        intermediaryName <- Gen.alphaStr
      } yield IntermediaryDetails(
        intermediaryNumber,
        intermediaryName
      )
    }
  }

  implicit lazy val arbitraryEtmpCustomerIdentification: Arbitrary[EtmpCustomerIdentification] = {
    Arbitrary {
      for {
        etmpIdType <- Gen.oneOf(EtmpIdType.values)
        vrn <- Gen.alphaStr
        intermediaryDetails <- arbitraryIntermediaryDetails.arbitrary
      } yield EtmpCustomerIdentification(etmpIdType, vrn, intermediaryDetails.intermediaryNumber)
    }
  }

  implicit lazy val arbitraryEtmpWebsite: Arbitrary[EtmpWebsite] = {
    Arbitrary {
      for {
        websiteAddress <- Gen.alphaStr
      } yield EtmpWebsite(websiteAddress)
    }
  }

  implicit lazy val arbitraryEtmpTradingName: Arbitrary[EtmpTradingName] = {
    Arbitrary {
      for {
        tradingName <- Gen.alphaStr
      } yield EtmpTradingName(tradingName)
    }
  }

  implicit lazy val arbitraryVatNumberTraderId: Arbitrary[VatNumberTraderId] = {
    Arbitrary {
      for {
        vatNumber <- Gen.alphaNumStr
      } yield VatNumberTraderId(vatNumber)
    }
  }

  implicit lazy val arbitraryEtmpEuRegistrationDetails: Arbitrary[EtmpEuRegistrationDetails] = {
    Arbitrary {
      for {
        countryOfRegistration <- arbitraryCountry.arbitrary.map(_.code)
        traderId <- arbitraryVatNumberTraderId.arbitrary
        tradingName <- arbitraryEtmpTradingName.arbitrary.map(_.tradingName)
        fixedEstablishmentAddressLine1 <- Gen.alphaStr
        fixedEstablishmentAddressLine2 <- Gen.alphaStr
        townOrCity <- Gen.alphaStr
        regionOrState <- Gen.alphaStr
        postcode <- Gen.alphaStr
      } yield {
        EtmpEuRegistrationDetails(
          countryOfRegistration = countryOfRegistration,
          traderId = traderId,
          tradingName = tradingName,
          fixedEstablishmentAddressLine1 = fixedEstablishmentAddressLine1,
          fixedEstablishmentAddressLine2 = Some(fixedEstablishmentAddressLine2),
          townOrCity = townOrCity,
          regionOrState = Some(regionOrState),
          postcode = Some(postcode)
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpSchemeDetails: Arbitrary[EtmpSchemeDetails] = {
    Arbitrary {
      for {
        commencementDate <- arbitrary[LocalDate].map(_.toString)
        euRegistrationDetails <- Gen.listOfN(3, arbitraryEtmpEuRegistrationDetails.arbitrary)
        contactName <- Gen.alphaStr
        businessTelephoneNumber <- Gen.alphaNumStr
        businessEmailId <- Gen.alphaStr
        nonCompliant <- Gen.oneOf("1", "2")
      } yield {
        EtmpSchemeDetails(
          commencementDate = commencementDate,
          euRegistrationDetails = euRegistrationDetails,
          contactName = contactName,
          businessTelephoneNumber = businessTelephoneNumber,
          businessEmailId = businessEmailId,
          nonCompliantReturns = Some(nonCompliant),
          nonCompliantPayments = Some(nonCompliant),
          previousEURegistrationDetails = Seq.empty,
          websites = None
        )
      }
    }
  }

  implicit lazy val genIntermediaryNumber: Gen[String] = {
    for {
      intermediaryNumber <- Gen.listOfN(12, Gen.alphaChar).map(_.mkString)
    } yield intermediaryNumber
  }

  implicit lazy val arbitraryEtmpPreviousEuRegistrationDetails: Arbitrary[EtmpPreviousEuRegistrationDetails] = {
    Arbitrary {
      for {
        issuedBy <- arbitraryCountry.arbitrary.map(_.code)
        registrationNumber <- arbitrary[String]
        schemeType <- Gen.oneOf(SchemeType.values)
        intermediaryNumber <- genIntermediaryNumber
      } yield {
        EtmpPreviousEuRegistrationDetails(
          issuedBy = issuedBy,
          registrationNumber = registrationNumber,
          schemeType = schemeType,
          intermediaryNumber = Some(intermediaryNumber)
        )
      }
    }
  }

  implicit lazy val arbitraryOtherIossIntermediaryRegistrations: Arbitrary[EtmpOtherIossIntermediaryRegistrations] = {
    Arbitrary {
      for {
        issuedBy <- arbitraryCountry.arbitrary.map(_.code)
        intermediaryNumber <- genIntermediaryNumber
      } yield {
        EtmpOtherIossIntermediaryRegistrations(
          issuedBy = issuedBy,
          intermediaryNumber = intermediaryNumber
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpOtherAddress: Arbitrary[EtmpOtherAddress] = {
    Arbitrary {
      for {
        issuedBy <- Gen.listOfN(2, Gen.alphaChar).map(_.mkString)
        tradingName <- Gen.listOfN(20, Gen.alphaChar).map(_.mkString)
        addressLine1 <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        addressLine2 <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        townOrCity <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        regionOrState <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        postcode <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
      } yield EtmpOtherAddress(
        issuedBy,
        Some(tradingName),
        addressLine1,
        Some(addressLine2),
        townOrCity,
        Some(regionOrState),
        Some(postcode)
      )
    }
  }

  implicit lazy val arbitrarySchemeType: Arbitrary[SchemeType] = {
    Arbitrary {
      Gen.oneOf(SchemeType.values)
    }
  }

  implicit lazy val arbitraryEtmpOtherIossIntermediaryRegistrations: Arbitrary[EtmpOtherIossIntermediaryRegistrations] = {
    Arbitrary {
      for {
        countryCode <- Gen.listOfN(2, Gen.alphaChar).map(_.mkString)
        intermediaryNumber <- Gen.listOfN(12, Gen.alphaChar).map(_.mkString)
      } yield EtmpOtherIossIntermediaryRegistrations(countryCode, intermediaryNumber)
    }
  }

  implicit lazy val arbitraryEtmpIntermediaryDetails: Arbitrary[EtmpIntermediaryDetails] = {
    Arbitrary {
      for {
        otherRegistrationDetails <- Gen.listOfN(2, arbitraryEtmpOtherIossIntermediaryRegistrations.arbitrary)
      } yield EtmpIntermediaryDetails(otherRegistrationDetails)
    }
  }

  implicit lazy val arbitraryEtmpAmendRegistrationChangeLog: Arbitrary[EtmpAmendRegistrationChangeLog] = {
    Arbitrary {
      for {
        tradingNames <- arbitrary[Boolean]
        fixedEstablishments <- arbitrary[Boolean]
        contactDetails <- arbitrary[Boolean]
        bankDetails <- arbitrary[Boolean]
        reRegistration <- arbitrary[Boolean]
        otherAddress <- arbitrary[Boolean]
      } yield EtmpAmendRegistrationChangeLog(tradingNames, fixedEstablishments, contactDetails, bankDetails, reRegistration, otherAddress)
    }
  }

  implicit lazy val arbitraryEuVatNumber: Gen[String] = {
    for {
      countryCode <- Gen.oneOf(Country.euCountries.map(_.code))
      matchedCountryRule = CountryWithValidationDetails.euCountriesWithVRNValidationRules.find(_.country.code == countryCode).head
    } yield s"$countryCode${matchedCountryRule.exampleVrn}"
  }

  implicit lazy val genEuTaxReference: Gen[String] = {
    Gen.listOfN(maxEuTaxReferenceLength, Gen.alphaNumChar).map(_.mkString)
  }


  implicit lazy val arbitraryEtmpDisplayEuRegistrationDetails: Arbitrary[EtmpDisplayEuRegistrationDetails] = {
    Arbitrary {
      for {
        issuedBy <- arbitraryCountry.arbitrary.map(_.code)
        vatNumber <- arbitraryEuVatNumber
        taxIdentificationNumber <- genEuTaxReference
        fixedEstablishmentTradingName <- arbitraryEtmpTradingName.arbitrary.map(_.tradingName)
        fixedEstablishmentAddressLine1 <- Gen.alphaStr
        fixedEstablishmentAddressLine2 <- Gen.alphaStr
        townOrCity <- Gen.alphaStr
        regionOrState <- Gen.alphaStr
        postcode <- Gen.alphaStr
      } yield {
        EtmpDisplayEuRegistrationDetails(
          issuedBy = issuedBy,
          vatNumber = Some(vatNumber),
          taxIdentificationNumber = Some(taxIdentificationNumber),
          fixedEstablishmentTradingName = fixedEstablishmentTradingName,
          fixedEstablishmentAddressLine1 = fixedEstablishmentAddressLine1,
          fixedEstablishmentAddressLine2 = Some(fixedEstablishmentAddressLine2),
          townOrCity = townOrCity,
          regionOrState = Some(regionOrState),
          postcode = Some(postcode)
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpDisplaySchemeDetails: Arbitrary[EtmpDisplaySchemeDetails] = {
    Arbitrary {
      for {
        commencementDate <- arbitrary[LocalDate].map(_.toString)
        euRegistrationDetails <- Gen.listOfN(3, arbitraryEtmpDisplayEuRegistrationDetails.arbitrary)
        contactName <- Gen.alphaStr
        businessTelephoneNumber <- Gen.alphaNumStr
        businessEmailId <- Gen.alphaStr
        unusableStatus <- arbitrary[Boolean]
        nonCompliant <- Gen.oneOf("1", "2")
        previousEURegistrationDetails <- Gen.listOfN(3, arbitraryEtmpPreviousEuRegistrationDetails.arbitrary)
        websites <- Gen.listOfN(3, arbitraryEtmpWebsite.arbitrary)
      } yield {
        EtmpDisplaySchemeDetails(
          commencementDate = commencementDate,
          euRegistrationDetails = euRegistrationDetails,
          contactName = contactName,
          businessTelephoneNumber = businessTelephoneNumber,
          businessEmailId = businessEmailId,
          unusableStatus = unusableStatus,
          nonCompliantReturns = Some(nonCompliant),
          nonCompliantPayments = Some(nonCompliant),
          previousEURegistrationDetails = previousEURegistrationDetails,
          websites = websites
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpDisplayCustomerIdentification: Arbitrary[EtmpDisplayCustomerIdentification] =
    Arbitrary {
      for {
        etmpIdType <- Gen.oneOf(EtmpIdType.values)
        vrn <- Gen.alphaStr
      } yield EtmpDisplayCustomerIdentification(etmpIdType, vrn)
    }
  
  implicit lazy val arbitraryEtmpDisplayRegistration: Arbitrary[EtmpDisplayRegistration] = {
    Arbitrary {
      for {
        customerIdentification <- arbitraryEtmpDisplayCustomerIdentification.arbitrary
        tradingNames <- Gen.listOfN(3, arbitraryEtmpTradingName.arbitrary)
        otherAddress <- arbitraryEtmpOtherAddress.arbitrary
        schemeDetails <- arbitraryEtmpDisplaySchemeDetails.arbitrary
        exclusions <- Gen.listOfN(1, arbitraryEtmpExclusion.arbitrary)
        bankDetails <- arbitraryEtmpBankDetails.arbitrary
        adminUse <- arbitraryEtmpAdminUse.arbitrary
      } yield {
        EtmpDisplayRegistration(
          customerIdentification = customerIdentification,
          tradingNames = tradingNames,
          otherAddress = Some(otherAddress),
          schemeDetails = schemeDetails,
          exclusions = exclusions,
          adminUse = adminUse
        )
      }
    }
  }
  
  implicit lazy val arbitraryEtmpDisplayIntermediaryRegistration: Arbitrary[EtmpDisplayIntermediaryRegistration] = {
    Arbitrary {
      for {
        clientDetails <- Gen.listOfN(3, arbitraryEtmpClientDetails.arbitrary)
      } yield {
        EtmpDisplayIntermediaryRegistration(
          clientDetails = clientDetails
        )
      }
    }
  }
}
