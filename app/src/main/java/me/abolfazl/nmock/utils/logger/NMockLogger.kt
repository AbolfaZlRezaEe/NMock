package me.abolfazl.nmock.utils.logger

import android.content.Context
import io.sentry.Sentry
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NMockLogger constructor(
    fileName: String,
    context: Context
) {
    companion object {
        private const val DIRECTORY_NAME = "NDH" // NMock-Debugger-Helper
        private const val TIME_PATTERN = "yyyy.MMMMM.dd hh:mm:ss aaa"
        private const val START_TIME_TITLE_KEY = "START time/data"
        private const val END_TIME_TITLE_KEY = "END time/data"
        private const val TIME_SEPARATOR =
            "***************************************************************************"
    }

    private val fileName: String = "ND${fileName}SY"
    private val directoryPath: String =
        context.filesDir.toString() + File.separator + DIRECTORY_NAME
    private val filePath = directoryPath + File.separator + "${this.fileName}.txt"
    private var file: File? = null
    private var dataFormat: SimpleDateFormat = SimpleDateFormat(TIME_PATTERN, Locale.US)

    private var loggerAttached = false
    private var attachingProcessDisabled = false

    init {
        try {
            createDirectoryIfNotExist()
            file = createFileIfNotExist()
        } catch (exception: Exception) {
            Sentry.captureMessage("we couldn't create file/directory. message was-> ${exception.message}")
        }
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
        if (!loggerAttached && !attachingProcessDisabled) {
            throw IllegalStateException("Writing log into file was failed. you should attach logger to this class or you should disable log header!")
        }
        file?.let { nonNullFile ->
            var message = if (key != null) "${key}: $value" else value
            message = if (setTime) "${getRealTime()}-> $message" else message
            nonNullFile.appendText("{${message}}\n")
        }
    }

    fun attachLogger(className: String) {
        if (attachingProcessDisabled) {
            throw IllegalStateException("Attaching logger to class was failed. why you are trying to attach this class when you disabled the logger header?")
        }
        loggerAttached = true
        writeLog(value = createLogTitle(className))
        writeLog(key = START_TIME_TITLE_KEY, value = getRealTime())
        writeLog(value = TIME_SEPARATOR)
    }

    fun detachLogger() {
        if (attachingProcessDisabled) {
            throw IllegalStateException("Detaching logger from class was failed. why you are trying to detach this class when you disabled the logger header?")
        }
        loggerAttached = false
        writeLog(value = TIME_SEPARATOR)
        writeLog(key = END_TIME_TITLE_KEY, value = getRealTime())
    }

    fun disableLogHeaderForThisClass() {
        attachingProcessDisabled = true
    }

    private fun getRealTime() = dataFormat.format(Calendar.getInstance().time)

    private fun createLogTitle(className: String) =
        "------------------------------ $className ------------------------------"
}

fun main() {
    println(SimpleDateFormat("yyyy.MMMMM.dd hh:mm:ssaaa", Locale.US).format(Calendar.getInstance().time))
}