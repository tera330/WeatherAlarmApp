package com.example.weatheralarmapp.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ExpandButton(
    modifier: Modifier,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = null,
        )
    }
}

@Preview
@Composable
private fun ExpandButtonPreview() {
    var expanded by remember { mutableStateOf(false) }

    ExpandButton(
        modifier = Modifier,
        expanded = expanded,
        onClick = { expanded = !expanded },
    )
}
