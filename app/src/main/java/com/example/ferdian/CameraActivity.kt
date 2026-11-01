package com.example.ferdian

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ferdian.databinding.ActivityCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService

    private var imageAnalyzer: ImageAnalysis? = null
    private var model: Interpreter? = null

    @Volatile
    private var isProcessingImage = false

    private val modelLock = Any()

    // Hasil deteksi
    @Volatile
    private var detectedClass: String = ""

    @Volatile
    private var accuracy: Double = 0.0

    // Diisi saat model dimuat (dibaca dari bentuk tensor model, bukan hardcode)
    private var labels: List<String> = emptyList()
    private var inputSize = 300
    private var numClasses = 6
    private var inputBuffer: ByteBuffer? = null
    private var pixels = IntArray(0)
    private var outputBuffer: Array<FloatArray> = arrayOf(FloatArray(0))

    private var lensFacing = CameraSelector.LENS_FACING_BACK

    // Simpan gambar yang benar-benar masuk ke model (sekali) untuk diperiksa
    @Volatile
    private var saveDebugFrame = SAVE_DEBUG_FRAME


    // ============================================================
    // ON CREATE
    // ============================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK
        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = false
        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightNavigationBars = false

        cameraExecutor = Executors.newSingleThreadExecutor()

        loadLabels()
        loadModel()
        cameraExecutor.execute { runAssetSelfTest() }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        setupDetectionClickListener()
    }


    // ============================================================
    // CLICK DETECTION
    // ============================================================

    private fun setupDetectionClickListener() {

        binding.detectionLabel.setOnClickListener {

            val currentClass = detectedClass

            val validClasses = listOf(
                "Busuk Kering - Leaf Mold",
                "Bercak Coklat - Septoria",
                "Bercak Coklat - Early Blight",
                "Daun Tomat Sehat",
                "Busuk Basah - Late Blight"
            )

            if (currentClass in validClasses) {
                val (description, details) = when (currentClass) {
                    "Busuk Kering - Leaf Mold" -> Pair(
                        getString(R.string.leaf_mold_description),
                        getString(R.string.leaf_mold_detail)
                    )
                    "Bercak Coklat - Septoria" -> Pair(
                        getString(R.string.septoria_description),
                        getString(R.string.septoria_detail)
                    )
                    "Bercak Coklat - Early Blight" -> Pair(
                        getString(R.string.early_blight_description),
                        getString(R.string.early_blight_detail)
                    )
                    "Daun Tomat Sehat" -> Pair(
                        getString(R.string.healthy_leaf_description),
                        getString(R.string.healthy_leaf_detail)
                    )
                    "Busuk Basah - Late Blight" -> Pair(
                        getString(R.string.late_blight_description),
                        getString(R.string.late_blight_detail)
                    )
                    else -> throw IllegalStateException("Unexpected detected class: $currentClass")
                }

                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("judul", currentClass)
                    putExtra(
                        "deskripsi",
                        "$description\n\nConfidence: ${String.format("%.1f", accuracy * 100)}%"
                    )
                    putExtra("gambar", R.drawable.icon_tomato_leaf)
                    putExtra("penjelasan", details)
                }

                startActivity(intent)

            } else {
                // Pesan dibedakan: "tidak yakin" vs "bukan kategori" vs kelas lain
                val message = when (currentClass) {
                    LOW_CONFIDENCE_LABEL ->
                        "Hasil belum yakin. Dekatkan kamera ke satu daun tomat."
                    LABEL_DISPLAY_NAMES[NON_CATEGORY_LABEL] ->
                        "Objek ini bukan daun tomat yang dikenali."
                    else ->
                        "Kelas tidak valid untuk detail."
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    // ============================================================
    // LOAD LABELS
    // ============================================================

    private fun loadLabels() {
        labels = try {
            assets.open(LABELS_FILE).bufferedReader().useLines { lines ->
                lines.map { it.trim() }.filter { it.isNotEmpty() }.toList()
            }
        } catch (e: Exception) {
            Log.w("TensorFlow", "labels.txt tidak ditemukan di assets, memakai daftar bawaan", e)
            DEFAULT_LABELS
        }
        Log.d("TensorFlow", "Labels: $labels")
    }


    // ============================================================
    // LOAD TFLITE MODEL
    // ============================================================

    private fun loadModel() {

        try {

            synchronized(modelLock) {

                if (model != null) return

                val mappedBuffer = assets.openFd(MODEL_FILE).use { fd ->
                    FileInputStream(fd.fileDescriptor).use { input ->
                        input.channel.map(
                            FileChannel.MapMode.READ_ONLY,
                            fd.startOffset,
                            fd.declaredLength
                        )
                    }
                }

                val interpreter = Interpreter(
                    mappedBuffer,
                    Interpreter.Options().apply { setNumThreads(4) }
                )

                // Ukuran input & jumlah kelas dibaca langsung dari model
                val inputTensor = interpreter.getInputTensor(0)
                val inShape = inputTensor.shape()          // [1, 300, 300, 3]
                val outShape = interpreter.getOutputTensor(0).shape()   // [1, jumlah_kelas]

                Log.d("TensorFlow", "Input shape: ${inShape.contentToString()}")
                Log.d("TensorFlow", "Input type: ${inputTensor.dataType()}")
                Log.d("TensorFlow", "Output shape: ${outShape.contentToString()}")

                require(inShape.size == 4 && inShape[3] == 3 && inShape[1] == inShape[2]) {
                    "Bentuk input model tidak sesuai: ${inShape.contentToString()}"
                }
                require(inputTensor.dataType() == DataType.FLOAT32) {
                    "Input model harus FLOAT32, bukan ${inputTensor.dataType()}"
                }
                require(outShape.size == 2 && outShape[0] == 1) {
                    "Bentuk output model tidak sesuai: ${outShape.contentToString()}"
                }

                inputSize = inShape[1]
                numClasses = outShape[1]

                // Peringatan jika yang terpasang ternyata model lama (Xception 299 / 5 kelas)
                if (inputSize != EXPECTED_INPUT_SIZE) {
                    Log.w(
                        "TensorFlow",
                        "Ukuran input model $inputSize != $EXPECTED_INPUT_SIZE. " +
                                "Pastikan yang terpasang model EfficientNetV2-B3 terbaru."
                    )
                }

                if (labels.size != numClasses) {
                    Log.w(
                        "TensorFlow",
                        "Jumlah label (${labels.size}) != jumlah output model ($numClasses)"
                    )
                    labels = List(numClasses) { labels.getOrNull(it) ?: "kelas_$it" }
                }

                inputBuffer = ByteBuffer
                    .allocateDirect(4 * inputSize * inputSize * 3)
                    .order(ByteOrder.nativeOrder())
                pixels = IntArray(inputSize * inputSize)
                outputBuffer = Array(1) { FloatArray(numClasses) }

                model = interpreter

                Log.d("TensorFlow", "Model berhasil dimuat")
            }

        } catch (e: Exception) {

            Log.e("TensorFlow", "Error saat memuat model", e)

            Toast.makeText(
                this,
                "Error saat memuat model: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    // ============================================================
    // START CAMERA
    // ============================================================

    private fun startCamera() {

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            try {

                cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                    }

                imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetRotation(binding.viewFinder.display.rotation)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            if (!isProcessingImage) {
                                isProcessingImage = true
                                processImage(imageProxy)
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                cameraProvider?.unbindAll()

                cameraProvider?.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )

            } catch (e: Exception) {

                Log.e("Camera", "Error saat memulai kamera", e)

                Toast.makeText(this, "Gagal memulai kamera", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }


    // ============================================================
    // PROCESS IMAGE
    // ============================================================

    private fun processImage(imageProxy: ImageProxy) {

        try {

            synchronized(modelLock) {

                val interpreter = model ?: return
                val input = inputBuffer ?: return

                // ImageProxy -> Bitmap -> input tensor
                val bitmap = imageProxy.toBitmap()
                try {
                    fillInputBuffer(bitmap, input, saveDebugFrame)
                    saveDebugFrame = false
                } finally {
                    bitmap.recycle()
                }

                interpreter.run(input, outputBuffer)
            }

            // Output model: probabilitas tiap kelas (softmax), tidak ada bounding box
            val probs = outputBuffer[0].copyOf()

            var maxIndex = 0
            for (i in probs.indices) {
                if (probs[i] > probs[maxIndex]) maxIndex = i
            }

            val realConfidence = probs[maxIndex].coerceIn(0f, 1f)
            val rawLabel = labels[maxIndex]

            // Threshold tetap memakai confidence ASLI model
            val label = if (realConfidence >= CONFIDENCE_THRESHOLD) {
                displayName(rawLabel)
            } else {
                LOW_CONFIDENCE_LABEL
            }

            detectedClass = label
            accuracy = realConfidence.toDouble()

            Log.d(
                "TensorFlow",
                "Class: $label | confidence=$realConfidence | " +
                        "probs=${probs.joinToString { "%.3f".format(it) }}"
            )

            runOnUiThread {
                updateDetectionUI(label, realConfidence.toDouble())
            }

        } catch (e: Exception) {

            Log.e("TensorFlow", "Error saat memproses gambar", e)

        } finally {

            imageProxy.close()
            isProcessingImage = false
        }
    }

    private fun fillInputBuffer(src: Bitmap, buffer: ByteBuffer, saveDebug: Boolean = false) {

        val side = minOf(src.width, src.height)

        val cropped = if (CENTER_CROP && src.width != src.height) {
            Bitmap.createBitmap(
                src,
                (src.width - side) / 2,
                (src.height - side) / 2,
                side,
                side
            )
        } else {
            src
        }

        val scaled = Bitmap.createScaledBitmap(cropped, inputSize, inputSize, true)

        scaled.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        buffer.rewind()
        for (p in pixels) {
            buffer.putFloat(((p shr 16) and 0xFF).toFloat())   // R (0..255)
            buffer.putFloat(((p shr 8) and 0xFF).toFloat())    // G (0..255)
            buffer.putFloat((p and 0xFF).toFloat())            // B (0..255)
        }
        buffer.rewind()

        if (saveDebug) saveDebugBitmap(scaled)
        if (scaled !== cropped) scaled.recycle()
        if (cropped !== src) cropped.recycle()
    }
    private fun saveDebugBitmap(bitmap: Bitmap) {
        try {
            val file = java.io.File(getExternalFilesDir(null), "debug_input.png")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            Log.d("SELFTEST", "Gambar input model disimpan: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("SELFTEST", "Gagal menyimpan gambar debug", e)
        }
    }

    /** Jalankan model pada gambar dataset di assets, lewat preprocessing yang sama dengan kamera. */
    private fun runAssetSelfTest() {

        if (DEBUG_ASSET_IMAGES.isEmpty()) return

        synchronized(modelLock) {

            val interpreter = model ?: return
            val input = inputBuffer ?: return

            for (name in DEBUG_ASSET_IMAGES) {
                try {
                    val bmp = assets.open(name).use { BitmapFactory.decodeStream(it) }
                    try {
                        fillInputBuffer(bmp, input)
                    } finally {
                        bmp.recycle()
                    }

                    interpreter.run(input, outputBuffer)

                    val probs = outputBuffer[0]
                    var best = 0
                    for (i in probs.indices) if (probs[i] > probs[best]) best = i

                    val conf = "%.3f".format(probs[best])
                    val all = probs.joinToString { "%.3f".format(it) }
                    Log.d("SELFTEST", "$name -> ${labels[best]} ($conf) | probs=[$all]")

                } catch (e: Exception) {
                    Log.e("SELFTEST", "Gagal menguji $name", e)
                }
            }
        }
    }


    // ============================================================
    // IMAGE PROXY -> BITMAP
    // ============================================================

    private fun ImageProxy.toBitmap(): Bitmap {

        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)

        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()
        out.close()

        val rotation = imageInfo.rotationDegrees

        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        return if (rotation != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotation.toFloat())
            val rotated = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )
            if (rotated !== bitmap) bitmap.recycle()
            rotated
        } else {
            bitmap
        }
    }
    // ============================================================
    // CLASS MAPPING
    // ============================================================

    private fun displayName(label: String): String {
        return LABEL_DISPLAY_NAMES[label]
            ?: label.replace('_', ' ').replaceFirstChar { it.uppercase() }
    }


    // ============================================================
    // UPDATE UI
    // ============================================================

    private fun updateDetectionUI(className: String, accuracy: Double) {

        binding.apply {

            detectionLabel.text = className

            accuracyLabel.text = "Confidence: ${String.format("%.1f", accuracy * 100)}%"
        }
    }


    // ============================================================
    // PERMISSION
    // ============================================================

    private fun allPermissionsGranted(): Boolean {

        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }


    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {

            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Izin kamera tidak diberikan.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // ============================================================
    // DESTROY
    // ============================================================

    override fun onDestroy() {

        super.onDestroy()

        try {

            cameraProvider?.unbindAll()

            imageAnalyzer?.clearAnalyzer()

            cameraExecutor.shutdown()

            synchronized(modelLock) {
                model?.close()
                model = null
            }

        } catch (e: Exception) {

            Log.e("Camera", "Error saat membersihkan", e)
        }
    }


    // ============================================================
    // CONSTANT
    // ============================================================

    companion object {

        private const val REQUEST_CODE_PERMISSIONS = 10

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val MODEL_FILE = "tomat_effnet_raw.tflite"
        private const val LABELS_FILE = "labels.txt"
        private const val EXPECTED_INPUT_SIZE = 300
        private const val CONFIDENCE_THRESHOLD = 0.1f
        private const val LOW_CONFIDENCE_LABEL = "Tidak yakin"
        private const val NON_CATEGORY_LABEL = "bukan_kategori"
        private const val CENTER_CROP = false
        private val DEBUG_ASSET_IMAGES = listOf<String>()
        private const val SAVE_DEBUG_FRAME = true
        private val DEFAULT_LABELS = listOf(
            "bukan_kategori",
            "early_blight",
            "healthy",
            "late_blight",
            "leaf_mold",
            "septoria_leaf_spot"
        )

        private val LABEL_DISPLAY_NAMES = mapOf(
            "early_blight" to "Bercak Coklat - Early Blight",
            "healthy" to "Daun Tomat Sehat",
            "late_blight" to "Busuk Kering - Leaf Mold",
            "leaf_mold" to "Busuk Basah - Late Blight",
            "septoria_leaf_spot" to "Bercak Coklat - Septoria",
            "bukan_kategori" to "Bukan Kategori"
        )
    }
}