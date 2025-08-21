package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.StoppedUsingServiceDatePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object StoppedUsingServiceDateSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(StoppedUsingServiceDatePage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "stoppedUsingServiceDate.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.StoppedUsingServiceDateController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("stoppedUsingServiceDate.change.hidden"))
          )
        )
    }
}
