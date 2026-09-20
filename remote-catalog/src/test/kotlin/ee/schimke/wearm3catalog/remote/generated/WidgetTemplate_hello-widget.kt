// Generated from a Compose UI builder design. Do not edit by hand.
@file:Suppress("RestrictedApi")

package ee.schimke.wearm3catalog.remote.generated.hellowidget

import android.content.Context
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.remote.creation.compose.state.rsp
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.glance.wear.GlanceWearWidget
import androidx.glance.wear.WearWidgetBrush
import androidx.glance.wear.WearWidgetData
import androidx.glance.wear.WearWidgetDocument
import androidx.glance.wear.color
import androidx.glance.wear.core.WearWidgetParams
import androidx.glance.wear.tooling.preview.RectangularSmallWidgetPreviewParams
import androidx.glance.wear.tooling.preview.RoundSmallWidgetPreviewParams
import androidx.glance.wear.tooling.preview.SquircleSmallWidgetPreviewParams
import androidx.glance.wear.tooling.preview.WearWidgetPreview
import androidx.wear.compose.remote.material3.RemoteColorScheme
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemoteText

@RemoteComposable
@Composable
fun HelloWidgetContent() {
    RemoteMaterialTheme {
        RemoteBox(
            modifier = RemoteModifier.fillMaxSize(),
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteText(
                text = "Hello, World!".rs,
                color = RemoteMaterialTheme.colorScheme.onPrimary,
                fontSize = 20.rsp,
            )
        }
    }
}

class HelloWidget : GlanceWearWidget() {
    override suspend fun provideWidgetData(
        context: Context,
        params: WearWidgetParams,
    ): WearWidgetData {
        val colorScheme = RemoteColorScheme()
        return WearWidgetDocument(background = WearWidgetBrush.color(colorScheme.primary)) {
            HelloWidgetContent()
        }
    }
}

@Preview(name = "Rectangular Preview")
@Composable
fun HelloWidgetRectangularPreview() =
    WearWidgetPreview(
        HelloWidget(),
        RectangularSmallWidgetPreviewParams().values.maxBy { it.widthDp },
    )

@Preview(name = "Squircle Preview")
@Composable
fun HelloWidgetSquirclePreview() =
    WearWidgetPreview(
        HelloWidget(),
        SquircleSmallWidgetPreviewParams().values.maxBy { it.widthDp },
    )

@Preview(name = "Round Preview")
@Composable
fun HelloWidgetRoundPreview() =
    WearWidgetPreview(
        HelloWidget(),
        RoundSmallWidgetPreviewParams().values.maxBy { it.widthDp },
    )
