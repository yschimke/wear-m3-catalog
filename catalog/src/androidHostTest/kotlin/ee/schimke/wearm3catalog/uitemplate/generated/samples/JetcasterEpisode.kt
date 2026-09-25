// Generated from a Compose UI builder design. Do not edit by hand.

package ee.schimke.wearm3catalog.uitemplate.generated.samples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalScrollCaptureInProgress
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ButtonGroup
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.Icon
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
fun JetcasterEpisodeScreen() {
    val listState = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()
    AppScaffold(timeText = { TimeText { timeTextCurvedText("10:10") } }) {
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
                        modifier = Modifier.transformedHeight(this, spec),
                        transformation = SurfaceTransformation(spec),
                    ) {
                        Text(
                            text = "Episode 140: Lorem ipsum dolor",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                item {
                    ButtonGroup(
                        modifier = Modifier.transformedHeight(this, spec),
                    ) {
                        FilledIconButton(
                            onClick = {},
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                            )
                        }
                        FilledIconButton(
                            onClick = {},
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlaylistAdd,
                                contentDescription = null,
                            )
                        }
                    }
                }
                item {
                    Text(
                        text = "Android Developers",
                        modifier = Modifier.transformedHeight(this, spec),
                    )
                }
                item {
                    Text(
                        text = "Jun 2, 2020 • 45 mins",
                        modifier = Modifier.transformedHeight(this, spec),
                    )
                }
                item {
                    Text(
                        text = "In this episode, Romain, Chet and Tor talked with Mady Melor and Artur Tsurkan from the System UI team about... Bubbles!",
                        modifier = Modifier.transformedHeight(this, spec),
                    )
                }
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun JetcasterEpisodeScreenPreview() = JetcasterEpisodeScreen()

@Preview(device = "id:wearos_small_round", showBackground = true, backgroundColor = 0xFF000000)
@ScrollingPreview(modes = [ScrollMode.LONG])
@Composable
fun JetcasterEpisodeScreenLongPreview() = JetcasterEpisodeScreen()
