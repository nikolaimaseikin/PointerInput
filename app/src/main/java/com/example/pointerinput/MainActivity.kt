package com.example.pointerinput

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import com.example.pointerinput.ui.theme.PointerInputTheme
import kotlin.math.absoluteValue


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PointerInputTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var offsetX by remember { mutableStateOf(0f) }
                    var offsetY by remember { mutableStateOf(0f) }

                    //DualTouchExample()
                    MultiTouchGestureHandler(onDeltaXYChange = { x, y ->
                        offsetX += x
                        offsetY += y
                        Log.d("PointerAction", "x: $offsetX y: $offsetY") }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MultiTouchGestureHandler(onDeltaXYChange: (Float, Float) -> Unit) {
    val pointerIds = remember { mutableSetOf<Int>() }
    var previousPointerCount by remember {
        mutableStateOf( 0)
    }
    var startX1 = remember { 0f }
    var startY1 = remember { 0f }
    var startX2 = remember { 0f }
    var startY2 = remember { 0f }
    var previousXOffset = remember { 0f }
    var previousYOffset = remember { 0f }

    var xScale by remember {
        mutableStateOf(1f)
    }
    var yScale by remember {
        mutableStateOf(1f)
    }
    var center by remember {
        mutableStateOf(Offset.Zero)
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter { event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                        val id = event.getPointerId(event.actionIndex)
                        pointerIds.add(id)
                        if (pointerIds.size == 1) {
                            startX1 = event.getX(event.findPointerIndex(pointerIds.elementAt(0)))
                            startY1 = event.getY(event.findPointerIndex(pointerIds.elementAt(0)))
                        }
                        if (pointerIds.size == 2) {
                            startX1 = event.getX(event.findPointerIndex(pointerIds.elementAt(0)))
                            startY1 = event.getY(event.findPointerIndex(pointerIds.elementAt(0)))
                            startX2 = event.getX(event.findPointerIndex(pointerIds.elementAt(1)))
                            startY2 = event.getY(event.findPointerIndex(pointerIds.elementAt(1)))
                            previousXOffset = (startX2 - startX1).absoluteValue
                            previousYOffset = (startY2 - startY1).absoluteValue
                        }
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (pointerIds.size == 1) {
                            val x1 = event.getX(event.findPointerIndex(pointerIds.elementAt(0)))
                            val y1 = event.getY(event.findPointerIndex(pointerIds.elementAt(0)))
                            val xOffset = x1 - startX1
                            val yOffset = y1 - startY1
                            center += Offset(xOffset, yOffset)
                            startX1 = x1
                            startY1 = y1
                        }
                        if (pointerIds.size == 2) {
                            if(previousPointerCount < 3){
                                val x1 = event.getX(event.findPointerIndex(pointerIds.elementAt(0)))
                                val y1 = event.getY(event.findPointerIndex(pointerIds.elementAt(0)))
                                val x2 = event.getX(event.findPointerIndex(pointerIds.elementAt(1)))
                                val y2 = event.getY(event.findPointerIndex(pointerIds.elementAt(1)))

                                val currentXOffset = (x2 - x1).absoluteValue
                                val currentYOffset = (y2 - y1).absoluteValue

                                onDeltaXYChange(
                                    currentXOffset - previousXOffset,
                                    currentYOffset - previousYOffset
                                )
                                xScale += (currentXOffset - previousXOffset) * 0.01f
                                yScale += (currentYOffset - previousYOffset) * 0.01f

                                startX1 = x1
                                startY1 = y1
                                startX2 = x2
                                startY2 = y2
                                previousXOffset = currentXOffset
                                previousYOffset = currentYOffset
                            }
                        }
                        previousPointerCount = pointerIds.size
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                        val id = event.getPointerId(event.actionIndex)
                        pointerIds.remove(id)
                        if(previousPointerCount <= 2 && pointerIds.size != 0){
                            startX1 = event.getX(event.findPointerIndex(pointerIds.elementAt(0)))
                            startY1 = event.getY(event.findPointerIndex(pointerIds.elementAt(0)))
                        }
                    }
                }
                true
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        drawRect(
            color = Color.Blue, // Цвет прямоугольника
            topLeft = Offset(center.x - (100f * xScale) / 2f, center.y - (100f * yScale) / 2f),
            size = Size(100f * xScale, 100f * yScale),
            style = Stroke(width = 8f) // Ширина обводки
        )
    }
}