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

package models.etmp.amend

import base.SpecBase
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}

class EtmpAmendCustomerIdentificationSpec extends SpecBase with Matchers {

  private val iossNumber = "IN900123456"

  "EtmpAmendCustomerIdentification" - {

    "must deserialise/serialise to and from EtmpAmendCustomerIdentification when all fields are present" in {

      val json = Json.obj(
        "iossNumber" -> iossNumber
      )

      val expectedResult = EtmpAmendCustomerIdentification(
        iossNumber = iossNumber
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EtmpAmendCustomerIdentification] mustBe JsSuccess(expectedResult)
    }
  }
}