package com.github.mheerwaarden.eventdemo.ui.icons.filled

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

// Copied icon
val EditCalendar: ImageVector
    get() {
        if (_editCalendar != null) {
            return _editCalendar!!
        }
        _editCalendar = materialIcon(name = "Filled.EditCalendar") {
            materialPath {
                moveTo(12.0f, 22.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-1.11f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                lineTo(3.01f, 6.0f)
                curveToRelative(0.0f, -1.1f, 0.88f, -2.0f, 1.99f, -2.0f)
                horizontalLineToRelative(1.0f)
                verticalLineTo(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineTo(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(10.0f)
                horizontalLineToRelative(7.0f)
                verticalLineTo(22.0f)
                close()
                moveTo(22.13f, 16.99f)
                lineToRelative(0.71f, -0.71f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineToRelative(-0.71f, -0.71f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineToRelative(-0.71f, 0.71f)
                lineTo(22.13f, 16.99f)
                close()
                moveTo(21.42f, 17.7f)
                lineToRelative(-5.3f, 5.3f)
                horizontalLineTo(14.0f)
                verticalLineToRelative(-2.12f)
                lineToRelative(5.3f, -5.3f)
                lineTo(21.42f, 17.7f)
                close()
            }
        }
        return _editCalendar!!
    }

private var _editCalendar: ImageVector? = null
