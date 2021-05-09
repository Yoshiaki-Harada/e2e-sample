package com.harada.usecase

import org.jetbrains.exposed.sql.transactions.transaction

class DbTransactionalExecutor : TransactionalExecutor {
    override fun <T> run(block: () -> T): T = transaction {
        block()
    }
}