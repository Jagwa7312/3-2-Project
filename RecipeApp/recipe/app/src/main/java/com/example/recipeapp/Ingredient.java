package com.example.recipeapp;

public class Ingredient {
    private String category;
    private String name;
    private int num;

    public Ingredient(String category, String name, int num) {
        this.category = category;
        this.name = name;
        this.num = num;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}