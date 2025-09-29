package com.mobile.pokedex;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.gson.Gson;
import com.mobile.pokedex.adapter.PokemonAdapter;
import com.mobile.pokedex.api.PokeApi;
import com.mobile.pokedex.listener.OnPokemonClickListener;
import com.mobile.pokedex.model.Pokemon;
import com.mobile.pokedex.model.PokemonDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PokemonDetailsActivity extends AppCompatActivity implements OnPokemonClickListener {
    public static final String POKEMON_ID_EXTRA = "POKEMON_ID_EXTRA";

    private ImageView imageBig;
    private TextView descriptionBig;
    private TextView nameBig;
    private TextView numberBig;
    private TextView generationBig;
    private TextView weightBig;
    private TextView heightBig;

    private TextView lifeBig;
    private TextView attackBig;
    private TextView defenseBig;
    private TextView specialAttack;
    private TextView specialDefense;
    private TextView speed;

    private PokemonAdapter evolutionAdapter;
    private PokemonAdapter similarAdapter;

    private PokeApi pokeApi;
    private Gson gson;

    private final List<Pokemon> loadedEvolutionPokemons = new ArrayList<>();
    private final List<Pokemon> loadedSimilarPokemons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pokemon_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://pokeapi.co/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.pokeApi = retrofit.create(PokeApi.class);
        this.gson = new Gson();

        int pokemonId = getIntent().getIntExtra(POKEMON_ID_EXTRA, -1);
        if (pokemonId != -1) {
            loadPokemonDetails(pokemonId);
        } else {
            Toast.makeText(this, "ID do Pokémon inválido.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        descriptionBig = findViewById(R.id.descriptionBig);
        imageBig = findViewById(R.id.imageBig);
        nameBig = findViewById(R.id.nameBig);
        numberBig = findViewById(R.id.numberBig);
        generationBig = findViewById(R.id.generationBig);
        weightBig = findViewById(R.id.weightBig);
        heightBig = findViewById(R.id.heightBig);

        lifeBig = findViewById(R.id.lifeBig);
        attackBig = findViewById(R.id.attackBig);
        defenseBig = findViewById(R.id.defenseBig);
        specialAttack = findViewById(R.id.specialAttack);
        specialDefense = findViewById(R.id.specialDefense);
        speed = findViewById(R.id.speed);

        RecyclerView evolutionChainRecycler = findViewById(R.id.evolutionChainRecycler);
        RecyclerView similarPokemonsRecycler = findViewById(R.id.similarPokemonsRecycler);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_margin_horizontal);
        int spacingInPixels2 = getResources().getDimensionPixelSize(R.dimen.item_margin_vertical);

        DetailsSpacingItemDecoration itemDecoration = new DetailsSpacingItemDecoration(spacingInPixels, spacingInPixels2);

        evolutionChainRecycler.addItemDecoration(itemDecoration);

        similarPokemonsRecycler.addItemDecoration(itemDecoration);

        evolutionChainRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        similarPokemonsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        evolutionAdapter = new PokemonAdapter(new ArrayList<>(), this);
        similarAdapter = new PokemonAdapter(new ArrayList<>(), this);

        evolutionChainRecycler.setAdapter(evolutionAdapter);
        similarPokemonsRecycler.setAdapter(similarAdapter);
    }

    private void loadPokemonDetails(int id) {
        pokeApi.getPokemonDetails(id).enqueue(new Callback<PokemonDetails>() {
            @Override
            public void onResponse(@NonNull Call<PokemonDetails> call, @NonNull Response<PokemonDetails> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PokemonDetails details = response.body();

                    nameBig.setText(capitalize(details.getName()));
                    numberBig.setText(String.valueOf(details.getId()));
                    generationBig.setText(getPokemonGeneration(details.getId()));

                    double weightKg = details.getWeight() / 10.0;
                    double heightM = details.getHeight() / 10.0;
                    weightBig.setText(String.format(Locale.getDefault(), "%.1f kg", weightKg));
                    heightBig.setText(String.format(Locale.getDefault(), "%.2f m", heightM));

                    mapStats(details.getStats());

                    Glide.with(PokemonDetailsActivity.this)
                            .load(details.getSprites().getOther().getOfficialArtwork().getFrontDefault())
                            .into(imageBig);

                    String pokemonTypes = getTypesAsString(details.getTypes());
                    loadRelatedPokemonsWithGemini(details.getName(), pokemonTypes);

                } else {
                    Log.e("DetailsActivity", "Erro ao carregar detalhes: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PokemonDetails> call, @NonNull Throwable t) {
                Log.e("DetailsActivity", "Falha na requisição: " + t.getMessage());
            }
        });
    }

    private String getTypesAsString(List<PokemonDetails.Type> types) {
        StringBuilder typesStr = new StringBuilder();
        for (int i = 0; i < types.size(); i++) {
            typesStr.append(capitalize(types.get(i).getType().getName()));
            if (i < types.size() - 1) {
                typesStr.append(", ");
            }
        }
        return typesStr.toString();
    }

    private void mapStats(List<PokemonDetails.Stat> stats) {
        for (PokemonDetails.Stat stat : stats) {
            int value = stat.getBaseStat();
            String name = stat.getStat().getName();

            switch (name) {
                case "hp":
                    lifeBig.setText(String.valueOf(value));
                    break;
                case "attack":
                    attackBig.setText(String.valueOf(value));
                    break;
                case "defense":
                    defenseBig.setText(String.valueOf(value));
                    break;
                case "special-attack":
                    specialAttack.setText(String.valueOf(value));
                    break;
                case "special-defense":
                    specialDefense.setText(String.valueOf(value));
                    break;
                case "speed":
                    speed.setText(String.valueOf(value));
                    break;
            }
        }
    }

    @Override
    public void onPokemonClick(Pokemon pokemon) {
        Intent intent = new Intent(this, PokemonDetailsActivity.class);
        intent.putExtra(POKEMON_ID_EXTRA, pokemon.getId());
        startActivity(intent);
        finish();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase(Locale.ROOT) + str.substring(1).toLowerCase(Locale.ROOT);
    }

    private String getPokemonGeneration(int id) {
        if (id >= 1 && id <= 151) return "Kanto";
        if (id >= 152 && id <= 251) return "Johto";
        if (id >= 252 && id <= 386) return "Hoenn";
        if (id >= 387 && id <= 493) return "Sinnoh";
        if (id >= 494 && id <= 649) return "Unova";
        if (id >= 650 && id <= 721) return "Kalos";
        if (id >= 722 && id <= 809) return "Alola";
        if (id >= 810 && id <= 905) return "Galar";
        if (id >= 906 && id <= 1025) return "Paldea";
        return "Desconhecida";
    }

    private void loadRelatedPokemonsWithGemini(String pokemonName, String pokemonTypes) {
        try {
            GenerativeModel geminiModel = new GenerativeModel("gemini-2.5-flash", "");

            GenerativeModelFutures model = GenerativeModelFutures.from(geminiModel);

            String prompt = String.format(
                    "Gere uma descrição atraente entre aspas     de aproximadamente 2 linhas de mobile na fonte 12sp em português para o Pokémon %s (Tipo: %s), e retorne APENAS um JSON válido contendo:\n" +
                            "1. Uma 'description' (string) com essa descrição.\n" +
                            "2. Uma 'evolutionChain' (array de {name, id}) com a cadeia de evolução completa.\n" +
                            "3. Uma 'similarPokemons' (array de {name, id}) com 4 pokemons parecidos (com nome em minúsculo e ID).\n\n" +
                            "Exemplo de formato: {\"description\":\"Sua descrição...\", \"evolutionChain\":[{\"name\":\"pikachu\",\"id\":25}], \"similarPokemons\":[{\"name\":\"raichu\",\"id\":26}]}",
                    pokemonName, pokemonTypes
            );

            Content content = new Content.Builder().addText(prompt).build();

            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    GenerateContentResponse response = model.generateContent(content).get();

                    String jsonResponse = response.getText();

                    if (jsonResponse != null && !jsonResponse.trim().isEmpty()) {
                        runOnUiThread(() -> processGeminiResponse(jsonResponse.trim()));
                    } else {
                        Log.w("Gemini", "Resposta vazia ou nula");
                    }

                } catch (Exception e) {
                    Log.e("Gemini", "Erro ao gerar resposta do Gemini: " + e.getMessage());
                    e.printStackTrace();
                    runOnUiThread(() -> loadFallbackData());
                }
            });

        } catch (Exception e) {
            Log.e("Gemini", "Erro ao inicializar Gemini: " + e.getMessage());
        }
    }

    private void processGeminiResponse(String jsonResponse) {
        try {
            String cleanJson = jsonResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            Log.d("Gemini", "JSON recebido: " + cleanJson);

            GeminiResponse geminiResponse = gson.fromJson(cleanJson, GeminiResponse.class);

            if (geminiResponse != null) {
                if (geminiResponse.description != null && !geminiResponse.description.isEmpty()) {
                    descriptionBig.setText(geminiResponse.description);
                    Log.d("Gemini", "Descrição carregada: " + geminiResponse.description.substring(0, Math.min(geminiResponse.description.length(), 30)) + "...");
                }

                loadFullPokemonDetailsForList(
                        geminiResponse.evolutionChain,
                        evolutionAdapter,
                        loadedEvolutionPokemons
                );

                loadFullPokemonDetailsForList(
                        geminiResponse.similarPokemons,
                        similarAdapter,
                        loadedSimilarPokemons
                );

            }
        } catch (Exception e) {
            Log.e("Gemini", "Erro ao processar JSON: " + e.getMessage());
            Log.e("Gemini", "JSON problemático: " + jsonResponse);
            loadFallbackData();
        }
    }

    private List<Pokemon> convertToPokemonList(List<GeminiPokemon> geminiPokemons) {
        List<Pokemon> pokemons = new ArrayList<>();

        for (GeminiPokemon geminiPokemon : geminiPokemons) {
            if (geminiPokemon.id > 0 && geminiPokemon.id <= 1025) {
                Pokemon pokemon = new Pokemon();
                pokemon.setName(geminiPokemon.name.toLowerCase());
                pokemon.setUrl("https://pokeapi.co/api/v2/pokemon/" + geminiPokemon.id + "/");
                pokemons.add(pokemon);
            }
        }

        return pokemons;
    }
    private static class GeminiResponse {
        String description;
        List<GeminiPokemon> evolutionChain;
        List<GeminiPokemon> similarPokemons;
    }

    private static class GeminiPokemon {
        String name;
        int id;
    }

    private void loadFallbackData() {
        Log.d("Gemini", "Carregando dados de fallback para teste");
        String fallbackJson = "{\"evolutionChain\":[{\"name\":\"pichu\",\"id\":172},{\"name\":\"pikachu\",\"id\":25},{\"name\":\"raichu\",\"id\":26}], \"similarPokemons\":[{\"name\":\"plusle\",\"id\":311},{\"name\":\"minun\",\"id\":312},{\"name\":\"pachirisu\",\"id\":417}]}";
        processGeminiResponse(fallbackJson);
    }

    private void loadFullPokemonDetailsForList(
            List<GeminiPokemon> geminiPokemons,
            PokemonAdapter adapter,
            List<Pokemon> targetList) {

        targetList.clear();

        final int totalPokemons = geminiPokemons.size();
        if (totalPokemons == 0) {
            runOnUiThread(() -> adapter.updatePokemons(targetList));
            return;
        }

        final int[] loadedCount = {0};

        for (GeminiPokemon geminiPokemon : geminiPokemons) {
            if (geminiPokemon.id <= 0) continue;

            pokeApi.getPokemonDetails(geminiPokemon.id).enqueue(new Callback<PokemonDetails>() {
                @Override
                public void onResponse(@NonNull Call<PokemonDetails> call, @NonNull Response<PokemonDetails> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        PokemonDetails details = response.body();

                        Pokemon displayPokemon = convertDetailsToDisplayPokemon(details);
                        targetList.add(displayPokemon);
                    }

                    if (++loadedCount[0] == totalPokemons) {
                        runOnUiThread(() -> {
                            targetList.sort((p1, p2) -> Integer.compare(p1.getId(), p2.getId()));
                            adapter.updatePokemons(targetList);
                        });
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PokemonDetails> call, @NonNull Throwable t) {
                    Log.e("LoadFull", "Falha ao carregar detalhe (ID: " + geminiPokemon.id + "): " + t.getMessage());

                    if (++loadedCount[0] == totalPokemons) {
                        runOnUiThread(() -> {
                            targetList.sort((p1, p2) -> Integer.compare(p1.getId(), p2.getId()));
                            adapter.updatePokemons(targetList);
                        });
                    }
                }
            });
        }
    }

    private Pokemon convertDetailsToDisplayPokemon(PokemonDetails details) {
        Pokemon pokemon = new Pokemon();

        pokemon.setId(details.getId());
        pokemon.setName(details.getName());
        pokemon.setUrl("https://pokeapi.co/api/v2/pokemon/" + details.getId() + "/");

        pokemon.setGeneration(getPokemonGeneration(details.getId()));
        pokemon.setTypes(extractTypes(details.getTypes()));
        extractStats(details.getStats(), pokemon);
        return pokemon;
    }

    private List<String> extractTypes(List<PokemonDetails.Type> types) {
        List<String> typeNames = new ArrayList<>();
        for (PokemonDetails.Type type : types) {
            typeNames.add(capitalize(type.getType().getName()));
        }
        return typeNames;
    }

    private void extractStats(List<PokemonDetails.Stat> stats, Pokemon pokemon) {
        for (PokemonDetails.Stat stat : stats) {
            String name = stat.getStat().getName();
            int value = stat.getBaseStat();

            switch (name) {
                case "hp":
                    pokemon.setHp(value);
                    break;
                case "attack":
                    pokemon.setAttack(value);
                    break;
                case "defense":
                    pokemon.setDefense(value);
                    break;
            }
        }
    }
}