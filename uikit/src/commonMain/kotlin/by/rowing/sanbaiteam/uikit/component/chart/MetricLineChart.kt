package by.rowing.sanbaiteam.uikit.component.chart

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.uikit.component.chart.models.ChartRange
import by.rowing.sanbaiteam.uikit.component.chart.models.MetricChartPoint

/**
 * Line chart with a transposed layout: time grows top to bottom, values grow left to right.
 *
 * The component is presentation-only and does not know anything about training metrics, so it can
 * be reused for any time series. Pass formatting lambdas for the axes.
 */
@AllowDetektPublic
@Composable
fun MetricLineChart(
    points: List<MetricChartPoint>,
    durationMillis: Long,
    valueFormatter: (Double) -> String,
    timeFormatter: (Long) -> String,
    modifier: Modifier = Modifier,
    averageValue: Double? = null,
    reverseValueAxis: Boolean = false,
) {
    val textMeasurer = rememberTextMeasurer()
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val lineColor = MaterialTheme.colorScheme.primary
    val averageColor = MaterialTheme.colorScheme.secondary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = labelColor)

    Canvas(modifier = modifier) {
        if (points.isEmpty() || durationMillis <= 0L) return@Canvas
        val leftInset = 48.dp.toPx()
        val bottomInset = 22.dp.toPx()
        val topInset = 8.dp.toPx()
        val rightInset = 10.dp.toPx()
        val plotRight = size.width - rightInset
        val plotBottom = size.height - bottomInset
        if (plotRight <= leftInset || plotBottom <= topInset) return@Canvas

        val valueRange = niceRange(
            min = points.minOf { it.value },
            max = points.maxOf { it.value },
        )
        val layout = ChartLayout(
            plotLeft = leftInset,
            plotTop = topInset,
            plotRight = plotRight,
            plotBottom = plotBottom,
            durationMillis = durationMillis,
            valueRange = valueRange,
            reverseValueAxis = reverseValueAxis,
            textMeasurer = textMeasurer,
            labelStyle = labelStyle,
            gridColor = gridColor,
        )

        drawValueAxis(
            layout = layout,
            valueFormatter = valueFormatter,
        )
        drawTimeAxis(
            layout = layout,
            timeFormatter = timeFormatter,
        )

        drawAverageLine(
            averageValue = averageValue,
            valueRange = valueRange,
            averageColor = averageColor,
            layout = layout
        )
        drawValuesLine(
            points = points,
            durationMillis = durationMillis,
            lineColor = lineColor,
            xForValue = layout::xForValue,
            yForTime = layout::yForTime
        )
    }
}

private fun DrawScope.drawValuesLine(
    points: List<MetricChartPoint>,
    durationMillis: Long,
    lineColor: Color,
    xForValue: (Double) -> Float,
    yForTime: (Long) -> Float
) {
    buildContinuousSegments(
        points = points,
        durationMillis = durationMillis
    ).forEach { segment ->
        if (segment.size == 1) {
            drawValueDot(
                point = segment.first(),
                lineColor = lineColor,
                xForValue = xForValue,
                yForTime = yForTime
            )
        } else {
            drawValueSegment(
                segment = segment,
                xForValue = xForValue,
                yForTime = yForTime,
                lineColor = lineColor
            )
        }
    }
}

