package com.example.subscriptionmanager;

import java.util.ArrayList;
import java.util.List;

// Currently a subscription can be a member of multiple categories, should change this
// Maybe have a main category (All) that contains all subscriptions not assigned to a category
// CategoryList would manage all of these
public class Category {
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
        myCategorySubscriptions.add(subscription);
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
