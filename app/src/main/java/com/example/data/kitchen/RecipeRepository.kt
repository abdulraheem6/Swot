package com.example.data.kitchen

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeRepository(context: Context) {

    private val db = RecipeDatabase.getInstance(context)
    private val dao = db.recipeDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    val allRecipes: Flow<List<RecipeEntity>> = dao.getAllRecipes()
    val recipeCount: Flow<Int> = dao.getRecipeCount()

    init {
        // Ensure initial seeds are inserted if DB is fresh
        scope.launch {
            try {
                val current = dao.getRecipeById("recipe_chicken_1")
                if (current == null) {
                    dao.insertAll(RecipeDatabase.getInitialPreseededRecipes())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun insertRecipe(recipe: RecipeEntity) {
        withContext(Dispatchers.IO) {
            dao.insertRecipe(recipe)
        }
    }

    suspend fun updateRecipe(recipe: RecipeEntity) {
        withContext(Dispatchers.IO) {
            dao.updateRecipe(recipe)
        }
    }

    suspend fun deleteRecipe(recipe: RecipeEntity) {
        withContext(Dispatchers.IO) {
            dao.deleteRecipe(recipe)
        }
    }

    suspend fun deleteRecipeById(id: String) {
        withContext(Dispatchers.IO) {
            dao.deleteRecipeById(id)
        }
    }

    suspend fun toggleFavorite(id: String, currentStatus: Boolean) {
        withContext(Dispatchers.IO) {
            dao.updateFavorite(id, !currentStatus)
        }
    }

    suspend fun getRecipeById(id: String): RecipeEntity? {
        return withContext(Dispatchers.IO) {
            dao.getRecipeById(id)
        }
    }
}
