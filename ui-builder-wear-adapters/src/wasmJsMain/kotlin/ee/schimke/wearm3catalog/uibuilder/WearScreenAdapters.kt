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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.wear.compose.material3.ColorScheme
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
import ee.schimke.composeai.uibuilder.export.ThemeTypefaces
import ee.schimke.composeai.uibuilder.export.WearScreenTheme
import ee.schimke.composeai.uibuilder.rememberThemeRoleFamilies
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasMode
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasNodeScope
import ee.schimke.composeai.uibuilder.renderer.sdk.canvasAdapterRegistry
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

/**
 * The row transformation the enclosing list is applying, and how many components in the row are
 * applying it themselves. The list transforms a row that no component took it for — a `Text`, an
 * `IconButton` — as a whole, the way `ButtonGroup` transforms itself; without that such a row got
 * `transformedHeight`'s shorter box near the bezel while drawing at full size.
 */
internal class RowTransformation(val surface: SurfaceTransformation) {
  var appliedBy by mutableIntStateOf(0)
    private set

  fun claim() {
    appliedBy++
  }

  fun release() {
    appliedBy--
  }
}

private val LocalRowTransformation = staticCompositionLocalOf<RowTransformation?> { null }

/**
 * The row's transformation for a component that takes one, or null outside a transforming list. The
 * component then draws inside [OutsideRow]: upstream transforms a surface with everything on it,
 * and a button in a card or a `ButtonGroup` taking it again would be scaled twice.
 */
@Composable
internal fun rowTransformation(): SurfaceTransformation? {
  val row = LocalRowTransformation.current ?: return null
  DisposableEffect(row) {
    row.claim()
    onDispose { row.release() }
  }
  return row.surface
}

@Composable
internal fun OutsideRow(content: @Composable () -> Unit) {
  CompositionLocalProvider(LocalRowTransformation provides null, content = content)
}

