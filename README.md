# Comrade 🤝

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


## How to Use (Not yet Live)
Implement the Comrade SDK in your app and call the backup method.

```groovy
implementation "com.yogeshpaliyal:comrade-sdk:0.0.1"
```

Initialize the Comrade

```kotlin
val comrade = Comrade(context, listener)
comrade.backup(fileToBackup)
```


## Discussion Thread
- https://bsky.app/profile/yogeshpaliyal.com/post/3levtmlemc22g
- https://www.reddit.com/r/androiddev/s/LYNsyXnrdI
