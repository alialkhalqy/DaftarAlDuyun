package com.daftar.alduyun.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PersonEntity::class, TransactionEntity::class, LabelEntity::class],
    version = 1,
    exportSchema = true
)
abstract class DaftarDatabase : RoomDatabase() {
    abstract fun dao(): DaftarDao

    companion object {
        @Volatile private var INSTANCE: DaftarDatabase? = null

        fun get(context: Context): DaftarDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DaftarDatabase::class.java,
                    "daftar_alduyun.db"
                ).build().also { INSTANCE = it }
            }
    }
}
