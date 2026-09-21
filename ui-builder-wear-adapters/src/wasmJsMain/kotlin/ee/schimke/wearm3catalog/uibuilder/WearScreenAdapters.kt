package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppCard
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.ChildButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.OutlinedCard
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.TitleCard
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.material3.timeTextCurvedText
import ee.schimke.composeai.uibuilder.CanvasMode
import ee.schimke.composeai.uibuilder.CanvasNodeScope
import ee.schimke.composeai.uibuilder.canvasAdapterRegistry
import ee.schimke.wearcmp.port.LocalWearDeviceConfiguration

/** Wear screen structure and components whose rendering depends on its lazy-row receiver. */
val wearScreenAdapters = canvasAdapterRegistry {
  register("frame/round-screen") { WearScreenFrame() }
  register("wear-m3/transforming-lazy-column") { WearTransformingLazyColumn() }
  register("wear-m3/card") { WearCard() }
  register("wear-m3/button") { WearButton() }
}

private val LocalScreenListState = staticCompositionLocalOf<TransformingLazyColumnState?> { null }
private val LocalScreenContentPadding = staticCompositionLocalOf { PaddingValues() }
private val LocalSurfaceTransformation = staticCompositionLocalOf<SurfaceTransformation?> { null }

@Composable
private fun CanvasNodeScope.WearScreenFrame() {
  val canvas = this
  val state = rememberTransformingLazyColumnState()
  val padding = screenContentPadding()
  MaterialTheme {
    if (mode == CanvasMode.AuthoringUnrolled) {
      Box(
        modifier
          .fillMaxSize()
          .clip(RoundedCornerShape(percent = 50))
          .background(MaterialTheme.colorScheme.background)
      ) {
        CompositionLocalProvider(
          LocalScreenListState provides state,
          LocalScreenContentPadding provides padding,
        ) {
          Column(Modifier.fillMaxWidth().padding(padding)) { canvas.Slot("content") }
        }
        canvas.Slot("overlays", Modifier.fillMaxSize())
      }
    } else {
      val timeText: @Composable () -> Unit = {
        canvas
          .string("timeText")
          .takeIf { it.isNotEmpty() }
          ?.let { value -> TimeText { timeTextCurvedText(value) } }
      }
      Box(modifier.fillMaxSize()) {
        AppScaffold(timeText = timeText) {
          val edgeButton: (@Composable BoxScope.() -> Unit)? =
            if (canvas.node.slots["edgeButton"].isNullOrEmpty()) null
            else ({ canvas.Slot("edgeButton") })
          val scrollIndicator: (@Composable BoxScope.() -> Unit)? =
            if (canvas.boolean("scrollIndicator", true)) ({ ScrollIndicator(state) }) else null
          val content: @Composable BoxScope.(PaddingValues) -> Unit = { contentPadding ->
            CompositionLocalProvider(
              LocalScreenListState provides state,
              LocalScreenContentPadding provides contentPadding,
            ) {
              canvas.Slot("content", Modifier.fillMaxSize())
            }
          }
          if (edgeButton == null) {
            ScreenScaffold(
              scrollState = state,
              scrollIndicator = scrollIndicator,
              content = content,
            )
          } else {
            ScreenScaffold(
              scrollState = state,
              edgeButton = edgeButton,
              scrollIndicator = scrollIndicator,
              content = content,
            )
          }
        }
        canvas.Slot("overlays", Modifier.fillMaxSize())
      }
    }
  }
}

