package com.rrajath.grove.ui.attachments

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rrajath.grove.ui.components.GroveTopBar
import com.rrajath.grove.ui.theme.PlexSans
import com.rrajath.grove.ui.theme.grove
import com.rrajath.grove.vault.Vault

/**
 * Full-screen viewer for an image attachment in the vault ([path] is the
 * vault-relative path a `[[file:…]]` image link resolved to). The bytes are
 * decoded from the vault on entry; a tap (or the back arrow) closes.
 */
@Composable
fun ImageViewerScreen(
    path: String,
    vault: Vault?,
    onBack: () -> Unit,
) {
    val c = MaterialTheme.grove
    var bitmap by remember(path) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var failed by remember(path) { mutableStateOf(false) }

    LaunchedEffect(path, vault) {
        if (vault == null) {
            failed = true
            return@LaunchedEffect
        }
        val bytes = runCatching { vault.readAttachment(path) }.getOrNull()
        bitmap = bytes?.let { b ->
            runCatching { BitmapFactory.decodeByteArray(b, 0, b.size) }.getOrNull()
        }
        if (bitmap == null) failed = true
    }

    Scaffold(
        containerColor = c.bg,
        topBar = {
            GroveTopBar(
                leading = {
                    androidx.compose.material3.TextButton(onClick = onBack) {
                        Text("← Back", fontFamily = PlexSans, color = c.accent)
                    }
                },
                title = {
                    Text(
                        path.substringAfterLast('/'),
                        fontFamily = PlexSans, fontSize = 15.sp, color = c.ink,
                    )
                },
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(c.bg)
                .pointerInput(onBack) { detectTapGestures(onTap = { onBack() }) }
                .testTag("image_viewer_screen"),
            contentAlignment = Alignment.Center,
        ) {
            when {
                bitmap != null -> Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = path.substringAfterLast('/'),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
                failed -> Text(
                    "Couldn't load image",
                    fontFamily = PlexSans, color = c.ink2,
                )
                else -> CircularProgressIndicator(
                    modifier = Modifier.size(32.dp), color = c.accent,
                )
            }
        }
    }
}
