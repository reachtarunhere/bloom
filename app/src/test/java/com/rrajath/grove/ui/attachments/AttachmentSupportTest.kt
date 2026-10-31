package com.rrajath.grove.ui.attachments

import com.rrajath.grove.org.TextEdit
import com.rrajath.grove.ui.attachments.AttachmentSupport.ParagraphPart
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

    @Test
    fun `consecutive image lines each render as an image`() {
        assertEquals(
            listOf(ParagraphPart.Image("attachments/a.png"), ParagraphPart.Image("attachments/b.jpg")),
            AttachmentSupport.splitParagraph(
                listOf("[[file:attachments/a.png]]", "[[file:attachments/b.jpg]]"),
                "journal.org",
            ),
        )
    }

    @Test
    fun `image lines split surrounding text into their own runs`() {
        assertEquals(
            listOf(
                ParagraphPart.Text(listOf("before", "still before")),
                ParagraphPart.Image("attachments/a.png"),
                ParagraphPart.Text(listOf("see [[file:attachments/b.png]] inline")),
            ),
            AttachmentSupport.splitParagraph(
                listOf("before", "still before", "[[file:attachments/a.png]]", "see [[file:attachments/b.png]] inline"),
                "journal.org",
            ),
        )
    }

    @Test
    fun `plain paragraph stays a single text run`() {
        assertEquals(
            listOf(ParagraphPart.Text(listOf("one", "two"))),
            AttachmentSupport.splitParagraph(listOf("one", "two"), "journal.org"),
        )
    }

    @Test
    fun `image link inserts at the cursor on its own line`() {
        assertEquals(
            TextEdit("* H\nab\n[[file:x.png]]\ncd\n", 22),
            AttachmentSupport.insertImageLink("* H\nab\ncd\n", 7, "[[file:x.png]]"),
        )
    }

    @Test
    fun `image link mid-line breaks the line before it`() {
        assertEquals(
            TextEdit("abc\n[[file:x.png]]\n", 19),
            AttachmentSupport.insertImageLink("abc", 3, "[[file:x.png]]"),
        )
    }

    @Test
    fun `cursor past the end is clamped`() {
        assertEquals(
            TextEdit("[[file:x.png]]\n", 15),
            AttachmentSupport.insertImageLink("", 5, "[[file:x.png]]"),
        )
    }
}
