package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class StoppedUsingServiceDateFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "value" -> int(
        "stoppedUsingServiceDate.error.required",
        "stoppedUsingServiceDate.error.wholeNumber",
        "stoppedUsingServiceDate.error.nonNumeric")
          .verifying(inRange(0, Int.MaxValue, "stoppedUsingServiceDate.error.outOfRange"))
    )
}
