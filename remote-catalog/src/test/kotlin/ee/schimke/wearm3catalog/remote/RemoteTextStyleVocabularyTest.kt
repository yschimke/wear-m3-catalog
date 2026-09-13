package ee.schimke.wearm3catalog.remote

import androidx.wear.compose.remote.material3.RemoteTypography
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test

/**
 * The type-scale roles `ui-builder.policy.json` publishes are the ones this library has.
 *
 * ## Why a transcribed list needs a gate and the other properties do not
 *
 * `remote-m3/remote-text` states its own vocabulary rather than letting the preview server derive
 * one from `RemoteText`'s signature, because the derivation keeps only what maps to a JSON scalar
 * and `style` is a `RemoteTextStyle`. That is the designed mechanism — a catalog's vocabulary is
 * not its component's parameter list, and `propertyCapabilities` is the field for saying so — but
 * `style`'s `allowedValues` are different in kind from the rest of that block: they are eighteen
 * names **copied out of an alpha library**, and `RemoteTypography` is free to rename one.
 *
 * Nothing would notice. The policy is JSON, so a stale role stays valid JSON; the shelf offers it;
 * a designer picks it; and the export writes `RemoteMaterialTheme.typography.<gone>`, which fails
 * to compile at the far end of the pipeline from the file that is wrong. So the list is asserted
 * against the class instead of trusted.
 *
 * Both directions, because they fail differently. A role the library **dropped** is the one above.
 * A role it **added** is quieter and still wrong: the catalog silently stops offering type a design
 * could legitimately be set in, and no build anywhere goes red.
 *
 * Reflection over the getters rather than a second hand-written list, which would just move the
 * transcription. Java reflection rather than `kotlin-reflect`, which this module does not depend
 * on; `RemoteTypography`'s roles are `val`s, so each is a no-argument `getX()` returning
 * `RemoteTextStyle`.
 *
 * The sibling [ee.schimke.wearm3catalog.remote.probe.remoteValueVocabularyProbe] proves the
 * *spelling* compiles (`RemoteMaterialTheme.typography.bodyMedium` is a `RemoteTextStyle`). This
 * proves the *set* is complete. Neither implies the other.
 */
class RemoteTextStyleVocabularyTest {

  private val policyFile = File("ui-builder.policy.json")

  /** The roles `RemoteTypography` publishes, read off the compiled class. */
  private fun libraryRoles(): Set<String> =
    RemoteTypography::class
      .java
      .methods
      .filter { method ->
        !method.isSynthetic &&
          method.parameterCount == 0 &&
          method.name.startsWith("get") &&
          method.name.length > 3 &&
          method.returnType.name == "androidx.compose.remote.creation.compose.text.RemoteTextStyle"
      }
      .map { it.name.removePrefix("get").replaceFirstChar(Char::lowercaseChar) }
      .toSet()

  /** The `allowedValues` the policy publishes for `remote-m3/remote-text`'s `style`. */
  private fun publishedRoles(): Set<String> {
    assertWithMessage("the policy file is read relative to the module directory")
      .that(policyFile.exists())
      .isTrue()
    val policy = Json.parseToJsonElement(policyFile.readText()).jsonObject
    val component =
      policy.getValue("components").jsonObject.getValue("remote-m3/remote-text").jsonObject
    val style: JsonObject =
      component
        .getValue("propertyCapabilities")
        .jsonArray
        .map { it.jsonObject }
        .single { it.getValue("name").jsonPrimitive.contentOrNull == "style" }
    return style
      .getValue("allowedValues")
      .jsonArray
      .mapNotNull { it.jsonPrimitive.contentOrNull }
      .toSet()
  }

  @Test
  fun `the published type scale is exactly the one the library has`() {
    val library = libraryRoles()
    // Guards the guard: a filter that matched nothing would make every assertion below vacuous,
    // and a renamed return type is exactly the change that would do it.
    assertWithMessage(
        "no RemoteTextStyle roles found on RemoteTypography — the reflection filter " +
          "is matching nothing, so this test proves nothing"
      )
      .that(library)
      .isNotEmpty()

    val published = publishedRoles()
    assertWithMessage(
        "ui-builder.policy.json offers a type-scale role RemoteTypography does not have; a design " +
          "picking it would export a call that does not compile"
      )
      .that(published - library)
      .isEmpty()
    assertWithMessage(
        "RemoteTypography has a type-scale role ui-builder.policy.json does not offer; the shelf " +
          "is withholding type a design could be set in"
      )
      .that(library - published)
      .isEmpty()
    assertThat(published).isEqualTo(library)
  }
}
