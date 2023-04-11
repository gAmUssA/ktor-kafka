package io.confluent.developer.e2e

import org.testingisdocumenting.webtau.WebTauDsl.*

class AppUi {
    companion object {
        val appUi = AppUi()
    }

    val movie = browser.element("#movieId")
    val rating = browser.element("#rating")

    fun open() {
       browser.open("/")
    }

    fun submit() {
       browser.element("button").get("Submit").click()
    }
}
