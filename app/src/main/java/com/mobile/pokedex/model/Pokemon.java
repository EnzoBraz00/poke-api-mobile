package com.mobile.pokedex.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Pokemon {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("url")
    private String url;

    private String generation;
    private List<String> types;
    private int hp;
    private int attack;
    private int defense;

    public Pokemon() {}
    public Pokemon(int id, String name) {
        this.id = id;
        this.name = name;
        this.url = "https://pokeapi.co/api/v2/pokemon/" + id + "/";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public int getId() {
        if (this.id == 0 && this.url != null) {
            String[] urlParts = url.split("/");
            if (urlParts.length > 0) {
                try {
                    this.id = Integer.parseInt(urlParts[urlParts.length - 1]);
                } catch (NumberFormatException ignored) {}
            }
        }
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGeneration() {
        return generation;
    }

    public void setGeneration(String generation) {
        this.generation = generation;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }
}