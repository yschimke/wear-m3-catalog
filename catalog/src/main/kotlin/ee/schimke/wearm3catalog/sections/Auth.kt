@file:CatalogGroup(name = "Sign-in", section = "Horologist")

package ee.schimke.wearm3catalog.sections

import androidx.compose.runtime.Composable
import com.google.android.horologist.auth.composables.material3.buttons.GuestModeButton
import com.google.android.horologist.auth.composables.material3.buttons.SignInButton
import com.google.android.horologist.auth.composables.material3.screens.SelectAccountScreen
import com.google.android.horologist.auth.composables.material3.screens.SignInPlaceholderScreen
import com.google.android.horologist.auth.composables.material3.screens.SignedInConfirmationDialog
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.composeai.preview.SettledPreview
import ee.schimke.wearm3catalog.CatalogFullScreenModes
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.FullScreenSticker
import ee.schimke.wearm3catalog.HorologistSamples
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.counted

// Horologist's Material 3 sign-in surfaces.
//
// EVERY COMPONENT HERE ENTERS THROUGH THE LIBRARY'S DOOR, and for a blunter reason than usual: the
// kit has no sign-in page at all. `kit-sets.json` lists every set the kit publishes and there is
// nothing to point at — not a set with the wrong axes, not a private base node, nothing. So each
// `@CatalogComponent` says so in `noReference` rather than inventing a mapping, which is exactly
// the case AGENTS.md's second door is for.
//
// That does not make them off-topic. Signing in is the first screen of most Wear apps that have a
// phone counterpart, it is a flow Wear's own guidance shapes tightly, and Horologist publishes it
// as components rather than as sample code — which is the bar for being on this sheet.
//
// TWO SHAPES OF THING, TWO FRAMES. `SignInButton` and `GuestModeButton` are ordinary Wear buttons
// with a fixed role, so they wrap and publish cropped on a transparent ground. The three screens
// lay themselves out against the round display — `SignInPlaceholderScreen` installs its own
// `ScreenScaffold`, `SelectAccountScreen` is a `TransformingLazyColumn`, and the confirmation is a
// dialog that owns the screen — so they take `FullScreenSticker` and the breakpoint fan-out.
//
// The `auth-ui-material3` artifact is deliberately NOT a dependency. Its screens
// (`SignInPromptScreen`, `StreamlineSignInScreen`) are ViewModel-driven — they take a
// `SignInPromptViewModel` and drive a real auth repository — so a sticker for one would be a
// sticker for a fake, not for the component. `auth-composables-material3` is the stateless half,
// which is the half a catalog can honestly publish.

@CatalogComponent(
  id = "Auth/SignInButton",
  noReference =
    "The kit publishes no sign-in page — there is no set, and no private base node either, to " +
      "point at. This is Horologist's named button for the flow's primary action.",
  caption = "The primary action of a sign-in prompt, with the account glyph Wear's flow expects.",
)
@CatalogModes
@OverrideVariant(name = "disabled", booleans = ["enabled=false"])
@Composable
fun AuthSignInButton() = Sticker {
  val c = counted("Sign in")
  SignInButton(
    onClick = c.onClick,
    label = c.label,
    enabled = previewOverrideBoolean("enabled", true),
  )
}

@CatalogComponent(
  id = "Auth/GuestModeButton",
  noReference =
    "The other half of the sign-in prompt, and the kit publishes neither. Horologist ships it as " +
      "its own composable because the choice it offers — carry on without an account — is the one " +
      "Wear's guidance wants given equal weight, not hidden behind the primary button.",
  caption = "The way past the sign-in prompt: continue without an account.",
)
@CatalogModes
@OverrideVariant(name = "disabled", booleans = ["enabled=false"])
@Composable
fun AuthGuestModeButton() = Sticker {
  val c = counted("Guest mode")
  GuestModeButton(
    onClick = c.onClick,
    label = c.label,
    enabled = previewOverrideBoolean("enabled", true),
  )
}

@CatalogComponent(
  id = "Auth/SelectAccountScreen",
  noReference =
    "No kit set: the kit draws lists and cards, but not the account picker built from them. " +
      "Horologist's is a `TransformingLazyColumn` of accounts with the avatar, name and address " +
      "laid out the way Wear's sign-in guidance specifies.",
  caption = "Pick which account to sign in with — avatar, name and address per row.",
)
@CatalogFullScreenModes
@Composable
fun AuthSelectAccountScreen() = FullScreenSticker {
  SelectAccountScreen(accounts = HorologistSamples.accounts, onAccountClicked = { _, _ -> })
}

@CatalogComponent(
  id = "Auth/SignInPlaceholderScreen",
  noReference =
    "The kit's `Placeholder` sets cover the button, icon button and card; it publishes no " +
      "placeholder SCREEN. This is the shimmer a sign-in shows while it works out who is already " +
      "signed in on the phone.",
  caption = "What the sign-in flow shows while it is still deciding what to show.",
)
@CatalogFullScreenModes
@Composable
fun AuthSignInPlaceholderScreen() = FullScreenSticker { SignInPlaceholderScreen() }

@CatalogComponent(
  id = "Auth/SignedInConfirmationDialog",
  noReference =
    "Wear Compose publishes `ConfirmationDialog` and the kit publishes `Dialog`, but neither " +
      "publishes the signed-in confirmation: the avatar, the greeting and the timeout are " +
      "Horologist's, and it is the composable a sign-in flow actually calls at the end.",
  caption = "The end of the flow: who you signed in as, shown briefly and then dismissed.",
)
@CatalogFullScreenModes
@SettledPreview
@Composable
fun AuthSignedInConfirmationDialog() = FullScreenSticker {
  SignedInConfirmationDialog(
    onDismissOrTimeout = {},
    accountUiModel = HorologistSamples.account,
    // The dialog dismisses itself on a timer. A baked capture would race it — the renderer would
    // sometimes catch the dialog and sometimes catch the empty screen behind it — so the timeout is
    // pushed past any render. Deterministic renders are the rule, not a nicety (AGENTS.md).
    durationMillis = Long.MAX_VALUE,
  )
}
