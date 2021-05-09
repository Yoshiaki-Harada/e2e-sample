package com.harada.gateway

import com.harada.domainmodel.user.UpdateUser
import com.harada.domainmodel.user.User
import com.harada.domainmodel.user.UserId
import com.harada.driver.dao.UserDao
import com.harada.driver.entity.UserEntity
import com.harada.driver.entity.UserUpdateEntity
import com.harada.getDateTimeNow
import com.harada.port.UserWriteStore
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class UserWritePostgresDB(private val dao: UserDao.Companion, private val db: Database) :
    UserWriteStore {
    override fun save(user: User): UserId = transaction(db = db) {
        dao.create(
            UserEntity(
                UUID.randomUUID(),
                user.name.value,
                user.mail.value,
                LocalDate.ofInstant(user.birthday.toInstant(), ZoneId.of("UTC")),
                getDateTimeNow(),
                getDateTimeNow()
            )
        ).let {
            UserId(it)
        }
    }

    override fun update(userId: UserId, user: UpdateUser) {
        transaction(db = db) {
            dao.update(
                UserUpdateEntity(
                    id = userId.value,
                    name = user.name?.value,
                    birthday = user.birthday?.let { LocalDate.ofInstant(it.toInstant(), ZoneId.of("UTC")) },
                    mail = user.mail?.value,
                    updatedAt = getDateTimeNow()
                )
            )
        }
    }
}