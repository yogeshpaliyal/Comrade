package com.yogeshpaliyal.comrade.di

import android.content.Context
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.yogeshpaliyal.comrade.Database
import com.yogeshpaliyal.comrade.types.BackupStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import data.ComradeBackup
import javax.inject.Inject
import kotlin.reflect.KProperty

private const val DB_NAME = "comrade.db"

class DatabaseProvider @Inject constructor(@ApplicationContext val context: Context) {

    private var database: Database? = null
    private var driverInstance: SqlDriver? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Database {
        if (database == null) {
            val driver = createDriver(context)
            database = createDatabaseWithDriver(driver)
        }
        return database!!
    }

    fun recreateDatabase(context: Context): Database {

        // Close existing driver
        try {
            driverInstance?.close()
        } catch (e: Exception) {
            // Ignore, might be already closed
        }

        // Create new driver and database
        val driver = createDriver(context)
        database = createDatabaseWithDriver(driver)
        return database!!

    }

    fun resetDatabase() {
        recreateDatabase(context)
    }

    private fun createDriver(context: Context): SqlDriver {
        val driver = AndroidSqliteDriver(Database.Schema, context, DB_NAME)
        driverInstance = driver
        return driver
    }

    private fun createDatabaseWithDriver(driver: SqlDriver): Database {
        val backupStatusAdapter = object : ColumnAdapter<BackupStatus, Long> {
            override fun decode(databaseValue: Long): BackupStatus {
                return BackupStatus.valueOf(databaseValue)
            }

            override fun encode(value: BackupStatus): Long {
                return value.status
            }
        }

        return Database(
            driver,
            comradeBackupAdapter = ComradeBackup.Adapter(
                backupStatusAdapter = backupStatusAdapter
            )
        )
    }

}