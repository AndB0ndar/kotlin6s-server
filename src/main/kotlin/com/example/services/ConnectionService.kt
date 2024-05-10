package com.example.services

import java.sql.Connection
import java.sql.Statement

class ConnectionService(private val connection: Connection) {
    companion object {
        private const val TABLE_NAME = "Connections"
        private const val COLUMN_ID = "ID"
        private const val COLUMN_USER_ID = "USER_ID"
        private const val COLUMN_QUEUE_ID = "QUEUE_ID"
        private const val CREATE_TABLE_CONNECTIONS =
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                    "$COLUMN_ID SERIAL PRIMARY KEY" +
                    ", $COLUMN_USER_ID INT" +
                    ", $COLUMN_QUEUE_ID INT" +
                    ", FOREIGN KEY (${COLUMN_USER_ID}) REFERENCES ${AuthorizationService.TABLE_NAME}(${AuthorizationService.COLUMN_ID})" +
                    ", FOREIGN KEY (${COLUMN_QUEUE_ID}) REFERENCES ${QueueService.TABLE_NAME}(${QueueService.COLUMN_ID})" +
                    ");"
        private const val INSERT_CONNECTION =
            "INSERT INTO $TABLE_NAME ($COLUMN_USER_ID, $COLUMN_QUEUE_ID) VALUES (?, ?)"
        private const val SELECT_CONNECTION_BY_USER_ID =
            "SELECT $COLUMN_QUEUE_ID FROM $TABLE_NAME  WHERE $COLUMN_USER_ID = ?"
        private const val DELETE_CONNECTION =
            "DELETE FROM $TABLE_NAME WHERE $COLUMN_USER_ID = ? AND $COLUMN_QUEUE_ID = ?"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_CONNECTIONS)
    }

    fun createConnection(userId: Int, queueId: Int): Int {
        val preparedStatement = connection.prepareStatement(INSERT_CONNECTION, Statement.RETURN_GENERATED_KEYS)
        preparedStatement.setInt(1, userId)
        preparedStatement.setInt(2, queueId)
        preparedStatement.executeUpdate()
        val generatedKeys = preparedStatement.generatedKeys
        return if (generatedKeys.next()) {
            generatedKeys.getInt(1)
        } else {
            return -1
        }
    }

    fun getQueueIdsByUser(userId: Int): List<Int> {
        val preparedStatement = connection.prepareStatement(SELECT_CONNECTION_BY_USER_ID)
        preparedStatement.setInt(1, userId)
        val resultSet = preparedStatement.executeQuery()
        val connections = mutableListOf<Int>()
        while (resultSet.next()) {
            connections.add(resultSet.getInt(COLUMN_QUEUE_ID))
        }
        return connections
    }

    fun deleteConnection(userId: Int, queueId: Int): Boolean {
        val preparedStatement = connection.prepareStatement(DELETE_CONNECTION)
        preparedStatement.setInt(1, userId)
        preparedStatement.setInt(2, queueId)
        val rowsAffected = preparedStatement.executeUpdate()
        return rowsAffected > 0
    }
}