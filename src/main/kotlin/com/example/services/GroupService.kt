package com.example.services

import com.example.models.table.Group
import java.sql.Connection
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

        preparedStatement.executeUpdate()
        val generatedKeys = preparedStatement.generatedKeys
        return if (generatedKeys.next()) {
            generatedKeys.getInt(1)
        } else {
            return -1
        }
    }

    fun getAllGroups(): List<Group> {
        val preparedStatement = connection.prepareStatement(SELECT_ALL_GROUP)
        val resultSet = preparedStatement.executeQuery()
        val groups = mutableListOf<Group>()
        while (resultSet.next()) {
            groups.add(
                Group(
                    id = resultSet.getInt(COLUMN_ID),
                    groupName = resultSet.getString(COLUMN_GROUP_NAME),
                    groupSuffix = resultSet.getString(COLUMN_GROUP_SUFFIX),
                    unitName = resultSet.getString(COLUMN_UNIT_NAME),
                    unitCourse = resultSet.getString(COLUMN_UNIT_COURSE)
                )
            )
        }
        return groups
    }

    fun getGroupById(groupId: Int): Group? {
        val preparedStatement = connection.prepareStatement(SELECT_GROUP_BY_ID)
        preparedStatement.setInt(1, groupId)
        val resultSet = preparedStatement.executeQuery()
        return if (resultSet.next()) {
            Group(
                id = resultSet.getInt(COLUMN_ID),
                groupName = resultSet.getString(COLUMN_GROUP_NAME),
                groupSuffix = resultSet.getString(COLUMN_GROUP_SUFFIX),
                unitName = resultSet.getString(COLUMN_UNIT_NAME),
                unitCourse = resultSet.getString(COLUMN_UNIT_COURSE)
            )
        } else {
            null
        }
    }

    fun getGroupByName(groupName: String): Group? {
        val preparedStatement = connection.prepareStatement(SELECT_GROUP_BY_NAME)
        preparedStatement.setString(1, groupName)
        val resultSet = preparedStatement.executeQuery()
        return if (resultSet.next()) {
            Group(
                id = resultSet.getInt(COLUMN_ID),
                groupName = resultSet.getString(COLUMN_GROUP_NAME),
                groupSuffix = resultSet.getString(COLUMN_GROUP_SUFFIX),
                unitName = resultSet.getString(COLUMN_UNIT_NAME),
                unitCourse = resultSet.getString(COLUMN_UNIT_COURSE)
            )
        } else {
            null
        }
    }
}

