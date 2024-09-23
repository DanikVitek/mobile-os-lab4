package me.danikvitek.lab4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.danikvitek.lab4.screen.WebRadioHistory
import me.danikvitek.lab4.ui.theme.Lab4Theme
import me.danikvitek.lab4.viewmodel.HistoryViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab4Theme {
                val viewModel: HistoryViewModel by viewModels()
                WebRadioHistory(viewModel = viewModel)
            }
        }
    }
}
