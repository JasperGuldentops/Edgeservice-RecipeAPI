package com.jasperg.edge.model;

import com.jasperg.edge.helper.Helper;

import java.util.ArrayList;
import java.util.List;

public class RecipeComplete {

    private String recipeName;
    private String recipeDescription;
    private String cookingTime;
    private String creatorName;
    private List<RecipeIngredient> recipeIngredients;

    public RecipeComplete(Recipe recipe, User user, List<Ingredient> ingredients) {
        this.recipeName = recipe.getName();
        this.recipeDescription = recipe.getDescription();
        this.cookingTime = Helper.intToTimeString(recipe.getCookingTime());
        this.creatorName = user.getFirstName() + " " + user.getLastName();
        recipeIngredients = new ArrayList<>();
        ingredients.forEach(ingredient -> {
            recipeIngredients.add(new RecipeIngredient(ingredient.getName(), ingredient.getAmount()));
        });
        setRecipeIngredients(recipeIngredients);
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getRecipeDescription() {
        return recipeDescription;
    }

    public void setRecipeDescription(String recipeDescription) {
        this.recipeDescription = recipeDescription;
    }

    public String getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(String cookingTime) {
        this.cookingTime = cookingTime;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public List<RecipeIngredient> getRecipeIngredients() {
        return recipeIngredients;
    }

    public void setRecipeIngredients(List<RecipeIngredient> recipeIngredients) {
        this.recipeIngredients = recipeIngredients;
    }
}
