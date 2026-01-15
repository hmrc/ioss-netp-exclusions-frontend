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

package models.etmp.amend

import base.SpecBase
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate

class EtmpExclusionDetailsSpec extends SpecBase {

  private val revertExclusion = true
  private val noLongerSupplyGoods = false
  private val noLongerEligible = false
  private val partyType = "NETP"
  private val exclusionRequestDate = Some(LocalDate.of(2023, 1, 1))
  private val identificationValidityDate = Some(LocalDate.of(2024, 1, 1))
  private val intExclusionRequestDate = None
  private val newMemberState = Some(EtmpNewMemberState(
    newMemberState = true,
    ceaseSpecialSchemeDate = Some(LocalDate.of(2023, 1, 1)),
    ceaseFixedEstDate = Some(LocalDate.of(2024, 1, 1)),
    movePOBDate = Some(LocalDate.of(2024, 1, 1)),
    issuedBy = Some("HMRC"),
    vatNumber = Some("GB123456789")
  ))

  "EtmpExclusionDetails" - {

    "must deserialise/serialise to and from EtmpExclusionDetails" - {

      "when all optional values are present" in {

        val json = Json.obj(
          "exclusionRequestDate" -> exclusionRequestDate.map(_.toString),
          "newMemberState" -> Json.obj(
            "ceaseFixedEstDate" -> identificationValidityDate.map(_.toString),
            "newMemberState" -> true,
            "vatNumber" -> "GB123456789",
            "ceaseSpecialSchemeDate" -> exclusionRequestDate.map(_.toString),
            "movePOBDate" -> LocalDate.of(2024, 1, 1).toString,
            "issuedBy" -> "HMRC",
          ),
          "identificationValidityDate" -> identificationValidityDate.map(_.toString),
          "noLongerSupplyGoods" -> noLongerSupplyGoods,
          "noLongerEligible" -> noLongerEligible,
          "revertExclusion" -> revertExclusion,
          "partyType" -> partyType,
        )

        val expectedResult = EtmpExclusionDetails(
          revertExclusion = revertExclusion,
          noLongerSupplyGoods = noLongerSupplyGoods,
          noLongerEligible = noLongerEligible,
          partyType = partyType,
          exclusionRequestDate = exclusionRequestDate,
          identificationValidityDate = identificationValidityDate,
          intExclusionRequestDate = intExclusionRequestDate,
          newMemberState = newMemberState,
          establishedMemberState = None
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[EtmpExclusionDetails] mustBe JsSuccess(expectedResult)
      }

      "when all optional values are absent" in {

        val json = Json.obj(
          "revertExclusion" -> revertExclusion,
          "noLongerSupplyGoods" -> noLongerSupplyGoods,
          "noLongerEligible" -> noLongerEligible,
          "partyType" -> partyType
        )

        val expectedResult = EtmpExclusionDetails(
          revertExclusion = revertExclusion,
          noLongerSupplyGoods = noLongerSupplyGoods,
          noLongerEligible = noLongerEligible,
          partyType = partyType,
          exclusionRequestDate = None,
          identificationValidityDate = None,
          intExclusionRequestDate = None,
          newMemberState = None,
          establishedMemberState = None
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[EtmpExclusionDetails] mustBe JsSuccess(expectedResult)
      }
    }
  }
}
