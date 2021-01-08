package com.jasperg.edge.controller;

import com.jasperg.edge.model.Ingredient;
import com.jasperg.edge.model.Recipe;
import com.jasperg.edge.model.RecipeComplete;
import com.jasperg.edge.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RecipeCompleteController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${userservice.baseurl}")
    private String userServiceBaseUrl;

    @Value("${recipeservice.baseurl}")
    private String recipeServiceBaseUrl;

    @Value("${ingredientservice.baseurl}")
    private String ingredientServiceBaseUrl;

    @GetMapping("/recipes/recipe/name/{name}")
    public List<RecipeComplete> getRecipesByRecipeName(@PathVariable String name){

        List<RecipeComplete> returnList= new ArrayList();

        ResponseEntity<List<Recipe>> responseEntityRecipes =
                restTemplate.exchange(
                        "http://" + recipeServiceBaseUrl + "/recipes/name/{name}",
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<Recipe>>() {},
                        name);

        List<Recipe> recipes = responseEntityRecipes.getBody();

        for (Recipe recipe: recipes) {

            User user = getUserByUserCode(recipe.getUserCode());

            List<Ingredient> ingredients = getIngredientsByRecipeCode(recipe.getCode());

            returnList.add(new RecipeComplete(recipe, user, ingredients));
        }

        return returnList;
    }

    @GetMapping("/recipes/user/code/{code}")
    public List<RecipeComplete> getRecipesByUserCode(@PathVariable String code){

        List<RecipeComplete> returnList= new ArrayList();

        User user = getUserByUserCode(code);

        ResponseEntity<List<Recipe>> responseEntityRecipes =
                restTemplate.exchange(
                        "http://" + recipeServiceBaseUrl + "/recipes/usercode/{userCode}",
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<Recipe>>() {},
                        user.getCode());

        List<Recipe> recipes = responseEntityRecipes.getBody();

        for (Recipe recipe: recipes) {

            List<Ingredient> ingredients = getIngredientsByRecipeCode(recipe.getCode());

            returnList.add(new RecipeComplete(recipe, user, ingredients));
        }

        return returnList;
    }

    @GetMapping("/recipes/ingredient/name/{name}")
    public List<RecipeComplete> getRecipesByIngredient(@PathVariable String name){

        List<RecipeComplete> returnList= new ArrayList();

        ResponseEntity<List<Ingredient>> responseEntityIngredients =
                restTemplate.exchange(
                        "http://" + ingredientServiceBaseUrl + "/ingredients/name/{name}",
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<Ingredient>>() {},
                        name);

        List<Ingredient> ingredients = responseEntityIngredients.getBody();

        //Get all unique recipe codes from the ingredients
        List<String> recipeCodes = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            if(!recipeCodes.contains(ingredient.getRecipeCode())) {
                recipeCodes.add(ingredient.getRecipeCode());
            }
        }

        //Loop through codes, get recipe, user and ingredients
        for (String recipeCode : recipeCodes) {
            Recipe recipe = restTemplate.getForObject(
                    "http://" + recipeServiceBaseUrl + "/recipes/code/{code}",
                    Recipe.class, recipeCode);

            List<Ingredient> recipeIngredients = getIngredientsByRecipeCode(recipe.getCode());
            User user = getUserByUserCode(recipe.getUserCode());

            returnList.add(new RecipeComplete(recipe, user, ingredients));
        }

        return returnList;
    }

    @GetMapping("/recipes/recipe/code/{code}")
    public RecipeComplete getRecipesByRecipeCode(@PathVariable String code){

        Recipe recipe = restTemplate.getForObject(
                "http://" + recipeServiceBaseUrl + "/recipes/code/{code}",
                Recipe.class, code);

        List<Ingredient> ingredients = getIngredientsByRecipeCode(recipe.getCode());
        User user = getUserByUserCode(recipe.getUserCode());

        RecipeComplete recipeComplete = new RecipeComplete(recipe, user, ingredients);

        return recipeComplete;
    }

    private List<Ingredient> getIngredientsByRecipeCode(String recipeCode) {

        ResponseEntity<List<Ingredient>> responseEntityIngredients =
                restTemplate.exchange(
                        "http://" + ingredientServiceBaseUrl + "/ingredients/recipeCode/{recipeCode}",
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<Ingredient>>() {},
                        recipeCode);

        return responseEntityIngredients.getBody();
    }

    private User getUserByUserCode(String userCode) {

        User user = restTemplate.getForObject(
                "http://" + userServiceBaseUrl + "/users/code/{code}",
                User.class, userCode);

        return user;
    }
}
