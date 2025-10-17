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

package controllers.actions

import connectors.RegistrationConnector

import javax.inject.Inject
import controllers.routes
import models.requests.{DataRequest, OptionalDataRequest}
import logging.Logging
import models.etmp.display.RegistrationWrapper
import models.responses.ErrorResponse
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import queries.IossNumberQuery
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class DataRequiredActionImpl @Inject()(registrationConnector: RegistrationConnector)
                                      (implicit val executionContext: ExecutionContext) extends DataRequiredAction with Logging {

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {

    request.userAnswers match {
      case None =>
        Future.successful(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))

      case Some(userAnswers) =>
        val iossNumber = userAnswers
          .get(IossNumberQuery)
          .getOrElse(throw new RuntimeException("IOSS number missing in user answers"))

        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request.request, request.session)

        registrationConnector.displayRegistration(iossNumber)(hc).flatMap {
          case Left(error: ErrorResponse) =>
            val msg = s"Failed to retrieve registration: ${error.body}"
            logger.error(msg)
            Future.failed(new RuntimeException(msg))

          case Right(registrationWrapper: RegistrationWrapper) =>
            val displayNetpRegistration = registrationWrapper.etmpDisplayRegistration
            val dataRequest = DataRequest(
              request = request.request,
              userId = request.userId,
              userAnswers = userAnswers,
              iossNumber = iossNumber,
              displayNetpRegistration = displayNetpRegistration)
            Future.successful(Right(dataRequest))
        }
    }
  }
}

trait DataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
