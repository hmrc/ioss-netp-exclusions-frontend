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

package base

import controllers.actions.*
import generators.Generators
import models.UserAnswers
import models.etmp.display.EtmpDisplayRegistration
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, Waypoints}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import queries.IossNumberQuery
import uk.gov.hmrc.domain.Vrn

import java.time.{Clock, Instant, LocalDate, ZoneId}

trait SpecBase
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with MockitoSugar
    with IntegrationPatience
    with Generators {

  val userAnswersId: String = "id"
  val vrn: Vrn = Vrn("123456789")
  val intermediaryNumber = "IN9001234567"
  val iossNumber = "IM9001234567"
  val waypoints: Waypoints = EmptyWaypoints
  val clientName = "There is no Try Ltd"
  val etmpDisplayRegistration: EtmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "/endpoint").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  val emptyUserAnswersWithIossNumber: UserAnswers = emptyUserAnswers.set(IossNumberQuery, iossNumber).success.value

  val arbitraryDate: LocalDate = datesBetween(LocalDate.of(2023, 3, 1), LocalDate.of(2025, 12, 31)).sample.value
  val arbitraryInstant: Instant = arbitraryDate.atStartOfDay(ZoneId.systemDefault).toInstant
  val stubClockAtArbitraryDate: Clock = Clock.fixed(arbitraryInstant, ZoneId.systemDefault)


  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def applicationBuilder(
                                    userAnswers: Option[UserAnswers] = None
                                  ): GuiceApplicationBuilder = {
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[CheckIntermediaryClientFilterProvider].toInstance(new FakeCheckIntermediaryClientFilterProvider()),
        bind[DataRequiredAction].toInstance(new FakeDataRequiredAction(
          userAnswers = userAnswers,
          displayNetpRegistration = etmpDisplayRegistration
        )(scala.concurrent.ExecutionContext.global))
      )
  }
}
