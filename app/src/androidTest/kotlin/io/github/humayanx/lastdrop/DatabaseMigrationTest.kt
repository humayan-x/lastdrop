package io.github.humayanx.lastdrop

import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import io.github.humayanx.lastdrop.data.local.AppDatabase
import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DatabaseMigrationTest {
    @Test fun migrationPreservesHistoryStarsAndSearch() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "migration-test.db"
        context.deleteDatabase(name)
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context).name(name)
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("""CREATE TABLE notifications (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            packageName TEXT NOT NULL, appName TEXT NOT NULL,
                            title TEXT, content TEXT, bigText TEXT, subText TEXT,
                            timestamp INTEGER NOT NULL, isRead INTEGER NOT NULL,
                            isStarred INTEGER NOT NULL)""")
                        db.execSQL("CREATE INDEX index_notifications_packageName ON notifications(packageName)")
                        db.execSQL("CREATE INDEX index_notifications_timestamp ON notifications(timestamp)")
                        db.execSQL("CREATE VIRTUAL TABLE notifications_fts USING FTS4(title TEXT, content TEXT, content=notifications)")
                        db.execSQL("INSERT INTO notifications VALUES (1, 'chat', 'Chat', 'Alice', 'Legacy', NULL, NULL, 1, 1, 1)")
                        db.execSQL("INSERT INTO notifications_fts(notifications_fts) VALUES ('rebuild')")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                }).build()
        )
        helper.writableDatabase
        helper.close()
        val db = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(AppDatabase.MIGRATION_1_2).build()
        try {
            val dao = db.notificationDao()
            val legacy = dao.getAllNotifications().first().single()
            assertEquals(1L, legacy.id)
            assertEquals(true, legacy.isStarred)
            assertEquals(true, legacy.isRead)
            assertNull(legacy.notificationKey)
            assertEquals(1, dao.searchNotifications("Legacy").first().size)
            dao.insertUnlessDuplicate(NotificationRecord(packageName = "chat", appName = "Chat",
                notificationKey = "chat|1", title = "Bob", content = "Fresh", timestamp = 2))
            assertEquals(1, dao.searchNotifications("Fresh").first().size)
            dao.deleteOlderThan(3)
            assertEquals(1, dao.getTotalCount().first())
            assertEquals(0, dao.searchNotifications("Fresh").first().size)
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }
}
