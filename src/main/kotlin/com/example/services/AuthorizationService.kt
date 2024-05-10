package com.example.services

import com.example.models.table.User
import java.sql.Connection
import java.security.MessageDigest
import java.util.*


class AuthorizationService(private val connection: Connection) {
    companion object {
        const val TABLE_NAME = "UserAuth"
        const val COLUMN_ID = "ID"
        private const val COLUMN_LOGIN = "LOGIN"
        private const val COLUMN_FIRSTNAME = "FIRSTNAME"
        private const val COLUMN_LASTNAME = "LASTNAME"
        private const val COLUMN_GROUP_ID = "GROUP_ID"
        private const val COLUMN_PASSWORD_HASH = "PASSWORD_HASH"
        private const val COLUMN_TOKEN = "TOKEN"
        private const val CREATE_TABLE_USERS =
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                    "$COLUMN_ID SERIAL PRIMARY KEY" +
                    ", $COLUMN_LOGIN VARCHAR(255)" +
                    ", $COLUMN_FIRSTNAME VARCHAR(255)" +
                    ", $COLUMN_LASTNAME VARCHAR(255)" +
                    ", $COLUMN_GROUP_ID INT" +
                    ", $COLUMN_PASSWORD_HASH VARCHAR(255)" +
                    ", $COLUMN_TOKEN VARCHAR(255)" +
                    ", FOREIGN KEY ($COLUMN_GROUP_ID) REFERENCES ${GroupService.TABLE_NAME}($COLUMN_ID)" +
                    ");"
        private const val INSERT_USER =
            "INSERT INTO $TABLE_NAME (" +
                    "$COLUMN_LOGIN, $COLUMN_FIRSTNAME, $COLUMN_LASTNAME, $COLUMN_GROUP_ID, $COLUMN_PASSWORD_HASH, $COLUMN_TOKEN" +
                    ") VALUES (?, ?, ?, ?, ?, ?)"
        private const val SELECT_USER_BY_LOGIN =
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_LOGIN = ?"
        private const val SELECT_USER_BY_LOGIN_AND_PASSWORD =
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_LOGIN = ? AND $COLUMN_PASSWORD_HASH = ?"
        private const val SELECT_USER_BY_TOKEN =
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_TOKEN = ?"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_USERS)
    }

    fun register(login: String, firstName: String, lastName: String, groupId: Int, password: String): String? {
        if (checkUserExistsByLogin(login)) {
            return null
        }
        val hashedPassword = hashPassword(password)
        val token = UUID.randomUUID().toString()
        val preparedStatement = connection.prepareStatement(INSERT_USER)
        preparedStatement.setString(1, login)
        preparedStatement.setString(2, firstName)
        preparedStatement.setString(3, lastName)
        preparedStatement.setInt(4, groupId)
        preparedStatement.setString(5, hashedPassword)
        preparedStatement.setString(6, token)
        preparedStatement.executeUpdate()
        return token
    }

    fun login(login: String, password: String): String? {
        val hashedPassword = hashPassword(password)
        val preparedStatement = connection.prepareStatement(SELECT_USER_BY_LOGIN_AND_PASSWORD)
        preparedStatement.setString(1, login)
        preparedStatement.setString(2, hashedPassword)
        val resultSet = preparedStatement.executeQuery()
        return if (resultSet.next()) {
            resultSet.getString(COLUMN_TOKEN)
        } else {
            null
        }
    }

    fun getUserByToken(token: String): User? {
        val preparedStatement = connection.prepareStatement(SELECT_USER_BY_TOKEN)
        preparedStatement.setString(1, token)
        val resultSet = preparedStatement.executeQuery()
        return if (resultSet.next()) {
            User(
                id = resultSet.getInt(COLUMN_ID),
                login = resultSet.getString(COLUMN_LOGIN),
                firstName = resultSet.getString(COLUMN_FIRSTNAME),
                lastName = resultSet.getString(COLUMN_LASTNAME),
                groupId = resultSet.getInt(COLUMN_GROUP_ID),
                token = resultSet.getString(COLUMN_TOKEN)
            )
        } else {
            null
        }
    }


    private fun checkUserExistsByLogin(login: String): Boolean {
        val preparedStatement = connection.prepareStatement(SELECT_USER_BY_LOGIN)
        preparedStatement.setString(1, login)
        val resultSet = preparedStatement.executeQuery()
        return resultSet.next()
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(bytes)
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
}
