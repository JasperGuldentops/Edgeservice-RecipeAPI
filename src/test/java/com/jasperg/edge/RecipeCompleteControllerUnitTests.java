package com.jasperg.edge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasperg.edge.model.Ingredient;
import com.jasperg.edge.model.Recipe;
import com.jasperg.edge.model.RecipeIngredient;
import com.jasperg.edge.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RecipeCompleteControllerUnitTests {

    @Value("${userservice.baseurl}")
    private String userServiceBaseUrl;

    @Value("${recipeservice.baseurl}")
    private String recipeServiceBaseUrl;

    @Value("${ingredientservice.baseurl}")
    private String ingredientServiceBaseUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MockMvc mockMvc;

    private MockRestServiceServer mockServer;
    private ObjectMapper mapper = new ObjectMapper();

    private User user1 = new User("Jasper", "Guldentops",
            "jg@gmail.com", "jg@gmail.com-0000");
    private User user2 = new User("Andre", "Arboon",
            "aa@gmail.com", "aa@gmail.com-0000");

    private Recipe recipe1 = new Recipe("Pizza", 45, "Roll dough, bake, ready",
            "jg@gmail.com-0000", "Pizza-0000");
    private Recipe recipe2 = new Recipe("Fries", 30, "Slice potato, fry, ready",
            "aa@gmail.com-0000", "Fries-0000");

    private Ingredient ingredient1 = new Ingredient("Tomato", 2,
            "Pizza-0000", "0000");
    private Ingredient ingredient2 = new Ingredient("Potato", 5,
            "Fries-0000", "0000");

    private List<Recipe> recipe1List = Arrays.asList(recipe1);
    private List<Recipe> recipe2List = Arrays.asList(recipe2);
    private List <Recipe> allRecipes = Arrays.asList(recipe1, recipe2);

    private List<Ingredient> ingredient1List = Arrays.asList(ingredient1);
    private List<Ingredient> ingredient2List = Arrays.asList(ingredient2);
    private List<Ingredient> allIngredients = Arrays.asList(ingredient1, ingredient2);

    private List<User> user1List = Arrays.asList(user1);
    private List<User> user2List = Arrays.asList(user2);
    private List <User> allUsers = Arrays.asList(user1, user2);

    @BeforeEach
    public void initializeMockserver() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void whenGetCookbookByRecipe_thenReturnRecipesCompleteJson() throws Exception {

        // GET all recipes
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + recipeServiceBaseUrl + "/recipes")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(allRecipes))
                );

        // GET user1 info (jg)
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + userServiceBaseUrl + "/users/code/jg@gmail.com-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(user1))
                );

        // GET all ingredients from recipe 1
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + ingredientServiceBaseUrl + "/ingredients/recipeCode/Pizza-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(ingredient1List))
                );

        // GET user2 info (jg)
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + userServiceBaseUrl + "/users/code/aa@gmail.com-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(user2))
                );

        // GET all ingredients from recipe 2
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + ingredientServiceBaseUrl + "/ingredients/recipeCode/Fries-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(ingredient2List))
                );

        mockMvc.perform(get("/cookbook/recipes"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))

                .andExpect(jsonPath("$[0].recipeName", is("Pizza")))
                .andExpect(jsonPath("$[0].recipeDescription", is("Roll dough, bake, ready")))
                .andExpect(jsonPath("$[0].cookingTime", is("45 minuten")))
                .andExpect(jsonPath("$[0].creatorName", is("Jasper Guldentops")))
                .andExpect(jsonPath("$[0].recipeIngredients[0].name", is("Tomato")))
                .andExpect(jsonPath("$[0].recipeIngredients[0].amount", is(2)))

                .andExpect(jsonPath("$[1].recipeName", is("Fries")))
                .andExpect(jsonPath("$[1].recipeDescription", is("Slice potato, fry, ready")))
                .andExpect(jsonPath("$[1].cookingTime", is("30 minuten")))
                .andExpect(jsonPath("$[1].creatorName", is("Andre Arboon")))
                .andExpect(jsonPath("$[1].recipeIngredients[0].name", is("Potato")))
                .andExpect(jsonPath("$[1].recipeIngredients[0].amount", is(5)));
    }

    @Test
    public void whenGetCookbookByRecipeName_thenReturnRecipesCompleteJson() throws Exception {

        // GET all recipes that match the name (pizza)
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + recipeServiceBaseUrl + "/recipes/name/zz")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(recipe1List))
                );

        // GET user1 info (jg)
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + userServiceBaseUrl + "/users/code/jg@gmail.com-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(user1))
                );

        // GET all ingredients from pizza
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + ingredientServiceBaseUrl + "/ingredients/recipeCode/Pizza-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(ingredient1List))
                );

        mockMvc.perform(get("/cookbook/recipe/name/{name}", "zz"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))

                .andExpect(jsonPath("$[0].recipeName", is("Pizza")))
                .andExpect(jsonPath("$[0].recipeDescription", is("Roll dough, bake, ready")))
                .andExpect(jsonPath("$[0].cookingTime", is("45 minuten")))
                .andExpect(jsonPath("$[0].creatorName", is("Jasper Guldentops")))
                .andExpect(jsonPath("$[0].recipeIngredients[0].name", is("Tomato")))
                .andExpect(jsonPath("$[0].recipeIngredients[0].amount", is(2)));
    }

    @Test
    public void whenGetCookbookByUserCode_thenReturnRecipesCompleteJson() throws Exception {

        // GET user2 info from code (aa)
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + userServiceBaseUrl + "/users/code/aa@gmail.com-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(user2))
                );

        // GET all recipes from user
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + recipeServiceBaseUrl + "/recipes/userCode/aa@gmail.com-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(recipe2List))
                );

        // GET all ingredients from fries
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + ingredientServiceBaseUrl + "/ingredients/recipeCode/Fries-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(ingredient2List))
                );

        mockMvc.perform(get("/cookbook/user/code/{code}", "aa@gmail.com-0000"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))

                .andExpect(jsonPath("$[0].recipeName", is("Fries")))
                .andExpect(jsonPath("$[0].recipeDescription", is("Slice potato, fry, ready")))
                .andExpect(jsonPath("$[0].cookingTime", is("30 minuten")))
                .andExpect(jsonPath("$[0].creatorName", is("Andre Arboon")))
                .andExpect(jsonPath("$[0].recipeIngredients[0].name", is("Potato")))
                .andExpect(jsonPath("$[0].recipeIngredients[0].amount", is(5)));
    }

    @Test
    public void whenGetCookbookByIngredientName_thenReturnRecipesCompleteJson() throws Exception {

        // GET all ingredients from name
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + ingredientServiceBaseUrl + "/ingredients/name/tato")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(ingredient2List))
                );

        // GET all recipes from ingredients (only fries)
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + recipeServiceBaseUrl + "/recipes/code/Fries-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(recipe2))
                );

        // GET all ingredients from recipe
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + ingredientServiceBaseUrl + "/ingredients/recipeCode/Fries-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(ingredient2List))
                );

        // GET all user from recipe
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + userServiceBaseUrl + "/users/code/aa@gmail.com-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(user2))
                );
        mockMvc.perform(get("/cookbook/ingredient/name/{name}", "tato"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))

                .andExpect(jsonPath("$[0].recipeName", is("Fries")))
                .andExpect(jsonPath("$[0].recipeDescription", is("Slice potato, fry, ready")))
                .andExpect(jsonPath("$[0].cookingTime", is("30 minuten")))
                .andExpect(jsonPath("$[0].creatorName", is("Andre Arboon")))
                .andExpect(jsonPath("$[0].recipeIngredients[0].name", is("Potato")))
                .andExpect(jsonPath("$[0].recipeIngredients[0].amount", is(5)));
    }

    @Test
    public void whenGetCookbookByRecipyCode_thenReturnRecipeCompleteJson() throws Exception {

        // GET recipe2
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + recipeServiceBaseUrl + "/recipes/code/Fries-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(recipe2))
                );

        // GET all ingredients from fries
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + ingredientServiceBaseUrl + "/ingredients/recipeCode/Fries-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(ingredient2List))
                );

        // GET user2 from recipe
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + userServiceBaseUrl + "/users/code/aa@gmail.com-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(user2))
                );

        mockMvc.perform(get("/cookbook/recipe/code/{code}", "Fries-0000"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.recipeName", is("Fries")))
                .andExpect(jsonPath("$.recipeDescription", is("Slice potato, fry, ready")))
                .andExpect(jsonPath("$.cookingTime", is("30 minuten")))
                .andExpect(jsonPath("$.creatorName", is("Andre Arboon")))
                .andExpect(jsonPath("$.recipeIngredients[0].name", is("Potato")))
                .andExpect(jsonPath("$.recipeIngredients[0].amount", is(5)));
    }

    @Test
    public void whenAddCookbook_thenReturnRecipeCompleteJson() throws Exception {

        Recipe recipe3 = new Recipe("Soup", 120, "Boil water, add tomato, done",
                "jg@gmail.com-0000");

        RecipeIngredient recipeIngredient1 = new RecipeIngredient("Tomato", 5);
        RecipeIngredient recipeIngredient2 = new RecipeIngredient("Water", 500);

        List<RecipeIngredient> recipeIngredients = Arrays.asList(recipeIngredient1);

        // GET user2 from recipe
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + userServiceBaseUrl + "/users/code/jg@gmail.com-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(user1))
                );

        // POST recipe
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + recipeServiceBaseUrl + "/recipes")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(recipe3))
                );

        // POST ingredient 1
        Ingredient ingredient1 = new Ingredient(recipeIngredient1.getName(), recipeIngredient1.getAmount(),  recipe3.getCode());
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + ingredientServiceBaseUrl + "/ingredients")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(ingredient1))
                );

        // POST ingredient 2
        Ingredient ingredient2 = new Ingredient(recipeIngredient2.getName(), recipeIngredient2.getAmount(),  recipe3.getCode());
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + ingredientServiceBaseUrl + "/ingredients")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(ingredient2))
                );

        mockMvc.perform(post("/cookbook")
                .param("name", recipe3.getName())
                .param("cookingTime", recipe3.getCookingTime().toString())
                .param("description", recipe3.getDescription())
                .param("userCode", recipe3.getUserCode())
                .param("recipeIngredients", ingredient1.getName() + "-" + ingredient1.getAmount())
                .param("recipeIngredients", ingredient2.getName() + "-" + ingredient2.getAmount())

                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.recipeName", is("Soup")))
                .andExpect(jsonPath("$.recipeDescription", is("Boil water, add tomato, done")))
                .andExpect(jsonPath("$.cookingTime", is("2 uur")))
                .andExpect(jsonPath("$.creatorName", is("Jasper Guldentops")))
                .andExpect(jsonPath("$.recipeIngredients[0].name", is("Tomato")))
                .andExpect(jsonPath("$.recipeIngredients[0].amount", is(5)));
    }

    @Test
    public void whenUpdateCookbook_thenReturnCompletedRecipeJson() throws Exception {

        Recipe updatedRecipe = new Recipe("Pizza Bolognese", 60, "Roll dough, in oven, ready",
                "jg@gmail.com-0000", "Pizza-0000");

        // GET recipe to update
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + recipeServiceBaseUrl + "/recipes/code/Pizza-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(recipe1))
                );

        // PUT new updated recipe
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + recipeServiceBaseUrl + "/recipes")))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(updatedRecipe))
                );

        // GET user info
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + userServiceBaseUrl + "/users/code/jg@gmail.com-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(user1))
                );

        // GET all ingredients from pizza
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + ingredientServiceBaseUrl + "/ingredients/recipeCode/Pizza-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(ingredient1List))
                );

        mockMvc.perform(put("/cookbook")
                .param("recipeCode", updatedRecipe.getCode())
                .param("recipeName", updatedRecipe.getName())
                .param("cookingTime", updatedRecipe.getCookingTime().toString())
                .param("recipeDescription", updatedRecipe.getDescription())

                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.recipeName", is("Pizza Bolognese")))
                .andExpect(jsonPath("$.recipeDescription", is("Roll dough, in oven, ready")))
                .andExpect(jsonPath("$.cookingTime", is("1 uur")))
                .andExpect(jsonPath("$.creatorName", is("Jasper Guldentops")))
                .andExpect(jsonPath("$.recipeIngredients[0].name", is("Tomato")))
                .andExpect(jsonPath("$.recipeIngredients[0].amount", is(2)));

    }

    @Test
    public void whenDeleteRanking_thenReturnStatusOk() throws Exception {

        // GET ingredients to delete
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + ingredientServiceBaseUrl + "/ingredients/recipeCode/Pizza-0000")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(ingredient1List))
                );

        // DELETE any linked ingredients
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + ingredientServiceBaseUrl + "/ingredients/Pizza-0000-0000")))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.OK)
                );

        // DELETE recipe
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + recipeServiceBaseUrl + "/recipes/Pizza-0000")))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.OK)
                );

        mockMvc.perform(delete("/cookbook/recipe/{code}", "Pizza-0000"))
                .andExpect(status().isOk());
    }

}
