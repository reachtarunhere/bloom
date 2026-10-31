package com.rrajath.grove.org

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AttachmentStoreTest {

    @Test
    fun `image extensions are images`() {
        assertTrue(AttachmentStore.isImagePath("attachments/a.png"))
        assertTrue(AttachmentStore.isImagePath("attachments/a.JPG"))
        assertTrue(AttachmentStore.isImagePath("x.webp"))
        assertFalse(AttachmentStore.isImagePath("attachments/doc.pdf"))
        assertFalse(AttachmentStore.isImagePath("notes.org"))
        assertFalse(AttachmentStore.isImagePath("noext"))
    }

    @Test
    fun `unique path keeps the name when free`() {
        assertEquals(
            "attachments/IMG_1.jpg",
            AttachmentStore.uniqueAttachmentPath("IMG_1.jpg") { false },
        )
    }

    @Test
    fun `collisions get a numeric suffix before the extension`() {
        val taken = setOf("attachments/IMG_1.jpg", "attachments/IMG_1-1.jpg")
        assertEquals(
            "attachments/IMG_1-2.jpg",
            AttachmentStore.uniqueAttachmentPath("IMG_1.jpg") { it in taken },
        )
    }

    @Test
    fun `comparison of taken paths is the caller's job; names keep their case`() {
        // uniqueAttachmentPath itself is case-sensitive; Vault passes a
        // case-insensitive existence check so SAF FAT/exFAT providers dedupe
        // the way the filesystem does.
        assertEquals(
            "attachments/Photo.PNG",
            AttachmentStore.uniqueAttachmentPath("Photo.PNG") { it.equals("attachments/photo.png", ignoreCase = true) == false },
        )
    }

    @Test
    fun `link text from a root-level note is vault-relative`() {
        assertEquals(
            "[[file:attachments/a.png]]",
            AttachmentStore.linkText("journal.org", "attachments/a.png"),
        )
    }

    @Test
    fun `link text from a nested note climbs out with dotdot`() {
        assertEquals(
            "[[file:../attachments/a.png]]",
            AttachmentStore.linkText("projects/acme.org", "attachments/a.png"),
        )
        assertEquals(
            "[[file:../../attachments/a.png][scan]]",
            AttachmentStore.linkText("a/b/c.org", "attachments/a.png", "scan"),
        )
    }

    @Test
    fun `link text when the attachment sits next to the note`() {
        assertEquals(
            "[[file:img.png]]",
            AttachmentStore.linkText("pics/note.org", "pics/img.png"),
        )
    }
}
