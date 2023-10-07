package com.example.muiscplayerproject.model

import android.net.Uri
import android.os.Parcelable
import java.io.Serializable

data class Song
    (
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
):Serializable