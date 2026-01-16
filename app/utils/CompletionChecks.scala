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

package utils

import models.requests.DataRequest
import pages.*
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}


trait CompletionChecks {

  private def hasStoppedSellingGoodsDateValid()(implicit request: DataRequest[AnyContent]): Boolean =
    request.userAnswers.get(StoppedSellingGoodsDatePage).isDefined


  private def hasStoppedUsingServiceDate()(implicit request: DataRequest[AnyContent]): Boolean =
    request.userAnswers.get(StoppedUsingServiceDatePage).isDefined

  private def checkHasStoppedSellingGoodsJourney()(implicit request: DataRequest[AnyContent]): Boolean = {
    val hasStoppedSellingGoods = request.userAnswers.get(StopSellingGoodsPage).contains(true)
    if (hasStoppedSellingGoods) hasStoppedSellingGoodsDateValid() else false
  }

  private def checkStoppedUsingServiceJourney()(implicit request: DataRequest[AnyContent]): Boolean = {
    request.userAnswers.get(LeaveSchemePage).exists {
      case true =>
        hasStoppedUsingServiceDate()
      case false =>
        false
    }
  }

  def validate()(implicit request: DataRequest[AnyContent]): Boolean = {
    checkHasStoppedSellingGoodsJourney() ||
      checkHasStoppedSellingGoodsJourney() ||
      checkStoppedUsingServiceJourney()
  }

  def getFirstValidationErrorRedirect(waypoints: Waypoints)(implicit request: DataRequest[AnyContent]): Option[Result] = {
    checkHasStoppedSellingGoods(waypoints) orElse
      incompleteStoppedSellingGoodsDateRedirect(waypoints) orElse
      incompleteStoppedUsingServiceRedirect(waypoints) orElse
      noJourneyAnswered()

  }

  private def checkHasStoppedSellingGoods(waypoints: Waypoints)(implicit request: DataRequest[AnyContent]): Option[Result] = {
    if (request.userAnswers.get(StopSellingGoodsPage).contains(true)) {
      incompleteStoppedSellingGoodsDateRedirect(waypoints)
    } else {
      None
    }
  }

  private def incompleteStoppedSellingGoodsDateRedirect(waypoints: Waypoints)(implicit request: DataRequest[AnyContent]): Option[Result] = {
    if (request.userAnswers.get(StopSellingGoodsPage).contains(true)) {
      if (!hasStoppedSellingGoodsDateValid()) {
        Some(Redirect(controllers.routes.StoppedSellingGoodsDateController.onPageLoad(waypoints)))
      } else {
        None
      }
    } else {
      None
    }
  }

  private def incompleteStoppedUsingServiceRedirect(waypoints: Waypoints)(implicit request: DataRequest[AnyContent]): Option[Result] = {
    if (request.userAnswers.get(LeaveSchemePage).contains(true)) {
      if (!hasStoppedUsingServiceDate()) {
        Some(Redirect(controllers.routes.StoppedUsingServiceDateController.onPageLoad(waypoints)))
      } else {
        None
      }
    } else {
      None
    }
  }

  private def noJourneyAnswered()(implicit request: DataRequest[AnyContent]): Option[Result] = {
    if (request.userAnswers.get(StopSellingGoodsPage).contains(true)) {
      None
    } else {
      if (request.userAnswers.get(LeaveSchemePage).contains(true)) {
        None
      } else {
        Some(Redirect(controllers.routes.StopSellingGoodsController.onPageLoad(EmptyWaypoints)))
      }
    }
  }
}
