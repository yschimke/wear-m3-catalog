import androidx.wear.compose.material3.Typography

fun main() {
  println("before Typography()")
  val typography = Typography()
  println("after Typography(): ${typography.bodyLarge.fontSize}")
}
