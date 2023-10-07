package com.example.muiscplayerproject.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.muiscplayerproject.typeConverter.Converters

@Database(entities = [SongEntity::class], version = 5)
@TypeConverters(Converters::class)
abstract class MusicDatabase: RoomDatabase() {
    abstract val dao:MusicDao
    companion object{
        @Volatile
        private var instance: MusicDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?:
        synchronized(LOCK){
            instance ?:
            createDatabase(context).also{
                instance = it
            }
        }

        private fun createDatabase(context: Context)=
            Room.databaseBuilder(
                context.applicationContext,
                MusicDatabase::class.java,
                "note_db"
            ).fallbackToDestructiveMigration()
                .build()

    }

}