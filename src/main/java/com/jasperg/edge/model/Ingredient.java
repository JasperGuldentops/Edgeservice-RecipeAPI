package com.jasperg.edge.model;

public class Ingredient {

    private int id;

    private String name;

    private int amount;

    private String recipeCode;

    private String code;

    public Ingredient() {
    }

    public Ingredient(String name, int amount, String recipeCode) {
        this.name = name;
        this.amount = amount;
        this.recipeCode = recipeCode;
    }

    public Ingredient(String name, int amount, String recipeCode, String code) {
        this.name = name;
        this.amount = amount;
        this.recipeCode = recipeCode;
        this.code = recipeCode + "-" + code;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getRecipeCode() {
        return recipeCode;
    }

    public void setRecipeCode(String recipeCode) {
        this.recipeCode = recipeCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
