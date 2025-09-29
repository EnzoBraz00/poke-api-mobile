package com.mobile.pokedex.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mobile.pokedex.R;
import com.mobile.pokedex.api.PokeApi;
import com.mobile.pokedex.listener.OnPokemonClickListener;
import com.mobile.pokedex.model.Pokemon;
import com.mobile.pokedex.model.PokemonDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder> {

    private final List<Pokemon> pokemonList;
    private final OnPokemonClickListener listener;
    private final PokeApi pokeApi;

    public PokemonAdapter(List<Pokemon> pokemonList, OnPokemonClickListener listener, PokeApi pokeApi) {
        this.pokemonList = pokemonList;
        this.listener = listener;
        this.pokeApi = pokeApi;
    }

    public PokemonAdapter(List<Pokemon> pokemonList, OnPokemonClickListener listener) {
        this(pokemonList, listener, null);
    }

    @NonNull
    @Override
    public PokemonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pokemon_item, parent, false);
        return new PokemonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PokemonViewHolder holder, int position) {
        Pokemon pokemon = pokemonList.get(position);
        Context context = holder.itemView.getContext();

        holder.nameTextView.setText(capitalize(pokemon.getName()));

        String generation = (pokemon.getGeneration() != null && !pokemon.getGeneration().isEmpty()) ? pokemon.getGeneration() : "???";
        holder.pokeIdTextView.setText(String.format("#%d - %s", pokemon.getId(), generation));

        String imageUrl = String.format(
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/%d.png",
                pokemon.getId()
        );
        Glide.with(context).load(imageUrl).into(holder.pokemonImageView);

        if (pokeApi != null && pokemon.getHp() == 0) {
            holder.lifeTextView.setText("...");
            holder.attackTextView.setText("...");
            holder.defenseTextView.setText("...");
            hideTypeContainers(holder);

            loadPokemonDetailsForMainList(holder, pokemon, position);

        } else {
            holder.lifeTextView.setText(String.valueOf(pokemon.getHp()));
            holder.attackTextView.setText(String.valueOf(pokemon.getAttack()));
            holder.defenseTextView.setText(String.valueOf(pokemon.getDefense()));

            List<String> types = pokemon.getTypes();
            hideTypeContainers(holder);
            if (types != null && !types.isEmpty()) {
                applyTypeData(holder.pokemonType1, holder.typeContainer1, types.get(0), context);
                if (types.size() > 1) {
                    applyTypeData(holder.pokemonType2, holder.typeContainer2, types.get(1), context);
                }
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onPokemonClick(pokemon));
    }

    @Override
    public int getItemCount() {
        return pokemonList.size();
    }

    public void updatePokemons(List<Pokemon> newPokemons) {
        pokemonList.clear();
        pokemonList.addAll(newPokemons);
        notifyDataSetChanged();
    }

    private void hideTypeContainers(PokemonViewHolder holder) {
        holder.typeContainer1.setVisibility(View.GONE);
        holder.typeContainer2.setVisibility(View.GONE);
    }
    private void loadPokemonDetailsForMainList(PokemonViewHolder holder, Pokemon pokemon, int position) {
        if (pokeApi == null) return;
        holder.itemView.setTag(position);

        pokeApi.getPokemonDetails(pokemon.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PokemonDetails> call, @NonNull Response<PokemonDetails> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PokemonDetails details = response.body();

                    updatePokemonModelWithDetails(pokemon, details);

                    if (holder.itemView.getTag() != null && (int) holder.itemView.getTag() == position) {

                        holder.lifeTextView.setText(String.valueOf(pokemon.getHp()));
                        holder.attackTextView.setText(String.valueOf(pokemon.getAttack()));
                        holder.defenseTextView.setText(String.valueOf(pokemon.getDefense()));

                        List<String> types = pokemon.getTypes();
                        Context context = holder.itemView.getContext();
                        hideTypeContainers(holder);
                        if (types != null && !types.isEmpty()) {
                            applyTypeData(holder.pokemonType1, holder.typeContainer1, types.get(0), context);
                            if (types.size() > 1) {
                                applyTypeData(holder.pokemonType2, holder.typeContainer2, types.get(1), context);
                            }
                        }

                        String generation = pokemon.getGeneration() != null ? pokemon.getGeneration() : "???";
                        holder.pokeIdTextView.setText(String.format("#%d - %s", pokemon.getId(), generation));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PokemonDetails> call, @NonNull Throwable t) {
                holder.lifeTextView.setText("?");
                holder.attackTextView.setText("?");
                holder.defenseTextView.setText("?");
            }
        });
    }

    private void updatePokemonModelWithDetails(Pokemon pokemon, PokemonDetails details) {
        // 1. Stats
        for (PokemonDetails.Stat stat : details.getStats()) {
            int value = stat.getBaseStat();
            switch (stat.getStat().getName()) {
                case "hp": pokemon.setHp(value); break;
                case "attack": pokemon.setAttack(value); break;
                case "defense": pokemon.setDefense(value); break;
            }
        }
        pokemon.setTypes(extractTypes(details.getTypes()));
        pokemon.setGeneration(getPokemonGeneration(details.getId()));
    }

    private List<String> extractTypes(List<PokemonDetails.Type> types) {
        List<String> typeNames = new ArrayList<>();
        for (PokemonDetails.Type type : types) {
            typeNames.add(capitalize(type.getType().getName()));
        }
        return typeNames;
    }
    private void applyTypeData(TextView textView, ConstraintLayout container, String typeName, Context context) {
        textView.setText(typeName);
        int colorResId = getTypeColor(typeName.toLowerCase(Locale.ROOT));
        int color = ContextCompat.getColor(context, colorResId);

        container.setBackgroundTintList(ColorStateList.valueOf(color));
        container.setVisibility(View.VISIBLE);
    }
    private int getTypeColor(String typeName) {
        switch (typeName.toLowerCase(Locale.ROOT)) {
            case "fire": return R.color.type_fire; case "water": return R.color.type_water;
            case "grass": return R.color.type_grass; case "electric": return R.color.type_electric; case "ice": return R.color.type_ice;
            case "fighting": return R.color.type_fighting; case "poison": return R.color.type_poison; case "ground": return R.color.type_ground;
            case "flying": return R.color.type_flying; case "psychic": return R.color.type_psychic; case "bug": return R.color.type_bug;
            case "rock": return R.color.type_rock; case "ghost": return R.color.type_ghost; case "dragon": return R.color.type_dragon;
            case "steel": return R.color.type_steel; case "dark": return R.color.type_dark; case "fairy": return R.color.type_fairy;
            default: return R.color.type_normal;
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0,1).toUpperCase(Locale.ROOT) + str.substring(1).toLowerCase(Locale.ROOT);
    }

    public static class PokemonViewHolder extends RecyclerView.ViewHolder {
        final TextView pokeIdTextView, nameTextView, lifeTextView, attackTextView, defenseTextView;
        final ImageView pokemonImageView;
        final ConstraintLayout typeContainer1, typeContainer2;
        final TextView pokemonType1, pokemonType2;

        public PokemonViewHolder(@NonNull View itemView) {
            super(itemView);
            pokeIdTextView = itemView.findViewById(R.id.pokeId);
            nameTextView = itemView.findViewById(R.id.name);
            pokemonImageView = itemView.findViewById(R.id.imgFoto);
            lifeTextView = itemView.findViewById(R.id.pokemonLife);
            attackTextView = itemView.findViewById(R.id.pokemonAttack);
            defenseTextView = itemView.findViewById(R.id.pokemonDefense);
            typeContainer1 = itemView.findViewById(R.id.typeContainer1);
            pokemonType1 = itemView.findViewById(R.id.pokemonType1);
            typeContainer2 = itemView.findViewById(R.id.typeContainer2);
            pokemonType2 = itemView.findViewById(R.id.pokemonType2);
        }
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

}