package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class ApplicationCompleteFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("applicationComplete.error.required")
        .verifying(maxLength(100, "applicationComplete.error.length"))
    )
}
