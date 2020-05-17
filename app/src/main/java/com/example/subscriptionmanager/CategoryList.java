package com.example.subscriptionmanager;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CategoryList implements Serializable {
    private static ArrayList<Category> myCategories;

    public CategoryList() {
        // On initialization add category with all subscriptions to the category list
        if (myCategories == null) {
            myCategories = new ArrayList<Category>();
            SubscriptionList subscriptionList = new SubscriptionList();
            Category main = new Category("All", false);
            main.addList(subscriptionList.getMySubscriptions());
            myCategories.add(main);
        }
    }

    public void addCategory(Category category) {
        myCategories.add(category);
        removeListFromMain(category.getSubscriptions());
    }

    public void removeCategory(Category category) {
        myCategories.remove(category);
        addListToMain(category.getSubscriptions());
    }


    private void removeListFromMain(List<Subscription> subscriptions) {
        for (Subscription subscription: subscriptions) {
            removeFromMain(subscription);
        }
    }

    private void removeFromMain(Subscription subscription) {
        Category main = myCategories.get(0); // should be the main category
        main.remove(subscription);
    }

    private void addListToMain(List<Subscription> subscriptions) {
        for (Subscription subscription: subscriptions) {
            addToMain(subscription);
        }
    }

    private void addToMain(Subscription subscription) {
        Category main = myCategories.get(0);
        main.add(subscription);
    }

    public List<Subscription> getMainSubscriptions() {
        return myCategories.get(0).getSubscriptions();
    }

    public List<Category> getMyCategories() {
        return myCategories;
    }
}
