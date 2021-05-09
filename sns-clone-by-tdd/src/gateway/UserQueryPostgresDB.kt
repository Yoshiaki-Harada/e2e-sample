package com.harada.gateway

import com.harada.domainmodel.user.UserFilter
import com.harada.domainmodel.user.UserId
import com.harada.driver.dao.SqlOldFilter
import com.harada.driver.dao.SqlUserFilter
import com.harada.driver.dao.UserDao
import com.harada.formatter
import com.harada.port.UserNotFoundException
import com.harada.port.UserQueryService
import com.harada.viewmodel.UserInfo
import com.harada.viewmodel.UsersInfo
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.time.LocalDate
import java.time.ZoneId

class UserQueryPostgresDB(private val dao: UserDao.Companion, private val db: Database) :
    UserQueryService {
    override fun get(filter: UserFilter): UsersInfo = transaction(
        db = db,
        transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
        repetitionAttempts = 2
    ) {
        val users = dao.get(SqlUserFilter(
            nameFilter = filter.name?.value,
            oldFilter = filter.old?.let {
                val thisYearMonth = LocalDate.now()
                val fromYearMonth = thisYearMonth.minusYears(filter.old.to.toLong())
                val toYearMonth = thisYearMonth.minusYears(filter.old.from.toLong())
                SqlOldFilter(fromYearMonth, toYearMonth)
            }
        ))

        return@transaction UsersInfo(users.sortedByDescending { it.createdAt }.map { it.toInfo() })
    }

    override fun get(id: UserId) = transaction(
        db = db,
        transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
        repetitionAttempts = 2
    ) {
        dao.findById(id.value)?.toInfo() ?: throw UserNotFoundException(userId = id.value)
    }

    override fun isFoundByMail(mail: String) = transaction(
        db = db,
        transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
        repetitionAttempts = 2
    ) { dao.getByMail(mail).isNotEmpty() }


    override fun isNotFound(id: UserId): Boolean =
        transaction(
            db = db,
            transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
            repetitionAttempts = 2
        ) {
            dao.findById(id.value) == null
        }
}

fun UserDao.toInfo() = UserInfo(
    id = id.value.toString(),
    name = name,
    mail = mail,
    birthday = birthday.toString(),
    createdAt = createdAt.atZone(ZoneId.of("UTC")).format(formatter),
    updatedAt = updatedAt.atZone(ZoneId.of("UTC")).format(formatter)
)
