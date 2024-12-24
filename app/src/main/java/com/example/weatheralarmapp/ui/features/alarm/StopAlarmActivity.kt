package com.example.weatheralarmapp.ui.features.alarm

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.weatheralarmapp.R
import com.example.weatheralarmapp.sound.ExoPlayerManager
import com.example.weatheralarmapp.ui.theme.WeatherAlarmAppTheme

class AlarmActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION_CODES.O_MR1 <= Build.VERSION.SDK_INT) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            )
        }

        enableEdgeToEdge()
        setContent {
            WeatherAlarmAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    StopAlarmScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun StopAlarmScreen(
    modifier: Modifier,
    context: Application = LocalContext.current.applicationContext as Application,
    activityContext: AlarmActivity = LocalContext.current as AlarmActivity,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Button(onClick = {
            ExoPlayerManager.getPlayer(context).stop()
            (activityContext as? AlarmActivity)?.finish()
        }) {
            Text(stringResource(R.string.stop_alarm))
        }
    }
}

@Preview
@Composable
fun StopAlarmScreenPreview() {
    WeatherAlarmAppTheme {
        StopAlarmScreen(modifier = Modifier)
    }
}
