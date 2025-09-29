package com.mobile.pokedex.api;

import com.mobile.pokedex.model.PokemonDetails;
import com.mobile.pokedex.model.PokemonList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PokeApi {
    @GET("pokemon")
    Call<PokemonList> getPokemons(@Query("limit") int limit);

    @GET("pokemon/{id}")
    Call<PokemonDetails> getPokemonDetails(@Path("id") int pokemonId);
}
