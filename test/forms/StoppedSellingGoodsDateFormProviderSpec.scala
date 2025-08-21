package forms

import forms.behaviours.DateBehaviours
import play.api.data.FormError

class StoppedSellingGoodsDateFormProviderSpec extends DateBehaviours {

  ".value" - {

    val commencementDate = LocalDate.parse("2013-12-03")
    val currentDate = LocalDate.parse("2013-12-01")
    val endOfPeriod = LocalDate.parse("2013-12-31")

    val form = new StoppedSellingGoodsDateFormProvider()(currentDate, commencementDate)

    val validData = datesBetween(
      min = commencementDate,
      max = endOfPeriod
    )

    behave like dateField(form, "value", validData)
    behave like mandatoryDateField(form, "value", "stoppedSellingGoodsDate.error.required.all")
  }
}
