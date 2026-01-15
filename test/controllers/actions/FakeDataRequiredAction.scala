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

import controllers.routes
import models.UserAnswers
import models.etmp.display.EtmpDisplayRegistration
import models.requests.{DataRequest, OptionalDataRequest}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import utils.FutureSyntax.FutureOps

import scala.concurrent.{ExecutionContext, Future}

class FakeDataRequiredAction(
                              userAnswers: Option[UserAnswers],
                              iossNumber: String = "IM9001234567",
                              displayNetpRegistration: EtmpDisplayRegistration
                            )(implicit val executionContext: ExecutionContext) extends DataRequiredAction {

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    
    userAnswers match {
      case Some(userAnswers) =>
        val dataRequest = DataRequest(
          request = request.request,
          userId = request.userId,
          userAnswers = userAnswers,
          iossNumber = iossNumber,
          displayNetpRegistration = displayNetpRegistration,
          intermediaryNumber = request.intermediaryNumber
        )
        Future.successful(Right(dataRequest))
      case None =>
        Left(Redirect(routes.JourneyRecoveryController.onPageLoad())).toFuture
    }
  }
}
