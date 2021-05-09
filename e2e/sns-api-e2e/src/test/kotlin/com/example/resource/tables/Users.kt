package com.example.resource.tables

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = uuid("id")
    val name = varchar("name", 100)
    val mail = varchar("mail", 100)
    override val primaryKey = PrimaryKey(id)
}