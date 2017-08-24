package com.example.childsugar.nfc_app_2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kirill on 5/12/17.
 */

public class FoodData {
    private String name;
    private int imageResource;
    private String amount;
    boolean checked = false;
    int foodType = 0; // Default - 0; // Liquid 1
    private List<String> amountCategories = new ArrayList<String>();
    private String[] defaultListFood= {"20gr", "50gr", "70gr", "90gr", "100gr", "120gr"};
    private String[] defaultListLiquied= {"20ml", "50ml", "70ml", "90ml", "100ml", "200ml"};

    public FoodData(String name, int imageResource, int foodType) {
        this.name = name;
        this.imageResource = imageResource;
        this.foodType = foodType;
    }

    public FoodData(String name, int imageResource, String amount) {
        this.name = name;
        this.imageResource = imageResource;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
        if (amountCategories.contains(amount)) {
            amountCategories.remove(amount);
        }
        amountCategories.add(0, amount);
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }

    public List<String> getAmountCategories() {
        if (foodType == 0) {
            for (int i = 0; i < defaultListFood.length; i++) {
                if (!amountCategories.contains(defaultListFood[i])) {
                    amountCategories.add(defaultListFood[i]);
                }
            }
        } else if (foodType == 1){
            for (int i = 0; i < defaultListLiquied.length; i++) {
                if (!amountCategories.contains(defaultListLiquied[i])) {
                    amountCategories.add(defaultListLiquied[i]);
                }
            }
        }
        return amountCategories;
    }

    public void setAmountCategories(List<String> amountCategories) {
        this.amountCategories = amountCategories;
    }

    public int getFoodType() {
        return foodType;
    }

    public void setFoodType(int foodType) {
        this.foodType = foodType;
    }
}
