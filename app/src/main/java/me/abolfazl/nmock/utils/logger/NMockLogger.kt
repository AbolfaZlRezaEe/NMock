package me.abolfazl.nmock.utils.logger

import android.content.Context
import android.content.SharedPreferences
import io.sentry.Attachment
import io.sentry.Sentry
import io.sentry.SentryLevel
import me.abolfazl.nmock.utils.SHARED_FIREBASE_TOKEN
import me.abolfazl.nmock.utils.SHARED_LOG_CODE
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

        // Log keys
        private const val KEY_LOG_START_TIME_TITLE = "START time/data"
        private const val KEY_LOG_ANDROID_ID = "Android-Id"
        private const val KEY_LOG_END_TIME_TITLE = "END time/data"
        private const val KEY_LOG_FIREBASE_TOKEN = "Firebase Token"

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
    private var logsRemoved = false

    private var className: String? = null

    init {
        initializeLoggerPlace()
    }

    private fun initializeLoggerPlace() {
        try {
            createDirectoryIfNotExist()
            file = createFileIfNotExist()
            if (logsRemoved) logsRemoved = false
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
        setTime: Boolean = true,
        setClassName: Boolean = true
    ) {
        if (logsRemoved) {
            initializeLoggerPlace()
            className?.let { name ->
                attachLogger(name, true)
            }
        }
        if (!loggerAttached && !attachingProcessDisabled) {
            throw IllegalStateException("Writing log into file was failed. you should attach logger to this class or you should disable log header!")
        }
        file?.let { nonNullFile ->
            var message = if (key != null) "${key}: $value" else value
            message = if (setTime) "${getRealTime()}-> $message" else message
            message = if (className != null && setClassName) "$className: $message" else message
            nonNullFile.appendText("${message}\n")
            Timber.i(message)
        }
    }

    private fun attachLogger(
        className: String,
        forceAttach: Boolean
    ) {
        if (attachingProcessDisabled && !forceAttach) {
            throw IllegalStateException("Attaching logger to class was failed. why you are trying to attach this class when you disabled the logger header?")
        }
        loggerAttached = true
        this.className = className

        writeLog(
            value = createLogTitle(getLogCode()),
            setTime = false,
            setClassName = false
        )
        writeLog(
            key = KEY_LOG_START_TIME_TITLE, value = getRealTime(),
            setTime = false,
            setClassName = false
        )
        writeLog(
            key = KEY_LOG_ANDROID_ID,
            value = androidId,
            setTime = false,
            setClassName = false
        )
        writeLog(
            key = KEY_LOG_FIREBASE_TOKEN,
            value = getFirebaseToken()!!,
            setTime = false,
            setClassName = false
        )
        writeLog(
            value = TIME_SEPARATOR,
            setTime = false,
            setClassName = false
        )

        SharedManager.putInt(
            sharedPreferences = sharedPreferences,
            key = SHARED_LOG_CODE,
            value = getLogCode() + 1
        )
    }

    fun attachLogger(className: String) {
        attachLogger(className, false)
    }

    fun detachLogger() {
        if (attachingProcessDisabled) {
            throw IllegalStateException("Detaching logger from class was failed. why you are trying to detach this class when you disabled the logger header?")
        }
        loggerAttached = false
        writeLog(value = TIME_SEPARATOR, setTime = false)
        writeLog(key = KEY_LOG_END_TIME_TITLE, value = getRealTime(), setTime = false)
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

    fun sendLogsFile(
        fromExceptionHandler: Boolean = false,
        fromPush: Boolean = false,
        message: String? = null,
        sentryEventLevel: SentryLevel = SentryLevel.INFO
    ) {
        if (fromExceptionHandler || fromPush || logCanSend()) {
            Sentry.configureScope {
                it.addAttachment(Attachment(filePath))
            }
            if (fromPush) {
                Sentry.captureMessage("Logs sent from Push", sentryEventLevel)
            } else {
                val eventMessage = message ?: "Logs Reported from $androidId"
                Sentry.captureMessage(eventMessage, sentryEventLevel)
            }
            Sentry.configureScope {
                it.clearAttachments()
            }
        }
    }

    fun clearLogsFile() {
        file?.delete()
        file = null
        logsRemoved = true
    }

    private fun logCanSend(): Boolean {
        if (logsRemoved || file == null) return false
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

    private fun getLogCode() = SharedManager.getInt(
        sharedPreferences = sharedPreferences,
        key = SHARED_LOG_CODE,
        defaultValue = 0
    )

    private fun getFirebaseToken(): String? = SharedManager.getString(
        sharedPreferences = sharedPreferences,
        key = SHARED_FIREBASE_TOKEN,
        defaultValue = "NoN"
    )

    private fun createLogTitle(code: Int) =
        "code$code---------------------------------------------------------------------"
}