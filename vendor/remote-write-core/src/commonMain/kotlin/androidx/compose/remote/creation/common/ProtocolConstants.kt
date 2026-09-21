package androidx.compose.remote.creation.common

public object CoreDocument {
  public const val DOCUMENT_API_LEVEL: Int = 8
  public const val DENSITY_BEHAVIOR_LEGACY: Int = 0
  public const val DENSITY_BEHAVIOR_PIXELS: Int = 1
  public const val DENSITY_BEHAVIOR_DP: Int = 2
}

public object RemoteContext {
  public const val ID_CONTINUOUS_SEC: Int = 1
  public const val ID_TIME_IN_SEC: Int = 2
  public const val ID_TIME_IN_MIN: Int = 3
  public const val ID_TIME_IN_HR: Int = 4
  public const val ID_WINDOW_WIDTH: Int = 5
  public const val ID_WINDOW_HEIGHT: Int = 6
  public const val ID_COMPONENT_WIDTH: Int = 7
  public const val ID_COMPONENT_HEIGHT: Int = 8
  public const val ID_CALENDAR_MONTH: Int = 9
  public const val ID_OFFSET_TO_UTC: Int = 10
  public const val ID_WEEK_DAY: Int = 11
  public const val ID_DAY_OF_MONTH: Int = 12
  public const val ID_TOUCH_POS_X: Int = 13
  public const val ID_TOUCH_POS_Y: Int = 14
  public const val ID_TOUCH_VEL_X: Int = 15
  public const val ID_TOUCH_VEL_Y: Int = 16
  public const val ID_ACCELERATION_X: Int = 17
  public const val ID_ACCELERATION_Y: Int = 18
  public const val ID_ACCELERATION_Z: Int = 19
  public const val ID_GYRO_ROT_X: Int = 20
  public const val ID_GYRO_ROT_Y: Int = 21
  public const val ID_GYRO_ROT_Z: Int = 22
  public const val ID_MAGNETIC_X: Int = 23
  public const val ID_MAGNETIC_Y: Int = 24
  public const val ID_MAGNETIC_Z: Int = 25
  public const val ID_LIGHT: Int = 26
  public const val ID_DENSITY: Int = 27
  public const val ID_API_LEVEL: Int = 28
  public const val ID_TOUCH_EVENT_TIME: Int = 29
  public const val ID_ANIMATION_TIME: Int = 30
  public const val ID_ANIMATION_DELTA_TIME: Int = 31
  public const val ID_EPOCH_SECOND: Int = 32
  public const val ID_FONT_SIZE: Int = 33
  public const val ID_DAY_OF_YEAR: Int = 34
  public const val ID_YEAR: Int = 35
  public val FLOAT_CONTINUOUS_SEC: Float = Utils.asNan(ID_CONTINUOUS_SEC)
  public val FLOAT_TIME_IN_SEC: Float = Utils.asNan(ID_TIME_IN_SEC)
  public val FLOAT_TIME_IN_MIN: Float = Utils.asNan(ID_TIME_IN_MIN)
  public val FLOAT_TIME_IN_HR: Float = Utils.asNan(ID_TIME_IN_HR)
  public val FLOAT_WINDOW_WIDTH: Float = Utils.asNan(ID_WINDOW_WIDTH)
  public val FLOAT_WINDOW_HEIGHT: Float = Utils.asNan(ID_WINDOW_HEIGHT)
  public val FLOAT_COMPONENT_WIDTH: Float = Utils.asNan(ID_COMPONENT_WIDTH)
  public val FLOAT_COMPONENT_HEIGHT: Float = Utils.asNan(ID_COMPONENT_HEIGHT)
  public val FLOAT_CALENDAR_MONTH: Float = Utils.asNan(ID_CALENDAR_MONTH)
  public val FLOAT_OFFSET_TO_UTC: Float = Utils.asNan(ID_OFFSET_TO_UTC)
  public val FLOAT_WEEK_DAY: Float = Utils.asNan(ID_WEEK_DAY)
  public val FLOAT_DAY_OF_MONTH: Float = Utils.asNan(ID_DAY_OF_MONTH)
  public val FLOAT_TOUCH_POS_X: Float = Utils.asNan(ID_TOUCH_POS_X)
  public val FLOAT_TOUCH_POS_Y: Float = Utils.asNan(ID_TOUCH_POS_Y)
  public val FLOAT_TOUCH_VEL_X: Float = Utils.asNan(ID_TOUCH_VEL_X)
  public val FLOAT_TOUCH_VEL_Y: Float = Utils.asNan(ID_TOUCH_VEL_Y)
  public val FLOAT_ACCELERATION_X: Float = Utils.asNan(ID_ACCELERATION_X)
  public val FLOAT_ACCELERATION_Y: Float = Utils.asNan(ID_ACCELERATION_Y)
  public val FLOAT_ACCELERATION_Z: Float = Utils.asNan(ID_ACCELERATION_Z)
  public val FLOAT_GYRO_ROT_X: Float = Utils.asNan(ID_GYRO_ROT_X)
  public val FLOAT_GYRO_ROT_Y: Float = Utils.asNan(ID_GYRO_ROT_Y)
  public val FLOAT_GYRO_ROT_Z: Float = Utils.asNan(ID_GYRO_ROT_Z)
  public val FLOAT_MAGNETIC_X: Float = Utils.asNan(ID_MAGNETIC_X)
  public val FLOAT_MAGNETIC_Y: Float = Utils.asNan(ID_MAGNETIC_Y)
  public val FLOAT_MAGNETIC_Z: Float = Utils.asNan(ID_MAGNETIC_Z)
  public val FLOAT_LIGHT: Float = Utils.asNan(ID_LIGHT)
  public val FLOAT_DENSITY: Float = Utils.asNan(ID_DENSITY)
  public val FLOAT_API_LEVEL: Float = Utils.asNan(ID_API_LEVEL)
  public val FLOAT_TOUCH_EVENT_TIME: Float = Utils.asNan(ID_TOUCH_EVENT_TIME)
  public val FLOAT_ANIMATION_TIME: Float = Utils.asNan(ID_ANIMATION_TIME)
  public val FLOAT_ANIMATION_DELTA_TIME: Float = Utils.asNan(ID_ANIMATION_DELTA_TIME)
  public val FLOAT_FONT_SIZE: Float = Utils.asNan(ID_FONT_SIZE)
  public val FLOAT_DAY_OF_YEAR: Float = Utils.asNan(ID_DAY_OF_YEAR)
  public val FLOAT_YEAR: Float = Utils.asNan(ID_YEAR)

