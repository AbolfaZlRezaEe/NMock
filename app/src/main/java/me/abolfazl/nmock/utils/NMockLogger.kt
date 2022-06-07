package me.abolfazl.nmock.utils

import android.content.Context
import io.sentry.Sentry
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NMockLogger constructor(
    className: String,
    context: Context
) {
    companion object {
        private const val DIRECTORY_NAME = "NDH" // NMock-Debugger-Helper
        private const val TIME_PATTERN = "yyyy.MMMMM.dd hh:mm aaa"
        private const val START_TIME_TITLE_KEY = "START time/data"
        private const val END_TIME_TITLE_KEY = "END time/data"
        private const val TIME_SEPARATOR =
            "***************************************************************************"
        private const val LOG_SEPARATOR =
            "---------------------------------------------------------------------------"
    }

    private val fileName: String = "ND${className}SY"
    private val directoryPath: String =
        context.filesDir.toString() + File.separator + DIRECTORY_NAME
    private val filePath = directoryPath + File.separator + "${fileName}.txt"
    private var file: File? = null
    private var dataFormat: SimpleDateFormat = SimpleDateFormat(TIME_PATTERN, Locale.US)

    init {
        attachLogger()
    }

    private fun createDirectoryIfNotExist() {
        val directory = File(directoryPath)
        if (directory.exists() && directory.isDirectory) return
        directory.mkdir()
    }

    private fun createFileIfNotExist(): File {
        val file = File(filePath)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }

    fun writeLog(
        key: String? = null,
        value: String,
        setTime: Boolean = true
    ) {
        file?.let { nonNullFile ->
            var message = if (key != null) "${key}: $value" else value
            message = if (setTime) "${getRealTime()}-> $message" else message
            nonNullFile.appendText("${message}\n")
        }
    }

    private fun attachLogger() {
        try {
            createDirectoryIfNotExist()
            file = createFileIfNotExist()
        } catch (exception: Exception) {
            Sentry.captureMessage("we couldn't create file/directory. message was-> ${exception.message}")
        }
        writeLog(key = START_TIME_TITLE_KEY, value = getRealTime())
        writeLog(value = TIME_SEPARATOR)
    }

    fun detachLogger() {
        writeLog(value = TIME_SEPARATOR)
        writeLog(key = END_TIME_TITLE_KEY, value = getRealTime())
        writeLog(value = LOG_SEPARATOR)
    }

    private fun getRealTime() = dataFormat.format(Calendar.getInstance().time)
}