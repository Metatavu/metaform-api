package fi.metatavu.metaform.server.pdf

import org.w3c.dom.Element
import org.xhtmlrenderer.extend.ReplacedElement
import org.xhtmlrenderer.extend.ReplacedElementFactory
import org.xhtmlrenderer.extend.UserAgentCallback
import org.xhtmlrenderer.layout.LayoutContext
import org.xhtmlrenderer.render.BlockBox
import org.xhtmlrenderer.simple.extend.FormSubmissionListener

class ChainingReplacedElementFactory : ReplacedElementFactory {
    private val replacedElementFactories: MutableList<ReplacedElementFactory> = mutableListOf()
    fun addReplacedElementFactory(replacedElementFactory: ReplacedElementFactory) {
        replacedElementFactories.add(0, replacedElementFactory)
    }

    override fun createReplacedElement(c: LayoutContext, box: BlockBox, uac: UserAgentCallback, cssWidth: Int,
                                       cssHeight: Int): ReplacedElement? {
        return replacedElementFactories.firstNotNullOfOrNull { replacedElementFactory ->
            replacedElementFactory.createReplacedElement(c, box, uac, cssWidth, cssHeight)
        }
    }

    override fun reset() {
        for (replacedElementFactory in replacedElementFactories) {
            replacedElementFactory.reset()
        }
    }

    override fun remove(e: Element) {
        for (replacedElementFactory in replacedElementFactories) {
            replacedElementFactory.remove(e)
        }
    }

    override fun setFormSubmissionListener(listener: FormSubmissionListener) {
        for (replacedElementFactory in replacedElementFactories) {
            replacedElementFactory.setFormSubmissionListener(listener)
        }
    }
}