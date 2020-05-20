package com.example.subscriptionmanager;

import android.util.Log;
import android.widget.ProgressBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Currently a subscription can be a member of multiple categories, should change this
// Maybe have a main category (All) that contains all subscriptions not assigned to a category
// CategoryList would manage all of these
public class Category implements Serializable {
    private String title; // The category's title
    private List<Subscription> myCategorySubscriptions; // the subscriptions in the category
    private Boolean notify; // True if this category should notify you of updates

    public Category(String title, Boolean notify) {
        this.title = title;
        this.notify = notify;
        myCategorySubscriptions = new ArrayList<>();
    }

    public List<Subscription> getSubscriptions() {
        return myCategorySubscriptions;
    }

    public void add(Subscription subscription) {
        // Make sure we are not adding the same subscription again
        if (alreadyInSubscriptionList(subscription)) { return; }
        myCategorySubscriptions.add(subscription);
    }

    private boolean alreadyInSubscriptionList(Subscription subscription) {
        for (Subscription sub: myCategorySubscriptions) {
            if (subscription.getImageUrl().equals(sub.getImageUrl())) {
//                Log.d("Category", subscription.getTitle() + " already in " + title);
                return true;
            }
//            Log.d("Category", subscription.getTitle() + " not in " + title);
        }
        return false;
    }

    public void remove(Subscription subscription) {
        myCategorySubscriptions.remove(subscription);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getNotify() {
        return notify;
    }

    public void setNotify(Boolean notify) {
        this.notify = notify;
    }

    public void addList(List<Subscription> subscriptions) {
        for (Subscription sub: subscriptions) {
            myCategorySubscriptions.add(sub);
        }
    }
}
