package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.CannotUseNotAnIntermediaryView

class CannotUseNotAnIntermediaryControllerSpec extends SpecBase {

  "CannotUseNotAnIntermediary Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CannotUseNotAnIntermediaryController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CannotUseNotAnIntermediaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
