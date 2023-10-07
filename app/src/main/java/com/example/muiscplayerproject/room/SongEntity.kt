package com.example.muiscplayerproject.room

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs" )
data class SongEntity (
    //member variables
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var uri: Uri,
    var name: String,
    var albumartUri: Uri?,
    var singer:String,
    var albumName:String
        )