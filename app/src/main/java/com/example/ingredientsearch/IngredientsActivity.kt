package com.example.ingredientsearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class IngredientsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var ingredientList: ArrayList<Ingredient>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredients)

        recyclerView = findViewById(R.id.ingredientsRecyclerView)

        // Set the direction of our list to be vertical
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get list of ingredients from intent
        ingredientList = intent.getParcelableArrayListExtra("ingredients")

        recyclerView.adapter = IngredientAdapter(ingredientList!!.toList())
    }
}
