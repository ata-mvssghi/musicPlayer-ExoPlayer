package com.example.a2ndproject.sharedViewModel

import android.text.BoringLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedViewModel : ViewModel() {
    private val _player: MutableStateFlow<ExoPlayer?> = MutableStateFlow(null)
    val player: StateFlow<ExoPlayer?> = _player

    fun setPlayer(player: ExoPlayer) {
        _player.value = player
    }
    companion object{
        private val _isPaused: MutableStateFlow<Boolean> = MutableStateFlow(true)
        val isPaused: StateFlow<Boolean> = _isPaused

        fun setIsPaused(paused: Boolean) {
            _isPaused.value = paused
        }
         var  initializedPlaying:Boolean=false
        private val _permissionGranted: MutableStateFlow<Boolean> = MutableStateFlow(false)
        val permissionGranted: StateFlow<Boolean> = _permissionGranted
        fun setPermissionGranted(granted:Boolean){
            _permissionGranted.value=granted
        }
    }
}
