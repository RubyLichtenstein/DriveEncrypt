package com.example.driveencrypt.utils

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.Callable

object LocalFilesExecutor {
    private val mExecutor: Executor =
        Executors.newCachedThreadPool()

    fun <V> execute(call: () -> V?): Task<V?> {
        return Tasks.call(mExecutor, Callable(call))
    }
}