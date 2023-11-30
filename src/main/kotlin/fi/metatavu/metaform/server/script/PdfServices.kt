package fi.metatavu.metaform.server.script

import fi.metatavu.metaform.server.controllers.ReplyController
import fi.metatavu.metaform.server.exceptions.PdfRenderException
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class PdfServices {

    @Inject
    lateinit var formRuntimeContext: FormRuntimeContext

    @Inject
    lateinit var replyController: ReplyController

    /**
     * Returns reply as PDF
     *
     * @return PDF data
     * @throws PdfRenderException thrown when rendering fails
     */
    @Throws(PdfRenderException::class)
    fun getReplyPdf(): ByteArray? {
        return replyController.getReplyPdf(
            formRuntimeContext.exportThemeName,
            formRuntimeContext.metaform,
            formRuntimeContext.reply,
            formRuntimeContext.attachmentMap,
            formRuntimeContext.locale
        )
    }
}