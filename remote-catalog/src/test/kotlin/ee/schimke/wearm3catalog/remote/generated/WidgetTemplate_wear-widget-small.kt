// Generated from a Compose UI builder design. Do not edit by hand.
@file:Suppress("RestrictedApi")

package ee.schimke.wearm3catalog.remote.generated.wearwidgetsmall

import android.content.Context
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.glance.wear.GlanceWearWidget
import androidx.glance.wear.WearWidgetBrush
import androidx.glance.wear.WearWidgetData
import androidx.glance.wear.WearWidgetDocument
import androidx.glance.wear.core.WearWidgetParams
import androidx.glance.wear.tooling.preview.RectangularSmallWidgetPreviewParams
import androidx.glance.wear.tooling.preview.RoundSmallWidgetPreviewParams
import androidx.glance.wear.tooling.preview.SquircleSmallWidgetPreviewParams
import androidx.glance.wear.tooling.preview.WearWidgetPreview

@RemoteComposable
@Composable
fun WearWidgetContent() {
    RemoteBox(modifier = RemoteModifier.fillMaxSize())
}

class WearWidget : GlanceWearWidget() {
    override suspend fun provideWidgetData(
        context: Context,
        params: WearWidgetParams,
    ): WearWidgetData {
        return WearWidgetDocument(background = WearWidgetBrush) {
            WearWidgetContent()
        }
    }
}

@Preview(name = "Rectangular Preview")
@Composable
fun WearWidgetRectangularPreview() =
    WearWidgetPreview(
        WearWidget(),
        RectangularSmallWidgetPreviewParams().values.maxBy { it.widthDp },
    )

@Preview(name = "Squircle Preview")
@Composable
fun WearWidgetSquirclePreview() =
    WearWidgetPreview(
        WearWidget(),
        SquircleSmallWidgetPreviewParams().values.maxBy { it.widthDp },
    )

@Preview(name = "Round Preview")
@Composable
fun WearWidgetRoundPreview() =
    WearWidgetPreview(
        WearWidget(),
        RoundSmallWidgetPreviewParams().values.maxBy { it.widthDp },
    )
