package me.danikvitek.lab4.service

import me.danikvitek.lab4.service.dto.Song
import retrofit2.Response
import retrofit2.http.GET

interface WebRadioService {
    @GET("radio/pi/current-song")
    suspend fun getCurrentSong(): Response<Song>

    @GET("radio/pi/song/picture")
    suspend fun getSongPicture(): Response<String>
}