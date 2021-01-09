package com.jasperg.edge.controller;

import com.jasperg.edge.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/cookbook/recipes")
    public List<RecipeComplete> getAllRecipes(){

        List<RecipeComplete> returnList= new ArrayList();

        ResponseEntity<List<Recipe>> responseEntityRecipes =
                restTemplate.exchange(
                        "http://" + recipeServiceBaseUrl + "/recipes",
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<Recipe>>() {});

        List<Recipe> recipes = responseEntityRecipes.getBody();

        for (Recipe recipe: recipes) {

            User user = getUserByUserCode(recipe.getUserCode());

            List<Ingredient> ingredients = getIngredientsByRecipeCode(recipe.getCode());

            returnList.add(new RecipeComplete(recipe, user, ingredients));
        }

        return returnList;
    }

    @GetMapping("/cookbook/recipe/name/{name}")
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

    @GetMapping("/cookbook/user/code/{code}")
    public List<RecipeComplete> getRecipesByUserCode(@PathVariable String code){

        List<RecipeComplete> returnList= new ArrayList();

        User user = getUserByUserCode(code);

        ResponseEntity<List<Recipe>> responseEntityRecipes =
                restTemplate.exchange(
                        "http://" + recipeServiceBaseUrl + "/recipes/userCode/{userCode}",
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<Recipe>>() {},
                        user.getCode());

        List<Recipe> recipes = responseEntityRecipes.getBody();

        for (Recipe recipe: recipes) {

            List<Ingredient> ingredients = getIngredientsByRecipeCode(recipe.getCode());

            returnList.add(new RecipeComplete(recipe, user, ingredients));
        }

        return returnList;
    }

    @GetMapping("/cookbook/ingredient/name/{name}")
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

            returnList.add(new RecipeComplete(recipe, user, recipeIngredients));
        }

        return returnList;
    }

    @GetMapping("/cookbook/recipe/code/{code}")
    public RecipeComplete getRecipeByRecipeCode(@PathVariable String code){

        Recipe recipe = restTemplate.getForObject(
                "http://" + recipeServiceBaseUrl + "/recipes/code/{code}",
                Recipe.class, code);

        List<Ingredient> ingredients = getIngredientsByRecipeCode(recipe.getCode());
        User user = getUserByUserCode(recipe.getUserCode());

        RecipeComplete recipeComplete = new RecipeComplete(recipe, user, ingredients);

        return recipeComplete;
    }

    @PostMapping("/cookbook")
    public RecipeComplete addRecipeComplete(@RequestParam String name,
                                            @RequestParam int cookingTime,
                                            @RequestParam String description,
                                            @RequestParam String userCode,
                                            @RequestParam List<String> recipeIngredients){

        User user = getUserByUserCode(userCode);

        Recipe recipe = restTemplate.postForObject("http://" + recipeServiceBaseUrl + "/recipes",
                new Recipe(name, cookingTime, description, userCode), Recipe.class);

        List<Ingredient> ingredients = new ArrayList<>();

        for (String recipeIngredient: recipeIngredients) {
            Ingredient ingredient = restTemplate.postForObject("http://" + ingredientServiceBaseUrl + "/ingredients",
                    new Ingredient(
                            recipeIngredient.substring(0, recipeIngredient.indexOf('-')),
                            Integer.parseInt(recipeIngredient.substring(recipeIngredient.indexOf('-'))),
                            recipe.getCode()
                    ), Ingredient.class);
            ingredients.add(ingredient);
        }

        return new RecipeComplete(recipe, user, ingredients);
    }

    @PostMapping("/cookbook/user")
    public UserNoId addUser(@RequestParam String firstName, @RequestParam String lastName, @RequestParam String email){

        User user = restTemplate.postForObject("http://" + userServiceBaseUrl + "/users",
                new User(firstName, lastName, email), User.class);

        return new UserNoId(user.getFirstName(), user.getLastName(), user.getEmail(), user.getCode());
    }

    @PutMapping("/cookbook")
    public RecipeComplete editRecipeComplete(@RequestParam String recipeCode,
                                            @RequestParam String recipeName,
                                            @RequestParam int cookingTime,
                                            @RequestParam String recipeDescription){

        Recipe recipe = restTemplate.getForObject("http://" + recipeServiceBaseUrl + "/recipes/code/" + recipeCode,
                Recipe.class);

        recipe.setName(recipeName);
        recipe.setCookingTime(cookingTime);
        recipe.setDescription(recipeDescription);

        ResponseEntity<Recipe> responseEntityRecipe =
                restTemplate.exchange("http://" + recipeServiceBaseUrl + "/recipes",
                        HttpMethod.PUT, new HttpEntity<>(recipe), Recipe.class);

        Recipe retrievedRecipe = responseEntityRecipe.getBody();

        User user = getUserByUserCode(recipe.getUserCode());

        List<Ingredient> ingredients = getIngredientsByRecipeCode(recipe.getCode());

        return new RecipeComplete(retrievedRecipe, user, ingredients);
    }

    @DeleteMapping("/cookbook/recipe/{code}")
    public ResponseEntity deleteRanking(@PathVariable String code){

        List<Ingredient> ingredients = getIngredientsByRecipeCode(code);

        for (Ingredient ingredient:
             ingredients) {
            restTemplate.delete("http://" + ingredientServiceBaseUrl + "/ingredients/" + ingredient.getCode());
        }

        restTemplate.delete("http://" + recipeServiceBaseUrl + "/recipes/" + code);

        return ResponseEntity.ok().build();
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
