package com.example.services

import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class GroupService(private val connection: Connection) {
    companion object {
        const val TABLE_NAME = "Groups"
        const val COLUMN_ID = "ID"
        const val COLUMN_GROUP_NAME = "GROUP_NAME"
        const val COLUMN_GROUP_SUFFIX = "GROUP_SUFFIX"
        const val COLUMN_UNIT_NAME = "UNIT_NAME"
        const val COLUMN_UNIT_COURSE = "UNIT_COURSE"
        private const val CREATE_TABLE_GROUPS =
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                    "$COLUMN_ID SERIAL PRIMARY KEY" +
                    ", $COLUMN_GROUP_NAME VARCHAR(255)" +
                    ", $COLUMN_GROUP_SUFFIX VARCHAR(255)" +
                    ", $COLUMN_UNIT_NAME VARCHAR(255)" +
                    ", $COLUMN_UNIT_COURSE VARCHAR(255)" +
                    ");"
        private const val INSERT_GROUP =
            "INSERT INTO $TABLE_NAME (" +
                    "$COLUMN_GROUP_NAME, $COLUMN_GROUP_SUFFIX, $COLUMN_UNIT_NAME, $COLUMN_UNIT_COURSE" +
                    ") VALUES (?, ?, ?, ?)"
        private const val SELECT_ALL_GROUP =
            "SELECT * FROM $TABLE_NAME"
        private const val SELECT_GROUP_BY_ID =
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = ?"
        private const val SELECT_GROUP_BY_NAME =
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_GROUP_NAME = ?"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_GROUPS)
    }

    fun createGroup(groupName: String, groupSuffix: String, unitName: String, unitCourse: String): Int {
        val preparedStatement = connection.prepareStatement(INSERT_GROUP, Statement.RETURN_GENERATED_KEYS)
        preparedStatement.setString(1, groupName)
        preparedStatement.setString(2, groupSuffix)
        preparedStatement.setString(3, unitName)
        preparedStatement.setString(4, unitCourse)

        val insertedRows = preparedStatement.executeUpdate()
        if (insertedRows > 0) {
            val generatedKeys = preparedStatement.generatedKeys
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1)
            }
        }
        return -1
    }

    fun getAllGroups(): ResultSet? {
        val preparedStatement = connection.prepareStatement(SELECT_ALL_GROUP)
        return preparedStatement.executeQuery().takeIf { it.next() }
    }

    fun getGroupById(groupId: Int): ResultSet? {
        val preparedStatement = connection.prepareStatement(SELECT_GROUP_BY_ID)
        preparedStatement.setInt(1, groupId)
        return preparedStatement.executeQuery().takeIf { it.next() }
    }

    fun getGroupByName(groupName: String): ResultSet? {
        val preparedStatement = connection.prepareStatement(SELECT_GROUP_BY_NAME)
        preparedStatement.setString(1, groupName)
        return preparedStatement.executeQuery().takeIf { it.next() }
    }
}

