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

import connectors.RegistrationConnector
import logging.Logging
import models.etmp.EtmpClientDetails
import models.requests.DataRequest
import pages.{CannotUseClientExcludedOrNotClientPage, EmptyWaypoints}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClientDetailService @Inject()(registrationConnector: RegistrationConnector)
                                   (implicit ec: ExecutionContext) extends Logging {

  def getClientName(implicit request: DataRequest[_], hc: HeaderCarrier): Future[String] = {
    getClientDetails(request.intermediaryNumber)
      .map(_.find(_.clientIossID == request.iossNumber).map(_.clientName).getOrElse("Unknown client"))
  }

  private def getClientDetails(intermediaryNumber: String)(implicit hc: HeaderCarrier): Future[Seq[EtmpClientDetails]] = {
    registrationConnector.displayIntermediaryRegistration(intermediaryNumber).map {

      case Right(wrapper) =>
        wrapper.etmpDisplayRegistration.clientDetails

      case Left(error) =>
        logger.error(s"Failed to fetch ETMP Intermediary Registration for intermediary: $intermediaryNumber, error: $error")
        Seq.empty[EtmpClientDetails]
    }.recover {
      case e: Throwable =>
        logger.error(s"Unexpected failure while fetching client details for intermediary: $intermediaryNumber", e)
        Seq.empty[EtmpClientDetails]
    }
  }

  def checkIntermediaryHasClient(intermediaryNumber: String, iossNumber: String)(implicit hc: HeaderCarrier): Future[Option[Result]] = {
    getClientDetails(intermediaryNumber).map(_.exists(cd => cd.clientIossID == iossNumber && !cd.clientExcluded)).map {
      case false =>
        Some(Redirect(CannotUseClientExcludedOrNotClientPage.route(EmptyWaypoints)))
      case true =>
        None
    }
  }
}
