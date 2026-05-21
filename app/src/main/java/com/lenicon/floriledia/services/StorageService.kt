package com.lenicon.floriledia.services

import android.content.Context
import com.lenicon.floriledia.models.PlantResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object StorageService {
    private const val MAX_FILE_SIZE = 20 * 1024 * 1024 // 20 MB
    private lateinit var appContext: Context
    private var currentAccount: String = "guest"
    
    // FIX 1: This prefix is computed dynamically to keep file names safe and separate
    private val accountFilePrefix: String
        get() = currentAccount.replace(Regex("[^a-zA-Z0-9_]"), "_")

    private val _plantsStateFlow = MutableStateFlow<List<JSONObject>>(emptyList())
    val plantsStateFlow: StateFlow<List<JSONObject>> = _plantsStateFlow.asStateFlow()

    val savedPlants: List<JSONObject>
        get() = _plantsStateFlow.value

    // This must clear on account change
    private val globalSavedPaths = mutableSetOf<String>()

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Switches the active account profile sandbox. Call this upon login.
     */
    suspend fun switchAccount(accountIdentifier: String) {
        currentAccount = if (accountIdentifier.isBlank()) "guest" else accountIdentifier
        
        // FIX 2: Purge the memory cache state so the new account doesn't inherit references
        globalSavedPaths.clear()
        
        load()
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        currentAccount = "guest"
        globalSavedPaths.clear()
        load() // Re-load to empty out the StateFlow for the guest sandbox
    }

    /**
     * Loads all plants from files matching the active account prefix.
     */
    suspend fun load() = withContext(Dispatchers.IO) {
        val plants = getAllSavedPlants()
        _plantsStateFlow.value = plants
    }

    /**
     * Appends a new plant result item to the active account file structure.
     */
    suspend fun savePlant(result: PlantResult) = withContext(Dispatchers.IO) {
        var fileIndex = 1
        var targetFile: File

        while (true) {
            // FIX 1: Applied sanitized token boundary instead of raw account email
            targetFile = File(appContext.filesDir, "${accountFilePrefix}_collection_$fileIndex.json")
            if (targetFile.exists()) {
                if (targetFile.length() < MAX_FILE_SIZE) {
                    break
                }
                fileIndex++
            } else {
                targetFile.createNewFile()
                targetFile.writeText("[]")
                break
            }
        }

        val content = targetFile.readText()
        val jsonArray = JSONArray(content)

        val plantJson = JSONObject().apply {
            put("id", result.id)
            put("nickname", result.nickname)
            put("firstImage", if (result.imagePaths.isNotEmpty()) result.imagePaths.first() else "")
            put("imagePaths", JSONArray(result.imagePaths))
            put("scientificName", result.scientificName)
            put("authorship", result.authorship)
            put("family", result.family)
            put("commonNames", JSONArray(result.commonNames))
            put("notes", result.notes)
            put("wikiSummary", result.wikiSummary)
            put("wikiImageURL", result.wikiImageURL)
        }

        jsonArray.put(plantJson)
        targetFile.writeText(jsonArray.toString())
        
        load() 
    }

    suspend fun updatePlant(updatedPlant: PlantResult) = withContext(Dispatchers.IO) {
        // FIX 1: Applied sanitized account token boundary 
        val files = appContext.filesDir.listFiles { _, name -> 
            name.startsWith("${accountFilePrefix}_collection_") && name.endsWith(".json") 
        } ?: return@withContext

        for (file in files) {
            val jsonArray = JSONArray(file.readText())
            var found = false

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.optString("id") == updatedPlant.id) {
                    obj.put("nickname", updatedPlant.nickname)
                    obj.put("notes", updatedPlant.notes)
                    found = true
                    break
                }
            }

            if (found) {
                file.writeText(jsonArray.toString())
                load()
                return@withContext
            }
        }
    }

    suspend fun deletePlant(id: String) = withContext(Dispatchers.IO) {
        // FIX 1: Applied sanitized account token boundary
        val files = appContext.filesDir.listFiles { _, name -> 
            name.startsWith("${accountFilePrefix}_collection_") && name.endsWith(".json") 
        } ?: return@withContext

        for (file in files) {
            val jsonArray = JSONArray(file.readText())
            var targetIndex = -1
            var targetObject: JSONObject? = null

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.optString("id") == id) {
                    targetIndex = i
                    targetObject = obj
                    break
                }
            }

            if (targetIndex != -1 && targetObject != null) {
                val imagesArray = targetObject.optJSONArray("imagePaths")
                if (imagesArray != null) {
                    for (j in 0 until imagesArray.length()) {
                        val path = imagesArray.optString(j)
                        if (path.isNotBlank()) {
                            val imgFile = File(path)
                            if (imgFile.exists()) {
                                imgFile.delete()
                            }
                        }
                    }
                }

                jsonArray.remove(targetIndex)
                file.writeText(jsonArray.toString())
                load()
                return@withContext
            }
        }
    }

    suspend fun getAllSavedPlants(): List<JSONObject> = withContext(Dispatchers.IO) {
        val context = appContext ?: return@withContext emptyList()
        val allPlants = mutableListOf<JSONObject>()
        
        // FIX 1: Applied sanitized account token boundary
        val files = context.filesDir.listFiles { _, name -> 
            name.startsWith("${accountFilePrefix}_collection_") && name.endsWith(".json") 
        } ?: return@withContext emptyList()

        for (file in files) {
            try {
                val fileContent = file.readText()
                if (fileContent.isNotBlank()) {
                    val jsonArray = JSONArray(fileContent)
                    for (i in 0 until jsonArray.length()) {
                        allPlants.add(jsonArray.getJSONObject(i))
                    }
                } else {
                    file.delete()
                }
            } catch (e: Exception) {
                android.util.Log.e("StorageService", "Skipping corrupted file ${file.name}: ${e.message}")
            }
        }

        return@withContext allPlants
    }

    suspend fun saveImagePermanently(tempPath: String): String = withContext(Dispatchers.IO) {
        if (tempPath.isBlank()) return@withContext tempPath
        
        val tempFile = File(tempPath)
        if (!tempFile.exists()) return@withContext tempPath

        val folder = File(appContext.filesDir, "photos")
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val permanentFile = File(folder, tempFile.name)
        try {
            tempFile.copyTo(permanentFile, overwrite = true)
            return@withContext permanentFile.absolutePath
        } catch (e: Exception) {
            return@withContext tempPath
        }
    }

    fun markAsSaved(path: String) {
        globalSavedPaths.add(path)
    }

    fun isSaved(path: String): Boolean {
        return globalSavedPaths.contains(path)
    }
}