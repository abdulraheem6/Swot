package com.example.data.kitchen

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import java.util.UUID

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String, // "Chicken", "Mutton", "Fish", "Vegetarian", "Egg", "Dal", "Drinks", "Bakers", "Other"
    val description: String = "",
    val ingredientsJson: String = "[]",
    val stepsJson: String = "[]",
    val photoUri: String = "",
    val cookingTimeMinutes: Int = 30,
    val servings: Int = 4,
    val familyMemberName: String = "", // e.g. "Grandma Amma", "Mom's Special"
    val youtubeUrl: String = "",
    val localVideoUri: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getIngredientsList(): List<String> {
        return try {
            val list = mutableListOf<String>()
            val arr = JSONArray(ingredientsJson)
            for (i in 0 until arr.length()) {
                val str = arr.optString(i)
                if (str.isNotBlank()) list.add(str)
            }
            if (list.isEmpty() && ingredientsJson.isNotBlank() && !ingredientsJson.trim().startsWith("[")) {
                ingredientsJson.lines().map { it.trim().removePrefix("-").removePrefix("•").trim() }.filter { it.isNotBlank() }
            } else {
                list
            }
        } catch (_: Exception) {
            ingredientsJson.lines().map { it.trim().removePrefix("-").removePrefix("•").trim() }.filter { it.isNotBlank() }
        }
    }

    fun getStepsList(): List<String> {
        return try {
            val list = mutableListOf<String>()
            val arr = JSONArray(stepsJson)
            for (i in 0 until arr.length()) {
                val str = arr.optString(i)
                if (str.isNotBlank()) list.add(str)
            }
            if (list.isEmpty() && stepsJson.isNotBlank() && !stepsJson.trim().startsWith("[")) {
                stepsJson.lines().map { it.trim().removePrefix("-").removePrefix("•").trim() }.filter { it.isNotBlank() }
            } else {
                list
            }
        } catch (_: Exception) {
            stepsJson.lines().map { it.trim().removePrefix("-").removePrefix("•").trim() }.filter { it.isNotBlank() }
        }
    }

    companion object {
        fun listToJson(list: List<String>): String {
            val arr = JSONArray()
            list.filter { it.isNotBlank() }.forEach { arr.put(it.trim()) }
            return arr.toString()
        }
    }
}
