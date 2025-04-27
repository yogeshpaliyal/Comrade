package com.yogeshpaliyal.comrade.di

import android.content.Context
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.yogeshpaliyal.comrade.Database
import com.yogeshpaliyal.comrade.types.BackupStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import data.ComradeBackup
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onSubscription
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private const val DB_NAME = "comrade.db"

@Singleton
class DatabaseProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : ReadOnlyProperty<Any?, Database> {

    private var databaseInstance: Database? = null
    private var driverInstance: SqlDriver? = null
    private var emitter: Int = 0

    // SharedFlow to signal that database has been refreshed
    // Use replay = 1 to ensure new subscribers get the last state immediately
    // Use tryEmit(Unit) in init to provide an initial trigger
    private val _databaseRefreshFlow = MutableSharedFlow<Int>(replay = 1).apply {
        tryEmit(emitter++) // Emit initial value
    }
    val databaseRefreshFlow = _databaseRefreshFlow.asSharedFlow()

    @Synchronized
    override fun getValue(thisRef: Any?, property: KProperty<*>): Database {
        if (databaseInstance == null) {
            databaseInstance = createDatabase()
        }
        return databaseInstance!!
    }

    @Synchronized
    fun resetDatabase() {
        // Close existing driver
        try {
            driverInstance?.close()
        } catch (e: Exception) {
            // Ignore errors when closing
        }
        // Emit event that database has been refreshed
        _databaseRefreshFlow.tryEmit(emitter++)
        // Create new database instance
        databaseInstance = createDatabase()
    }

    private fun createDatabase(): Database {
        val driver = AndroidSqliteDriver(Database.Schema, context, DB_NAME)
        driverInstance = driver

        val backupStatusAdapter = object : ColumnAdapter<BackupStatus, Long> {
            override fun decode(databaseValue: Long): BackupStatus {
                return BackupStatus.valueOf(databaseValue)
            }

            override fun encode(value: BackupStatus): Long {
                return value.status
            }
        }

        val db = Database(
            driver,
            comradeBackupAdapter = ComradeBackup.Adapter(
                backupStatusAdapter = backupStatusAdapter
            )
        )
        return db
    }
}
