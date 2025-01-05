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
package com.yogeshpaliyal.comrade.utils;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
public class DriveServiceHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    public DriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    public Task<String> createFile() {
        return Tasks.call(mExecutor, () -> {
                File metadata = new File()
                        .setParents(Collections.singletonList("root"))
                        .setMimeType("text/plain")
                        .setName("Untitled file");

                File googleFile = mDriveService.files().create(metadata).execute();
                if (googleFile == null) {
                    throw new IOException("Null result when requesting file creation.");
                }

                return googleFile.getId();
            });
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code name} and {@code
     * content}.
     */
    public Task<Void> saveFile(String fileId, String name, String content) {
        return Tasks.call(mExecutor, () -> {
                // Create a File containing any metadata changes.
                File metadata = new File().setName(name);

                // Convert content to an AbstractInputStreamContent instance.
                ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

                // Update the metadata and contents.
                mDriveService.files().update(fileId, metadata, contentStream).execute();
                return null;
            });
    }

    /**
     * Returns a {@link FileList} containing all the visible files in the user's My Drive.
     *
     * <p>The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the <a href="https://play.google.com/apps/publish">Google
     * Developer's Console</a> and be submitted to Google for verification.</p>
     */
    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, () ->
                mDriveService.files().list().setSpaces("drive").execute());
    }

    /**
     * Returns an {@link Intent} for opening the Storage Access Framework file picker.
     */
    public Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        return intent;
    }


}