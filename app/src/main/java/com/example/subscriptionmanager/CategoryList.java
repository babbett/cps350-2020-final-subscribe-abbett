package com.example.subscriptionmanager;

import android.util.Log;
import android.widget.ProgressBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CategoryList implements Serializable {
    private static ArrayList<Category> myCategories;
    private static HashSet<String> allSubscriptionsIds;

    public CategoryList() {
        // On initialization add category with all subscriptions to the category list
        if (myCategories == null || myCategories.size() == 0 ) {
            myCategories = new ArrayList<Category>();
            SubscriptionList subscriptionList = new SubscriptionList();
            Category main = new Category("Uncategorized", false);
            main.addList(subscriptionList.getMySubscriptions());
            myCategories.add(main);
            Log.d("CategoryList", "Add uncategorized in constructor, size: " + main.getSubscriptions().size());
        }
    }

    public void addCategoryListFromLoad(List<Category> categoryList) {
        // can remove the 1 default category, because we are loading new ones
        if (categoryList == null) {
            Log.d("CategoryList", "addCategoryFromLoad: Resetting categorylist");
            myCategories = new ArrayList<Category>();
            Category main = new Category("Uncategorized", false);
            SubscriptionList subscriptionList = new SubscriptionList();
            subscriptionList.addSubscriptionList(null);
            main.addList(subscriptionList.getMySubscriptions());
            myCategories.add(main);
            return;
        }

        myCategories.remove(0);
        for (Category category: categoryList) {
            addCategoryFromLoad(category);
        }
    }

    public boolean isAlreadyInSomeCategory(Subscription subscription) {
        boolean result = allSubscriptionsIds.add(subscription.getImageUrl());
        return result;
    }

    private void addCategoryFromLoad(Category category) {
        myCategories.add(category);
        Log.d("CategoryList", "added " + category.getTitle() + " with " + category.getSubscriptions().size() + " from load in position " + myCategories.indexOf(category));
    }

    public void updateCategories(List<Subscription> subscriptionsFromAPI) {
        HashSet<String> apiSubscription = new HashSet<>(); // the subscriptions found by the api
        HashSet<String> allSubscription = new HashSet<>(); // the subscriptions found by loading data

        for (Subscription subscriptionApi: subscriptionsFromAPI) {
            apiSubscription.add(subscriptionApi.getImageUrl());
        }

        // Check for removals
        // Had to do it this non-foreach way because foreach uses iterators, and Java didn't like that
        // i was modifying what was being iterated on, which is fair
        for (int ii = 0; ii < myCategories.size(); ii++) {
            Category category = myCategories.get(ii);
            List<Subscription> currSubs = category.getSubscriptions();
            for (int jj = 0; jj < currSubs.size(); jj++) {
                Subscription subscription = currSubs.get(jj);
                if (!apiSubscription.contains(subscription.getImageUrl())) {
                    category.remove(subscription);
                    Log.d("CategoryList", "Removed " + subscription.getTitle() + " from " + category.getTitle());
                } else {
                    allSubscription.add(subscription.getImageUrl());
                }
            }
        }

        // Check for additions
        for (Subscription subscription: subscriptionsFromAPI) {
            if (!allSubscription.contains(subscription.getImageUrl())) {
                addToMain(subscription);
//                Log.d("CategoryList", "Adding new subscription " + subscription.getTitle());
            }
        }
    }

    public void addCategory(Category category) {
        myCategories.add(category);
        removeListFromMain(category.getSubscriptions());
        Log.d("CategoryList", "added " + category.getTitle() + " with " + category.getSubscriptions().size() + " in position " + myCategories.indexOf(category));
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
