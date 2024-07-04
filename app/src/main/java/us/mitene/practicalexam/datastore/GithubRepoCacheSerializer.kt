package us.mitene.practicalexam.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import us.mitene.practicalexam.datastore.proto.GithubRepoCache
import java.io.InputStream
import java.io.OutputStream

object GithubRepoCacheSerializer : Serializer<GithubRepoCache> {
    override val defaultValue: GithubRepoCache = GithubRepoCache.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): GithubRepoCache {
        try {
            return GithubRepoCache.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: GithubRepoCache, output: OutputStream) = t.writeTo(output)
}