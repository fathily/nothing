package com.israadev.nuxlauncher.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.collection.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.israadev.nuxlauncher.ui.theme.NuxColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

private val imageCache = LruCache<String, ImageBitmap>(20)

private val imageHttpClient = OkHttpClient.Builder()
    .dns(com.israadev.nuxlauncher.core.network.NuxDns)
    .connectTimeout(5, TimeUnit.SECONDS)
    .readTimeout(8, TimeUnit.SECONDS)
    .build()

private fun decodeSmallBitmap(bytes: ByteArray, maxDimension: Int = 256): ImageBitmap? {
    if (bytes.isEmpty()) return null

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sample = 1
    while (bounds.outWidth / sample > maxDimension || bounds.outHeight / sample > maxDimension) {
        sample *= 2
    }

    val options = BitmapFactory.Options().apply {
        inSampleSize = sample
        inPreferredConfig = Bitmap.Config.RGB_565
    }

    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)?.asImageBitmap()
}

/**
 * Lightweight network/local image loader with a small LRU cache.
 * Large profile images are downsampled before entering the Compose image tree.
 */
@Composable
fun NuxNetworkImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    fallbackInitials: String = "N",
    shape: Shape = RoundedCornerShape(12.dp),
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    var imageBitmap by remember(model) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(model) { mutableStateOf(false) }

    LaunchedEffect(model) {
        if (model == null) {
            imageBitmap = null
            return@LaunchedEffect
        }

        val cacheKey = model.toString()
        if (cacheKey.isBlank()) {
            imageBitmap = null
            return@LaunchedEffect
        }

        imageCache.get(cacheKey)?.let {
            imageBitmap = it
            return@LaunchedEffect
        }

        isLoading = true

        val loadedBitmap = withContext(Dispatchers.IO) {
            try {
                val bytes = when (model) {
                    is Uri -> context.contentResolver.openInputStream(model)?.use { it.readBytes() }
                    is String -> {
                        when {
                            model.startsWith("http://") || model.startsWith("https://") -> {
                                val request = Request.Builder().url(model).get().build()
                                imageHttpClient.newCall(request).execute().use { response ->
                                    if (response.isSuccessful) response.body?.bytes() else null
                                }
                            }
                            model.startsWith("content://") || model.startsWith("file://") -> {
                                context.contentResolver.openInputStream(Uri.parse(model))?.use { it.readBytes() }
                            }
                            else -> null
                        }
                    }
                    else -> null
                }

                bytes?.let { decodeSmallBitmap(it) }
            } catch (_: Exception) {
                null
            }
        }

        if (loadedBitmap != null) {
            imageCache.put(cacheKey, loadedBitmap)
            imageBitmap = loadedBitmap
        }
        isLoading = false
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(NuxColors.SurfaceWhite),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = imageBitmap
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NuxColors.LightGray.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = NuxColors.ForestGreen,
                    strokeWidth = 2.dp
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NuxColors.SoftLime),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = fallbackInitials.take(2).uppercase(),
                    color = NuxColors.ForestGreen,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
