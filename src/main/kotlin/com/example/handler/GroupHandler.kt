package com.example.handler

import com.example.models.GroupRemote
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
            val groups = groupService.getAllGroups()
            val groupList = mutableListOf<GroupRemote>()

            groups?.use {
                while (it.next()) {
                    val groupName = it.getString(GroupService.COLUMN_GROUP_NAME)
                    val groupSuffix = it.getString(GroupService.COLUMN_GROUP_SUFFIX)
                    val unitName = it.getString(GroupService.COLUMN_UNIT_NAME)
                    val unitCourse = it.getString(GroupService.COLUMN_UNIT_COURSE)
                    val group = GroupRemote(groupName, groupSuffix, unitName, unitCourse)
                    groupList.add(group)
                }
            }

            if (groupList.isNotEmpty()) {
                call.respond(groupList)
            } else {
                call.respondText("No groups found", status = HttpStatusCode.NotFound)
            }
        }
    }
}