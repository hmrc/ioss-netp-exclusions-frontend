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

package connectors

import config.Service
import connectors.RegistrationConnectorHttpParser.*
import models.etmp.amend.EtmpAmendRegistrationRequest
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject()(config: Configuration, httpClientV2: HttpClientV2)
                                     (implicit executionContext: ExecutionContext) {

  private val baseUrl: Service = config.get[Service]("microservice.services.ioss-netp-registration")

  def amend(registrationRequest: EtmpAmendRegistrationRequest)(implicit hc: HeaderCarrier): Future[AmendRegistrationResultResponse] = {
    httpClientV2.post(url"$baseUrl/amend").withBody(Json.toJson(registrationRequest))
      .execute[AmendRegistrationResultResponse]
  }

  def displayRegistration(iossNumber: String)(implicit hc: HeaderCarrier): Future[EtmpDisplayRegistrationResponse] =
    httpClientV2.get(url"$baseUrl/registrations/$iossNumber").execute[EtmpDisplayRegistrationResponse]
}
