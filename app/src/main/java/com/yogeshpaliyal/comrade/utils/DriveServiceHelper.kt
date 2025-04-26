/**
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yogeshpaliyal.comrade.utils

import android.content.Intent
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.FileContent
import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
class DriveServiceHelper(private val mDriveService: Drive) {
    private val mExecutor: Executor = Executors.newSingleThreadExecutor()

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    suspend fun createFile(fileName: String?, mimeType: String?): String? {
        return withContext(Dispatchers.IO) {
            val metadata = File()
                .setParents(mutableListOf<String?>("root"))
                .setMimeType(mimeType)
                .setName(fileName)
            val googleFile = mDriveService.files().create(metadata).execute()
            if (googleFile == null) {
                throw IOException("Null result when requesting file creation.")
            }
            googleFile.id
        }
    }

    /**
     * Updates the file identified by `fileId` with the given `name` and `content`.
     */
    suspend fun saveFile(fileId: String?, name: String?, mimeType: String?, file: java.io.File) {
        return withContext(Dispatchers.IO) {
            // Create a File containing any metadata changes.
            val metadata = File().setName(name)

            // Update the metadata and contents.
            mDriveService.files().update(fileId, metadata, FileContent(mimeType, file)).execute()
        }
    }

    /**
     * Returns a [FileList] containing all the visible files in the user's My Drive.
     *
     *
     * The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the [Google
 * Developer's Console](https://play.google.com/apps/publish) and be submitted to Google for verification.
     */
    fun queryFiles(): Task<FileList?> {
        return Tasks.call<FileList?>(
            mExecutor,
            Callable { mDriveService.files().list().setSpaces("drive").execute() })
    }

    /**
     * Returns an [Intent] for opening the Storage Access Framework file picker.
     */
    fun createFilePickerIntent(): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("text/plain")

        return intent
    }
}