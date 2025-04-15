package com.vincent.pseudo.solid

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlin.math.*

// 三维点数据类
data class Point3D(
    val x: Float,
    val y: Float,
    val z: Float
) {
    companion object {
        val Zero = Point3D(0f, 0f, 0f)
    }
}

@Composable
fun Pseudo3DScreen(modifier: Modifier) {
    var rotationZ by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box3D(
            modifier = Modifier.size(400.dp),
            rotationZ = rotationZ
        )

        Spacer(modifier = Modifier.height(32.dp))

        Slider(
            value = rotationZ,
            onValueChange = { rotationZ = it },
            valueRange = 0f..(2 * PI).toFloat(),
            modifier = Modifier.width(300.dp)
        )
    }
}

@Composable
fun Box3D(
    modifier: Modifier = Modifier,
    rotationZ: Float = 0f
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // 投影函数
        fun project(p: Point3D): Offset {
            val scale = 40f
            val angle = (30f / 180f * PI).toFloat()

            // Z轴旋转
            val rx = p.x * cos(rotationZ) - p.y * sin(rotationZ)
            val ry = p.x * sin(rotationZ) + p.y * cos(rotationZ)

            // 等轴测投影
            val xProj = (rx - ry) * cos(angle)
            val yProj = (rx + ry) * sin(angle) - p.z

            return Offset(
                xProj * scale + canvasWidth / 2,
                -yProj * scale + canvasHeight / 2 // Y轴翻转
            )
        }

        // 绘制坐标系轴
        fun drawAxis(color: Color, end: Point3D, label: String) {
            val startOffset = project(Point3D.Zero)
            val endOffset = project(end)
            val paint = Paint().apply {
                this.color = color.toArgb()
                strokeWidth = 4f
                isAntiAlias = true
            }

            drawContext.canvas.nativeCanvas.drawLine(
                startOffset.x,
                startOffset.y,
                endOffset.x,
                endOffset.y,
                paint
            )

            // 绘制轴标签
            paint.textSize = 32f
            drawContext.canvas.nativeCanvas.drawText(
                label,
                endOffset.x + 20,
                endOffset.y - 20,
                paint
            )
        }

        // 绘制三维平面
        fun drawPlane() {
            val lines = listOf(
                Pair(Point3D(0f, 0f, 0f), Point3D(1f, 0f, 0f)),
                Pair(Point3D(0f, 0f, 0f), Point3D(0f, 1f, 0f)),
                Pair(Point3D(0f, 0f, 0f), Point3D(1f, 1f, 0f)),
                Pair(Point3D(0f, 1f, 0f), Point3D(1f, 1f, 0f)),
                Pair(Point3D(1f, 0f, 0f), Point3D(1f, 1f, 0f)),
                Pair(Point3D(0f, 1f, 0f), Point3D(1f, 0f, 0f))
            )

            val paint = Paint().apply {
                color = Color.White.toArgb()
                strokeWidth = 2f
                isAntiAlias = true
            }

            lines.forEach { (start, end) ->
                val startOffset = project(start)
                val endOffset = project(end)
                drawContext.canvas.nativeCanvas.drawLine(
                    startOffset.x,
                    startOffset.y,
                    endOffset.x,
                    endOffset.y,
                    paint
                )
            }
        }

        // 执行绘制
        drawAxis(Color.Red, Point3D(5f, 0f, 0f), "X")
        drawAxis(Color.Green, Point3D(0f, 5f, 0f), "Y")
        drawAxis(Color.Blue, Point3D(0f, 0f, -5f), "Z")
        drawPlane()
    }
}