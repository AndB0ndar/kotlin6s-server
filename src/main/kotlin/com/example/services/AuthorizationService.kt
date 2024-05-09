package com.example.services

import java.sql.Connection
import java.sql.ResultSet
import java.security.MessageDigest
import java.util.*


class AuthorizationService(private val connection: Connection) {

    companion object {
        private const val CREATE_TABLE_USERS =
            "CREATE TABLE IF NOT EXISTS UserAuth (" +
                    "ID SERIAL PRIMARY KEY" +
                    ", LOGIN VARCHAR(255)" +
                    ", EMAIL VARCHAR(255)" +
                    ", GROUP VARCHAR(255)" +
                    ", PASSWORD_HASH VARCHAR(255)" +
                    ", TOKEN VARCHAR(255)" +
                    ");"
        private const val INSERT_USER =
            "INSERT INTO UserAuth (LOGIN, EMAIL, GROUP, PASSWORD_HASH, TOKEN) VALUES (?, ?, ?, ?, ?)"
        private const val SELECT_USER_BY_LOGIN =
            "SELECT * FROM UserAuth WHERE LOGIN = ?"
        private const val SELECT_USER_BY_EMAIL =
            "SELECT * FROM UserAuth WHERE EMAIL = ?"
        private const val SELECT_USER_BY_LOGIN_AND_PASSWORD =
            "SELECT * FROM UserAuth WHERE LOGIN = ? AND PASSWORD_HASH = ?"
        private const val SELECT_USER_BY_EMAIL_AND_PASSWORD =
            "SELECT * FROM UserAuth WHERE EMAIL = ? AND PASSWORD_HASH = ?"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_USERS)
    }

    fun register(login: String, email: String, group: String, password: String): String? {
        if (checkUserExistsByLogin(login) || checkUserExistsByEmail(email)) {
            return null
        }

        val hashedPassword = hashPassword(password)
        val token = UUID.randomUUID().toString()
        val preparedStatement = connection.prepareStatement(INSERT_USER)
        preparedStatement.setString(1, login)
        preparedStatement.setString(2, email)
        preparedStatement.setString(2, group)
        preparedStatement.setString(3, hashedPassword)
        preparedStatement.setString(4, token)
        preparedStatement.executeUpdate()
        return token
    }

    fun login(login: String, password: String): String? {
        val hashedPassword = hashPassword(password)
        val userByLogin = getUserByLoginAndPassword(login, hashedPassword)
        val userByEmail = getUserByEmailAndPassword(login, hashedPassword)

        val user = userByLogin ?: userByEmail ?: return null
        return user.getString("TOKEN")
    }

    private fun checkUserExistsByLogin(login: String): Boolean {
        val preparedStatement = connection.prepareStatement(SELECT_USER_BY_LOGIN)
        preparedStatement.setString(1, login)
        val resultSet = preparedStatement.executeQuery()
        return resultSet.next()
    }

    private fun checkUserExistsByEmail(email: String): Boolean {
        val preparedStatement = connection.prepareStatement(SELECT_USER_BY_EMAIL)
        preparedStatement.setString(1, email)
        val resultSet = preparedStatement.executeQuery()
        return resultSet.next()
    }

    private fun getUserByLoginAndPassword(login: String, passwordHash: String): ResultSet? {
        val preparedStatement = connection.prepareStatement(SELECT_USER_BY_LOGIN_AND_PASSWORD)
        preparedStatement.setString(1, login)
        preparedStatement.setString(2, passwordHash)
        return preparedStatement.executeQuery().takeIf { it.next() }
    }

    private fun getUserByEmailAndPassword(email: String, passwordHash: String): ResultSet? {
        val preparedStatement = connection.prepareStatement(SELECT_USER_BY_EMAIL_AND_PASSWORD)
        preparedStatement.setString(1, email)
        preparedStatement.setString(2, passwordHash)
        return preparedStatement.executeQuery().takeIf { it.next() }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(bytes)
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
}
