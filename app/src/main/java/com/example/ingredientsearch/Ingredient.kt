package com.example.ingredientsearch

import android.os.Parcel
import android.os.Parcelable

data class Ingredient(val name: String, val percentage: Double) : Parcelable {

    companion object {
        @JvmField
        val CREATOR: IngredientCreator = IngredientCreator()
    }

    constructor(source: Parcel) : this(source.readString()!!, source.readDouble())

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeDouble(percentage)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    class IngredientCreator : Parcelable.Creator<Ingredient> {

        override fun createFromParcel(source: Parcel): Ingredient {
            return Ingredient(source)
        }

        override fun newArray(size: Int): Array<Ingredient> {
            return Array<Ingredient>(size) { Ingredient("", 0.0) }
        }
    }
}