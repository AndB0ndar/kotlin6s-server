package com.example.plugins

import com.example.handler.loginHandler
import com.example.handler.registerHandler
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection

fun Application.configureRouting(dbConnection: Connection) {
    routing {
        get("/") {
            call.respondText("Worked!")
        }
        loginHandler(dbConnection)
        registerHandler(dbConnection)
    }
}
