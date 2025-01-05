# BackupApp

```mermaid
sequenceDiagram
box Offline App
participant ClientApp
participant BackupAppSDK
end
box Internet Connected App
participant BackupApp
participant GoogleDrive
end
    ClientApp->>+BackupAppSDK: Backup Initiated
    BackupAppSDK->>+BackupApp: Check if App exists on user's device
    BackupAppSDK->>+BackupApp: Copy the app using File Provider and give read access to Backup App
    BackupAppSDK->>+BackupApp: Send app contentUri
    BackupApp->>+GoogleDrive: Upload File to Drive
    BackupApp->>+GoogleDrive: Modify the Backup DB File
    GoogleDrive-->>-BackupApp: Success callback for file backed up
    BackupApp-->>-BackupAppSDK: Success callback for file backed up
    BackupAppSDK-->>-ClientApp: Backup completed
```


## How to Use
Implement the BackupAppSDK in your app and call the backup method.

```groovy
implementation "testing:0.0.1"
```

```kotlin
val backupApp = BackupApp(context, listener)
backupApp.backup(fileToBackup)
```


## Discussion Thread
- https://bsky.app/profile/yogeshpaliyal.com/post/3levtmlemc22g
- https://www.reddit.com/r/androiddev/s/LYNsyXnrdI
