package controllers

import base.SpecBase
import config.FrontendAppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.play.language.LanguageUtils




class LanguageSwitchControllerSpec extends SpecBase  with MockitoSugar {


  "LanguageSwitchController" - {


    "must redirect to the fallback URL when an unsupported language is requested" in {


      val appConfig = mock[FrontendAppConfig]
      when(appConfig.languageMap).thenReturn(Map("en" -> Lang("en"), "fr" -> Lang("fr")))


      val languageUtils = mock[LanguageUtils]
      when(languageUtils.getCurrentLang(any())).thenReturn(Lang("en"))


      val cc = stubControllerComponents()


      val controller = new LanguageSwitchController(appConfig, languageUtils, cc)


      val request = FakeRequest(GET, "/lang/unsupportedLang")


      val result = controller.switchToLanguage("unsupportedLang")(request)


      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url
    }
  }


  "must redirect to the correct URL when a valid language is requested" in {


    val appConfig = mock[FrontendAppConfig]
    when(appConfig.languageMap).thenReturn(Map("en" -> Lang("en"), "fr" -> Lang("fr")))


    val languageUtils = mock[LanguageUtils]
    when(languageUtils.getCurrentLang(any())).thenReturn(Lang("en"))


    val cc = stubControllerComponents()


    val controller = new LanguageSwitchController(appConfig, languageUtils, cc)


    val request = FakeRequest(GET, "/lang/en")


    val result = controller.switchToLanguage("en")(request)


    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url
  }


  "must use the correct fallback URL" in {


    val appConfig = mock[FrontendAppConfig]
    when(appConfig.languageMap).thenReturn(Map("en" -> Lang("en"), "fr" -> Lang("fr")))


    val languageUtils = mock[LanguageUtils]
    when(languageUtils.getCurrentLang(any())).thenReturn(Lang("en"))


    val cc = stubControllerComponents()


    val controller = new LanguageSwitchController(appConfig, languageUtils, cc)


    controller.fallbackURL mustEqual routes.IndexController.onPageLoad().url
  }


  "must use the correct language map" in {


    val appConfig = mock[FrontendAppConfig]
    val mockLanguageMap = Map("en" -> Lang("en"), "fr" -> Lang("fr"))
    when(appConfig.languageMap).thenReturn(mockLanguageMap)


    val languageUtils = mock[LanguageUtils]
    when(languageUtils.getCurrentLang(any())).thenReturn(Lang("en"))


    val cc = stubControllerComponents()


    val controller = new LanguageSwitchController(appConfig, languageUtils, cc)


    controller.languageMap mustEqual mockLanguageMap
  }
}
