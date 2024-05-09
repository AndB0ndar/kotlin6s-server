package com.example.handler

import com.example.models.RegisterReceiveRemote
import com.example.models.TokenResponseRemote
import com.example.services.AuthorizationService
import com.example.services.GroupService
import com.example.utils.loadGroup
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection


fun Route.registerHandler(dbConnection: Connection) {
    val authService = AuthorizationService(dbConnection)
    val groupService = GroupService(dbConnection)

    route("/register") {
        post {
            val receive = call.receive<RegisterReceiveRemote>()
            val groupName = receive.group
            val group = groupService.getGroupByName(groupName)

            var groupId = -1
            if (group == null) {
                groupId = loadGroup(groupName, groupService)
            }

            if (groupId != -1) {
                val token = authService.register(receive.login, receive.email, groupId, receive.password)
                if (token == null) {
                    call.respond(HttpStatusCode.Conflict, "User already exists")
                } else {
                    call.respond(TokenResponseRemote(token = token))
                }
            }
        }
    }
}
