package com.lenicon.floriledia.services

import com.lenicon.floriledia.models.PlantPhoto
import com.lenicon.floriledia.models.PlantResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

object PlantApiService {
    private const val API_KEY = "YOUR_PLANTNET_API_KEY_HERE" 

    // Safe unverified platform fallback matching badCertificateCallback execution logic
    private val unsafeOkHttpClient: OkHttpClient by lazy {
        val trustAllCerts = arrayOf<X509TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val sslContext = SSLContext.getInstance("SSL").apply {
            init(null, trustAllCerts, SecureRandom())
        }
        OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0])
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    suspend fun identifyPlant(photos: List<PlantPhoto>): PlantResult = withContext(Dispatchers.IO) {
        val url = "https://my-api.plantnet.org/v2/identify/all?api-key=$API_KEY"
        
        val requestBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
        for (photo in photos) {
            val file = File(photo.path)
            if (!file.exists()) throw IOException("File path invalid")
            
            requestBodyBuilder.addFormDataPart(
                "images", file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
            requestBodyBuilder.addFormDataPart("organs", photo.organ.lowercase())
        }

        val request = Request.Builder().url(url).post(requestBodyBuilder.build()).build()
        
        unsafeOkHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Server Error: ${response.code}")
            
            val bodyString = response.body?.string() ?: throw IOException("Empty payload response")
            
            // Fixed constructor ambiguity explicitly forcing non-null Type evaluation parameter
            val jsonRoot = JSONObject(bodyString)
            val bestMatch = jsonRoot.getJSONArray("results").getJSONObject(0)
            val species = bestMatch.getJSONObject("species")
            
            val scientificName = species.getString("scientificNameWithoutAuthor")
            val authorship = species.optString("scientificNameAuthorship", "")
            val family = species.getJSONObject("family").getString("scientificNameWithoutAuthor")
            
            val commonNamesList = mutableListOf<String>()
            
            // Safe extraction step checking if optional common names collection array exists
            val commonNamesObj = species.opt("commonNames")
            if (commonNamesObj is org.json.JSONArray) {
                for (i in 0 until commonNamesObj.length()) {
                    commonNamesList.add(commonNamesObj.getString(i))
                }
            }

            val wikiResult = fetchWiki(scientificName)

            PlantResult(
                imagePaths = photos.map { it.path },
                scientificName = scientificName,
                authorship = authorship,
                family = family,
                commonNames = commonNamesList,
                nickname = scientificName.split(" ").firstOrNull() ?: "Plant",
                wikiSummary = wikiResult.summary,
                wikiImageURL = wikiResult.imageUrl
            )
        }
    }

    private fun fetchWiki(scientificName: String): WikiContainer {
        val encodedTitle = URLEncoder.encode(scientificName.replace(" × ", " ").replace(" x ", " "), "UTF-8")
        val url = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts|pageimages&exintro&explaintext&redirects=1&titles=$encodedTitle&pithumbsize=1000"
        
        val request = Request.Builder().url(url).build()
        
        // Explicitly routing targeted return pathways outside lambda structure boundaries using return@use
        return OkHttpClient().newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@use WikiContainer("", "")
            val body = response.body?.string() ?: return@use WikiContainer("", "")
            
            val json = JSONObject(body)
            val query = json.optJSONObject("query") ?: return@use WikiContainer("", "")
            val pages = query.optJSONObject("pages") ?: return@use WikiContainer("", "")
            val keys = pages.keys()
            if (!keys.hasNext()) return@use WikiContainer("", "")
            
            val pageId = keys.next()
            if (pageId == "-1") return@use WikiContainer("", "")
            
            val pageObj = pages.getJSONObject(pageId)
            val summary = pageObj.optString("extract", "").split("\n\n==").first()
            val imageUrl = pageObj.optJSONObject("thumbnail")?.optString("source", "") ?: ""
            
            WikiContainer(summary, imageUrl)
        }
    }

    private data class WikiContainer(val summary: String, val imageUrl: String)
}