package com.example.weatheralarmapp.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun IndeterminateLinearIndicator(
    modifier: Modifier,
    loading: Boolean,
) {
    if (!loading) return

    LinearProgressIndicator(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Preview
@Composable
fun LinearDeterminateIndicatorPreview(modifier: Modifier = Modifier) {
    IndeterminateLinearIndicator(
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        loading = true,
    )
}
