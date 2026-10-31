package com.rrajath.grove.ui.attachments

import com.rrajath.grove.org.AttachmentStore
import com.rrajath.grove.org.InlineTokenizer
import com.rrajath.grove.org.InlineType
import com.rrajath.grove.org.OrgLinkParser
import com.rrajath.grove.org.OrgLinkTarget
import com.rrajath.grove.org.TextEdit

/**
 * Pure helpers for attachment links in read mode: deciding whether a line (or
 * a whole paragraph) is just an image link that should render inline, and
 * resolving a tapped link to a viewer route. Kept Android-free so they're
 * JVM-unit-testable; the screens supply the vault lookups.
 */
object AttachmentSupport {

    /**
     * The single image link a body line consists of, when the line is exactly
     * one `[[file:…image…]]` link (optionally with a description) and nothing
     * else — the shape the editor's image button writes on its own line.
     * Returns the link's vault-relative path, or null for any other line.
     */
    fun standaloneImageLink(line: String, currentFile: String): String? {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return null
        val tokens = InlineTokenizer.tokenize(trimmed)
        if (tokens.size != 1 || tokens[0].type != InlineType.LINK) return null
        val target = tokens[0].target ?: return null
        return when (val parsed = OrgLinkParser.parse(target, currentFile)) {
            is OrgLinkTarget.Attachment -> parsed.takeIf { it.isImage }?.path
            else -> null
        }
    }

    /**
     * Whether a link target (the string between `[[ ]]`) points at a vault file
     * that is an image — i.e. tapping it should open the in-app image viewer
     * rather than navigate to a notebook. [currentFile] resolves relative
     * `file:` paths the same way [OrgLinkParser] does.
     */
    fun imageLinkPath(rawTarget: String, currentFile: String): String? =
        when (val parsed = OrgLinkParser.parse(rawTarget, currentFile)) {
            is OrgLinkTarget.Attachment -> parsed.takeIf { it.isImage }?.path
            else -> null
        }

    /** The org link text for an attachment already stored at [vaultPath], from a note in [fromFile]. */
    fun linkFor(fromFile: String, vaultPath: String): String =
        AttachmentStore.linkText(fromFile, vaultPath)

    sealed interface ParagraphPart {
        data class Text(val lines: List<String>) : ParagraphPart
        data class Image(val path: String) : ParagraphPart
    }

    fun splitParagraph(lines: List<String>, currentFile: String): List<ParagraphPart> {
        val parts = mutableListOf<ParagraphPart>()
        val text = mutableListOf<String>()
        fun flush() {
            if (text.isNotEmpty()) parts += ParagraphPart.Text(text.toList())
            text.clear()
        }
        lines.forEach { line ->
            val image = standaloneImageLink(line, currentFile)
            if (image != null) {
                flush()
                parts += ParagraphPart.Image(image)
            } else {
                text += line
            }
        }
        flush()
        return parts
    }

    fun insertImageLink(text: String, cursor: Int, link: String): TextEdit {
        val at = cursor.coerceIn(0, text.length)
        val prefix = if (at > 0 && text[at - 1] != '\n') "\n" else ""
        val snippet = "$prefix$link\n"
        return TextEdit(text.substring(0, at) + snippet + text.substring(at), at + snippet.length)
    }
}
