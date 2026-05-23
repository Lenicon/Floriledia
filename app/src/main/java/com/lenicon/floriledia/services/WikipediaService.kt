package com.lenicon.floriledia.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import com.lenicon.floriledia.models.WikipediaResult

object WikipediaService {

    suspend fun fetchWiki(scientificName: String): WikipediaResult = withContext(Dispatchers.IO) {
        val encodedTitle = encodePlantName(scientificName)
        val urlString = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts|pageimages&exintro&explaintext&redirects=1&titles=$encodedTitle&pithumbsize=1000"
        
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val responseStringBuilder = StringBuilder()
                var line: String?
                
                while (reader.readLine().also { line = it } != null) {
                    responseStringBuilder.append(line)
                }
                reader.close()
                connection.disconnect()


                val jsonResponse = JSONObject(responseStringBuilder.toString())
                val queryObj = jsonResponse.optJSONObject("query") ?: return@withContext WikipediaResult()
                val pagesObj = queryObj.optJSONObject("pages") ?: return@withContext WikipediaResult()
                
                
                val keys = pagesObj.keys()
                if (!keys.hasNext()) return@withContext WikipediaResult()
                val pageId = keys.next()

                if (pageId != "-1") {
                    val pageData = pagesObj.getJSONObject(pageId)
                    
                    
                    val rawExtract = pageData.optString("extract", "")
                    val summary = if (rawExtract.contains("\n\n==")) {
                        rawExtract.split("\n\n==")[0]
                    } else {
                        rawExtract
                    }


                    val thumbnailObj = pageData.optJSONObject("thumbnail")
                    val imageUrl = thumbnailObj?.optString("source", "") ?: ""

                    return@withContext WikipediaResult(
                        wikiSummary = summary,
                        wikiImageURL = imageUrl
                    )
                }
            }
            return@withContext WikipediaResult()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext WikipediaResult()
        }
    }

    private fun encodePlantName(scientificName: String): String {
        val cleanName = scientificName.replace(" × ", " ").replace(" x ", " ")
        return URLEncoder.encode(cleanName, "UTF-8")
    }
}