  public fun isTime(value: Float): Boolean =
    Utils.idFromNan(value) in setOf(
      ID_CONTINUOUS_SEC, ID_TIME_IN_SEC, ID_TIME_IN_MIN, ID_TIME_IN_HR,
      ID_CALENDAR_MONTH, ID_OFFSET_TO_UTC, ID_WEEK_DAY, ID_DAY_OF_MONTH,
      ID_DAY_OF_YEAR, ID_YEAR,
    )
}

public object PaintBundle {
  public const val BLEND_MODE_CLEAR: Int = 0
  public const val BLEND_MODE_SRC: Int = 1
  public const val BLEND_MODE_DST: Int = 2
  public const val BLEND_MODE_SRC_OVER: Int = 3
  public const val BLEND_MODE_DST_OVER: Int = 4
  public const val BLEND_MODE_SRC_IN: Int = 5
  public const val BLEND_MODE_DST_IN: Int = 6
  public const val BLEND_MODE_SRC_OUT: Int = 7
  public const val BLEND_MODE_DST_OUT: Int = 8
  public const val BLEND_MODE_SRC_ATOP: Int = 9
  public const val BLEND_MODE_DST_ATOP: Int = 10
  public const val BLEND_MODE_XOR: Int = 11
  public const val BLEND_MODE_PLUS: Int = 12
  public const val BLEND_MODE_MODULATE: Int = 13
  public const val BLEND_MODE_SCREEN: Int = 14
  public const val BLEND_MODE_OVERLAY: Int = 15
  public const val BLEND_MODE_DARKEN: Int = 16
  public const val BLEND_MODE_LIGHTEN: Int = 17
  public const val BLEND_MODE_COLOR_DODGE: Int = 18
  public const val BLEND_MODE_COLOR_BURN: Int = 19
  public const val BLEND_MODE_HARD_LIGHT: Int = 20
  public const val BLEND_MODE_SOFT_LIGHT: Int = 21
  public const val BLEND_MODE_DIFFERENCE: Int = 22
  public const val BLEND_MODE_EXCLUSION: Int = 23
  public const val BLEND_MODE_MULTIPLY: Int = 24
  public const val BLEND_MODE_HUE: Int = 25
  public const val BLEND_MODE_SATURATION: Int = 26
  public const val BLEND_MODE_COLOR: Int = 27
  public const val BLEND_MODE_LUMINOSITY: Int = 28
}

public object TextLayout {
  public const val TEXT_ALIGN_LEFT: Int = 1
  public const val TEXT_ALIGN_RIGHT: Int = 2
  public const val TEXT_ALIGN_CENTER: Int = 3
  public const val TEXT_ALIGN_JUSTIFY: Int = 4
  public const val TEXT_ALIGN_START: Int = 5
  public const val TEXT_ALIGN_END: Int = 6
  public const val OVERFLOW_CLIP: Int = 1
  public const val OVERFLOW_VISIBLE: Int = 2
  public const val OVERFLOW_ELLIPSIS: Int = 3
  public const val OVERFLOW_START_ELLIPSIS: Int = 4
  public const val OVERFLOW_MIDDLE_ELLIPSIS: Int = 5
}

