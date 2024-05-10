package com.example.handler

import com.example.models.LoginReceiveRemote
import com.example.models.TokenResponseRemote
import com.example.services.AuthorizationService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection


fun Route.loginHandler(dbConnection: Connection) {
    val authService = AuthorizationService(dbConnection)
    route("/login") {
        post {
            val receive = call.receive<LoginReceiveRemote>()
            val token: String? = authService.login(receive.login, receive.password)
            if (token == null){
                call.respond(HttpStatusCode.BadRequest, "User not found")
            } else {
                call.respond(TokenResponseRemote(token = token))
            }
        }
    }
}