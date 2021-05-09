package com.harada

import com.google.gson.JsonSyntaxException
import com.harada.port.UserNotFoundException
import com.harada.rest.ErrorResponse
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Locations
import io.ktor.response.respond
import mu.KotlinLogging
import java.text.ParseException
import java.time.format.DateTimeParseException

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)
val logger = KotlinLogging.logger { }

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(Locations)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(CallLogging) {}
    install(StatusPages) {
        exception<DateTimeParseException> { cause ->
            val errorMessage = cause.message ?: "Unknown error"
            logger.error { errorMessage }
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(errorMessage))
        }
        exception<ParseException> { cause ->
            val errorMessage = cause.message ?: "Unknown error"
            logger.error { errorMessage }
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(errorMessage))
        }
        exception<JsonSyntaxException> { cause ->
            val errorMessage = cause.message ?: "Unknown error"
            logger.error { errorMessage }
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(errorMessage))
        }
        exception<IllegalArgumentException> { cause ->
            val errorMessage = cause.message ?: "Unknown error"
            logger.error { errorMessage }
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(errorMessage))
        }
        exception<UserNotFoundException> { cause ->
            val errorMessage = cause.message ?: "Unknown error"
            logger.error { errorMessage }
            call.respond(HttpStatusCode.NotFound, ErrorResponse(errorMessage))
        }
        exception<InvalidFormatIdException> { cause ->
            val errorMessage = cause.message ?: "Unknown error"
            logger.error { errorMessage }
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(errorMessage))
        }
        exception<Throwable> { cause ->
            val errorMessage = cause.message ?: "Unknown error"
            logger.error { errorMessage }
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse(errorMessage))
        }
    }
}

data class InvalidFormatIdException(val id: String) : Throwable("id: $id is not correct format.")