public object ImageScaling {
  public const val SCALE_NONE: Int = 0
  public const val SCALE_INSIDE: Int = 1
  public const val SCALE_FILL_WIDTH: Int = 2
  public const val SCALE_FILL_HEIGHT: Int = 3
  public const val SCALE_FIT: Int = 4
  public const val SCALE_CROP: Int = 5
  public const val SCALE_FILL_BOUNDS: Int = 6
}

public object ColorAttribute {
  public const val COLOR_HUE: Short = 0
  public const val COLOR_SATURATION: Short = 1
  public const val COLOR_BRIGHTNESS: Short = 2
  public const val COLOR_RED: Short = 3
  public const val COLOR_GREEN: Short = 4
  public const val COLOR_BLUE: Short = 5
  public const val COLOR_ALPHA: Short = 6
}

public object ImageAttribute {
  public const val IMAGE_WIDTH: Short = 0
  public const val IMAGE_HEIGHT: Short = 1
}

public object BitmapTextMeasure {
  public const val MEASURE_WIDTH: Int = 0
  public const val MEASURE_HEIGHT: Int = 1
}

public object TextFromFloat {
  public const val PAD_AFTER_SPACE: Int = 0
  public const val PAD_AFTER_NONE: Int = 1
  public const val PAD_AFTER_ZERO: Int = 3
  public const val PAD_PRE_SPACE: Int = 0
  public const val PAD_PRE_NONE: Int = 4
  public const val PAD_PRE_ZERO: Int = 12
  public const val GROUPING_NONE: Int = 0
  public const val GROUPING_BY3: Int = 1 shl 4
  public const val GROUPING_BY4: Int = 2 shl 4
  public const val GROUPING_BY32: Int = 3 shl 4
  public const val SEPARATOR_COMMA_PERIOD: Int = 0
  public const val SEPARATOR_PERIOD_COMMA: Int = 1 shl 6
  public const val SEPARATOR_SPACE_COMMA: Int = 2 shl 6
  public const val SEPARATOR_UNDER_PERIOD: Int = 3 shl 6
  public const val OPTIONS_NEGATIVE_PARENTHESES: Int = 1 shl 8
  public const val OPTIONS_ROUNDING: Int = 2 shl 8
}

public object TextTransform {
  public const val TEXT_TO_LOWERCASE: Int = 1
  public const val TEXT_TO_UPPERCASE: Int = 2
  public const val TEXT_TRIM: Int = 3
  public const val TEXT_CAPITALIZE: Int = 4
  public const val TEXT_UPPERCASE_FIRST_CHAR: Int = 5
}

public object TimeAttribute {
  public const val TIME_FROM_NOW_SEC: Short = 0
  public const val TIME_FROM_NOW_MIN: Short = 1
  public const val TIME_FROM_NOW_HR: Short = 2
  public const val TIME_IN_SEC: Short = 6
  public const val TIME_IN_MIN: Short = 7
  public const val TIME_IN_HR: Short = 8
  public const val TIME_DAY_OF_MONTH: Short = 9
  public const val TIME_MONTH_VALUE: Short = 10
  public const val TIME_DAY_OF_WEEK: Short = 11
  public const val TIME_YEAR: Short = 12
}

public object MatrixOperations {
  private const val OFFSET = 0x320000
  public val IDENTITY: Float = Utils.asNan(OFFSET + 1)
  public val ROT_Z: Float = Utils.asNan(OFFSET + 4)
  public val TRANSLATE_X: Float = Utils.asNan(OFFSET + 5)
  public val TRANSLATE_Y: Float = Utils.asNan(OFFSET + 6)
  public val TRANSLATE2: Float = Utils.asNan(OFFSET + 8)
  public val SCALE_X: Float = Utils.asNan(OFFSET + 10)
  public val SCALE_Y: Float = Utils.asNan(OFFSET + 11)
  public val MUL: Float = Utils.asNan(OFFSET + 15)
  public val ROT_PZ: Float = Utils.asNan(OFFSET + 16)
}

public object Rc {
  public object Texture {
    public const val FILTER_DEFAULT: Short = 0
  }

  public object System {
    public val FONT_SIZE: Float = RemoteContext.FLOAT_FONT_SIZE
  }
}

public object DrawTextOnCircle {
  public enum class Alignment { START, CENTER, END }
  public enum class Placement { OUTSIDE, INSIDE }
}
