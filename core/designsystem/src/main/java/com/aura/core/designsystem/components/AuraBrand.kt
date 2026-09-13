package com.aura.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aura.core.designsystem.R
import com.aura.core.designsystem.theme.AuraColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object AuraIcons {
    val Flow: ImageVector by lazy {
        ImageVector.Builder(
            name = "AuraFlow",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black), stroke = null) {
                repeat(18) { index ->
                    val angle = (2.0 * PI * index / 18.0) - (PI / 2.0)
                    val x = 12f + (9f * cos(angle)).toFloat()
                    val y = 12f + (9f * sin(angle)).toFloat()
                    val radius = 0.6f
                    moveTo(x - radius, y - radius)
                    horizontalLineTo(x + radius)
                    verticalLineTo(y + radius)
                    horizontalLineTo(x - radius)
                    close()
                }
            }
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(8.2f, 15.8f)
                lineTo(12f, 7.8f)
                lineTo(15.8f, 15.8f)
                moveTo(10.2f, 13.2f)
                lineTo(13.8f, 13.2f)
            }
        }.build()
    }
}

@Composable
fun AuraBrandMark(
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Image(
        painter = painterResource(R.drawable.aura_logo),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
fun AuraFlowGlyph(
    modifier: Modifier = Modifier,
    tint: Color = AuraColors.textPrimary,
    contentDescription: String? = null
) {
    Icon(
        imageVector = AuraIcons.Flow,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
    )
}
