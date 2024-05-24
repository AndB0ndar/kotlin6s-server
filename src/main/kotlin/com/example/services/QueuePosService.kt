package com.example.services

import com.example.models.table.QueueItem
import java.sql.Connection
import java.sql.Statement


class QueuePosService(private val connection: Connection) {
    companion object {
        private const val TABLE_NAME = "QueueItem"
        private const val COLUMN_ID = "ID"
        private const val COLUMN_QUEUE_ID = "QUEUE_ID"
        private const val COLUMN_USER_ID = "USER_ID"
        private const val COLUMN_POSITION = "POSITION"
        private const val CREATE_TABLE_QUEUES =
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                    "$COLUMN_ID SERIAL PRIMARY KEY" +
                    ", $COLUMN_QUEUE_ID INT" +
                    ", $COLUMN_USER_ID INT" +
                    ", $COLUMN_POSITION INT" +
                    ", FOREIGN KEY ($COLUMN_QUEUE_ID) REFERENCES ${QueueService.TABLE_NAME}(${QueueService.COLUMN_ID})" +
                    ", FOREIGN KEY ($COLUMN_USER_ID) REFERENCES ${AuthorizationService.TABLE_NAME}(${AuthorizationService.COLUMN_ID})" +
                    ");"
        private const val INSERT_QUEUE_POS =
            "INSERT INTO $TABLE_NAME (" +
                    "$COLUMN_QUEUE_ID, $COLUMN_USER_ID, $COLUMN_POSITION" +
                    ") VALUES (?, ?, ?)"
        private const val SELECT_MAX_POSITION =
            "SELECT MAX($COLUMN_POSITION) FROM $TABLE_NAME WHERE $COLUMN_QUEUE_ID = ? AND $COLUMN_USER_ID = ?"
        private const val SELECT_ALL_QUEUE_POS =
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_QUEUE_ID = ?"
        private const val DELETE_QUEUE_POS =
            "DELETE FROM $TABLE_NAME WHERE $COLUMN_QUEUE_ID = ? AND $COLUMN_USER_ID = ?"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_QUEUES)
    }

    fun createQueuePos(queueId: Int, userId: Int, position: Int): Int {
        val preparedStatement = connection.prepareStatement(INSERT_QUEUE_POS, Statement.RETURN_GENERATED_KEYS)
        preparedStatement.setInt(1, queueId)
        preparedStatement.setInt(2, userId)
        preparedStatement.setInt(3, position)

        preparedStatement.executeUpdate()
        val generatedKeys = preparedStatement.generatedKeys
        return if (generatedKeys.next()) {
            generatedKeys.getInt(1)
        } else {
            return -1
        }
    }

    fun getMaxPosition(queueId: Int, userId: Int): Int {
        val preparedStatement = connection.prepareStatement(SELECT_MAX_POSITION)
        preparedStatement.setInt(1, queueId)
        preparedStatement.setInt(2, userId)
        val resultSet = preparedStatement.executeQuery()
        return if (resultSet.next()) {
            resultSet.getInt(1)
        } else {
            -1
        }
    }

    fun getAllQueuePos(queueId: Int): List<QueueItem> {
        val queuePosList = mutableListOf<QueueItem>()
        val preparedStatement = connection.prepareStatement(SELECT_ALL_QUEUE_POS)
        preparedStatement.setInt(1, queueId)
        val resultSet = preparedStatement.executeQuery()
        while (resultSet.next()) {
            queuePosList.add(
                QueueItem(
                    id = resultSet.getInt(COLUMN_ID),
                    queueId = resultSet.getInt(COLUMN_QUEUE_ID),
                    userId = resultSet.getInt(COLUMN_USER_ID),
                    position = resultSet.getInt(COLUMN_POSITION)
                )
            )
        }
        return queuePosList
    }

    fun deleteQueuePos(queueId: Int, userId: Int): Boolean {
        val preparedStatement = connection.prepareStatement(DELETE_QUEUE_POS)
        preparedStatement.setInt(1, queueId)
        preparedStatement.setInt(2, userId)
        val rowsAffected = preparedStatement.executeUpdate()
        return rowsAffected > 0
    }
}