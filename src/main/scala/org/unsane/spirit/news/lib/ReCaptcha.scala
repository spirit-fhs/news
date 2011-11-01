package org.unsane.spirit.news.lib


/**
 * Got this from
 * http://www.assembla.com/spaces/liftweb/wiki/ReCaptcha
 */
trait ReCaptcha {
  import net.liftweb.common.{Box, Empty, Full, Failure}
  import net.liftweb.util.{FieldError, FieldIdentifier}
  import net.liftweb.http.S
  import net.tanesha.recaptcha.ReCaptchaFactory
  import net.liftweb.json.JsonAST._
  import net.liftweb.json.JsonDSL._

  // add ReCaptcha
  /**
   * Define the public key to used to connect to reCapcha service
   */
  protected def reCaptchaPublicKey : String

  /**
   * Define the private key to used to connect to reCapcha service
   */
  protected def reCaptchaPrivateKey : String

  /**
   * Define the option to configure reCaptcha widget.
   *
   * @see http://code.google.com/apis/recaptcha/docs/customization.html to have the list possible customization
   * @return the javascript option map (as JObject)
   * @codeAsDoc
   */
  protected def reCaptchaOptions = ("theme" -> "white") ~ ("lang" -> S.containerRequest.flatMap(_.locale).map(_.getLanguage).getOrElse("en"))

  private lazy val reCaptcha = ReCaptchaFactory.newReCaptcha(reCaptchaPublicKey, reCaptchaPrivateKey, false)

  protected def captchaXhtml() = {
    import scala.xml.Unparsed
    import net.liftweb.http.js.JE
    import net.liftweb.json.JsonAST._
    import net.liftweb.json.compact
  
   val RecaptchaOptions = compact(render(reCaptchaOptions))
    <xml:group>
    <script>
        var RecaptchaOptions = {Unparsed(RecaptchaOptions)};
      </script>
      <script type="text/javascript" src={"http://api.recaptcha.net/challenge?k=" + reCaptchaPublicKey}></script>
    </xml:group>
  }

  protected def validateCaptcha(): List[FieldError] = {
    def checkAnswer(remoteAddr : String, challenge : String, uresponse : String) : Box[String]= {
      val reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse)
      reCaptchaResponse.isValid() match {
        case true => Empty
        case false => Full(S.?("reCaptcha." + reCaptchaResponse.getErrorMessage))
      }
    }
    val res = for (
      remoteAddr <- S.containerRequest.map(_.remoteAddress);
      challenge <- S.param("recaptcha_challenge_field");
      uresponse <- S.param("recaptcha_response_field") ;
      b <- checkAnswer(remoteAddr, challenge, uresponse)
    ) yield b

    res match {
      case Failure(msg, _, _) => List(FieldError(FakeFieldIdentifier(Full("reCaptcha")), msg))
      case Full(msg) => List(FieldError(FakeFieldIdentifier(Full("reCaptcha")), msg))
      case Empty => Nil
    }
  }

  case class FakeFieldIdentifier(override val uniqueFieldId : Box[String]) extends FieldIdentifier

}
