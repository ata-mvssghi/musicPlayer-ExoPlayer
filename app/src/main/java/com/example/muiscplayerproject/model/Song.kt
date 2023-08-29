package com.tonevellah.musicplayerapp.model

import android.net.Uri

data class Song    //constructor
    (//getters
    //member variables
    var id: Long,
    var uri: Uri,
    var name: String,
    var duration: Int,
    var size: Int,
    var albumId: Long,
    var albumName:String,
    var albumartUri: Uri?,
    var singer:String
)