package fi.metatavu.metaform.server.xlsx

import fi.metatavu.metaform.api.spec.model.MetaformField

data class CellSource (
    var field: MetaformField?,
    var type: CellSourceType
)