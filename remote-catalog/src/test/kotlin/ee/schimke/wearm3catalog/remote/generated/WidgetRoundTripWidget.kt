// Generated from a Compose UI builder design. Do not edit by hand.
@file:Suppress("RestrictedApi")

package ee.schimke.wearm3catalog.remote.generated

import android.content.Context
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.state.rs
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
import androidx.wear.compose.remote.material3.RemoteText

@RemoteComposable
@Composable
fun WidgetRoundTripWidgetContent() {
    RemoteColumn {
        RemoteText(text = "Next train".rs)
        RemoteText(text = "09:24 to Tallinn".rs)
    }
}

class WidgetRoundTripWidget : GlanceWearWidget() {
    override suspend fun provideWidgetData(
        context: Context,
        params: WearWidgetParams,
    ): WearWidgetData {
        return WearWidgetDocument(background = WearWidgetBrush) {
            WidgetRoundTripWidgetContent()
        }
    }
}

@Preview(name = "Squircle Preview")
@Composable
fun WidgetRoundTripWidgetSquirclePreview() =
    WearWidgetPreview(
        WidgetRoundTripWidget(),
        SquircleSmallWidgetPreviewParams().values.maxBy { it.widthDp },
    )

@Preview(name = "Rectangular Preview")
@Composable
fun WidgetRoundTripWidgetRectangularPreview() =
    WearWidgetPreview(
        WidgetRoundTripWidget(),
        RectangularSmallWidgetPreviewParams().values.maxBy { it.widthDp },
    )

@Preview(name = "Round Preview")
@Composable
fun WidgetRoundTripWidgetRoundPreview() =
    WearWidgetPreview(
        WidgetRoundTripWidget(),
        RoundSmallWidgetPreviewParams().values.maxBy { it.widthDp },
    )
