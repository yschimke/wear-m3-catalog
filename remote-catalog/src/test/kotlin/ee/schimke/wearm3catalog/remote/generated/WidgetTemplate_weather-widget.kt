// Generated from a Compose UI builder design. Do not edit by hand.
@file:Suppress("RestrictedApi")

package ee.schimke.wearm3catalog.remote.generated.weatherwidget

import android.content.Context
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteArrangement
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.fillMaxWidth
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.remote.creation.compose.state.rsp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.glance.wear.GlanceWearWidget
import androidx.glance.wear.WearWidgetBrush
import androidx.glance.wear.WearWidgetData
import androidx.glance.wear.WearWidgetDocument
import androidx.glance.wear.color
import androidx.glance.wear.core.WearWidgetParams
import androidx.glance.wear.tooling.preview.RectangularLargeWidgetPreviewParams
import androidx.glance.wear.tooling.preview.RoundLargeWidgetPreviewParams
import androidx.glance.wear.tooling.preview.SquircleLargeWidgetPreviewParams
import androidx.glance.wear.tooling.preview.WearWidgetPreview
import androidx.wear.compose.remote.material3.RemoteText

@RemoteComposable
@Composable
fun WeatherWidgetContent() {
    RemoteBox(modifier = RemoteModifier.fillMaxSize(), contentAlignment = RemoteAlignment.Center) {
        RemoteColumn(
            modifier = RemoteModifier.fillMaxWidth(),
            verticalArrangement = RemoteArrangement.spacedBy(4.rdp),
        ) {
            RemoteText(
                text = "London".rs,
                modifier = RemoteModifier.fillMaxWidth(),
                color = Color(0xFFFFFFFF).rc,
                fontSize = 14.rsp,
                textAlign = TextAlign.Center,
            )
            RemoteText(
                text = "75° ☀️".rs,
                modifier = RemoteModifier.fillMaxWidth(),
                color = Color(0xFFFFFFFF).rc,
                fontSize = 36.rsp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

class WeatherWidget : GlanceWearWidget() {
    override suspend fun provideWidgetData(
        context: Context,
        params: WearWidgetParams,
    ): WearWidgetData {
        return WearWidgetDocument(background = WearWidgetBrush.color(Color(0xFF2196F3).rc)) {
            WeatherWidgetContent()
        }
    }
}

@Preview(name = "Squircle Preview")
@Composable
fun WeatherWidgetSquirclePreview() =
    WearWidgetPreview(
        WeatherWidget(),
        SquircleLargeWidgetPreviewParams().values.maxBy { it.widthDp },
    )

@Preview(name = "Rectangular Preview")
@Composable
fun WeatherWidgetRectangularPreview() =
    WearWidgetPreview(
        WeatherWidget(),
        RectangularLargeWidgetPreviewParams().values.maxBy { it.widthDp },
    )

@Preview(name = "Round Preview")
@Composable
fun WeatherWidgetRoundPreview() =
    WearWidgetPreview(
        WeatherWidget(),
        RoundLargeWidgetPreviewParams().values.maxBy { it.widthDp },
    )
