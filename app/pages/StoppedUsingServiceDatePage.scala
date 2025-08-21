package pages

import play.api.libs.json.JsPath

case object StoppedUsingServiceDatePage extends QuestionPage[Int] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "stoppedUsingServiceDate"
}
