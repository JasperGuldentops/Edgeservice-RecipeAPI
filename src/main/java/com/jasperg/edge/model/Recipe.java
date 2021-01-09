package com.jasperg.edge.model;

public class Recipe {

    private String id;
    private String name;
    private int cookingTime;
    private String description;
    private String userCode;
    private String code;

    public Recipe() {
    }

    public Recipe(String name, int cookingTime, String description, String userCode) {
        this.name = name;
        this.cookingTime = cookingTime;
        this.description = description;
        this.userCode = userCode;
    }

    public Recipe(String name, int cookingTime, String description, String userCode, String code) {
        this.name = name;
        this.cookingTime = cookingTime;
        this.description = description;
        this.userCode = userCode;
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(Integer cookingTime) {
        this.cookingTime = cookingTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
