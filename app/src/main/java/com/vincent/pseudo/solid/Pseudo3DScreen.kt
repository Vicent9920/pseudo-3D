package com.vincent.pseudo.solid

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * 三维点数据类
 */
data class Point3D(
    val x: Float,
    val y: Float,
    val z: Float
) {
    companion object {
        // 零点
        val Zero = Point3D(0f, 0f, 0f)
    }

}


@Composable
fun Rotating3DPlanesDemo(modifier: Modifier) {
    var angleDeg by remember { mutableDoubleStateOf(0.0) }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        // Canvas 部分
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // 平移到圆心，重置起点
            drawContext.canvas.nativeCanvas.translate(size.width / 2, size.height / 2)

            // X轴
            drawAxis(
                drawContext.canvas.nativeCanvas,
                Color.Red,
                Point3D(5f, 0f, 0f),
                "X",
                angleDeg
            )
            // Y轴
            drawAxis(
                drawContext.canvas.nativeCanvas,
                Color.Green,
                Point3D(0f, 5f, 0f),
                "Y",
                angleDeg
            )

            // Z轴
            drawAxis(
                drawContext.canvas.nativeCanvas,
                Color.Blue,
                Point3D(0f, 0f, 5f),
                "Z",
                angleDeg
            )
        }
        Spacer(Modifier.height(16.dp))
        // Slider 部分
        Text(
            "Rotation: ${angleDeg.toInt()}°",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Slider(
            value = angleDeg.toFloat(),
            onValueChange = { angleDeg = it.toDouble() },
            // 绕Z轴旋转的范围是0到2π
            valueRange = 0f..(Math.PI*2).toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// 绘制坐标系轴
private fun drawAxis(
    canvas: NativeCanvas,
    color: Color,
    end: Point3D,
    label: String,
    rotationZ: Double
) {
    val startOffset = project(Point3D.Zero, rotationZ)
    val endOffset = project(end, rotationZ)
    val paint = Paint().apply {
        this.color = color.toArgb()
        strokeWidth = 2.dp.value
        isAntiAlias = true
    }
    canvas.drawLine(
        startOffset.x,
        startOffset.y,
        endOffset.x,
        endOffset.y,
        paint
    )

    // 绘制轴标签
    paint.textSize = 32f
    canvas.drawText(
        label,
        endOffset.x + 20,
        endOffset.y - 20,
        paint
    )
}

// 投影函数 正等轴测投影
fun project(p: Point3D, rotationZ: Double): Offset {

    val scale = 40f
    // 使用弧度进行计算，
    val angle = 30.0 / 180.0 * Math.PI // 取整数会出错，需要注意数字计算
    // 绕Z轴旋转
    val rx = p.x * cos(rotationZ) - p.y * sin(rotationZ)
    val ry = p.x * sin(rotationZ) + p.y * cos(rotationZ)
    // 等轴测投影
    val xProj = (rx - ry) * cos(angle)
    val yProj = (rx + ry) * sin(angle) - p.z

    return Offset((xProj * scale).toFloat(), (yProj * scale).toFloat())
}
