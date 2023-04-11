package io.confluent.developer.e2e

import io.confluent.developer.e2e.AppUi.Companion.appUi
import org.junit.jupiter.api.Test
import org.testingisdocumenting.webtau.WebTauDsl.*
import org.testingisdocumenting.webtau.junit5.WebTau

@WebTau // optional for HTML report generation (can be set as a global extension)
class SocketTestE2e {
    @Test
    fun `submit and wait on socket message`() {
        val wsSession = websocket.connect("/kafka?clientId=e2ehttptest")

        http.post("/rating", http.json("movieId", "123", "rating", "5.0")) { header, _ ->
            // implicit status code check expects 201 for POST
            header.statusCode.should(equal(202))
        }

        wsSession.received.waitTo(equal(mapOf("movieId" to 123, "rating" to 5.0)))
        wsSession.close()
    }

    @Test
    fun `post through UI and check websocket`() {
        val wsSession = websocket.connect("/kafka?clientId=e2euitest")

        appUi.open()
        appUi.movie.setValue("Guardians of the Galaxy")
        appUi.rating.setValue("5")
        appUi.submit()

        wsSession.received.waitTo(equal(mapOf(
            "movieId" to 363,
            "rating" to greaterThan(5))))

        wsSession.close()
    }
}
