package com.vincent.pseudo.solid

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
    var rotationX by remember { mutableFloatStateOf(30f) }
    var rotationY by remember { mutableFloatStateOf(45f) }
    val rotationZ by remember { mutableFloatStateOf(0f) }
    var lastDragPosition by remember { mutableStateOf<Offset?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box3D(
            modifier = Modifier
                .size(400.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset -> lastDragPosition = offset },
                        onDragEnd = { lastDragPosition = null },
                        onDragCancel = { lastDragPosition = null },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            rotationX = (rotationX + dragAmount.y / 3) % 360
                            rotationY = (rotationY + dragAmount.x / 3) % 360
                        }
                    )
                },
            rotationX = rotationX,
            rotationY = rotationY,
            rotationZ = rotationZ
        )
    }
}

@Composable
fun Box3D(
    modifier: Modifier = Modifier,
    rotationX: Float = 30f,
    rotationY: Float = 45f,
    rotationZ: Float = 0f
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // 投影函数
        fun project(p: Point3D): Offset {
            val scale = 40f
            
            // 转换为弧度
            val rx = (rotationX * PI / 180).toFloat()
            val ry = (rotationY * PI / 180).toFloat()
            val rz = (rotationZ * PI / 180).toFloat()

            // 绕X轴旋转
            val y1 = p.y * cos(rx) - p.z * sin(rx)
            val z1 = p.y * sin(rx) + p.z * cos(rx)

            // 绕Y轴旋转
            val x2 = p.x * cos(ry) + z1 * sin(ry)
//            val z2 = -p.x * sin(ry) + z1 * cos(ry)

            // 绕Z轴旋转
            val x3 = x2 * cos(rz) - y1 * sin(rz)
            val y3 = x2 * sin(rz) + y1 * cos(rz)

            return Offset(
                x3 * scale + canvasWidth / 2,
                -y3 * scale + canvasHeight / 2 // Y轴翻转
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

        // 绘制网格
        fun drawGrid() {
            val gridSize = 5
            val step = 1f
            val gridColor = Color.Gray.copy(alpha = 0.3f)
            val paint = Paint().apply {
                color = gridColor.toArgb()
                strokeWidth = 1f
                isAntiAlias = true
            }

            // XY平面网格
            for (i in -gridSize..gridSize) {
                val startX = project(Point3D(i * step, -gridSize * step, 0f))
                val endX = project(Point3D(i * step, gridSize * step, 0f))
                val startY = project(Point3D(-gridSize * step, i * step, 0f))
                val endY = project(Point3D(gridSize * step, i * step, 0f))

                drawContext.canvas.nativeCanvas.apply {
                    drawLine(startX.x, startX.y, endX.x, endX.y, paint)
                    drawLine(startY.x, startY.y, endY.x, endY.y, paint)
                }
            }

            // XZ平面网格
            for (i in -gridSize..gridSize) {
                val startX = project(Point3D(i * step, 0f, -gridSize * step))
                val endX = project(Point3D(i * step, 0f, gridSize * step))
                val startZ = project(Point3D(-gridSize * step, 0f, i * step))
                val endZ = project(Point3D(gridSize * step, 0f, i * step))

                drawContext.canvas.nativeCanvas.apply {
                    drawLine(startX.x, startX.y, endX.x, endX.y, paint)
                    drawLine(startZ.x, startZ.y, endZ.x, endZ.y, paint)
                }
            }

            // YZ平面网格
            for (i in -gridSize..gridSize) {
                val startY = project(Point3D(0f, i * step, -gridSize * step))
                val endY = project(Point3D(0f, i * step, gridSize * step))
                val startZ = project(Point3D(0f, -gridSize * step, i * step))
                val endZ = project(Point3D(0f, gridSize * step, i * step))

                drawContext.canvas.nativeCanvas.apply {
                    drawLine(startY.x, startY.y, endY.x, endY.y, paint)
                    drawLine(startZ.x, startZ.y, endZ.x, endZ.y, paint)
                }
            }
        }

        // 执行绘制
        drawGrid()
        drawAxis(Color.Red, Point3D(5f, 0f, 0f), "X")
        drawAxis(Color.Green, Point3D(0f, 5f, 0f), "Y")
        drawAxis(Color.Blue, Point3D(0f, 0f, 5f), "Z")
    }
}