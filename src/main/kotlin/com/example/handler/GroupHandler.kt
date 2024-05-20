package com.example.handler

import com.example.services.AuthorizationService
import com.example.services.GroupService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection


fun Route.groupHandler(dbConnection: Connection) {
    val authService = AuthorizationService(dbConnection)
    val groupService = GroupService(dbConnection)
    route("/group") {
        get("/all") {
            val groupList = groupService.getAllGroups()
            if (groupList.isNotEmpty()) {
                call.respond(groupList)
            } else {
                call.respondText("No groups found", status = HttpStatusCode.NotFound)
            }
        }
        get("/my") {
            val token = call.parameters["token"].toString()
            if (token.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid input parameters")
                return@get
            }
            val user = authService.getUserByToken(token)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@get
            }

            val group = groupService.getGroupById(user.groupId)
            if (group != null) {
                call.respond(HttpStatusCode.OK, group)
            } else {
                call.respondText("No groups found", status = HttpStatusCode.NotFound)
            }
        }
    }
}
