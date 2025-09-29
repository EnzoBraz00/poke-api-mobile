package com.mobile.pokedex.model;

import java.util.List;

public class Gemini {
    public static class GeminiPokemonSuggestion {
        private String name;
        private int id;

        public GeminiPokemonSuggestion(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
    public static class GeminiResponseContainer {
        String description;
        private List<GeminiPokemonSuggestion> evolutionChain;
        private List<GeminiPokemonSuggestion> similarPokemons;

        public GeminiResponseContainer(String description, List<GeminiPokemonSuggestion> evolutionChain, List<GeminiPokemonSuggestion> similarPokemons) {
            this.description = description;
            this.evolutionChain = evolutionChain;
            this.similarPokemons = similarPokemons;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<GeminiPokemonSuggestion> getEvolutionChain() {
            return evolutionChain;
        }

        public void setEvolutionChain(List<GeminiPokemonSuggestion> evolutionChain) {
            this.evolutionChain = evolutionChain;
        }

        public List<GeminiPokemonSuggestion> getSimilarPokemons() {
            return similarPokemons;
        }

        public void setSimilarPokemons(List<GeminiPokemonSuggestion> similarPokemons) {
            this.similarPokemons = similarPokemons;
        }
    }
}
