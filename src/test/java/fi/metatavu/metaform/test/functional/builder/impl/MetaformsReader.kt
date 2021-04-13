package fi.metatavu.metaform.test.functional.builder.impl

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.adapters.EnumJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.api.client.models.MetaformFieldType
import org.apache.commons.io.IOUtils
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Class for reading and deserializing metaforms from json files
 */
class MetaformsReader {
    private class UUIDAdapter {
        @FromJson
        fun fromJson(string: String): UUID {
            return UUID.fromString(string)
        }

        @ToJson
        fun toJson(uuid: UUID): String {
            return uuid.toString()
        }
    }

    companion object {
        /**
         * Reads a Metaform from JSON file
         *
         * @param form file name
         * @return Metaform object
         * @throws IOException throws IOException when JSON reading fails
         */
        @Throws(IOException::class)
        fun readMetaform(form: String?): Metaform? {
            val path = String.format("fi/metatavu/metaform/testforms/%s.json", form)
            val formStream = this::class.java.classLoader.getResourceAsStream(path)
            val moshi: Moshi = Moshi.Builder()
                .add(UUIDAdapter())
                .add(
                    MetaformFieldType::class.java,
                    EnumJsonAdapter.create<MetaformFieldType>(MetaformFieldType::class.java).withUnknownFallback(null)
                )
                .addLast(KotlinJsonAdapterFactory()).build()
            val jsonAdapter = moshi.adapter(
                Metaform::class.java
            )
            return jsonAdapter.fromJson(IOUtils.toString(formStream, StandardCharsets.UTF_8.name()))
        }
    }
}