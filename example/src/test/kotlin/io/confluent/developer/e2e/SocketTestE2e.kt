package io.confluent.developer.e2e

import org.junit.jupiter.api.Test
import org.testingisdocumenting.webtau.WebTauDsl.*
import org.testingisdocumenting.webtau.junit5.WebTau

@WebTau // optional for HTML report generation (can be set as a global extension)
class SocketTestE2e {
    @Test
    fun `submit and wait on socket message`() {
        http.post("/rating", http.json("movieId", "123", "rating", "5.0")) { header, _ ->
            // implicit status code check expects 201 for POST
            header.statusCode.should(equal(202))
        }

        val wsSession = websocket.connect("/kafka");
        wsSession.received.waitTo(equal(mapOf("movieId" to 123, "rating" to 5.0)))
    }
}
