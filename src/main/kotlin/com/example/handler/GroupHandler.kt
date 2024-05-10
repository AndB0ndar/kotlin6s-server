package com.example.handler

import com.example.services.GroupService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection


fun Route.groupHandler(dbConnection: Connection) {
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
    }
}
