package rest

import com.google.gson.JsonSyntaxException
import com.harada.InvalidFormatIdException
import com.harada.port.UserNotFoundException
import com.harada.rest.ErrorResponse
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import java.text.ParseException

fun invokeWithTestApplication(
    moduleFunction: Application.() -> Unit,
    path: String,
    method: HttpMethod,
    body: String?,
    contentType: ContentType,
    assert: TestApplicationCall.() -> Unit
) = withTestApplication(moduleFunction) {
    application.install(ContentNegotiation) { gson { setPrettyPrinting() } }
    application.install(StatusPages) {
        exception<ParseException> { cause ->
            val errorMessage = cause.message ?: "Unknown error"
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(errorMessage))
        }
        exception<JsonSyntaxException> { cause ->
            val errorMessage = cause.message ?: "Unknown error"
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(errorMessage))
        }
        exception<IllegalArgumentException> { cause ->
            val errorMessage = cause.message ?: "Unknown error"
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(errorMessage))
        }
        exception<UserNotFoundException> { cause ->
            val errorMessage = cause.message ?: "Unknown error"
            call.respond(HttpStatusCode.NotFound, ErrorResponse(errorMessage))
        }
        exception<InvalidFormatIdException> { cause ->
            val errorMessage = cause.message ?: "Unknown error"
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(errorMessage))
        }
    }
    with(handleRequest(method, path) {
        addHeader(
            HttpHeaders.ContentType, "$contentType"
        )
        if (body != null) setBody(body)
    }) {
        assert.invoke(this)
    }
}
