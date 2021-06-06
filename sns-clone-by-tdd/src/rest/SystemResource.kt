package com.harada.rest

import com.harada.Injector
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.Kodein


data class JsonResponse(val message: String)

@KtorExperimentalLocationsAPI
fun Application.systemModule() {
    systemModuleWithDepth(Injector.kodein)
}

@KtorExperimentalLocationsAPI
fun Application.systemModuleWithDepth(kodein: Kodein) {
    // テストの際にここでinstallしないとエラーを起こすため
    if (this.featureOrNull(Locations) == null) install(Locations)
    routing {
        get("/systems/ping") {
            call.respond(JsonResponse("ok"))
        }
    }
}