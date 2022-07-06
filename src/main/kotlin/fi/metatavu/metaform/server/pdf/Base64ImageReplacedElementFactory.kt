package fi.metatavu.metaform.server.pdf

import com.itextpdf.text.BadElementException
import com.itextpdf.text.Image
import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.StringUtils
import org.w3c.dom.Element
import org.xhtmlrenderer.extend.FSImage
import org.xhtmlrenderer.extend.ReplacedElement
import org.xhtmlrenderer.extend.ReplacedElementFactory
import org.xhtmlrenderer.extend.UserAgentCallback
import org.xhtmlrenderer.layout.LayoutContext
import org.xhtmlrenderer.pdf.ITextFSImage
import org.xhtmlrenderer.pdf.ITextImageElement
import org.xhtmlrenderer.render.BlockBox
import org.xhtmlrenderer.simple.extend.FormSubmissionListener
import java.io.IOException

class Base64ImageReplacedElementFactory : ReplacedElementFactory {
    /**
     * Creates a replacement image
     *
     * @param c layout context
     * @param box block box
     * @param uac user agent callback
     * @param cssWidth css width
     * @param cssHeight css height
     * @return replaced image
     */
    override fun createReplacedElement(c: LayoutContext?, box: BlockBox, uac: UserAgentCallback, cssWidth: Int, cssHeight: Int): ReplacedElement? {
        val element = box.element ?: return null
        val nodeName = element.nodeName
        if (nodeName == "img") {
            try {
                val attribute = element.getAttribute("src")
                val fsImage = buildImage(attribute, uac)
                if (fsImage != null) {
                    if (cssWidth != -1 || cssHeight != -1) {
                        fsImage.scale(cssWidth, cssHeight)
                    }
                    return ITextImageElement(fsImage)
                }
            } catch (e: BadElementException) {
                return null
            } catch (e: IOException) {
                return null
            }
        }
        return null
    }

    @Throws(IOException::class, BadElementException::class)
    protected fun buildImage(srcAttr: String, uac: UserAgentCallback): FSImage? {
        return if (srcAttr.startsWith("data:image/")) {
            val b64encoded = StringUtils.substringAfter(srcAttr, "base64,")
            ITextFSImage(Image.getInstance(Base64.decodeBase64(b64encoded)))
        } else {
            uac.getImageResource(srcAttr).image
        }
    }

    override fun remove(e: Element?) {
        // Do nothing
    }

    override fun reset() {
        // Do nothing
    }

    override fun setFormSubmissionListener(listener: FormSubmissionListener?) {
        // Do nothing
    }

}