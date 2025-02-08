package com.yogeshpaliyal.comrade.types


enum class BackupStatus(val status: Long) {
    BACKUP_PENDING(1),
    BACKUP_IN_PROGRESS(2),
    BACKUP_COMPLETED(3),
    BACKUP_FAILED(4),
    UNKNOWN(5);

    companion object{
        fun valueOf(value: Long) = entries.find { it.status == value } ?: UNKNOWN
    }
}
