package com.example.subscriptionmanager;

import android.util.Log;
import android.widget.ProgressBar;

import com.google.api.services.youtube.model.SubscriptionListResponse;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;


public class SubscriptionList {
    final String TAG = "SubscriptionList";

    private static ArrayList<Subscription> mySubscriptions = new ArrayList<Subscription>();

    public SubscriptionList() {
    }

    public void addSubscriptionList(SubscriptionListResponse subscriptionListResponse) {
        List<com.google.api.services.youtube.model.Subscription> subList = subscriptionListResponse.getItems();

        for (com.google.api.services.youtube.model.Subscription sub: subList) {
            String title = sub.getSnippet().getTitle();
            Log.d(TAG, "addSubscriptionList: " + sub.getSnippet());
            String url = sub.getSnippet().getThumbnails().getDefault().getUrl();
            Subscription newSub = new Subscription(title, url);
            addSubscription(newSub);
            Log.d(TAG, "addSubscriptionList: added " + title);
        }
    }

    public void addSubscription(Subscription subscription) {
        if(isAlreadyInSubscriptionList(subscription)) { return; }
        mySubscriptions.add(subscription);
    }

    private boolean isAlreadyInSubscriptionList(Subscription sub) {
        for (Subscription subscription: mySubscriptions) {
            if (subscription.getImageUrl().equals(sub.getImageUrl())) {
                Log.d("SubscriptionList", sub.getTitle() + " already in the mySubscriptions list");
                return true;
            }
        }
        Log.d("SubscriptionList", sub.getTitle() + " not in the mySubscriptions list");
        return false;
    }

    public ArrayList<Subscription> getMySubscriptions() {
        return mySubscriptions;
    }

    public ArrayList<String> getNamesList() {
        ArrayList<String> result = new ArrayList<>();
        for (Subscription sub: mySubscriptions) {
            result.add(sub.getTitle());
        }
        return result;
    }

    public ArrayList<Subscription> getSubscriptionsFromNames(ArrayList<String> nameList) {
        ArrayList<Subscription> result = new ArrayList<>();
        for (String name: nameList) {
            result.add(findByName(name));
        }
        return result;
    }

    private Subscription findByName(String name) {
        for (Subscription sub : mySubscriptions) {
            if (sub.getTitle() == name) {
                return sub;
            }
        }
        Log.d(TAG, "ERROR: " + name + " NOT FOUND");
        return null;
    }

    public static void setMySubscriptions(ArrayList<Subscription> mySubscriptions) {
        SubscriptionList.mySubscriptions = mySubscriptions;
    }
}
