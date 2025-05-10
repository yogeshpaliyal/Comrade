package com.yogeshpaliyal.comrade.common


const val COMPANION_APP_PACKAGE_NAME = "com.yogeshpaliyal.comrade"


/**
 * Intent Actions
 */
const val IA_BACKUP_REQUEST = "$COMPANION_APP_PACKAGE_NAME.backup.request"
const val IA_GET_BACKUP_FILE = "$COMPANION_APP_PACKAGE_NAME.get.backup.file"
const val IA_GET_BACKUP_FILES_LIST = "$COMPANION_APP_PACKAGE_NAME.get.backup.files.list"
const val IA_GET_BACKUP_FILE_STATUS = "$COMPANION_APP_PACKAGE_NAME.get.backup.file.status"
const val IA_FIREBASE_CRASH = "$COMPANION_APP_PACKAGE_NAME.firebase.crash"

/**
 * Intent for Callbacks
 */
const val IA_COMPANION_SETUP_COMPLETED = "$COMPANION_APP_PACKAGE_NAME.setup.uncompleted"
const val IA_BACKUP_ADDED_TO_QUEUE = "$COMPANION_APP_PACKAGE_NAME.added.to.queue"


/**
 * Extra Parameters for intents
 */
const val CLIENT_APP_PACKAGE_NAME = "clientAppPackageName"
const val SHARING_CONTENT_URI = "sharingContentUri"
