package fi.metatavu.metaform.server.rest

enum class ReplyMode {

    /**
     * Updates existing reply if one exists, otherwise create new
     */
    UPDATE,

    /**
     * Revision existing reply and create new one
     */
    REVISION,

    /**
     * Always create new reply
     */
    CUMULATIVE
}