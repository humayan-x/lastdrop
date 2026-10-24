package io.github.humayanx.lastdrop.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.humayanx.lastdrop.data.local.entity.NotificationFts
import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord

@Database(
    entities = [
        NotificationRecord::class,
        NotificationFts::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun notificationDao(): NotificationDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notifications ADD COLUMN notificationKey TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_notificationKey_timestamp ON notifications(notificationKey, timestamp)")
            }
        }

        private const val DATABASE_NAME = "lastdrop_notifications.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
