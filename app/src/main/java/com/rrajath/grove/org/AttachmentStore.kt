package com.rrajath.grove.org

/**
 * Where Bloom copies attachments inside the vault, so they live next to the
 * notes (and sync with them) instead of in app-private storage. Emacs
 * org-attach defaults to a per-heading `data/` directory; Bloom keeps one
 * flat `attachments/` folder at the vault root, which is what a `file:` link
 * with a relative path resolves cleanly against.
 */
object AttachmentStore {
    const val DIR = "attachments"

    /** File extensions Bloom renders inline as images in read mode. */
    private val IMAGE_EXTENSIONS = setOf("png", "jpg", "jpeg", "gif", "webp", "bmp", "heic", "heif")

    fun isImagePath(path: String): Boolean =
        path.substringAfterLast('.', "").lowercase() in IMAGE_EXTENSIONS

    /**
     * A vault-relative path for an attachment copied in from [displayName]
     * (e.g. `IMG_2041.jpg`). Collisions get a `-1`, `-2`, … suffix before the
     * extension, compared case-insensitively the way SAF providers treat
     * names on FAT/exFAT. Pure: [taken] is the caller's existence check.
     */
    fun uniqueAttachmentPath(displayName: String, taken: (String) -> Boolean): String {
        val leaf = displayName.substringAfterLast('/').ifBlank { "attachment" }
        val stem = leaf.substringBeforeLast('.')
        val ext = leaf.substringAfterLast('.', "")
        val suffix = if (ext.isNotEmpty() && ext != leaf) ".$ext" else ""
        var candidate = "$DIR/$leaf"
        var n = 1
        while (taken(candidate)) {
            candidate = "$DIR/$stem-$n$suffix"
            n++
        }
        return candidate
    }

    /**
     * The link target to splice into a note for an attachment stored at
     * [vaultPath], written relative to the linking file's directory the way
     * Emacs writes `[[file:…]]` links. A description, when given, becomes the
     * link's label.
     */
    fun linkText(fromFile: String, vaultPath: String, description: String? = null): String {
        val rel = relativePathFromNoteDir(fromFile, vaultPath)
        return if (description.isNullOrEmpty()) "[[file:$rel]]" else "[[file:$rel][$description]]"
    }

    /**
     * [vaultPath] expressed relative to the directory of the linking [fromFile]
     * (`projects/notes.org` → `../attachments/x.png`; a root file →
     * `attachments/x.png`), so the `file:` link survives the note being moved
     * within the vault the same way Emacs-relative links do.
     */
    fun relativePathFromNoteDir(fromFile: String, vaultPath: String): String {
        val fromDir = fromFile.substringBeforeLast('/', "").split('/').filter { it.isNotEmpty() }
        val toSegs = vaultPath.split('/').filter { it.isNotEmpty() }
        val toDir = toSegs.dropLast(1)
        val toName = toSegs.lastOrNull() ?: return vaultPath
        var shared = 0
        while (shared < fromDir.size && shared < toDir.size && fromDir[shared] == toDir[shared]) shared++
        val ups = List(fromDir.size - shared) { ".." }
        return (ups + toDir.drop(shared) + toName).joinToString("/")
    }
}
