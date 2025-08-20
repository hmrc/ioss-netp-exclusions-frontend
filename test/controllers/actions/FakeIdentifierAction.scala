package controllers.actions

import javax.inject.Inject
import models.requests.IdentifierRequest
import play.api.mvc.*
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.domain.Vrn

import scala.concurrent.{ExecutionContext, Future}

class FakeIdentifierAction @Inject()(bodyParsers: PlayBodyParsers) extends IdentifierAction {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] =
    block(IdentifierRequest(request, "id", Enrolments(Set.empty), Vrn("123456789"), "IN9001234567"))

  override def parser: BodyParser[AnyContent] =
    bodyParsers.default

  override protected def executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}
