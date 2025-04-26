package com.yogeshpaliyal.comrade.di

import android.content.Context
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.yogeshpaliyal.comrade.Database
import com.yogeshpaliyal.comrade.types.BackupStatus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import data.ComradeBackup
import data.ComradeBackupQueries
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun getDatabase(@ApplicationContext context: Context): Database {
        val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context, "comrade.db", )

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

    @Provides
    @Singleton
    fun getComradeTable(database: Database): ComradeBackupQueries {
        return database.comradeBackupQueries
    }

}
