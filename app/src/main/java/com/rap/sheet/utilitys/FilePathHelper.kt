package com.rap.sheet.utilitys

import android.annotation.TargetApi
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import java.io.*

class FilePathHelper {
    fun getMimeType(url: String): String? {
        var type: String? = null
        val extension: String? = MimeTypeMap.getFileExtensionFromUrl(url.replace(" ", ""))
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    fun getFilePathFromURI(contentUri: Uri?, context: Context): String? {
        //copy file and send new file path
        val fileName: String? = getFileName(contentUri)
        if (!TextUtils.isEmpty(fileName)) {
            val copyFile: File = File(context.externalCacheDir.toString() + File.separator + fileName)
            copy(context, contentUri, copyFile)
            return copyFile.absolutePath
        }
        return null
    }

    fun copy(context: Context, srcUri: Uri?, dstFile: File?) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream((srcUri)!!)
            if (inputStream == null) return
            val outputStream: OutputStream = FileOutputStream(dstFile)
            // IOUtils.copy(inputStream, outputStream);
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getPath(uri: Uri, context: Context): String? {
        var filePath: String? = null
        val isKitKat: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        if (isKitKat) {
            filePath = generateFromKitkat(uri, context)
        }
        if (filePath != null) {
            return filePath
        }
        val cursor: Cursor? = context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DATA), null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                filePath = cursor.getString(columnIndex)
            }
            cursor.close()
        }
        return if (filePath == null) uri.path else filePath
    }

    @TargetApi(19)
    private fun generateFromKitkat(uri: Uri, context: Context): String? {
        var filePath: String? = null
        if (DocumentsContract.isDocumentUri(context, uri)) {
            val wholeID: String = DocumentsContract.getDocumentId(uri)
            val id: String = wholeID.split(":").toTypedArray().get(1)
            val column: Array<String> = arrayOf(MediaStore.Video.Media.DATA)
            val sel: String = MediaStore.Video.Media._ID + "=?"
            val cursor: Cursor? = context.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    column, sel, arrayOf(id), null)
            val columnIndex: Int = cursor!!.getColumnIndex(column.get(0))
            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex)
            }
            cursor.close()
        }
        return filePath
    }

    fun getFileName(uri: Uri?): String? {
        if (uri == null) return null
        var fileName: String? = null
        val path: String? = uri.path
        val cut: Int = path!!.lastIndexOf('/')
        if (cut != -1) {
            fileName = path.substring(cut + 1)
        }
        return fileName
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun getPathnew(uri: Uri, context: Context): String? {
        val isKitKat: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId: String = DocumentsContract.getDocumentId(uri)
                val split: Array<String> = docId.split(":").toTypedArray()
                val type: String = split.get(0)
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split.get(1)
                }
                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id: String = DocumentsContract.getDocumentId(uri)
                val contentUri: Uri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId: String = DocumentsContract.getDocumentId(uri)
                val split: Array<String> = docId.split(":").toTypedArray()
                val type: String = split.get(0)
                var contentUri: Uri? = null
                if (("image" == type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if (("video" == type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if (("audio" == type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection: String = "_id=?"
                val selectionArgs: Array<String> = arrayOf(split.get(1))
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            // Return the remote address
            if (isGooglePhotosUri(uri)) return uri.lastPathSegment
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column: String = "_data"
        val projection: Array<String> = arrayOf(column)
        try {
            cursor = context.contentResolver.query((uri)!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Something with exception - " + e.toString())
        } finally {
            if (cursor != null) cursor.close()
        }
        return null
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return ("com.android.externalstorage.documents" == uri.authority)
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */

    fun isDownloadsDocument(uri: Uri): Boolean {
        return ("com.android.providers.downloads.documents" == uri.authority)
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return ("com.android.providers.media.documents" == uri.authority)
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return ("com.google.android.apps.photos.content" == uri.authority)
    }
}