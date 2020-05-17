package com.example.subscriptionmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NewCategory extends AppCompatActivity {
    final int REQUEST_CODE = 9002; // The request code used for creating new categories

    private final String TAG = "NewCategory"; // For debugging
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_category);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        // Set the title
        setTitle("Add channels to '" + title + "'");


//        mySubscriptions = new SubscriptionList();
        myCategories = new CategoryList();
        mySubscriptions = myCategories.getMainSubscriptions();

        myCategories.getMainSubscriptions();
//        Log.d("NEW CATEGORY", "onCreate: " + myCategories.getMainSubscriptions().get(2).getTitle());
        // TEST STUFF


        // The list that will hold the subscriptions
        listView = findViewById(R.id.list_view);
//        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        // The list that will hold the values which denote which items have been selected
        isSubscriptionChecked = new ArrayList<>();
        initializeBoolean();
        // Create the adapter for the list
        adapter = new ListAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(messageClickedHandler);


        // Add the search box
        // CURRENTLY SET TO INVISIBLE UNTIL I GET IT WORKING
        EditText searchBox = findViewById(R.id.search_text);

    }

//    private void loadImages() {
//        ImageView imageView = findViewById(R.id.imageView);
//        for (com.example.subscriptionmanager.Subscription sub: mySubscriptions.getMySubscriptions()) {
//            Picasso.get()
//                    .load(R.drawable.progress_animation)
//                    .placeholder(R.drawable.progress_animation)
//                    .into(imageView);
//        }
//
//        Log.d("Load", "loadImages: DONE");
//    }

    private class ListAdapter extends BaseAdapter {
        // override other abstract methods here

        @Override
        public int getCount() {
            return mySubscriptions.size();
        }

        @Override
        public Subscription getItem(int position) {
            return mySubscriptions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0L;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            final int the_position = position;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.checkable_list_item, container, false);
            }

            ImageView imgView = convertView.findViewById(R.id.subscription_thumbnail_view);
            TextView textView = convertView.findViewById(R.id.subscription_title_view);
            CheckBox checkBox = convertView.findViewById(R.id.check_box);

            Subscription currentSubscription = mySubscriptions.get(position);
            Picasso.get()
                    .load(currentSubscription.getImageUrl())
                    .placeholder(R.drawable.progress_animation)
                    .into(imgView);
            textView.setText(currentSubscription.getTitle());

            // This code modifies a list of boolean values corresponding to each item in the list
            checkBox.setChecked(isSubscriptionChecked.get(the_position));

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isSubscriptionChecked.set(the_position, !isSubscriptionChecked.get(the_position));
                    Log.d("HELPMEPLS", the_position + " modified");
                }
            });

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    isSubscriptionChecked.set(the_position, isChecked);
                }
            });


            return convertView;
        }
    }

    private AdapterView.OnItemClickListener messageClickedHandler = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // Toggle the checkbox
            CheckBox checkBox = view.findViewById(R.id.check_box);
            Boolean currentVal = checkBox.isChecked();
            checkBox.setChecked(!currentVal);

            Toast.makeText(getApplicationContext(), mySubscriptions.get(position).getTitle(), Toast.LENGTH_SHORT).show();
        }
    };

    public void onConfirmSelection(View v) {

        List<Subscription> categorySubscriptions = new ArrayList<>();

        for (Subscription subscription: mySubscriptions) {
            Log.d("HELPMEPLS", isSubscriptionChecked.get(mySubscriptions.indexOf(subscription))?"true " + subscription.getTitle():"false" + subscription.getTitle());
            if (isSubscriptionChecked.get(mySubscriptions.indexOf(subscription))) {
                categorySubscriptions.add(subscription);
            }
        }

        for (Subscription subscription: categorySubscriptions) {
            Log.d("HELPMEPLS2", subscription.getTitle());
        }

        Category newCategory = new Category(title, false);
        newCategory.addList(categorySubscriptions);
        myCategories.addCategory(newCategory);

        // Return from this intent to pass subscription list and title back to mainactivity
        Bundle bundle = new Bundle();
//        bundle.("list", (Serializable) categorySubscriptions); // maybe wont work, but we'll see

        bundle.putString("title", title);

        Intent intent = new Intent();
        intent.putExtras(bundle);
//        Log.d(TAG,intent.getExtras().toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void initializeBoolean() {
        for (Subscription subscription: mySubscriptions) {
            isSubscriptionChecked.add(false);
        }
    }


    List<Boolean> isSubscriptionChecked;
    CategoryList myCategories;
    List<Subscription> mySubscriptions;
    ListAdapter adapter;
    List<Subscription> filteredList;
    List<Boolean> selected;
    ListView listView;
    String title;
}
