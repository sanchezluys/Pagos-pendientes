package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.PaymentsViewModel
import com.example.ui.PaymentsViewModelFactory
import com.example.ui.screens.MainPaymentsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as PagosApplication
        setContent {
            val viewModel: PaymentsViewModel = viewModel(
                factory = PaymentsViewModelFactory(app.repository, app.settingsRepository)
            )
            val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = appSettings.isDarkTheme) {
                MainPaymentsScreen(viewModel = viewModel)
            }
        }
    }
}
