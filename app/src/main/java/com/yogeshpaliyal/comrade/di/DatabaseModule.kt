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
import javax.inject.Singleton

private const val DB_NAME = "comrade.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideComradeQueries(database: Database): ComradeBackupQueries {
        return database.comradeBackupQueries
    }

    @Provides
    @Singleton
    fun provideDatabase(databaseProvider: DatabaseProvider): Database {
        val db by databaseProvider
        return db
    }
}
