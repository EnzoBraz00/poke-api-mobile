package com.mobile.pokedex.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PokemonList {
    @SerializedName("results")
    private List<Pokemon> results;

    public List<Pokemon> getResults() {
        return results;
    }
}
