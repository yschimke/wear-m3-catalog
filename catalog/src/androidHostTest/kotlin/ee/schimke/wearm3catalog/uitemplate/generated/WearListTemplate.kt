// Generated from a Compose UI builder design. Do not edit by hand.

package ee.schimke.wearm3catalog.uitemplate.generated

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalScrollCaptureInProgress
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListHeaderDefaults
import androidx.wear.compose.material3.MaterialTheme
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
fun ActivityScreen() {
    val listState = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()
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
                    Text(text = "Activity")
                }
            }
            item {
                TitleCard(
                    onClick = {},
                    title = {
                        Column(modifier = Modifier.fillMaxWidth().padding(start = 12.2.dp, top = 9.7.dp, end = 12.2.dp, bottom = 14.7.dp)) {
                            Text(
                                text = "Session 1",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start,
                            )
                            Text(
                                text = "4 min",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Start,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec).minimumVerticalContentPadding(CardDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(spec),
                )
            }
            item {
                TitleCard(
                    onClick = {},
                    title = {
                        Column(modifier = Modifier.fillMaxWidth().padding(start = 12.2.dp, top = 9.7.dp, end = 12.2.dp, bottom = 14.7.dp)) {
                            Text(
                                text = "Session 2",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start,
                            )
                            Text(
                                text = "8 min",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Start,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec).minimumVerticalContentPadding(CardDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(spec),
                )
            }
            item {
                TitleCard(
                    onClick = {},
                    title = {
                        Column(modifier = Modifier.fillMaxWidth().padding(start = 12.2.dp, top = 9.7.dp, end = 12.2.dp, bottom = 14.7.dp)) {
                            Text(
                                text = "Session 3",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start,
                            )
                            Text(
                                text = "12 min",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Start,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec).minimumVerticalContentPadding(CardDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(spec),
                )
            }
            item {
                TitleCard(
                    onClick = {},
                    title = {
                        Column(modifier = Modifier.fillMaxWidth().padding(start = 12.2.dp, top = 9.7.dp, end = 12.2.dp, bottom = 14.7.dp)) {
                            Text(
                                text = "Session 4",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start,
                            )
                            Text(
                                text = "16 min",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Start,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec).minimumVerticalContentPadding(CardDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(spec),
                )
            }
            item {
                TitleCard(
                    onClick = {},
                    title = {
                        Column(modifier = Modifier.fillMaxWidth().padding(start = 12.2.dp, top = 9.7.dp, end = 12.2.dp, bottom = 14.7.dp)) {
                            Text(
                                text = "Session 5",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start,
                            )
                            Text(
                                text = "20 min",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Start,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec).minimumVerticalContentPadding(CardDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(spec),
                )
            }
            item {
                TitleCard(
                    onClick = {},
                    title = {
                        Column(modifier = Modifier.fillMaxWidth().padding(start = 12.2.dp, top = 9.7.dp, end = 12.2.dp, bottom = 14.7.dp)) {
                            Text(
                                text = "Session 6",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start,
                            )
                            Text(
                                text = "24 min",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Start,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec).minimumVerticalContentPadding(CardDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(spec),
                )
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun ActivityScreenPreview() {
    AppScaffold(timeText = { TimeText { timeTextCurvedText("10:10") } }) { ActivityScreen() }
}

@Preview(device = "id:wearos_small_round", showBackground = true, backgroundColor = 0xFF000000)
@ScrollingPreview(modes = [ScrollMode.LONG])
@Composable
fun ActivityScreenLongPreview() {
    AppScaffold(timeText = { TimeText { timeTextCurvedText("10:10") } }) { ActivityScreen() }
}
