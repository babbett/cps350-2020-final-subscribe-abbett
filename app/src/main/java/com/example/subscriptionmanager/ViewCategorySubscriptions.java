package com.example.subscriptionmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

// This activity is called when you tap a category on the main activity
// it should show a list of the subscriptions in that category
public class ViewCategorySubscriptions extends AppCompatActivity {
    final private String TAG = "ViewCategory";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_category_subscriptions);

        Intent intent = getIntent();
        int key = intent.getIntExtra("key", -1);
        if (key == -1) {
            Log.d(TAG, "Something went wrong");
            finish();
        }
        myCategories = new CategoryList();
        currentCategory = myCategories.getMyCategories().get(key);
        currentSubscriptions = currentCategory.getSubscriptions();

        Log.d(TAG, currentCategory.getTitle());

        // The list that will hold the subscriptions
        listView = findViewById(R.id.list_view);

        // Create the adapter for the list
        adapter = new ListAdapter();
        listView.setAdapter(adapter);

    }

    // The list adapter, very similar to the one in the newCategory activity
    private class ListAdapter extends BaseAdapter {
        // override other abstract methods here

        @Override
        public int getCount() {
            return currentSubscriptions.size();
        }

        @Override
        public Subscription getItem(int position) {
            return currentSubscriptions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0L;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            final int the_position = position;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.subscription_list_item, container, false);
            }

            ImageView imgView = convertView.findViewById(R.id.subscription_thumbnail_view);
            TextView textView = convertView.findViewById(R.id.subscription_title_view);

            Subscription currentSubscription = currentSubscriptions.get(position);
            Picasso.get()
                    .load(currentSubscription.getImageUrl())
                    .placeholder(R.drawable.progress_animation)
                    .into(imgView);
            textView.setText(currentSubscription.getTitle());
            return convertView;
        }
    }

    ListView listView;
    ListAdapter adapter;
    List<Subscription> currentSubscriptions;
    Category currentCategory;
    CategoryList myCategories;
}
