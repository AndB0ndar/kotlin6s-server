package com.example.services

import com.example.models.table.Queue
import java.sql.Connection
import java.sql.Statement


class QueueService(private val connection: Connection) {
    companion object {
        const val TABLE_NAME = "Queues"
        const val COLUMN_ID = "ID"
        private const val COLUMN_QUEUE_NAME = "QUEUE_NAME"
        private const val COLUMN_CREATOR = "CREATOR"
        private const val COLUMN_GROUP = "GROUP_NAME"
        private const val COLUMN_DESCRIPTION = "DESCRIPTION"
        private const val CREATE_TABLE_QUEUES =
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                    "$COLUMN_ID SERIAL PRIMARY KEY" +
                    ", $COLUMN_QUEUE_NAME VARCHAR(255)" +
                    ", $COLUMN_CREATOR VARCHAR(255)" +
                    ", $COLUMN_GROUP INT" +
                    ", $COLUMN_DESCRIPTION TEXT" +
                    ", FOREIGN KEY ($COLUMN_GROUP) REFERENCES ${GroupService.TABLE_NAME}(${GroupService.COLUMN_ID})" +
                    ");"
        private const val INSERT_QUEUE =
            "INSERT INTO $TABLE_NAME (" +
                    "$COLUMN_QUEUE_NAME, $COLUMN_CREATOR, $COLUMN_GROUP, $COLUMN_DESCRIPTION" +
                    ") VALUES (?, ?, ?, ?)"
        private const val SELECT_QUEUE_BY_ID =
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = ?"
        private const val DELETE_QUEUE =
            "DELETE FROM $TABLE_NAME WHERE $COLUMN_ID = ?"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_QUEUES)
    }

    fun createQueue(queueName: String, creator: String, group: Int, description: String): Int {
        val preparedStatement = connection.prepareStatement(INSERT_QUEUE, Statement.RETURN_GENERATED_KEYS)
        preparedStatement.setString(1, queueName)
        preparedStatement.setString(2, creator)
        preparedStatement.setInt(3, group)
        preparedStatement.setString(4, description)

        preparedStatement.executeUpdate()
        val generatedKeys = preparedStatement.generatedKeys
        return if (generatedKeys.next()) {
            generatedKeys.getInt(1)
        } else {
            return -1
        }
    }

    fun getQueueById(queueId: Int): Queue? {
        val preparedStatement = connection.prepareStatement(SELECT_QUEUE_BY_ID)
        preparedStatement.setInt(1, queueId)
        val resultSet = preparedStatement.executeQuery()
        return if (resultSet.next()) {
            Queue(
                id = resultSet.getInt(COLUMN_ID),
                queueName = resultSet.getString(COLUMN_QUEUE_NAME),
                creatorToken = resultSet.getString(COLUMN_CREATOR),
                group = resultSet.getInt(COLUMN_GROUP),
                description = resultSet.getString(COLUMN_DESCRIPTION)
            )
        } else {
            null
        }
    }

    fun deleteQueue(queueId: Int): Boolean {
        val preparedStatement = connection.prepareStatement(DELETE_QUEUE)
        preparedStatement.setInt(1, queueId)
        val rowsAffected = preparedStatement.executeUpdate()
        return rowsAffected > 0
    }
}
