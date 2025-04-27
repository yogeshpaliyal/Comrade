package com.yogeshpaliyal.comrade.di

import android.content.Context
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.yogeshpaliyal.comrade.Database
import com.yogeshpaliyal.comrade.repository.DriveRepository
import com.yogeshpaliyal.comrade.types.BackupStatus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import data.ComradeBackup
import data.ComradeBackupQueries
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Singleton

private const val DB_NAME = "comrade.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val databaseReference = AtomicReference<Database>()
    private val databaseDriver = AtomicReference<SqlDriver>()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): Database {
        return createDatabase(context).also {
            databaseReference.set(it)
        }
    }

    @Provides
    @Singleton
    fun provideComradeQueries(@ApplicationContext database: Database): ComradeBackupQueries {
        return database.comradeBackupQueries
    }

    // Create a new database instance
    private fun createDatabase(context: Context): Database {
        val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context, DB_NAME)

        val backupStatusAdapter = object : ColumnAdapter<BackupStatus, Long> {
            override fun decode(databaseValue: Long): BackupStatus {
                return BackupStatus.valueOf(databaseValue)
            }

            override fun encode(value: BackupStatus): Long {
                return value.status
            }
        }
        databaseDriver.set(driver)
        return Database(
            driver,
            comradeBackupAdapter = ComradeBackup.Adapter(
                backupStatusAdapter = backupStatusAdapter
            )
        )
    }

    @Provides
    @Singleton
    fun provideDatabaseRestartManager(
        @ApplicationContext context: Context,
        driveRepository: DriveRepository
    ): DatabaseRestartManager {
        return DatabaseRestartManager(context, driveRepository)
    }

    class DatabaseRestartManager(
        private val context: Context,
        driveRepository: DriveRepository
    ) : DriveRepository.DatabaseRestartListener {

        init {
            driveRepository.setDatabaseRestartListener(this)
        }

        override fun onDatabaseRestarted() {
            // Close existing database connection
            val oldDatabase = databaseReference.get()
            oldDatabase?.let {
                try {
                    // Close connections
                    databaseDriver.get().close()
//                    (it.comradeBackupQueries.database.driver as? AndroidSqliteDriver)?.close()
                } catch (e: Exception) {
                    // Handle any exceptions when closing the database
                }
            }

            // Create a new database instance
            val newDatabase = createDatabase(context)
            
            // Update the reference
            databaseReference.set(newDatabase)
        }
    }
}
