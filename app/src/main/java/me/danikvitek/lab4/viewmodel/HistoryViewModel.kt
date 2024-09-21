package me.danikvitek.lab4.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.danikvitek.lab4.data.dao.HistoryDao
import me.danikvitek.lab4.data.entity.HistoryRecord
import me.danikvitek.lab4.di.WithTransaction
import me.danikvitek.lab4.service.WebRadioService
import me.danikvitek.lab4.service.dto.Song
import me.danikvitek.lab4.util.loopSuspending
import retrofit2.Response
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

sealed interface Status {
    @Suppress("REDUNDANT_MODIFIER")
    abstract sealed class WithHistory : Status {
        abstract val history: Flow<List<HistoryRecord>>
    }

    data object Initializing : Status
    data class Success(override val history: Flow<List<HistoryRecord>>) : Status, WithHistory()
    data class Error(
        val error: ErrorVariant,
        override val history: Flow<List<HistoryRecord>>,
    ) : Status, WithHistory()
}

enum class ErrorVariant {
    NO_INTERNET_CONNECTION,
    RESPONSE_ERROR,
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val webRadioService: WebRadioService,
    private val historyDao: HistoryDao,
    @ApplicationContext private val appContext: Context,
    private val withTransaction: WithTransaction,

    ) : ViewModel() {
    private val _status: MutableStateFlow<Status> = MutableStateFlow(Status.Initializing)
    val status: StateFlow<Status> = _status.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loopSuspending {
                when {
                    isOnline() -> {
                        val result: Result<Response<Song>> = runCatching {
                            webRadioService.getCurrentSong()
                        }
                        val response: Response<Song> = result.getOrElse { continueLoop }
                        when {
                            response.isSuccessful -> {
                                val song = response.body()!!
                                addRecord(song.title, song.artist)
                                when (val status = _status.value) {
                                    is Status.Initializing ->
                                        _status.value = Status.Success(historyDao.getHistory())

                                    is Status.Error ->
                                        _status.value = Status.Success(status.history)

                                    is Status.Success -> {}
                                }
                            }

                            else -> when (val status = _status.value) {
                                is Status.Initializing ->
                                    _status.value = Status.Error(
                                        ErrorVariant.RESPONSE_ERROR,
                                        historyDao.getHistory(),
                                    )

                                is Status.Success ->
                                    _status.value = Status.Error(
                                        ErrorVariant.RESPONSE_ERROR,
                                        status.history,
                                    )

                                is Status.Error -> when (status.error) {
                                    ErrorVariant.RESPONSE_ERROR -> {}
                                    ErrorVariant.NO_INTERNET_CONNECTION ->
                                        _status.value = Status.Error(
                                            ErrorVariant.RESPONSE_ERROR,
                                            status.history,
                                        )
                                }
                            }
                        }
                    }

                    else -> when (val status = _status.value) {
                        is Status.Initializing ->
                            _status.value = Status.Error(
                                ErrorVariant.NO_INTERNET_CONNECTION,
                                historyDao.getHistory(),
                            )

                        is Status.Success ->
                            _status.value = Status.Error(
                                ErrorVariant.NO_INTERNET_CONNECTION,
                                status.history,
                            )

                        is Status.Error -> when (status.error) {
                            ErrorVariant.NO_INTERNET_CONNECTION -> {}
                            ErrorVariant.RESPONSE_ERROR -> _status.value = Status.Error(
                                ErrorVariant.NO_INTERNET_CONNECTION,
                                status.history,
                            )
                        }
                    }
                }

                delay(20.seconds)
            }
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager =
            appContext.getSystemService(ConnectivityManager::class.java)
            ?: return false

        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            ?: return false

        val hasCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val hasEthernet = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

        return hasCellular || hasWifi || hasEthernet
    }

    private suspend fun addRecord(title: String, artist: String) = withTransaction {
        val lastRecord = historyDao.getLastRecord()
        if (lastRecord.title == title && lastRecord.artist == artist) return@withTransaction
        historyDao.addRecord(title, artist)
    }
}