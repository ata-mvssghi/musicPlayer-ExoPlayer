package com.example.a2ndproject.sharedViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer

class SharedViewModel : ViewModel() {
    private var _player: MutableLiveData<ExoPlayer> = MutableLiveData()
    val player: LiveData<ExoPlayer> = _player

    fun setPlayer(player: ExoPlayer) {
        _player.value = player
    }
}