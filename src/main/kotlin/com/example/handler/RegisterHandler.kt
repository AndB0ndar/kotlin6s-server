package com.example.handler

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
                //groupId = loadGroup(groupName, groupService)
                val name= "ИКБО-01-21"
                val groupSuffix = "МОСИТ"
                val unitName = "Институт информационных технологий"
                val unitCourse = "Бакалавриат/специалитет, 3 курс"
                groupId = groupService.createGroup(name, groupSuffix, unitName, unitCourse)
            }

            if (groupId != -1) {
                val token = authService.register(receive.login, receive.firsName, receive.lastName, groupId, receive.password)

                call.respond(1)
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
}
