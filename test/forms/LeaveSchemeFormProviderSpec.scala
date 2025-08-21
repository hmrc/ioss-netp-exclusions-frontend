package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class LeaveSchemeFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "leaveScheme.error.required"
  val invalidKey = "error.boolean"

  val form = new LeaveSchemeFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
