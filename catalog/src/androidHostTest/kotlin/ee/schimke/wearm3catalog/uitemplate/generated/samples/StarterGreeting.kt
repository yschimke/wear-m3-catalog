// Generated from a Compose UI builder design. Do not edit by hand.

package ee.schimke.wearm3catalog.uitemplate.generated.samples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalScrollCaptureInProgress
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.material3.timeTextCurvedText
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import ee.schimke.composeai.preview.ScrollMode
import ee.schimke.composeai.preview.ScrollingPreview

@Composable
fun StarterGreetingScreen() {
    val listState = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()
    AppScaffold(timeText = { TimeText { timeTextCurvedText("10:10") } }) {
        ScreenScaffold(scrollState = listState,
            scrollIndicator = { if (!LocalScrollCaptureInProgress.current) ScrollIndicator(listState) },
            edgeButton = {
                EdgeButton(onClick = {}, buttonSize = EdgeButtonSize.ExtraSmall) {
                    Text(text = "Show list")
                }
            },
        ) { contentPadding ->
            TransformingLazyColumn(
                state = listState,
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    ListHeader(
                        modifier = Modifier.transformedHeight(this, spec),
                        transformation = SurfaceTransformation(spec),
                    ) {
                        Text(text = "From the Round world, Hello, Android!")
                    }
                }
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun StarterGreetingScreenPreview() = StarterGreetingScreen()

@Preview(device = "id:wearos_small_round", showBackground = true, backgroundColor = 0xFF000000)
@ScrollingPreview(modes = [ScrollMode.LONG])
@Composable
fun StarterGreetingScreenLongPreview() = StarterGreetingScreen()
