package allen.town.podcast.discovery

import allen.town.podcast.BuildConfig
import allen.town.podcast.core.ClientConfig
import allen.town.podcast.core.service.download.PodcastHttpClient
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.*

class PodcastIndexPodcastSearcher : PodcastSearcher {
    override fun search(query: String?): Single<List<PodcastSearchResult?>?>? {
        return Single.create(
            SingleOnSubscribe { subscriber: SingleEmitter<List<PodcastSearchResult?>?> ->
                val calendar = Calendar.getInstance(
                    TimeZone.getTimeZone("UTC")
                )
                calendar.clear()
                val now = Date()
                calendar.time = now
                val secondsSinceEpoch = calendar.timeInMillis / 1000L
                val apiHeaderTime = secondsSinceEpoch.toString()
                val data4Hash =
                    BuildConfig.PODCASTINDEX_API_KEY + BuildConfig.PODCASTINDEX_API_SECRET + apiHeaderTime
                val hashString = sha1(data4Hash)
                val encodedQuery: String?
                encodedQuery = try {
                    URLEncoder.encode(query, "UTF-8")
                } catch (e: UnsupportedEncodingException) {
                    // this won't ever be thrown
                    query
                }
                val formattedUrl = String.format(PODCASTINDEX_API_URL, encodedQuery)
                val client = PodcastHttpClient.getHttpClient()
                val httpReq = Request.Builder()
                    .addHeader("X-Auth-Date", apiHeaderTime)
                    .addHeader("X-Auth-Key", BuildConfig.PODCASTINDEX_API_KEY)
                    .addHeader("Authorization", hashString!!)
                    .addHeader("User-Agent", ClientConfig.USER_AGENT)
                    .url(formattedUrl)
                val podcasts: MutableList<PodcastSearchResult?> = ArrayList()
                try {
                    val response = client.newCall(httpReq.build()).execute()
                    if (response.isSuccessful) {
                        val resultString = response.body!!.string()
                        val result = JSONObject(resultString)
                        val j = result.getJSONArray("feeds")
                        for (i in 0 until j.length()) {
                            val podcastJson = j.getJSONObject(i)
                            val podcast: PodcastSearchResult =
                                PodcastSearchResult.Companion.fromPodcastIndex(podcastJson)
                            if (podcast.feedUrl != null) {
                                podcasts.add(podcast)
                            }
                        }
                    } else {
                        subscriber.onError(IOException(response.toString()))
                    }
                } catch (e: IOException) {
                    subscriber.onError(e)
                } catch (e: JSONException) {
                    subscriber.onError(e)
                }
                subscriber.onSuccess(podcasts)
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun lookupUrl(url: String): Single<String> {
        return Single.just(url)
    }

    override fun urlNeedsLookup(url: String): Boolean {
        return false
    }

    override val name: String
        get() = "Podcast Index"

    companion object {
        const val SEARCH_ENGINE_TAG = "podIndex_podcast_search"
        private const val PODCASTINDEX_API_URL =
            "https://api.podcastindex.org/api/1.0/search/byterm?q=%s"

        private fun sha1(clearString: String): String? {
            return try {
                val messageDigest = MessageDigest.getInstance("SHA-1")
                messageDigest.update(clearString.toByteArray(charset("UTF-8")))
                toHex(messageDigest.digest())
            } catch (ignored: Exception) {
                ignored.printStackTrace()
                null
            }
        }

        private fun toHex(bytes: ByteArray): String {
            val buffer = StringBuilder()
            for (b in bytes) {
                buffer.append(String.format(Locale.getDefault(), "%02x", b))
            }
            return buffer.toString()
        }
    }
}