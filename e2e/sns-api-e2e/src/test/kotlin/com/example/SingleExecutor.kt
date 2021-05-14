package com.example

import java.util.concurrent.atomic.AtomicBoolean


object SingleExecutor {
    private val done: AtomicBoolean = AtomicBoolean(false)
    fun execute(block: () -> Any) {
        if (done.get()) return
        done.set(true)
        block()
    }
}