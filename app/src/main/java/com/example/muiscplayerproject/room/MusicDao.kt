package com.example.muiscplayerproject.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface MusicDao {
    @Query("SELECT COUNT(*) FROM songs WHERE name = :name")
    fun doesSongNameExist(name: String): Int
    @Upsert
     fun insertSongToFavorites(song:SongEntity)
    @Query("DELETE FROM songs WHERE name = :songName")
     fun deleteSongByName(songName: String):Int
     @Query("SELECT * FROM songs")
    fun getAllSongs(): List<SongEntity>
}