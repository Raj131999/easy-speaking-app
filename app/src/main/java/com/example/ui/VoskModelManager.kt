package com.example.ui

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.vosk.Model
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

private const val UNIFIED_MODEL_FOLDER = "vosk-model-en-us-0.22-lgraph"
private const val UNIFIED_MODEL_URL = "https://alphacephei.com/vosk/models/vosk-model-en-us-0.22-lgraph.zip"
private const val UNIFIED_MODEL_APPROX_BYTES = 134_000_000L

enum class SpeechModelOption(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val folderName: String,
    val downloadUrl: String,
    val approxBytes: Long
) {
    INDIA(
        id = "en-in",
        displayName = "Indian English",
        subtitle = "Default • Tuned for Indian English pronunciation",
        folderName = UNIFIED_MODEL_FOLDER,
        downloadUrl = UNIFIED_MODEL_URL,
        approxBytes = UNIFIED_MODEL_APPROX_BYTES
    ),
    US(
        id = "en-us",
        displayName = "US English",
        subtitle = "Tuned for General American English",
        folderName = UNIFIED_MODEL_FOLDER,
        downloadUrl = UNIFIED_MODEL_URL,
        approxBytes = UNIFIED_MODEL_APPROX_BYTES
    );

    companion object {
        fun fromId(id: String?): SpeechModelOption {
            return entries.firstOrNull { it.id == id } ?: INDIA
        }
    }
}

sealed interface ModelDownloadState {
    data object Checking : ModelDownloadState
    data class Downloading(
        val option: SpeechModelOption,
        val progress: Float,
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : ModelDownloadState
    data class Extracting(
        val option: SpeechModelOption,
        val progress: Float
    ) : ModelDownloadState
    data class Loading(val option: SpeechModelOption) : ModelDownloadState
    data class Ready(val option: SpeechModelOption) : ModelDownloadState
    data class Error(
        val option: SpeechModelOption,
        val message: String
    ) : ModelDownloadState
}

class VoskModelManager(private val context: Context) {

