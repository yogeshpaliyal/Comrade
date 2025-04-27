package com.yogeshpaliyal.comrade.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.IOException
import java.util.Collections

class DriveServiceHelper(
    private val context: Context,
    private val account: GoogleSignInAccount
) {
    private val drive: Drive
    
    companion object {
        private const val TAG = "DriveServiceHelper"
    }
    
    init {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singleton(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account
        
        drive = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName("Comrade")
            .build()
    }
    
    // Create folder if it doesn't exist and return folder ID
    suspend fun createFolderIfNotExists(folderName: String): String = withContext(Dispatchers.IO) {
        val folderList = drive.files().list()
            .setQ("mimeType='application/vnd.google-apps.folder' and name='$folderName' and trashed=false")
            .setSpaces("drive")
            .execute()
        
        if (folderList.files != null && folderList.files.isNotEmpty()) {
            return@withContext folderList.files[0].id
        }
        
        // Folder doesn't exist, create it
        val folderMetadata = File()
            .setName(folderName)
            .setMimeType("application/vnd.google-apps.folder")
        
        val folder = drive.files().create(folderMetadata)
            .setFields("id")
            .execute()
        
        return@withContext folder.id
    }
    
    // Upload a file to Google Drive
    suspend fun uploadFile(localFile: java.io.File, folderId: String, fileName: String, mimeType: String?): String? = withContext(Dispatchers.IO) {
        try {
            // Check if file with the same name already exists
            val query = "name='$fileName' and '$folderId' in parents and trashed=false"
            val fileList = drive.files().list()
                .setQ(query)
                .setSpaces("drive")
                .execute()
            
            // If file exists, update it
            if (fileList.files != null && fileList.files.isNotEmpty()) {
                val fileId = fileList.files[0].id
                val existingFile = drive.files().get(fileId).execute()
                
                // Update the file content
                drive.files().update(fileId, null, com.google.api.client.http.FileContent(mimeType, localFile))
                    .execute()
                
                Log.d(TAG, "Updated file: $fileName")
                return@withContext fileId
            } else {
                // File doesn't exist, create it
                val fileMetadata = File()
                    .setName(fileName)
                    .setMimeType(mimeType)
                    .setParents(Collections.singletonList(folderId))
                
                val uploadedFile = drive.files().create(fileMetadata, com.google.api.client.http.FileContent(mimeType, localFile))
                    .setFields("id")
                    .execute()
                
                Log.d(TAG, "Created file: $fileName")
                return@withContext uploadedFile.id
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading file", e)
            return@withContext null
        }
    }
    
    // List all files in a folder
    suspend fun listFilesInFolder(folderId: String): List<File> = withContext(Dispatchers.IO) {
        val result = mutableListOf<File>()
        var pageToken: String? = null
        
        do {
            val fileList: FileList = drive.files().list()
                .setQ("'$folderId' in parents and trashed=false")
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name, mimeType)")
                .setPageToken(pageToken)
                .execute()
            
            result.addAll(fileList.files)
            pageToken = fileList.nextPageToken
        } while (pageToken != null)
        
        return@withContext result
    }
    
    // Download a file from Google Drive
    suspend fun downloadFile(fileId: String, destinationFile: java.io.File): Boolean = withContext(Dispatchers.IO) {
        try {
            val outputStream = FileOutputStream(destinationFile)
            drive.files().get(fileId)
                .executeMediaAndDownloadTo(outputStream)
            outputStream.flush()
            outputStream.close()
            return@withContext true
        } catch (e: IOException) {
            Log.e(TAG, "Error downloading file", e)
            return@withContext false
        }
    }
}
