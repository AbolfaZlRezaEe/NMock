package me.abolfazl.nmock.utils.managers

import java.io.File

object FileManager {

    fun writeTextToFileWithPath(
        mainDirectory: String,
        directoryName: String,
        fileName: String,
        text: String
    ): File {
        val directoryPath = mainDirectory + directoryName
        checkDirectoryExistAndCreateIfNot(directoryPath)
        val filePath = directoryPath + File.separator + fileName
        val file = checkFileExistAndCreateIfNot(filePath)
        file.writeText(text)
        return file
    }

    fun checkFileExistAndCreateIfNot(filePath: String): File {
        val file = File(filePath)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }

    fun checkDirectoryExistAndCreateIfNot(directoryPath: String) {
        val directory = File(directoryPath)
        if (directory.exists() && directory.isDirectory) return
        directory.mkdir()
    }
}