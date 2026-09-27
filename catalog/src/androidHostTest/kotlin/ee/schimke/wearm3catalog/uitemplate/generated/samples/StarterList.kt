// Generated from a Compose UI builder design. Do not edit by hand.

package ee.schimke.wearm3catalog.uitemplate.generated.samples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalScrollCaptureInProgress
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ButtonGroup
import androidx.wear.compose.material3.ButtonGroupDefaults
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListHeaderDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.TitleCard
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.material3.timeTextCurvedText
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import ee.schimke.composeai.preview.ScrollMode
import ee.schimke.composeai.preview.ScrollingPreview

@Composable
fun StarterListScreen() {
    val listState = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()
    var errorDialogVisible by remember { mutableStateOf(false) }
    ScreenScaffold(scrollState = listState,
        scrollIndicator = { if (!LocalScrollCaptureInProgress.current) ScrollIndicator(listState) },
    ) { contentPadding ->
        TransformingLazyColumn(
            state = listState,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                ListHeader(
                    modifier = Modifier.transformedHeight(this, spec).minimumVerticalContentPadding(ListHeaderDefaults.minimumTopListContentPadding, ListHeaderDefaults.minimumBottomListContentPadding),
                    transformation = SurfaceTransformation(spec),
                ) {
                    Text(text = "Header")
                }
            }
            item {
                TitleCard(
                    onClick = {},
                    title = {
                        Text(text = "Example Title")
                    },
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec).minimumVerticalContentPadding(CardDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(spec),
                ) {
                    Text(text = "Example Content More Lines And More")
                }
            }
            item {
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec).minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(spec),
                ) {
                    Text(text = "Example Button")
                }
            }
            item {
                ButtonGroup(
                    modifier = Modifier.transformedHeight(this, spec).minimumVerticalContentPadding(ButtonGroupDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(spec),
                ) {
                    FilledIconButton(
                        onClick = {},
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings Button",
                        )
                    }
                    FilledIconButton(
                        onClick = {},
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ThumbUp,
                            contentDescription = "Thumbs up Button",
                        )
                    }
                }
            }
        }
    }
    AlertDialog(
        visible = errorDialogVisible,
        onDismissRequest = { errorDialogVisible = false },
        title = { Text(text = "Title") },
        text = { Text(text = "An unknown error occurred during the request.") },
        confirmButton = { AlertDialogDefaults.ConfirmButton(onClick = { errorDialogVisible = false }) },
        dismissButton = { AlertDialogDefaults.DismissButton(onClick = { errorDialogVisible = false }) },
    )
}

@WearPreviewDevices
@Composable
fun StarterListScreenPreview() {
    AppScaffold(timeText = { TimeText { timeTextCurvedText("10:10") } }) { StarterListScreen() }
}

@Preview(device = "id:wearos_small_round", showBackground = true, backgroundColor = 0xFF000000)
@ScrollingPreview(modes = [ScrollMode.LONG])
@Composable
fun StarterListScreenLongPreview() {
    AppScaffold(timeText = { TimeText { timeTextCurvedText("10:10") } }) { StarterListScreen() }
}
