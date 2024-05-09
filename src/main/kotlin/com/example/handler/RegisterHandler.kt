package com.example.handler

import com.example.models.RegisterReceiveRemote
import com.example.models.TokenResponseRemote
import com.example.services.AuthorizationService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection

fun Route.registerHandler(dbConnection: Connection) {
    val authService = AuthorizationService(dbConnection)
    //accept(ContentType.Application.Json)
    route("/register") {
        post {
            val receive = call.receive<RegisterReceiveRemote>()
            val token = authService.register(receive.login, receive.email, receive.group, receive.password)

            if (token == null) {
                call.respond(HttpStatusCode.Conflict, "User already exists")
            } else {
                call.respond(TokenResponseRemote(token = token))
            }
        }
    }
}
