package com.example.ingredientsearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.BitmapFactory
import android.graphics.Bitmap



class IngredientsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var foodImage: ImageView
    private var ingredientList: ArrayList<Ingredient>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredients)

        title = "Results"

        recyclerView = findViewById(R.id.ingredientsRecyclerView)
        foodImage = findViewById(R.id.foodImage)

        // Set the direction of our list to be vertical
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get image from intent
        val imageBytes = intent.getByteArrayExtra("image")
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes!!.size)
        foodImage.setImageBitmap(bitmap)

        // Get list of ingredients from intent
        ingredientList = intent.getParcelableArrayListExtra("ingredients")

        recyclerView.adapter = IngredientAdapter(ingredientList!!.toList())
    }
}