@Composable
private fun CanvasNodeScope.WearTransformingLazyColumn() {
  val canvas = this
  val count = itemCount("items")
  if (mode == CanvasMode.AuthoringUnrolled) {
    Column(
      modifier = modifier,
      verticalArrangement = Arrangement.spacedBy(float("verticalSpacingDp", 4f).dp),
    ) {
      repeat(count) { index -> canvas.Item("items", index) }
    }
    return
  }

  val state = LocalScreenListState.current ?: rememberTransformingLazyColumnState()
  val transformationSpec = rememberTransformationSpec()
  val transform = string("transformation") != "none"
  TransformingLazyColumn(
    state = state,
    contentPadding = LocalScreenContentPadding.current,
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(float("verticalSpacingDp", 4f).dp),
  ) {
    items(count) { index ->
      if (transform) {
        CompositionLocalProvider(
          LocalSurfaceTransformation provides SurfaceTransformation(transformationSpec)
        ) {
          canvas.Item(
            "items",
            index,
            Modifier.fillMaxWidth()
              .minimumVerticalContentPadding(CardDefaults.minimumVerticalListContentPadding)
              .transformedHeight(this@items, transformationSpec),
          )
        }
      } else {
        canvas.Item(
          "items",
          index,
          Modifier.fillMaxWidth()
            .minimumVerticalContentPadding(CardDefaults.minimumVerticalListContentPadding),
        )
      }
    }
  }
}

@Composable
private fun CanvasNodeScope.WearCard() {
  val canvas = this
  val transformation = LocalSurfaceTransformation.current
  when (string("variant")) {
    "title" ->
      if (transformation == null) {
        TitleCard(onClick = {}, title = { canvas.Slot("content") }, modifier = modifier) {}
      } else {
        TitleCard(
          onClick = {},
          title = { canvas.Slot("content") },
          modifier = modifier,
          transformation = transformation,
        ) {}
      }
    "app" ->
      if (transformation == null) {
        AppCard(
          onClick = {},
          appName = {},
          title = { canvas.Slot("content") },
          modifier = modifier,
        ) {}
      } else {
        AppCard(
          onClick = {},
          appName = {},
          title = { canvas.Slot("content") },
          modifier = modifier,
          transformation = transformation,
        ) {}
      }
    "outlined" ->
      if (transformation == null) {
        OutlinedCard(onClick = {}, modifier = modifier) { canvas.Slot("content") }
      } else {
        OutlinedCard(
          onClick = {},
          modifier = modifier,
          transformation = transformation,
        ) {
          canvas.Slot("content")
        }
      }
    else ->
      if (transformation == null) {
        Card(onClick = {}, modifier = modifier) { canvas.Slot("content") }
      } else {
        Card(onClick = {}, modifier = modifier, transformation = transformation) {
          canvas.Slot("content")
        }
      }
  }
}

@Composable
private fun CanvasNodeScope.WearButton() {
  val canvas = this
  val label: @Composable RowScope.() -> Unit = { canvas.Slot("content") }
  val transformation = LocalSurfaceTransformation.current
  val enabled = boolean("enabled", true)
  when (string("variant")) {
    "filled-tonal" ->
      if (transformation == null) {
        FilledTonalButton(onClick = {}, modifier = modifier, enabled = enabled, label = label)
      } else {
        FilledTonalButton(
          onClick = {},
          modifier = modifier,
          enabled = enabled,
          label = label,
          transformation = transformation,
        )
      }
    "outlined" ->
      if (transformation == null) {
        OutlinedButton(onClick = {}, modifier = modifier, enabled = enabled, label = label)
      } else {
        OutlinedButton(
          onClick = {},
          modifier = modifier,
          enabled = enabled,
          label = label,
          transformation = transformation,
        )
      }
    "child" ->
      if (transformation == null) {
        ChildButton(onClick = {}, modifier = modifier, enabled = enabled, label = label)
      } else {
        ChildButton(
          onClick = {},
          modifier = modifier,
          enabled = enabled,
          label = label,
          transformation = transformation,
        )
      }
    else ->
      if (transformation == null) {
        Button(onClick = {}, modifier = modifier, enabled = enabled, label = label)
      } else {
        Button(
          onClick = {},
          modifier = modifier,
          enabled = enabled,
          label = label,
          transformation = transformation,
        )
      }
  }
}

@Composable
private fun screenContentPadding(): PaddingValues {
  val width = LocalWearDeviceConfiguration.current.screenWidthDp
  return when {
    width >= 240 -> PaddingValues(horizontal = 13.dp, vertical = 24.dp)
    width >= 225 -> PaddingValues(horizontal = 12.dp, vertical = 23.dp)
    else -> PaddingValues(horizontal = 10.dp, vertical = 20.dp)
  }
}