private fun DrawScope.drawValueSegment(
    segment: List<MetricChartPoint>,
    xForValue: (Double) -> Float,
    yForTime: (Long) -> Float,
    lineColor: Color
) {
    val path = Path()
    segment.forEachIndexed { index, point ->
        val x = xForValue(point.value)
        val y = yForTime(point.elapsedMillis)
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(
        path = path,
        color = lineColor,
        style = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}

private fun DrawScope.drawValueDot(
    point: MetricChartPoint,
    lineColor: Color,
    xForValue: (Double) -> Float,
    yForTime: (Long) -> Float
) {
    drawCircle(
        color = lineColor,
        radius = 2.dp.toPx(),
        center = Offset(
            x = xForValue(point.value),
            y = yForTime(point.elapsedMillis),
        ),
    )
}

private fun DrawScope.drawAverageLine(
    averageValue: Double?,
    valueRange: ChartRange,
    averageColor: Color,
    layout: ChartLayout
) {
    averageValue?.takeIf { it in valueRange.min..valueRange.max }?.let { average ->
        val x = layout.xForValue(average)
        drawLine(
            color = averageColor,
            start = Offset(
                x = x,
                y = layout.plotTop
            ),
            end = Offset(
                x = x,
                y = layout.plotBottom
            ),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
        )
    }
}

private class ChartLayout(
    val plotLeft: Float,
    val plotTop: Float,
    val plotRight: Float,
    val plotBottom: Float,
    val durationMillis: Long,
    val valueRange: ChartRange,
    val reverseValueAxis: Boolean,
    val textMeasurer: TextMeasurer,
    val labelStyle: TextStyle,
    val gridColor: Color,
) {

    fun xForValue(value: Double): Float {
        val ratio = ((value - valueRange.min) / (valueRange.max - valueRange.min)).coerceIn(0.0, 1.0)
        val normalized = if (reverseValueAxis) 1.0 - ratio else ratio
        return plotLeft + normalized.toFloat() * (plotRight - plotLeft)
    }

    fun yForTime(elapsed: Long): Float =
        plotTop + (elapsed.toDouble() / durationMillis).coerceIn(0.0, 1.0).toFloat() * (plotBottom - plotTop)
}

private fun DrawScope.drawValueAxis(
    layout: ChartLayout,
    valueFormatter: (Double) -> String,
) {
    var tick = layout.valueRange.min
    val epsilon = layout.valueRange.step / 1000.0
    while (tick <= layout.valueRange.max + epsilon) {
        val x = layout.xForValue(tick)
        drawLine(
            color = layout.gridColor,
            start = Offset(
                x = x,
                y = layout.plotTop
            ),
            end = Offset(
                x = x,
                y = layout.plotBottom
            ),
            strokeWidth = 0.5.dp.toPx(),
        )
        val text = valueFormatter(tick)
        val measured = layout.textMeasurer.measure(
            text = text,
            style = layout.labelStyle
        )
        drawText(
            textMeasurer = layout.textMeasurer,
            text = text,
            topLeft = Offset(
                x = (x - measured.size.width / 2f).coerceIn(0f, size.width - measured.size.width),
                y = layout.plotBottom + 4.dp.toPx()
            ),
            style = layout.labelStyle,
        )
        tick += layout.valueRange.step
        if (layout.valueRange.step <= 0.0) break
    }
}

private fun DrawScope.drawTimeAxis(
    layout: ChartLayout,
    timeFormatter: (Long) -> String,
) {
    val step = timeTickStep(durationMillis = layout.durationMillis)
    val ticks = buildList {
        var value = 0L
        while (value <= layout.durationMillis) {
            add(value)
            value += step
        }
        if (lastOrNull() != layout.durationMillis) {
            if (layout.durationMillis - last() > step / 2) {
                add(layout.durationMillis)
            } else {
                set(lastIndex, layout.durationMillis)
            }
        }
    }
    ticks.forEach { tick ->
        val y = layout.yForTime(tick)
        drawLine(
            color = layout.gridColor,
            start = Offset(
                x = layout.plotLeft,
                y = y
            ),
            end = Offset(
                x = layout.plotRight,
                y = y
            ),
            strokeWidth = 0.5.dp.toPx(),
        )
        val text = timeFormatter(tick)
        val measured = layout.textMeasurer.measure(
            text = text,
            style = layout.labelStyle
        )
        val textY = (y - measured.size.height / 2f).coerceIn(0f, size.height - measured.size.height)
        drawText(
            textMeasurer = layout.textMeasurer,
            text = text,
            topLeft = Offset(
                x = 0f,
                y = textY
            ),
            style = layout.labelStyle,
        )
    }
}