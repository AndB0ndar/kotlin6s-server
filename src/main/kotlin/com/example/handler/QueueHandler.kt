package com.example.handler

import com.example.models.*
import com.example.models.table.Queue
import com.example.models.table.QueueItemResponse
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
            val connectedQueues = mutableListOf<Queue>()
            if (connectedQueueIds.isNotEmpty()) {
                for (queueId in connectedQueueIds) {
                    val queue = queueService.getQueueById(queueId)
                    if (queue != null) {
                        connectedQueues.add(queue)
                    }
                }
            }
            call.respond(connectedQueues)
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
        delete("/delete/{id}") {
            val token = call.parameters["token"].toString()
            val queueId = call.parameters["id"]?.toIntOrNull()
            // val queueId = call.receive<Int>()

            if (token.isEmpty() || queueId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid input parameters")
                return@delete
            }

            val user = authService.getUserByToken(token)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@delete
            }

            val connectionDeleted = connectionService.deleteConnection(user.id, queueId)
            if (!connectionDeleted) {
                call.respond(HttpStatusCode.NotFound, "User is not connected to this queue")
                return@delete
            }
            val queueDeleted = queueService.deleteQueue(queueId)
            call.respond(queueDeleted)
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
                val queue = queueService.getQueueById(id)
                if (queue == null) {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                    return@get
                }
                
                val queuePosList = queuePosService.getAllQueuePos(id)
                val queuePosResponseList = mutableListOf<QueueItemResponse>()
                queuePosList.forEach { queueItem ->
                    val user = authService.getUserById(queueItem.userId)
                    if (user != null) {
                        queuePosResponseList.add(QueueItemResponse(queue.queueName, user.login, queueItem.position))
                    }
                }
                println(queuePosResponseList)
                call.respond(queuePosResponseList)
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

                if (queuePosService.getAllQueuePos(id).isNotEmpty()) {
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
            delete("/delete") {
                val token = call.parameters["token"].toString()
                val id = call.parameters["id"]?.toIntOrNull()
                if (token.isEmpty() || id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid input parameters")
                    return@delete
                }

                val user = authService.getUserByToken(token)
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                    return@delete
                }

                val deleted = queuePosService.deleteQueuePos(id, user.id)
                call.respond(if (deleted) HttpStatusCode.OK else HttpStatusCode.NotFound, deleted)
            }
        }
    }
}
