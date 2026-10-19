package com.example.data.kitchen

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [RecipeEntity::class], version = 1, exportSchema = false)
abstract class RecipeDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null

        fun getInstance(context: Context): RecipeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "kitchen_recipes_vault.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial authentic recipes in background
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.recipeDao()?.insertAll(getInitialPreseededRecipes())
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun getInitialPreseededRecipes(): List<RecipeEntity> {
            return listOf(
                RecipeEntity(
                    id = "recipe_chicken_1",
                    title = "Grandma's Andhra Spicy Chicken Curry",
                    category = "Chicken",
                    description = "A fiery, richly spiced homestyle Andhra chicken curry simmered with freshly roasted coriander, poppy seeds, cinnamon, cloves, and curry leaves.",
                    ingredientsJson = RecipeEntity.listToJson(listOf(
                        "1 kg Chicken, curry cut",
                        "2 large Onions, finely sliced",
                        "3 Green Chillies, slit lengthwise",
                        "2 tbsp Ginger-Garlic paste",
                        "2 tsp Kashmiri Red Chilli powder",
                        "1/2 tsp Turmeric powder",
                        "2 tbsp Roasted Coriander & Cumin powder",
                        "1 sprig Fresh Curry Leaves",
                        "3 tbsp Sesame or Mustard Oil",
                        "1/2 cup Fresh Coconut paste",
                        "Fresh Coriander leaves for garnish",
                        "Salt to taste"
                    )),
                    stepsJson = RecipeEntity.listToJson(listOf(
                        "Marinate chicken with turmeric, half the chilli powder, ginger-garlic paste and salt for 30 minutes.",
                        "Heat sesame oil in a heavy-bottomed kadai, add curry leaves, slit chillies, and sliced onions. Sauté until deep golden brown.",
                        "Add marinated chicken and sear on high flame for 5-7 minutes until the juices release.",
                        "Stir in coriander-cumin powder, remaining chilli powder, and coconut paste with 1 cup of warm water.",
                        "Cover with lid and simmer on low-medium flame for 20 minutes until chicken is tender and oil floats on the gravy surface.",
                        "Garnish with chopped cilantro and serve piping hot with steamed basmati rice or hot chapatis."
                    )),
                    photoUri = "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?auto=format&fit=crop&w=800&q=80",
                    cookingTimeMinutes = 45,
                    servings = 4,
                    familyMemberName = "Grandma Amma",
                    youtubeUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    localVideoUri = "",
                    isFavorite = true,
                    createdAt = System.currentTimeMillis() - 1000000
                ),
                RecipeEntity(
                    id = "recipe_mutton_1",
                    title = "Royal Kashmiri Mutton Rogan Josh",
                    category = "Mutton",
                    description = "Tender bone-in mutton slow-cooked in aromatic mustard oil, infused with fennel seeds, dry ginger, cloves, and vibrant Kashmiri deggi mirch.",
                    ingredientsJson = RecipeEntity.listToJson(listOf(
                        "800g Tender Mutton pieces (goat or lamb)",
                        "1/2 cup Mustard oil (smoked)",
                        "1 cup Whisked Greek Yogurt",
                        "2 Black Cardamoms & 4 Green Cardamoms",
                        "1-inch Cinnamon stick & 3 Cloves",
                        "1 tbsp Saunf (Fennel powder)",
                        "1 tsp Sonth (Dry ginger powder)",
                        "2 tbsp Kashmiri Red Chilli powder",
                        "1/2 tsp Hing (Asafoetida)",
                        "Freshly ground Garam Masala",
                        "Salt to taste"
                    )),
                    stepsJson = RecipeEntity.listToJson(listOf(
                        "Heat mustard oil until smoking, let cool slightly, then add whole spices (cardamom, cinnamon, cloves, hing).",
                        "Add mutton pieces and sear over high flame until browned on all sides.",
                        "Mix Kashmiri red chilli powder with 2 tbsp water into a smooth paste and pour over mutton.",
                        "Gradually whisk in yogurt on low heat to prevent curdling, stirring constantly until oil separates.",
                        "Add fennel powder, dry ginger, salt, and 1.5 cups of hot water.",
                        "Cover tightly and slow simmer on low heat for 50-60 minutes (or pressure cook for 4 whistles) until mutton is melt-in-mouth soft.",
                        "Finish with aromatic garam masala and rest for 10 minutes before serving with garlic naan."
                    )),
                    photoUri = "https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&w=800&q=80",
                    cookingTimeMinutes = 75,
                    servings = 5,
                    familyMemberName = "Uncle Farooq",
                    youtubeUrl = "https://www.youtube.com",
                    localVideoUri = "",
                    isFavorite = true,
                    createdAt = System.currentTimeMillis() - 2000000
                ),
                RecipeEntity(
                    id = "recipe_fish_1",
                    title = "Malabar Coconut Tamarind Fish Curry",
                    category = "Fish",
                    description = "Fresh Kingfish or Pomfret steaks cooked with kokum, shallots, green chillies, fenugreek, and thick fresh coconut milk in an earthen clay pot.",
                    ingredientsJson = RecipeEntity.listToJson(listOf(
                        "500g Fresh Fish Steaks (Kingfish or Salmon)",
                        "1 cup Thick Coconut Milk + 1/2 cup thin milk",
                        "3 pieces Kudampuli (Malabar Tamarind / Kokum)",
                        "10 Shallots (Small onions), thinly sliced",
                        "2 Green Chillies, slit",
                        "1 tsp Mustard seeds & 1/4 tsp Fenugreek seeds",
                        "1 tbsp Ginger, julienned",
                        "1 tbsp Red Chilli powder & 1/2 tsp Turmeric",
                        "2 sprigs Fresh Curry Leaves",
                        "2 tbsp Coconut Oil",
                        "Rock salt to taste"
                    )),
                    stepsJson = RecipeEntity.listToJson(listOf(
                        "Soak Malabar tamarind in 1/2 cup warm water for 15 minutes.",
                        "In an earthen pot (meen chatti), heat coconut oil, crackle mustard and fenugreek seeds, then add curry leaves.",
                        "Sauté sliced shallots, ginger, and green chillies until fragrant and translucent.",
                        "Add chilli powder, turmeric, and thin coconut milk along with soaked tamarind water.",
                        "Bring to a gentle boil, gently slide in the fish steaks, and season with rock salt.",
                        "Cook covered on medium flame for 12 minutes until fish is cooked through.",
                        "Pour in thick coconut milk, swirl the pot gently (do not stir with spoon to prevent breaking fish), heat through, and garnish with fresh curry leaves."
                    )),
                    photoUri = "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=800&q=80",
                    cookingTimeMinutes = 35,
                    servings = 4,
                    familyMemberName = "Aunt Meera",
                    youtubeUrl = "",
                    localVideoUri = "",
                    isFavorite = false,
                    createdAt = System.currentTimeMillis() - 3000000
                ),
                RecipeEntity(
                    id = "recipe_veg_1",
                    title = "Mom's Paneer Butter Masala Supreme",
                    category = "Vegetarian",
                    description = "Silky, velvety tomato-cashew makhani gravy infused with butter, kasuri methi, and fresh soft cottage cheese cubes.",
                    ingredientsJson = RecipeEntity.listToJson(listOf(
                        "300g Fresh Paneer, cut into cubes",
                        "4 large Ripe Tomatoes, pureed",
                        "15 Cashews soaked in warm milk",
                        "3 tbsp Salted Butter & 1 tbsp Oil",
                        "1 tbsp Ginger-Garlic paste",
                        "1.5 tsp Kashmiri Chilli powder",
                        "1/2 tsp Garam Masala",
                        "1 tbsp Crushed Kasuri Methi (Fenugreek)",
                        "3 tbsp Fresh Cream",
                        "1 tsp Honey or Sugar to balance acidity",
                        "Salt to taste"
                    )),
                    stepsJson = RecipeEntity.listToJson(listOf(
                        "Blend soaked cashews into a smooth, silky paste with a little milk.",
                        "Melt butter with oil in a pan, add ginger-garlic paste and sauté for 1 minute until raw aroma dissipates.",
                        "Add tomato puree, Kashmiri chilli powder, and salt. Cook until butter starts leaving sides of the pan.",
                        "Stir in cashew paste and 1/2 cup water. Simmer gently on low flame for 6 minutes.",
                        "Add paneer cubes, crushed kasuri methi, and honey. Cook on low heat for 3-4 minutes so paneer absorbs the rich gravy.",
                        "Swirl in fresh cream, drizzle melted butter, and serve with hot tandoori rotis or jeera rice."
                    )),
                    photoUri = "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?auto=format&fit=crop&w=800&q=80",
                    cookingTimeMinutes = 30,
                    servings = 4,
                    familyMemberName = "Mom's Secret Recipe",
                    youtubeUrl = "https://www.youtube.com",
                    localVideoUri = "",
                    isFavorite = true,
                    createdAt = System.currentTimeMillis() - 4000000
                ),
                RecipeEntity(
                    id = "recipe_egg_1",
                    title = "Highway Dhaba Egg Masala Curry",
                    category = "Egg",
                    description = "Boiled eggs pricked and pan-roasted in spices, simmered in a robust, rustic onion-tomato masala with whole cumin and crushed black pepper.",
                    ingredientsJson = RecipeEntity.listToJson(listOf(
                        "6 Hard-Boiled Eggs, peeled & scored",
                        "2 large Onions, finely chopped",
                        "2 Tomatoes, grated",
                        "1 tbsp Ginger-Garlic paste",
                        "1 tsp Cumin seeds & 2 Bay leaves",
                        "1.5 tsp Coriander powder",
                        "1 tsp Roasted Cumin powder",
                        "1/2 tsp Black Pepper, freshly crushed",
                        "1/2 tsp Turmeric & 1 tsp Chilli powder",
                        "3 tbsp Mustard or Vegetable Oil",
                        "Fresh Cilantro for garnish"
                    )),
                    stepsJson = RecipeEntity.listToJson(listOf(
                        "Prick boiled eggs with fork, toss in pinch of turmeric and chilli powder, then shallow fry in 1 tbsp hot oil until golden-crusted. Set aside.",
                        "In the same pan, heat remaining oil, add cumin seeds and bay leaves until aromatic.",
                        "Add chopped onions and sauté until deep caramelized brown.",
                        "Add ginger-garlic paste, then add grated tomatoes and dry spice powders.",
                        "Cook masala till oil releases, pour 1 cup water and bring to a brisk boil.",
                        "Drop in the roasted eggs, cover and simmer for 8 minutes to let the flavours infuse.",
                        "Sprinkle freshly crushed black pepper and cilantro. Best enjoyed with parathas!"
                    )),
                    photoUri = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?auto=format&fit=crop&w=800&q=80",
                    cookingTimeMinutes = 25,
                    servings = 3,
                    familyMemberName = "Brother Arjun",
                    youtubeUrl = "",
                    localVideoUri = "",
                    isFavorite = false,
                    createdAt = System.currentTimeMillis() - 5000000
                ),
                RecipeEntity(
                    id = "recipe_dal_1",
                    title = "Desi Ghee Tadka Dal",
                    category = "Dal",
                    description = "Hearty yellow Toor and Moong lentils slow-cooked with turmeric, tempered with sizzling hot Desi Ghee, cumin seeds, garlic slivers, and whole Kashmiri chillies.",
                    ingredientsJson = RecipeEntity.listToJson(listOf(
                        "3/4 cup Toor Dal (Yellow pigeon peas)",
                        "1/4 cup Yellow Moong Dal",
                        "3 cups Water",
                        "1/2 tsp Turmeric & 1 tsp Salt",
                        "1 Tomato, diced",
                        "3 tbsp Pure Desi Ghee",
                        "1 tsp Cumin seeds (Jeera)",
                        "6 cloves Garlic, finely sliced",
                        "1-inch Ginger, minced",
                        "2 Whole dried Red Chillies",
                        "1/4 tsp Hing (Asafoetida)",
                        "1/2 tsp Kashmiri Red Chilli powder (for tempering)",
                        "Fresh Coriander leaves"
                    )),
                    stepsJson = RecipeEntity.listToJson(listOf(
                        "Wash lentils thoroughly and pressure cook with water, turmeric, diced tomato and salt for 4 whistles until creamy.",
                        "Whisk dal gently with a wooden churner (mathani) to achieve a smooth, comforting consistency.",
                        "In a small tadka pan, heat pure desi ghee over medium flame.",
                        "Add cumin seeds, whole red chillies, and hing. When seeds splutter, add sliced garlic and fry until golden brown.",
                        "Turn off heat and stir in Kashmiri chilli powder for brilliant red color.",
                        "Pour the sizzling tadka immediately over the hot dal and cover with lid for 2 minutes to lock in smoky aroma.",
                        "Stir in chopped cilantro and serve with hot basmati rice and roasted papad."
                    )),
                    photoUri = "https://images.unsplash.com/photo-1546833999-b9f581a1996d?auto=format&fit=crop&w=800&q=80",
                    cookingTimeMinutes = 25,
                    servings = 4,
                    familyMemberName = "Grandma Amma",
                    youtubeUrl = "",
                    localVideoUri = "",
                    isFavorite = true,
                    createdAt = System.currentTimeMillis() - 6000000
                ),
                RecipeEntity(
                    id = "recipe_drinks_1",
                    title = "Royal Kesar Mango Cardamom Lassi",
                    category = "Drinks",
                    description = "Chilled, thick, velvety Indian yogurt smoothie whipped with sweet Alphonso mango pulp, fragrant saffron strands, and green cardamom.",
                    ingredientsJson = RecipeEntity.listToJson(listOf(
                        "2 cups Fresh Alphonso Mango pulp or sweet diced mangoes",
                        "2 cups Chilled Thick Greek or Plain Yogurt (Dahi)",
                        "1/2 cup Chilled Milk or Ice cubes",
                        "3 tbsp Sugar or Honey (adjust to mango sweetness)",
                        "1/4 tsp Freshly ground Green Cardamom powder",
                        "A generous pinch of Saffron strands (Kesar) steeped in 1 tbsp warm milk",
                        "Sliced Pistachios and Almonds for garnish",
                        "Dried Rose petals for decoration"
                    )),
                    stepsJson = RecipeEntity.listToJson(listOf(
                        "Steep saffron threads in 1 tbsp warm milk for 10 minutes until deep orange-yellow.",
                        "In a high-speed blender, combine mango pulp, chilled yogurt, milk/ice, sugar, and cardamom powder.",
                        "Blend on high for 60 seconds until frothy and smooth.",
                        "Pour into tall chilled clay kulhad cups or glasses.",
                        "Drizzle steeped saffron milk over the top and garnish with chopped pistachios, almonds, and rose petals.",
                        "Serve immediately as a refreshing companion to spicy curries."
                    )),
                    photoUri = "https://images.unsplash.com/photo-1543362906-acfc16c67564?auto=format&fit=crop&w=800&q=80",
                    cookingTimeMinutes = 10,
                    servings = 2,
                    familyMemberName = "Sister Priya",
                    youtubeUrl = "",
                    localVideoUri = "",
                    isFavorite = true,
                    createdAt = System.currentTimeMillis() - 7000000
                ),
                RecipeEntity(
                    id = "recipe_bakers_1",
                    title = "Cardamom, Rose & Pistachio Tea Cake",
                    category = "Bakers",
                    description = "A moist, golden buttery sponge cake infused with crushed green cardamom pods, rose water, and crunchy roasted Iranian pistachios.",
                    ingredientsJson = RecipeEntity.listToJson(listOf(
                        "1.5 cups All-Purpose Flour",
                        "1 tsp Baking powder & 1/4 tsp Baking soda",
                        "1/2 cup Unsalted Butter, softened at room temp",
                        "3/4 cup Fine Castor Sugar",
                        "2 Large Eggs (or 1/2 cup thick curd for eggless version)",
                        "1/2 cup Whole Milk",
                        "1 tsp Freshly crushed Green Cardamom seeds",
                        "1 tsp Pure Rose water or extract",
                        "1/3 cup Roasted Pistachios, finely chopped",
                        "Powdered sugar for dusting"
                    )),
                    stepsJson = RecipeEntity.listToJson(listOf(
                        "Preheat oven to 175°C (350°F). Grease and line an 8-inch loaf or cake tin with parchment paper.",
                        "Whisk together flour, baking powder, baking soda, and cardamom powder in a bowl.",
                        "In a separate bowl, cream together softened butter and sugar until pale and fluffy (about 3-4 minutes).",
                        "Beat in eggs one at a time, followed by rose water.",
                        "Alternate folding in dry flour mixture and milk until a smooth, velvety batter forms. Fold in half the chopped pistachios.",
                        "Pour batter into prepared cake tin, level with spatula, and scatter remaining pistachios on top.",
                        "Bake for 35-40 minutes until a skewer inserted in the center comes out clean.",
                        "Cool on a wire rack, slice and enjoy with hot masala chai."
                    )),
                    photoUri = "https://images.unsplash.com/photo-1578985545062-69928b1d9587?auto=format&fit=crop&w=800&q=80",
                    cookingTimeMinutes = 50,
                    servings = 8,
                    familyMemberName = "Priya's Bake",
                    youtubeUrl = "",
                    localVideoUri = "",
                    isFavorite = false,
                    createdAt = System.currentTimeMillis() - 8000000
                )
            )
        }
    }
}
