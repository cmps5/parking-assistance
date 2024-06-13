package com.cdjr.parkassist.car_info_video.ui

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cdjr.parkassist.ui.theme.ParkAssistTheme
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CarInfoVideoScreenEntry(
    modifier: Modifier,
    viewModel: CarInfoVideoViewModel = viewModel()
) {
    val uiData by viewModel.uiData.collectAsState()
    val showUI by viewModel.showUI.collectAsState(initial = false)

    if (showUI) {
        CarInfoVideoScreen(
            uiData = uiData,
            modifier = modifier,
        )
    }
}

@Composable
private fun CarInfoVideoScreen(
    uiData: CarInfoUIData,
    modifier: Modifier,
) {
    var showAroundCarInformation by remember { mutableStateOf(false) }
    ParkAssistTheme {
        Scaffold(
            modifier = modifier,
            containerColor = Color.Black,
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CarVideo(modifier = Modifier.fillMaxWidth())
                CarInfo(
                    uiData = uiData,
                    modifier = Modifier.fillMaxWidth(),
                    show = showAroundCarInformation,
                    close = { showAroundCarInformation = false },
                )
                Button(
                    onClick = { showAroundCarInformation = true },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Text(text = "More Info")
                }
                if (uiData.showHittingWarning) {
                    Icon(
                        imageVector = Icons.Sharp.Warning,
                        contentDescription = null,
                        tint = Color.Yellow,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(24.dp)
                            .size(96.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CarVideo(modifier: Modifier) {
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { context ->
            val previewView = PreviewView(context).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val previewUseCase = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            coroutineScope.launch {
                val cameraProvider = context.getCameraProvider()
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, previewUseCase)
                } catch (ex: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", ex)
                }
            }
            previewView
        },
        modifier = modifier,
    )
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener({
            continuation.resume(future.get())
        }, executor)
    }
}

val Context.executor: Executor
    get() = ContextCompat.getMainExecutor(this)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarInfo(
    modifier: Modifier,
    uiData: CarInfoUIData,
    show: Boolean,
    close: (() -> Unit),
) {
    val sheetState = rememberModalBottomSheetState()
    if (show) {
        ModalBottomSheet(
            onDismissRequest = close,
            sheetState = sheetState,
            modifier = modifier,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                DistanceText(uiData = uiData, modifier = Modifier.padding(bottom = 16.dp))
                SpeedText(uiData = uiData, modifier = Modifier.padding(bottom = 16.dp))
                TimeToHitText(uiData = uiData)
            }
        }
    }
}

@Composable
private fun DistanceText(
    uiData: CarInfoUIData,
    modifier: Modifier = Modifier,
) {
    uiData.distance?.let {
        Text(
            text = "Distance: $it",
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier,
        )
    }
}

@Composable
private fun SpeedText(
    uiData: CarInfoUIData,
    modifier: Modifier = Modifier,
) {
    uiData.speed?.let {
        val speed = if (it < 0) "moving away" else it
        Text(
            text = "Speed: $speed",
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier,
        )
    }
}

@Composable
private fun TimeToHitText(
    uiData: CarInfoUIData,
    modifier: Modifier = Modifier,
) {
    uiData.timeToHit?.let {
        val timeToHit = if ((uiData.speed ?: 0F) < 0) "moving away" else it
        Text(
            text = "Time to hit: $timeToHit",
            style = MaterialTheme.typography.titleLarge,
            color = if (uiData.showHittingWarning) Color.Red else Color.Unspecified,
            modifier = modifier,
        )
    }
}

data class CarInfoUIData(
    val distance: Float? = null,
    val speed: Float? = null,
    val timeToHit: Float? = null,
    val showHittingWarning: Boolean = false,
)
