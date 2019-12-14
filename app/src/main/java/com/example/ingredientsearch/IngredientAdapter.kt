package com.example.ingredientsearch

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat
import kotlin.math.pow

class IngredientAdapter(val ingredients: List<Ingredient>) : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    val colorStart = floatArrayOf(146.0f, 0.88f, 0.32f)
    val colorEnd = floatArrayOf(0.0f, 0.94f, 0.61f)

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

        var color: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f)
        ColorUtils.blendHSL(colorStart, colorEnd, (1.0f - currentIngredient.percentage.toFloat().pow(2)), color)
        holder.ingredientPercentage.setTextColor(ColorUtils.HSLToColor(color))
    }

    class IngredientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ingredientName: TextView = view.findViewById(R.id.ingredientName)
        val ingredientPercentage: TextView = view.findViewById(R.id.ingredientPercentage)
    }
}