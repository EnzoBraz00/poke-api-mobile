package com.mobile.pokedex;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.pokedex.adapter.PokemonAdapter;
import com.mobile.pokedex.api.PokeApi;
import com.mobile.pokedex.listener.OnPokemonClickListener;
import com.mobile.pokedex.model.Pokemon;
import com.mobile.pokedex.model.PokemonList;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements OnPokemonClickListener {

    private RecyclerView recyclerView;
    private PokemonAdapter pokemonAdapter;
    private List<Pokemon> fullPokemonListForSearch;
    private PokeApi pokeApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText searchInput = findViewById(R.id.searchInput);
        recyclerView = findViewById(R.id.pokemons);

        int spacingInPixels = (int) (20 * Resources.getSystem().getDisplayMetrics().density);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spacingInPixels));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://pokeapi.co/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        pokeApi = retrofit.create(PokeApi.class);

        pokeApi.getPokemons(1500).enqueue(new Callback<PokemonList>() {
            @Override
            public void onResponse(@NonNull Call<PokemonList> call, @NonNull Response<PokemonList> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Pokemon> allPokemons = response.body().getResults();

                    fullPokemonListForSearch = new ArrayList<>(allPokemons);

                    List<Pokemon> initialDisplayList = allPokemons.size() > 50 ?
                            allPokemons.subList(0, 50) : allPokemons;

                    pokemonAdapter = new PokemonAdapter(initialDisplayList, MainActivity.this, pokeApi);
                    recyclerView.setAdapter(pokemonAdapter);

                } else {
                    Log.e("API_CALL", "Erro na requisição: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PokemonList> call, Throwable t) {
                Log.e("API_CALL", "Falha na requisição: " + t.getMessage());
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString().toLowerCase());
            }
        });
    }

    @Override
    public void onPokemonClick(Pokemon pokemon) {
        Intent intent = new Intent(this, PokemonDetailsActivity.class);
        intent.putExtra("POKEMON_ID_EXTRA", pokemon.getId());
        startActivity(intent);
    }

    private void filter(String text) {
        if (fullPokemonListForSearch == null || pokemonAdapter == null) return;

        if (text.isEmpty()) {
            List<Pokemon> initialDisplayList = fullPokemonListForSearch.size() > 50 ?
                    fullPokemonListForSearch.subList(0, 50) : fullPokemonListForSearch;
            pokemonAdapter.updatePokemons(initialDisplayList);
            return;
        }

        List<Pokemon> filteredList = new ArrayList<>();
        for (Pokemon item : fullPokemonListForSearch) {
            if (item.getName().toLowerCase().contains(text) || String.valueOf(item.getId()).contains(text)) {
                filteredList.add(item);
            }
        }

        pokemonAdapter.updatePokemons(filteredList);
    }
}
