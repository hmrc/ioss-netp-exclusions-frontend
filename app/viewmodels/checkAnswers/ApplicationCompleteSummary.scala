package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.ApplicationCompletePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ApplicationCompleteSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ApplicationCompletePage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "applicationComplete.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ApplicationCompleteController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("applicationComplete.change.hidden"))
          )
        )
    }
}
