package me.abolfazl.nmock.utils.logger

import android.content.Context
import io.sentry.Sentry
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NMockLogger constructor(
    fileName: String,
    context: Context,
    private val androidId: String
) {
    companion object {
        private const val DIRECTORY_NAME = "NDH" // NMock-Debugger-Helper
        private const val TIME_PATTERN = "yyyy.MMMMM.dd hh:mm:ssaaa"
        private const val START_TIME_TITLE_KEY = "START time/data"
        private const val ANDROID_ID_KEY = "Android-Id"
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

    private var className: String? = null

    init {
        try {
            createDirectoryIfNotExist()
            file = createFileIfNotExist()
        } catch (exception: Exception) {
            // todo: try to reinitialize that...
            Sentry.captureMessage("we couldn't create file/directory. message was-> ${exception.message}")
            exception.printStackTrace()
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
            message = if (className != null) "$className: $message" else message
            nonNullFile.appendText("${message}\n")
            Timber.i(message)
        }
    }

    fun attachLogger(className: String) {
        if (attachingProcessDisabled) {
            throw IllegalStateException("Attaching logger to class was failed. why you are trying to attach this class when you disabled the logger header?")
        }
        loggerAttached = true
        writeLog(value = createLogTitle(className), setTime = false)
        writeLog(key = START_TIME_TITLE_KEY, value = getRealTime(), setTime = false)
        writeLog(key = ANDROID_ID_KEY, value = androidId, setTime = false)
        writeLog(value = TIME_SEPARATOR, setTime = false)
    }

    fun detachLogger() {
        if (attachingProcessDisabled) {
            throw IllegalStateException("Detaching logger from class was failed. why you are trying to detach this class when you disabled the logger header?")
        }
        loggerAttached = false
        writeLog(value = TIME_SEPARATOR, setTime = false)
        writeLog(key = END_TIME_TITLE_KEY, value = getRealTime(), setTime = false)
    }

    fun disableLogHeaderForThisClass() {
        attachingProcessDisabled = true
    }

    fun setClassInformationForEveryLog(className: String) {
        if (!attachingProcessDisabled) {
            throw IllegalStateException("You already attach this class to Logger! why you want to add class name for every log?")
        }
        this.className = className
    }

    fun getFilePath(): String {
        return filePath
    }

    private fun getRealTime() = dataFormat.format(Calendar.getInstance().time)

    private fun createLogTitle(className: String) =
        "------------------------------ $className ------------------------------"
}