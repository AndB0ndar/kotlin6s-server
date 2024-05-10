package com.example.handler

import com.example.models.*
import com.example.models.table.Queue
import com.example.services.*
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection


fun Route.queueHandler(dbConnection: Connection) {
    val queueService = QueueService(dbConnection)
    val authService = AuthorizationService(dbConnection)
    val groupService = GroupService(dbConnection)
    val connectionService = ConnectionService(dbConnection)
    val queuePosService = QueuePosService(dbConnection)

    route("/{token}") {
        route("/queues") {
            get("/all") {
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

                val userId = user.id
                val connectedQueueIds = connectionService.getQueueIdsByUser(userId)
                if (connectedQueueIds.isNotEmpty()) {
                    val connectedQueues = mutableListOf<Queue>()
                    for (queueId in connectedQueueIds) {
                        val queue = queueService.getQueueById(queueId)
                        if (queue != null) {
                            connectedQueues.add(queue)
                        }
                    }
                    call.respond(connectedQueues)
                } else {
                    call.respondText("No connected queues found")
                }
            }
            post("/create") {
                val token = call.parameters["token"].toString()
                val receive = call.receive<QueueReceiveRemote>()

                if (token.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid input parameters")
                    return@post
                }

                val user = authService.getUserByToken(token)
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                    return@post
                }

                val group = groupService.getGroupByName(receive.group)
                if (group == null) {
                    call.respond(HttpStatusCode.NotFound, "Group not found")
                    return@post
                }

                val queueId = queueService.createQueue(receive.queueName, token, group.id, receive.description)
                if (queueId == -1) {
                    call.respond(HttpStatusCode.InternalServerError, "Queue not created")
                } else {
                    if (connectionService.createConnection(user.id, queueId) != -1) {
                        call.respond(queueId)
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "Failed to create connection")
                    }
                }
            }
            post("/delete/{id}") {
                val token = call.parameters["token"].toString()
                val queueId = call.parameters["id"]?.toIntOrNull()
                // val queueId = call.receive<Int>()

                if (token.isEmpty() || queueId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid input parameters")
                    return@post
                }

                val user = authService.getUserByToken(token)
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                    return@post
                }

                val connectionDeleted = connectionService.deleteConnection(user.id, queueId)
                if (!connectionDeleted) {
                    call.respond(HttpStatusCode.NotFound, "User is not connected to this queue")
                    return@post
                }

                val queueDeleted = queueService.deleteQueue(queueId)
                if (queueDeleted) {
                    call.respond(HttpStatusCode.OK, "Queue deleted successfully")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to delete queue")
                }
            }
            post("/connect/{id}") {
                val token = call.parameters["token"].toString()
                val id = call.parameters["id"]?.toIntOrNull()
                if (token.isEmpty() || id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid input parameters")
                    return@post
                }

                val user = authService.getUserByToken(token)
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                    return@post
                }

                val connectionId: Int = connectionService.createConnection(user.id, id)
                if (connectionId != -1) {
                    call.respond(connectionId)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to create connection")
                }
            }
            route("{id}/queue/") {
                get("/all") {
                    val token = call.parameters["token"].toString()
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (token.isEmpty() || id == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid queueId")
                        return@get
                    }
                    val user = authService.getUserByToken(token)
                    if (user == null) {
                        call.respond(HttpStatusCode.NotFound, "User not found")
                        return@get
                    }

                    val queuePosList = queuePosService.getAllQueuePos(id, user.id)
                    call.respond(queuePosList)
                }
                post("/add") {
                    val token = call.parameters["token"].toString()
                    val id = call.parameters["id"]?.toIntOrNull()

                    if (token.isEmpty() || id == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid queueId")
                        return@post
                    }

                    val user = authService.getUserByToken(token)
                    if (user == null) {
                        call.respond(HttpStatusCode.NotFound, "User not found")
                        return@post
                    }

                    if (queuePosService.getAllQueuePos(id, user.id).isNotEmpty()) {
                        call.respond(HttpStatusCode.Conflict, "User already exist")
                        return@post
                    }

                    val position = queuePosService.getMaxPosition(id, user.id) + 1
                    val queuePosId = queuePosService.createQueuePos(id, user.id, position)
                    if (queuePosId != -1) {
                        call.respond(HttpStatusCode.Created, queuePosId)
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "Failed to add queue position")
                    }
                }
                post("/delete") {
                    val token = call.parameters["token"].toString()
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (token.isEmpty() || id == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid input parameters")
                        return@post
                    }

                    val user = authService.getUserByToken(token)
                    if (user == null) {
                        call.respond(HttpStatusCode.NotFound, "User not found")
                        return@post
                    }

                    val deleted = queuePosService.deleteQueuePos(id, user.id)
                    if (deleted) {
                        call.respond(HttpStatusCode.OK, "Queue position deleted")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Queue position not found")
                    }
                }
            }
        }
    }
}
