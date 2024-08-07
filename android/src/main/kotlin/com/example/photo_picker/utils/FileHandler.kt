package com.example.photo_picker.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream

class FileHandler(private val context: Context) {

    private var _result: ResultCallback? = null

    interface ResultCallback {
        fun success(path: String)
        fun failure(error: String)
    }

    fun setResultCallback(callback: ResultCallback) {
        _result = callback
    }

    fun getPathFromUri(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                context.contentResolver.openInputStream(uri).use { inputStream ->
                    if (inputStream != null) {
                        val file = createTemporalFileFrom(inputStream)
                        // create thumbnail
                        withContext(Dispatchers.Main) {
                            _result?.success(file.absolutePath)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _result?.success("")
                        }
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    _result?.failure("IOException occurred")
                }
            } catch (e: SecurityException) {
                withContext(Dispatchers.Main) {
                    _result?.failure("SecurityException occurred $e")
                }
            }
        }
    }

    private fun createTemporalFileFrom(inputStream: InputStream): File {
        val tempFile = File.createTempFile("video_picker_handler", null, context.cacheDir)
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    fun deleteAll() {
        // delete all files in cache directory with video_picker_handler
        val cacheDir = context.cacheDir
        val files = cacheDir.listFiles()
        files?.forEach {
            if (it.name.startsWith("video_picker_handler")) {
                it.delete()
            }
        }
    }
}