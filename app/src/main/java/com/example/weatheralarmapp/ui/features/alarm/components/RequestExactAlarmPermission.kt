package com.example.weatheralarmapp.ui.features.alarm.components

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.weatheralarmapp.R
import com.example.weatheralarmapp.ui.common.Dialog

@Composable
fun RequestExactAlarmPermission(
    modifier: Modifier = Modifier,
    openDialog: MutableState<Boolean>,
) {
    val context = LocalContext.current

    Column(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        if (openDialog.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Dialog(
                onDismissRequest = {
                    openDialog.value = false
                },
                onConfirmation = {
                    openDialog.value = false
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    context.startActivity(intent)
                },
                dialogTitle = stringResource(R.string.allow_permission_set_alarm),
                dialogText = stringResource(R.string.request_exact_alarm_permission),
                icon = Icons.Default.Info,
            )
        }
    }
}

@Preview
@Composable
fun PreviewRequestExactAlarmPermission() {
    val openDialog = remember { mutableStateOf(true) }
    RequestExactAlarmPermission(
        openDialog = openDialog,
    )
}
