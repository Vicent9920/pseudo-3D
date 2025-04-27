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
    var distance by remember { mutableDoubleStateOf(50.0) }
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
                angleDeg,
                distance
            )
            // Y轴
            drawAxis(
                drawContext.canvas.nativeCanvas,
                Color.Green,
                Point3D(0f, 5f, 0f),
                "Y",
                angleDeg,
                distance
            )

            // Z轴
            drawAxis(
                drawContext.canvas.nativeCanvas,
                Color.Blue,
                Point3D(0f, 0f, 5f),
                "Z",
                angleDeg,
                distance
            )

            drawGrid(drawContext.canvas.nativeCanvas, angleDeg, distance)
        }
        Spacer(Modifier.height(16.dp))
        // Slider 部分
        Text(
            "Rotation: ${(angleDeg * 360 / 2 / Math.PI).toInt()}°",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Slider(
            value = angleDeg.toFloat(),
            onValueChange = { angleDeg = it.toDouble() },
            // 绕Z轴旋转的范围是0到2π
            valueRange = 0f..(Math.PI * 2).toFloat(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))
        // Slider 部分
        Text(
            "Distance: $distance",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Slider(
            value = distance.toFloat(),
            onValueChange = { distance = it.toDouble() },
            // 绕Z轴旋转的范围是0到2π
            valueRange = 18f..100f,
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
    rotationZ: Double,
    distance: Double
) {
    val startOffset = project(Point3D.Zero, rotationZ, distance)
    val endOffset = project(end, rotationZ, distance)
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

fun drawGrid(canvas: NativeCanvas, rotationZ: Double, distance: Double) {
    val lines: MutableList<Pair<Point3D, Point3D>> = mutableListOf()
    for (i in -32..32) {
        lines.add(Pair(Point3D(i.toFloat(), -5f, 0f), Point3D(i.toFloat(), 5f, 0f)))
        lines.add(Pair(Point3D(-5f, i.toFloat(), 0f), Point3D(5f, i.toFloat(), 0f)))
    }
    val paint = Paint().apply {
        this.color = Color.Black.toArgb()
        strokeWidth = 1.dp.value
        isAntiAlias = true
    }
    lines.forEach { line ->
        val p0 = project(line.first, rotationZ, distance)
        val p1 = project(line.second, rotationZ, distance)
        canvas.drawLine(p0.x, p0.y, p1.x, p1.y, paint)
    }

}

// 投影函数 正等轴测投影
fun project(p: Point3D, rotationZ: Double, distance: Double): Offset {

    val scale = 32f
    // 专业数学环境（微积分、复分析）中，三角函数的参数默认是“弧度”。
    // 因此 Math 三角函数中的参数都是弧度，因此这里需要转换角度
    val angle = 30.0 / 180.0 * Math.PI // 取整数会出错，需要注意数字计算
    // 绕Z轴旋转 线性变换xy
    val rx = p.x * cos(rotationZ) - p.y * sin(rotationZ)
    val ry = p.x * sin(rotationZ) + p.y * cos(rotationZ)
    // 等轴测投影 线性变换xy
    val xProj = (rx - ry) * cos(angle)
    val yProj = (rx + ry) * sin(angle) - p.z

    // 增加透视
    var radio = distance / (distance - yProj)
    if (distance == 0.0) {
        radio = 1.0
    }

    return Offset((xProj * scale * radio).toFloat(), (yProj * scale * radio).toFloat())
}