@Composable
private fun CanvasNodeScope.WearScreenFrame() {
  val canvas = this
  val state = rememberTransformingLazyColumnState()
  val padding = screenContentPadding()
  // The screen's typefaces, one family per group of type-scale roles, resolved through the
  // runtime's
  // font registry the way the editor's canvas resolves them (yschimke/wear-m3-catalog#684).
  val typefaces =
    rememberThemeRoleFamilies(
      ThemeTypefaces.families { canvas.string(it).takeIf(String::isNotEmpty) },
      wear = true,
    )
  MaterialTheme(
    colorScheme = canvas.screenColorScheme(),
    typography = MaterialTheme.typography.withRoleFamilies(typefaces),
  ) {
    // The scaffold's own `background`, read inside the screen's theme so an unset one follows a
    // re-skinned `background` role, the way the editor's canvas paints it.
    val background =
      resolveWearColor(canvas.string("background"), MaterialTheme.colorScheme.background)
    if (mode == CanvasMode.AuthoringUnrolled) {
      // The extent is the screen at its content's height, not the frame's: the host hands this
      // surface the frame's height and grows it to whatever the rows are measured to reach. Filled
      // to the frame instead, the list was measured inside one screenful, the rows past it were
      // squeezed to nothing, the extent never grew, and switching the editor off its device view
      // drew the same watch it had just left.
      val screen = LocalWearDeviceConfiguration.current.screenWidthDp.dp
      Box(
        Modifier.wrapContentHeight(Alignment.Top, unbounded = true)
          .then(modifier)
          .fillMaxWidth()
          .heightIn(min = screen)
          .clip(RoundedCornerShape(percent = 50))
          .background(background)
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
        AppScaffold(timeText = timeText, containerColor = background) {
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

/**
 * The screen's theme: Wear's scheme with the roles the scaffold overrides replaced, the same
 * `MaterialTheme.colorScheme.copy(…)` the generated screen wraps itself in and the editor's canvas
 * draws ([WearScreenTheme], yschimke/wear-m3-catalog#682). A value is a `#RRGGBB`/`#AARRGGBB`
 * literal or the name of another role, which reads the stock scheme around the screen; anything
 * else keeps Wear's own.
 */
@Composable
private fun CanvasNodeScope.screenColorScheme(): ColorScheme {
  val stock = MaterialTheme.colorScheme
  val overrides =
    WearScreenTheme.ROLES.mapNotNull { role ->
        val value = string(WearScreenTheme.property(role))
        val color =
          when {
            value.isEmpty() -> null
            value.startsWith("#") -> resolveWearColor(value)
            else -> stock.role(value)
          }
        color?.takeIf { it != Color.Unspecified }?.let { role to it }
      }
      .toMap()
  if (overrides.isEmpty()) return stock
  fun role(name: String): Color = overrides[name] ?: stock.role(name)!!
  return stock.copy(
    primary = role("primary"),
    onPrimary = role("onPrimary"),
    primaryContainer = role("primaryContainer"),
    onPrimaryContainer = role("onPrimaryContainer"),
    secondary = role("secondary"),
    onSecondary = role("onSecondary"),
    secondaryContainer = role("secondaryContainer"),
    onSecondaryContainer = role("onSecondaryContainer"),
    tertiary = role("tertiary"),
    onTertiary = role("onTertiary"),
    tertiaryContainer = role("tertiaryContainer"),
    onTertiaryContainer = role("onTertiaryContainer"),
    surfaceContainerLow = role("surfaceContainerLow"),
    surfaceContainer = role("surfaceContainer"),
    surfaceContainerHigh = role("surfaceContainerHigh"),
    onSurface = role("onSurface"),
    onSurfaceVariant = role("onSurfaceVariant"),
    outline = role("outline"),
    outlineVariant = role("outlineVariant"),
    background = role("background"),
    onBackground = role("onBackground"),
    error = role("error"),
    onError = role("onError"),
  )
}

/** One of Wear's colour roles by name, or null for a name the scheme has no role for. */
private fun ColorScheme.role(name: String): Color? =
  when (name) {
    "primary" -> primary
    "primaryDim" -> primaryDim
    "onPrimary" -> onPrimary
    "primaryContainer" -> primaryContainer
    "onPrimaryContainer" -> onPrimaryContainer
    "secondary" -> secondary
    "secondaryDim" -> secondaryDim
    "onSecondary" -> onSecondary
    "secondaryContainer" -> secondaryContainer
    "onSecondaryContainer" -> onSecondaryContainer
    "tertiary" -> tertiary
    "tertiaryDim" -> tertiaryDim
    "onTertiary" -> onTertiary
    "tertiaryContainer" -> tertiaryContainer
    "onTertiaryContainer" -> onTertiaryContainer
    "surface",
    "surfaceContainer" -> surfaceContainer
    "surfaceContainerLow" -> surfaceContainerLow
    "surfaceContainerHigh",
    "surfaceContainerHighest" -> surfaceContainerHigh
    "onSurface" -> onSurface
    "onSurfaceVariant" -> onSurfaceVariant
    "outline" -> outline
    "outlineVariant" -> outlineVariant
    "background" -> background
    "onBackground" -> onBackground
    "error" -> error
    "errorDim" -> errorDim
    "onError" -> onError
    "errorContainer" -> errorContainer
    "onErrorContainer" -> onErrorContainer
    "transparent" -> Color.Transparent
    else -> null
  }

@Composable
private fun CanvasNodeScope.WearTransformingLazyColumn() {
  val canvas = this
  val count = itemCount("items")
  val state = LocalScreenListState.current ?: rememberTransformingLazyColumnState()
  registerScrolling(state::dispatchRawDelta) { state.requestScrollToItem(it) }
  if (mode == CanvasMode.AuthoringUnrolled) {
    Column(
      modifier = modifier,
      verticalArrangement = Arrangement.spacedBy(float("verticalSpacingDp", 4f).dp),
    ) {
      repeat(count) { index -> canvas.Item("items", index) }
    }
    return
  }

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
        val row =
          remember(this, transformationSpec) {
            RowTransformation(SurfaceTransformation(transformationSpec))
          }
        CompositionLocalProvider(LocalRowTransformation provides row) {
          canvas.Item(
            "items",
            index,
            Modifier.fillMaxWidth()
              .minimumVerticalContentPadding(CardDefaults.minimumVerticalListContentPadding)
              .transformedHeight(this@items, transformationSpec)
              .graphicsLayer {
                if (row.appliedBy == 0) with(row.surface) { applyContainerTransformation() }
              },
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
  val transformation = rowTransformation()
  OutsideRow { WearCardVariant(canvas, transformation) }
}

@Composable
private fun CanvasNodeScope.WearCardVariant(
  canvas: CanvasNodeScope,
  transformation: SurfaceTransformation?,
) {
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
  val transformation = rowTransformation()
  val enabled = boolean("enabled", true)
  OutsideRow { WearButtonVariant(label, transformation, enabled) }
}

@Composable
private fun CanvasNodeScope.WearButtonVariant(
  label: @Composable RowScope.() -> Unit,
  transformation: SurfaceTransformation?,
  enabled: Boolean,
) {
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
