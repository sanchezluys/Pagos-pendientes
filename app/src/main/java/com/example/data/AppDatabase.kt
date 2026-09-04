package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [PaymentReminder::class, CategoryItem::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pagos_database.db"
                ).addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate default categories
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getInstance(context).paymentDao()
                            val defaults = listOf(
                                CategoryItem(name = "Servicios", isDefault = true, isActive = true),
                                CategoryItem(name = "Deudas", isDefault = true, isActive = true),
                                CategoryItem(name = "Créditos", isDefault = true, isActive = true),
                                CategoryItem(name = "Impuestos", isDefault = true, isActive = true),
                                CategoryItem(name = "Otros", isDefault = true, isActive = true)
                            )
                            defaults.forEach { dao.insertCategory(it) }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
