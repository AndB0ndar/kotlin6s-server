package com.example.handler

import com.example.models.LoginReceiveRemote
import com.example.models.RegisterReceiveRemote
import com.example.models.TokenResponseRemote
import com.example.services.AuthorizationService
import com.example.services.GroupService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection


fun Route.authorizationHandler(dbConnection: Connection) {
    val authService = AuthorizationService(dbConnection)
    val groupService = GroupService(dbConnection)
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
    route("/register") {
        post {
            val receive = call.receive<RegisterReceiveRemote>()
            val groupName = receive.group
            val group = groupService.getGroupByName(groupName)

            if (group != null) {
                val token = authService.register(receive.login, receive.firsName, receive.lastName, group.id, receive.password)
                if (token == null) {
                    call.respond(HttpStatusCode.Conflict, "User already exists")
                } else {
                    call.respond(TokenResponseRemote(token = token))
                }
            }
            else {
                call.respond(HttpStatusCode.NotFound, "Group not found")
            }
        }
    }
    get("/{token}/profile") {
        val token = call.parameters["token"].toString()
        if (token.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid input parameters")
            return@get
        }
        val user = authService.getUserByToken(token)
        if (user != null) {
            call.respond(HttpStatusCode.OK, user)
        } else {
            call.respond(HttpStatusCode.NotFound, "User not found")
        }
    }
}