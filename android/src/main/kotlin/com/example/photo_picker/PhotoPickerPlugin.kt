package com.example.photo_picker

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.photo_picker.utils.FileHandler
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry


/** PhotoPickerPlugin */
class PhotoPickerPlugin : FlutterPlugin, ActivityAware, MethodCallHandler, PluginRegistry.ActivityResultListener,
    PluginRegistry.RequestPermissionsResultListener {
    private lateinit var channel: MethodChannel
    private val REQUEST_CODE_CHOOSE_VIDEO_FROM_GALLERY = 2505
    private val PERMISSION_REQUEST_CODE = 1
    private var context: Context? = null
    private var thisActivity: Activity? = null
    private var _result: Result? = null
    private var _fileHandler: FileHandler? = null
    private var activityBinding: ActivityPluginBinding? = null

    private val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.MIME_TYPE,
        MediaStore.Video.Media.DURATION
    )


    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "photo_picker")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        _fileHandler = FileHandler(flutterPluginBinding.applicationContext)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        _result = result
        when (call.method) {
            "getPlatformVersion" -> {
                _result?.success("Android ${Build.VERSION.RELEASE}")
            }

            "pickMedia" -> {
                launchPickVideoFromGalleryIntent()
            }

            "requestPermission" -> {
                requestPermission()
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    /** call {@code setup} when the activity is attached*/
    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        setup(binding)
    }

    private fun setup(binding: ActivityPluginBinding) {
        activityBinding = binding
        binding.addActivityResultListener(this)
        binding.addRequestPermissionsResultListener(this)
        thisActivity = binding.activity

    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        // Clean up any resources when the activity is destroyed.
        this.tearDown()
    }

    private fun tearDown() {
        this.activityBinding?.removeActivityResultListener(this)
        this.activityBinding?.removeRequestPermissionsResultListener(this)
        this.activityBinding = null
        channel.setMethodCallHandler(null)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                return if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // permission was granted, yay! Do the
                    Log.d("Permission", "Permission granted")
                    _result?.success(true)
                    true
                } else {
                    // permission denied, boo! Disable the
                    Log.d("Permission", "Permission denied")
                    _result?.success(false)
                    false
                }
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
                return false
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == REQUEST_CODE_CHOOSE_VIDEO_FROM_GALLERY) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // get image
                if (data.data != null) {
                    getMediaFormUri(data.data!!)
                } else {
                    val clipData = data.clipData
                    if (clipData != null) {
                        getMediaFormUri(clipData.getItemAt(0).uri)
                    } else {
                        _result?.success(null)
                    }
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                _result?.success(null)
            }
        }
        return false
    }

    private fun getMediaFormUri(uri: Uri) {
        val image = getImages(context!!.contentResolver, uri)
        if (image == null) {
            _result?.success(null)
            return
        }
        val path = image.path
        if (path.isEmpty()) {
            _fileHandler?.setResultCallback(object : FileHandler.ResultCallback {
                override fun success(path: String) {
                    image.path = path
                    _result?.success(image.toMap())
                }

                override fun failure(error: String) {
                    _result?.success(null)
                }
            })
            _fileHandler?.getPathFromUri(uri)
        } else {
            _result?.success(image.toMap())
        }
    }

    private fun launchPickVideoFromGalleryIntent() {
        val pickVideoIntent: Intent = ActivityResultContracts.PickVisualMedia().createIntent(
            context!!, PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.VideoOnly
            )
        )
        thisActivity!!.startActivityForResult(pickVideoIntent, REQUEST_CODE_CHOOSE_VIDEO_FROM_GALLERY)
    }

    private fun getImages(contentResolver: ContentResolver, uri: Uri): Media? {
        contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val media = cursor.toMedia()
                if (media.path.isEmpty()) {
                    val wholeID = DocumentsContract.getDocumentId(uri)
                    val id = wholeID.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                    contentResolver.query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        "${MediaStore.Video.Media._ID} = ?",
                        arrayOf(id),
                        null
                    )?.use { newCursor ->
                        if (newCursor.moveToFirst()) {
                            newCursor.toMedia().let {
                                return it
                            }
                        }
                    }
                } else {
                    return media
                }

            }
        }

        return null
    }

    private fun Cursor.toMedia(): Media {
        val displayNameColumn = getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val sizeColumn = getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
        val mimeTypeColumn = getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
        val dataPath = getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        val durationColumn = getColumnIndex(MediaStore.Video.Media.DURATION)

        val name = getString(displayNameColumn)
        val size = getLong(sizeColumn)
        val mimeType = getString(mimeTypeColumn)
        val duration = getInt(durationColumn)
        val path = getString(dataPath) ?: ""

        return Media(path, name, size, mimeType, duration)
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ActivityCompat.requestPermissions(
                thisActivity!!,
                arrayOf(READ_MEDIA_VISUAL_USER_SELECTED, READ_MEDIA_IMAGES, READ_MEDIA_VIDEO),
                PERMISSION_REQUEST_CODE
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                thisActivity!!,
                arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // Full access up to Android 12 (API level 32)
            ActivityCompat.requestPermissions(
                thisActivity!!,
                arrayOf(READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }


}

data class Media(
    var path: String,
    val name: String,
    val size: Long,
    val mimeType: String,
    val duration: Int
    // to map
) {
    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "path" to path,
            "name" to name,
            "size" to size,
            "duration" to duration,
            "mimeType" to mimeType,
        )
    }
}



