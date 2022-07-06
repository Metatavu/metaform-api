package fi.metatavu.metaform.server.files

data class File (
        var meta: FileMeta,
        var data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as File

        if (meta != other.meta) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = meta.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}