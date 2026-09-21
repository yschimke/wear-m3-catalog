import androidx.wear.compose.material3.Typography
import kotlinx.browser.window
import org.jetbrains.skiko.InternalSkikoApi
import org.jetbrains.skiko.wasm.awaitSkiko

@OptIn(InternalSkikoApi::class)
fun main() {
  if (window.location.search == "?awaitSkiko") {
    println("awaiting Skiko")
    awaitSkiko.then {
      constructTypography()
      null
    }
  } else {
    constructTypography()
  }
}

private fun constructTypography() {
  println("before Typography()")
  val typography = Typography()
  println("after Typography(): ${typography.bodyLarge.fontSize}")
}
