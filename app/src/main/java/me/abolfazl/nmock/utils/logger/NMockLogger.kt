package me.abolfazl.nmock.utils.logger

import android.content.Context
import android.content.SharedPreferences
import io.sentry.Attachment
import io.sentry.Sentry
import io.sentry.SentryLevel
import me.abolfazl.nmock.utils.SHARED_LOG_TIME
import me.abolfazl.nmock.utils.managers.SharedManager
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NMockLogger constructor(
    private val androidId: String,
    private val sharedPreferences: SharedPreferences,
    fileName: String,
    context: Context,
) {
    companion object {
        private const val DIRECTORY_NAME = "NDH" // NMock-Debugger-Helper

        private const val SHARED_TIME_PATTERN = "hh:mm a"
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
    private val calendar = Calendar.getInstance()

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

    fun sendLogsFile() {
        if (!logCanSend()) return
        Sentry.configureScope {
            it.addAttachment(Attachment(getFilePath()))
        }
        Sentry.captureMessage("Log Reports from $androidId", SentryLevel.INFO)
        Sentry.configureScope {
            it.clearAttachments()
        }
    }

    private fun logCanSend(): Boolean {
        val simpleDateFormat = SimpleDateFormat(SHARED_TIME_PATTERN)
        val startTime = SharedManager.getString(
            sharedPreferences = sharedPreferences,
            key = SHARED_LOG_TIME,
            defaultValue = null
        )
        if (startTime == null) {
            SharedManager.putString(
                sharedPreferences = sharedPreferences,
                key = SHARED_LOG_TIME,
                value = simpleDateFormat.format(calendar.time)
            )
            return true
        }
        val result = calculateTimes(
            data1 = simpleDateFormat.parse(startTime),
            data2 = simpleDateFormat.parse(simpleDateFormat.format(calendar.time))
        )
        if (result >= 1) {
            // Save new time to shared...
            SharedManager.putString(
                sharedPreferences = sharedPreferences,
                key = SHARED_LOG_TIME,
                value = simpleDateFormat.format(calendar.time)
            )
        }
        return result >= 1 // User can send report every one hour(avoiding to spam!)
    }

    private fun calculateTimes(
        data1: Date?,
        data2: Date?
    ): Int {
        if (data1 == null || data2 == null) return -1
        val difference = data2.time - data1.time
        val days = (difference / (1000 * 60 * 60 * 24)).toInt()
        val hours = ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60)).toInt()
        return if (hours < 0) -hours else hours
    }

    private fun getRealTime() = dataFormat.format(calendar.time)

    private fun createLogTitle(className: String) =
        "------------------------------ $className ------------------------------"
}