    companion object {
        private const val TAG = "VoskModelManager"
        private const val PREFS_NAME = "easy_speaking_prefs"
        private const val KEY_SELECTED_MODEL = "selected_speech_model_id"
        private const val KEY_GRAMMAR_MODE = "use_grammar_guided_mode"
        private const val READY_MARKER = ".model_ready"
        private val LEGACY_MODEL_FOLDERS = listOf(
            "vosk-model-small-en-in-0.4",
            "vosk-model-small-en-us-0.15"
        )
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _selectedOption = MutableStateFlow(
        SpeechModelOption.fromId(prefs.getString(KEY_SELECTED_MODEL, SpeechModelOption.INDIA.id))
    )
    val selectedOption: StateFlow<SpeechModelOption> = _selectedOption.asStateFlow()

    private val _useGrammarMode = MutableStateFlow(
        prefs.getBoolean(KEY_GRAMMAR_MODE, true)
    )
    val useGrammarMode: StateFlow<Boolean> = _useGrammarMode.asStateFlow()

    private val _installedModelIds = MutableStateFlow<Set<String>>(emptySet())
    val installedModelIds: StateFlow<Set<String>> = _installedModelIds.asStateFlow()

    private val _state = MutableStateFlow<ModelDownloadState>(ModelDownloadState.Checking)
    val state: StateFlow<ModelDownloadState> = _state.asStateFlow()

    @Volatile
    private var loadedModel: Model? = null
    @Volatile
    private var loadedOption: SpeechModelOption? = null

    private val mutex = Mutex()
    private var activeJob: Job? = null

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    init {
        refreshInstalledModels()
    }

    private fun getModelDir(option: SpeechModelOption): File =
        File(context.filesDir, option.folderName)

    fun isModelInstalled(option: SpeechModelOption): Boolean {
        val dir = getModelDir(option)
        val marker = File(dir, READY_MARKER)
        val conf = File(dir, "conf/model.conf")
        return dir.exists() && dir.isDirectory && marker.exists() && conf.exists()
    }

    private fun refreshInstalledModels() {
        _installedModelIds.value = SpeechModelOption.entries
            .filter { isModelInstalled(it) }
            .map { it.id }
            .toSet()
    }

    fun getLoadedModel(): Model? = loadedModel

    fun setGrammarModeEnabled(enabled: Boolean) {
        _useGrammarMode.value = enabled
        prefs.edit().putBoolean(KEY_GRAMMAR_MODE, enabled).apply()
    }

    fun ensureModelReady(scope: CoroutineScope) {
        val target = _selectedOption.value
        if (loadedModel != null && loadedOption == target && _state.value is ModelDownloadState.Ready) return
        if (activeJob?.isActive == true) return

        activeJob = scope.launch(Dispatchers.IO) {
            mutex.withLock {
                prepareOptionLocked(target)
            }
        }
    }

    fun selectModel(option: SpeechModelOption, scope: CoroutineScope) {
        if (_selectedOption.value == option && loadedModel != null && loadedOption == option && _state.value is ModelDownloadState.Ready) {
            return
        }
        _selectedOption.value = option
        prefs.edit().putString(KEY_SELECTED_MODEL, option.id).apply()

        if (loadedModel != null && loadedOption?.folderName == option.folderName) {
            loadedOption = option
            _state.value = ModelDownloadState.Ready(option)
            return
        }

        activeJob?.cancel()
        activeJob = scope.launch(Dispatchers.IO) {
            mutex.withLock {
                if (loadedModel != null && loadedOption?.folderName == option.folderName) {
                    loadedOption = option
                    _state.value = ModelDownloadState.Ready(option)
                    return@withLock
                }
                if (loadedOption?.folderName != option.folderName) {
                    try {
                        loadedModel?.close()
                    } catch (_: Exception) {}
                    loadedModel = null
                    loadedOption = null
                }
                prepareOptionLocked(option)
            }
        }
    }

    fun redownloadSelectedModel(scope: CoroutineScope) {
        val target = _selectedOption.value
        activeJob?.cancel()
        activeJob = scope.launch(Dispatchers.IO) {
            mutex.withLock {
                try {
                    loadedModel?.close()
                } catch (_: Exception) {}
                loadedModel = null
                loadedOption = null
                getModelDir(target).deleteRecursively()
                refreshInstalledModels()
                prepareOptionLocked(target)
            }
        }
    }

    private fun cleanupLegacyModels() {
        LEGACY_MODEL_FOLDERS.forEach { legacyFolder ->
            runCatching {
                File(context.filesDir, legacyFolder).takeIf { it.exists() }?.deleteRecursively()
                File(context.filesDir, "${legacyFolder}_tmp").takeIf { it.exists() }?.deleteRecursively()
                File(context.cacheDir, "$legacyFolder.zip.part").takeIf { it.exists() }?.delete()
            }
        }
    }

    private fun sanitizeUserErrorMessage(raw: String?): String {
        val fallback = "Could not complete English voice setup. Tap Retry."
        val msg = raw?.trim().orEmpty()
        if (msg.isBlank()) return fallback
        val lower = msg.lowercase()
        return if (
            lower.contains("vosk") ||
            lower.contains("lgraph") ||
            lower.contains("0.22") ||
            lower.contains("alphacephei") ||
            lower.contains("http://") ||
            lower.contains("https://")
        ) {
            fallback
        } else {
            msg
        }
    }

    private suspend fun prepareOptionLocked(option: SpeechModelOption) {
        try {
            _state.value = ModelDownloadState.Checking
            cleanupLegacyModels()
            if (!isModelInstalled(option)) {
                downloadAndExtractModel(option)
                refreshInstalledModels()
            }
            loadModelIntoMemory(option)
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: UnknownHostException) {
            Log.e(TAG, "No internet connection preparing voice pack", e)
            _state.value = ModelDownloadState.Error(
                option = option,
                message = "No internet connection. Connect to Wi-Fi or mobile data and tap Retry to finish voice setup."
            )
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout preparing voice pack", e)
            _state.value = ModelDownloadState.Error(
                option = option,
                message = "Setup timed out. Check your connection and tap Retry."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed preparing voice pack", e)
            _state.value = ModelDownloadState.Error(
                option = option,
                message = sanitizeUserErrorMessage(e.message)
            )
        }
    }

    private fun downloadAndExtractModel(option: SpeechModelOption) {
        val tempZip = File(context.cacheDir, "${option.folderName}.zip.part")
        val stagingDir = File(context.filesDir, "${option.folderName}_tmp")
        val targetDir = getModelDir(option)

        if (tempZip.exists()) tempZip.delete()
        if (stagingDir.exists()) stagingDir.deleteRecursively()

        _state.value = ModelDownloadState.Downloading(option, 0f, 0L, option.approxBytes)

        val request = Request.Builder()
            .url(option.downloadUrl)
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Server returned HTTP ${response.code} during voice setup.")
            }
            val body = response.body ?: throw IOException("Unable to download voice pack. Please try again.")
            val contentLength = body.contentLength().takeIf { it > 0 } ?: option.approxBytes

            var downloaded = 0L
            var lastReportedPercent = -1

            body.byteStream().use { input ->
                FileOutputStream(tempZip).use { output ->
                    val buffer = ByteArray(32 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        downloaded += read

                        val fraction = (downloaded.toFloat() / contentLength.toFloat()).coerceIn(0f, 0.99f)
                        val percent = (fraction * 100).toInt()
                        if (percent != lastReportedPercent) {
                            lastReportedPercent = percent
                            _state.value = ModelDownloadState.Downloading(
                                option = option,
                                progress = fraction,
                                downloadedBytes = downloaded,
                                totalBytes = maxOf(contentLength, downloaded)
                            )
                        }
                    }
                    output.flush()
                }
            }
        }

        _state.value = ModelDownloadState.Extracting(option, 0f)
        extractZipSafely(option, tempZip, stagingDir)

        if (targetDir.exists()) {
            targetDir.deleteRecursively()
        }
        if (!stagingDir.renameTo(targetDir)) {
            stagingDir.copyRecursively(targetDir, overwrite = true)
            stagingDir.deleteRecursively()
        }

        File(targetDir, READY_MARKER).writeText("ok")
        if (tempZip.exists()) tempZip.delete()

        if (!isModelInstalled(option)) {
            throw IOException("Voice package verification failed for ${option.displayName}.")
        }
    }

