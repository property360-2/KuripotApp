package com.malikhain.kuripot_app.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun PieChart(
    data: List<PieChartData>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(200.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = min(size.width, size.height) / 2 * 0.8f
        var startAngle = 0f
        
        data.forEach { item ->
            val sweepAngle = ((item.value / data.sumOf { it.value.toDouble() }) * 360).toFloat()
            
            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            
            startAngle += sweepAngle
        }
    }
}

@Composable
fun BarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier
) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val shapeSmall = MaterialTheme.shapes.small
    Column(modifier = modifier) {
        data.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(80.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .background(
                            color = surfaceVariant,
                            shape = shapeSmall
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(if (data.maxOfOrNull { it.value } ?: 0f > 0) item.value / (data.maxOfOrNull { it.value } ?: 1f) else 0f)
                            .background(
                                color = item.color,
                                shape = shapeSmall
                            )
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "₱${String.format("%.0f", item.value)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LineChart(
    data: List<LineChartData>,
    modifier: Modifier = Modifier
) {
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val primary = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.height(200.dp)) {
        if (data.size < 2) return@Canvas
        
        val width = size.width
        val height = size.height
        val padding = 40f
        
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding
        
        val maxValue = data.maxOfOrNull { it.value } ?: 0f
        val minValue = data.minOfOrNull { it.value } ?: 0f
        val valueRange = maxValue - minValue
        
        // Draw grid lines
        val gridLines = 5
        for (i in 0..gridLines) {
            val y = padding + (chartHeight / gridLines) * i
            drawLine(
                color = outline,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f
            )
        }
        
        // Draw data points and lines
        val points = data.mapIndexed { index, item ->
            val x = padding + (chartWidth / (data.size - 1)) * index
            val y = if (valueRange > 0) {
                padding + chartHeight - (chartHeight * (item.value - minValue) / valueRange)
            } else {
                padding + chartHeight / 2
            }
            Offset(x, y)
        }
        
        // Draw connecting lines
        for (i in 0 until points.size - 1) {
            drawLine(
                color = primary,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 3f
            )
        }
        
        // Draw data points
        points.forEach { point ->
            drawCircle(
                color = primary,
                radius = 6f,
                center = point
            )
        }
    }
}

data class PieChartData(
    val label: String,
    val value: Float,
    val color: Color
)

data class BarChartData(
    val label: String,
    val value: Float,
    val color: Color
)

data class LineChartData(
    val label: String,
    val value: Float
) 