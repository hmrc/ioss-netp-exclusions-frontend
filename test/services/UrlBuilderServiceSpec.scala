package services

import config.FrontendAppConfig
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.*
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl


class UrlBuilderServiceSpec extends AnyFreeSpec with Matchers with MockitoSugar {

  ".loginContinueUrl" - {

    "must add an existing session Id as a querystring parameter" in {

      val config: FrontendAppConfig = mock[FrontendAppConfig]
      when(config.loginContinueUrl) thenReturn "http://localhost"

      val service = new UrlBuilderService(config)

      val result = service.loginContinueUrl(FakeRequest(GET, "/foo?k=session-id"))

      result mustEqual RedirectUrl("http://localhost/foo?k=session-id")
    }
  }

}