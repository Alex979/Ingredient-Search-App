package com.example.ingredientsearch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat

class IngredientAdapter(val ingredients: List<Ingredient>) : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout. row_ingredient, parent, false)

        return IngredientViewHolder(view)
    }

    override fun getItemCount(): Int {
        return ingredients.size
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val currentIngredient = ingredients[position]
        val df = DecimalFormat("#.##")

        holder.ingredientName.text = currentIngredient.name
        holder.ingredientPercentage.text = "${df.format(currentIngredient.percentage * 100)}%"
    }

    class IngredientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ingredientName: TextView = view.findViewById(R.id.ingredientName)
        val ingredientPercentage: TextView = view.findViewById(R.id.ingredientPercentage)
    }
}