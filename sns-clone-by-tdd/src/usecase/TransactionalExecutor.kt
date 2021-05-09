package com.harada.usecase

import com.harada.Injector
import org.kodein.di.generic.instance

interface TransactionalExecutor {
    fun <T> run(block: () -> T): T
}

val t by Injector.kodein.instance<TransactionalExecutor>()
fun <T> transactional(transactionalExecutor: TransactionalExecutor, block: () -> T) = transactionalExecutor.run(block)
