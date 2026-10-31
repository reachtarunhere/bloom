package com.rrajath.grove.ui.attachments

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AttachmentSupportTest {

    @Test
    fun `a lone image file link is a standalone image`() {
        assertEquals(
            "attachments/a.png",
            AttachmentSupport.standaloneImageLink("[[file:attachments/a.png]]", "journal.org"),
        )
        assertEquals(
            "attachments/a.png",
            AttachmentSupport.standaloneImageLink("  [[attachments/a.png]]  ", "journal.org"),
        )
    }

    @Test
    fun `a link with a description still counts`() {
        assertEquals(
            "attachments/a.jpg",
            AttachmentSupport.standaloneImageLink("[[file:attachments/a.jpg][scan of receipt]]", "journal.org"),
        )
    }

    @Test
    fun `relative image link resolves against the note's directory`() {
        assertEquals(
            "attachments/a.png",
            AttachmentSupport.standaloneImageLink("[[file:../attachments/a.png]]", "projects/acme.org"),
        )
    }

    @Test
    fun `prose around the link disqualifies inline rendering`() {
        assertNull(AttachmentSupport.standaloneImageLink("see [[file:attachments/a.png]] here", "journal.org"))
    }

    @Test
    fun `a non-image attachment link is not an inline image`() {
        assertNull(AttachmentSupport.standaloneImageLink("[[file:attachments/doc.pdf]]", "journal.org"))
    }

    @Test
    fun `an org file link is not an inline image`() {
        assertNull(AttachmentSupport.standaloneImageLink("[[file:other.org]]", "journal.org"))
    }

    @Test
    fun `image link path resolves for taps`() {
        assertEquals(
            "attachments/a.png",
            AttachmentSupport.imageLinkPath("file:attachments/a.png", "journal.org"),
        )
        assertNull(AttachmentSupport.imageLinkPath("file:notes.org", "journal.org"))
        assertNull(AttachmentSupport.imageLinkPath("https://example.com/x.png", "journal.org"))
    }
}