    private fun extractZipSafely(option: SpeechModelOption, zipFile: File, destDir: File) {
        if (!destDir.exists()) destDir.mkdirs()
        val destCanonical = destDir.canonicalPath + File.separator

        val totalZipBytes = zipFile.length().coerceAtLeast(1L)
        var approxProcessed = 0L
        var lastPercent = -1

        ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
            val buffer = ByteArray(16 * 1024)
            var entry = zis.nextEntry
            while (entry != null) {
                val rawName = entry.name.replace('\\', '/')
                val relativeName = when {
                    rawName == "${option.folderName}/" || rawName == option.folderName -> ""
                    rawName.startsWith("${option.folderName}/") -> rawName.removePrefix("${option.folderName}/")
                    else -> rawName
                }

                if (relativeName.isNotEmpty()) {
                    val outFile = File(destDir, relativeName)
                    if (!outFile.canonicalPath.startsWith(destCanonical)) {
                        throw SecurityException("Invalid archive entry path: ${entry.name}")
                    }

                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { fos ->
                            while (true) {
                                val count = zis.read(buffer)
                                if (count == -1) break
                                fos.write(buffer, 0, count)
                            }
                            fos.flush()
                        }
                    }
                }

                val compressedSize = entry.compressedSize
                if (compressedSize > 0) {
                    approxProcessed += compressedSize
                    val fraction = (approxProcessed.toFloat() / totalZipBytes.toFloat()).coerceIn(0f, 0.99f)
                    val pct = (fraction * 100).toInt()
                    if (pct != lastPercent) {
                        lastPercent = pct
                        _state.value = ModelDownloadState.Extracting(option, fraction)
                    }
                }

                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        _state.value = ModelDownloadState.Extracting(option, 1f)
    }

    private suspend fun loadModelIntoMemory(option: SpeechModelOption) = withContext(Dispatchers.IO) {
        if (loadedModel != null && loadedOption == option) {
            _state.value = ModelDownloadState.Ready(option)
            return@withContext
        }
        _state.value = ModelDownloadState.Loading(option)
        val modelPath = getModelDir(option).absolutePath
        Log.d(TAG, "Loading Vosk model (${option.id}) from $modelPath")
        loadedModel = Model(modelPath)
        loadedOption = option
        _state.value = ModelDownloadState.Ready(option)
        Log.d(TAG, "Vosk model (${option.id}) ready.")
    }

    fun release() {
        activeJob?.cancel()
        try {
            loadedModel?.close()
        } catch (_: Exception) {}
        loadedModel = null
        loadedOption = null
    }
}
