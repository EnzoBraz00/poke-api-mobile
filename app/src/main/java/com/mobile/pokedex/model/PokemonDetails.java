package com.mobile.pokedex.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PokemonDetails {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("height")
    private int height;
    @SerializedName("weight")
    private int weight;
    @SerializedName("stats")
    private List<Stat> stats;
    @SerializedName("types")
    private List<Type> types;
    @SerializedName("sprites")
    private Sprites sprites;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getHeight() {
        return height;
    }

    public int getWeight() {
        return weight;
    }

    public List<Stat> getStats() {
        return stats;
    }

    public List<Type> getTypes() {
        return types;
    }

    public Sprites getSprites() {
        return sprites;
    }

    public static class Stat {
        @SerializedName("base_stat")
        private int baseStat;
        @SerializedName("stat")
        private StatName stat;

        public int getBaseStat() {
            return baseStat;
        }

        public StatName getStat() {
            return stat;
        }
    }

    public static class StatName {
        @SerializedName("name")
        private String name;

        public String getName() {
            return name;
        }
    }

    public static class Type {
        @SerializedName("type")
        private TypeName type;

        public TypeName getType() {
            return type;
        }
    }

    public static class TypeName {
        @SerializedName("name")
        private String name;

        public String getName() {
            return name;
        }
    }

    public static class Sprites {
        @SerializedName("front_default")
        private String frontDefault;

        @SerializedName("other")
        private OtherSprites other;

        @SerializedName("cries")
        private Cries cries;

        public OtherSprites getOther() { return other; }
        public Cries getCries() { return cries; }
        public String getFrontDefault() { return frontDefault; }
    }

    public static class Cries {
        @SerializedName("latest")
        private String latest;

        public String getLatest() { return latest; }
    }

    public static class OtherSprites {
        @SerializedName("official-artwork")
        private OfficialArtwork officialArtwork;

        public OfficialArtwork getOfficialArtwork() { return officialArtwork; }
    }

    public static class OfficialArtwork {
        @SerializedName("front_default")
        private String frontDefault;

        @SerializedName("front_shiny")
        private String frontShiny;

        public String getFrontDefault() { return frontDefault; }
        public String getFrontShiny() { return frontShiny; }
    }
}
