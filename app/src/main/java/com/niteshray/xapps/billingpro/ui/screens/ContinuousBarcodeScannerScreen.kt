package com.niteshray.xapps.billingpro.ui.screens

import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.niteshray.xapps.billingpro.ui.theme.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContinuousBarcodeScannerScreen(
    onBarcodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isScanning by remember { mutableStateOf(true) }
    var lastScannedTime by remember { mutableStateOf(0L) }
    
    // Camera executor
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    
                    // Preview
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    // Image analyzer for barcode detection
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analyzer ->
                            analyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                                processImageProxy(
                                    imageProxy = imageProxy,
                                    onBarcodeScanned = { barcode ->
                                        val currentTime = System.currentTimeMillis()
                                        // Prevent rapid scanning - allow one scan every 2 seconds
                                        if (isScanning && currentTime - lastScannedTime > 2000) {
                                            lastScannedTime = currentTime
                                            onBarcodeScanned(barcode)
                                        }
                                    }
                                )
                            }
                        }
                    
                    // Camera selector
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                    } catch (exc: Exception) {
                        // Handle exception
                    }
                    
                }, ContextCompat.getMainExecutor(ctx))
                
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Overlay UI
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Scan Barcode",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                )
            )
            
            // Center scanning area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Scanning frame
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .background(
                            Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    // Corner indicators
                    ScanningFrame()
                }
                
                // Instructions
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.padding(horizontal = 32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (isScanning) {
                                "Point camera at barcode\nScanning will pause for 2 seconds after each scan"
                            } else {
                                "Processing...\nReady to scan in a moment"
                            },
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            
            // Bottom controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause/Resume button
                Button(
                    onClick = { isScanning = !isScanning },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isScanning) WarningOrange else SecondaryTeal
                    ),
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        imageVector = if (isScanning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isScanning) "Pause" else "Resume",
                        tint = Color.White
                    )
                }
                
                // Close button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    ),
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ScanningFrame() {
    val cornerSize = 20.dp
    val cornerThickness = 4.dp
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Top-left corner
        Box(
            modifier = Modifier
                .size(cornerSize)
                .align(Alignment.TopStart)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cornerThickness)
                    .background(Color.White)
            )
            Box(
                modifier = Modifier
                    .width(cornerThickness)
                    .fillMaxHeight()
                    .background(Color.White)
            )
        }
        
        // Top-right corner
        Box(
            modifier = Modifier
                .size(cornerSize)
                .align(Alignment.TopEnd)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cornerThickness)
                    .background(Color.White)
            )
            Box(
                modifier = Modifier
                    .width(cornerThickness)
                    .fillMaxHeight()
                    .align(Alignment.TopEnd)
                    .background(Color.White)
            )
        }
        
        // Bottom-left corner
        Box(
            modifier = Modifier
                .size(cornerSize)
                .align(Alignment.BottomStart)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cornerThickness)
                    .align(Alignment.BottomStart)
                    .background(Color.White)
            )
            Box(
                modifier = Modifier
                    .width(cornerThickness)
                    .fillMaxHeight()
                    .background(Color.White)
            )
        }
        
        // Bottom-right corner
        Box(
            modifier = Modifier
                .size(cornerSize)
                .align(Alignment.BottomEnd)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cornerThickness)
                    .align(Alignment.BottomEnd)
                    .background(Color.White)
            )
            Box(
                modifier = Modifier
                    .width(cornerThickness)
                    .fillMaxHeight()
                    .align(Alignment.BottomEnd)
                    .background(Color.White)
            )
        }
    }
}

private fun processImageProxy(
    imageProxy: ImageProxy,
    onBarcodeScanned: (String) -> Unit
) {
    val inputImage = InputImage.fromMediaImage(
        imageProxy.image!!,
        imageProxy.imageInfo.rotationDegrees
    )
    
    val scanner = BarcodeScanning.getClient()
    
    scanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            for (barcode in barcodes) {
                when (barcode.valueType) {
                    Barcode.TYPE_TEXT,
                    Barcode.TYPE_PRODUCT,
                    Barcode.TYPE_ISBN,
                    Barcode.TYPE_UNKNOWN -> {
                        barcode.rawValue?.let { value ->
                            onBarcodeScanned(value)
                        }
                    }
                }
            }
        }
        .addOnFailureListener {
            // Handle error
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}
