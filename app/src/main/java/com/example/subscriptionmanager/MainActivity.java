package com.example.subscriptionmanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity"; // For debugging
    AsyncTask<Context, Void, SubscriptionListResponse> getSubscriptionsAsync;
    final int REQUEST_CODE = 9002; // The request code used for creating new categories

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        setContentView(R.layout.fragment_collections);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginAddCategory();
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Progress bar and loading text will display while loading subscription data
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        loadingText = findViewById(R.id.loading_text);
        loadingText.setVisibility(View.INVISIBLE);

        // Category list will be invisible while loading subscription data
        categoryListView = findViewById(R.id.category_list_view);
        categoryListView.setVisibility(View.INVISIBLE);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            // Launch sign-in activity
            launchAuthenticate();
        } else {
            setGoogleSignInClient();

            // Get the list of subscriptions
            // If loading from memory is implemented, should add some sort of check to make sure that
            // progressbar is made visible while loading and invisible when done. Currently only becomes
            // invisible after API call is finished.
            progressBar.setVisibility(View.VISIBLE);
            loadingText.setVisibility(View.VISIBLE);
            mySubscriptions = new SubscriptionList();
            new getSubscriptionAsync().execute(this);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(TAG, "onResume");

    }




    //Will probably need to implement these to fix some login errors
//    @Override
//    protected void onStart(Bundle savedInstanceState) {
//
//    }
//
//    @Override
//    protected void onResume(Bundle savedInstanceState) {
//
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            openLogoutDialogue();
            return true;
        }

        if (id == R.id.action_rename) {
            openRenameDialogue();
            return true;
        }

        if (id == R.id.action_delete) {
            deleteButtonPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private class getSubscriptionAsync extends AsyncTask<Context, Void, SubscriptionListResponse> {
        String nextPageToken;
        Context[] context;
        protected SubscriptionListResponse doInBackground(Context... context) {
            this.context = context;
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context[0]);
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    context[0],
                    Collections.singleton("https://www.googleapis.com/auth/youtube.readonly"));
            credential.setSelectedAccount(account.getAccount());
            YouTube youTube = new YouTube.Builder(
                    new NetHttpTransport(),
                    new JacksonFactory(),
                    credential)
                    .setApplicationName("Subscription Manager")
                    .build();
            // TODO: Make this less ugly
            YouTube.Subscriptions.List request = null;
            try {
                request = youTube.subscriptions()
                        .list("snippet");
            } catch (IOException e) {
                e.printStackTrace();
            }
            SubscriptionListResponse response = null;
            try {
                response = request.setMine(true)
                        .setMaxResults(50L)
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            nextPageToken = getNextPageToken(response);
//            getListOfSubscribers(response);
            Log.d(TAG, Integer.toString(response.getItems().size()));
            mySubscriptions.addSubscriptionList(response); // adding to the list of subscription objects
            Log.d("MainActivity", "nextPageToken: " + nextPageToken);
            while (nextPageToken != "ERROR") {
                SubscriptionListResponse nextResponse;
                nextResponse = getNextSubscriptionPage(youTube, nextPageToken);
//                getListOfSubscribers(nextResponse);
                Log.d(TAG, Integer.toString(nextResponse.getItems().size()));

                mySubscriptions.addSubscriptionList(nextResponse); // adding to the list of subscription objects
                nextPageToken = getNextPageToken(nextResponse);
            }
            return response;
        }

        protected void onPostExecute(SubscriptionListResponse result) {
            // The result is currently only the first page

//            mySubscriptions.getMySubscriptions();
            for (com.example.subscriptionmanager.Subscription sub: mySubscriptions.getMySubscriptions()) {
                Log.d(TAG, "onPostExecute: "+ sub.getTitle());
            }
            myCategories = new CategoryList();
            progressBar.setVisibility(View.INVISIBLE);
            loadingText.setVisibility(View.INVISIBLE);
            displayCategories();
        }

        protected String getNextPageToken (SubscriptionListResponse response) {
            JSONObject responseJson = new JSONObject(response);
            String nextPageToken;
            try {
                nextPageToken = responseJson.getString("nextPageToken");
            } catch (JSONException e) {
                e.printStackTrace();
                return "ERROR";
            }
            return nextPageToken;
        }

        protected SubscriptionListResponse getNextSubscriptionPage(YouTube youTube, String pageToken) {
            YouTube.Subscriptions.List request = null;
            try {
                request = youTube.subscriptions()
                        .list("snippet");
            } catch (IOException e) {
                e.printStackTrace();
            }
            SubscriptionListResponse response = null;
            try {
                response = request.setMine(true)
                        .setMaxResults(50L)
                        .setPageToken(pageToken)
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        protected List getListOfSubscribers(SubscriptionListResponse response) {
            String thumbnailUrl;
            List subscriberList = new ArrayList<String>();
            List<Subscription> items = response.getItems();
            int index = 0;
            for (Subscription item : items) {
                subscriberList.add(item.getSnippet().getTitle());
//                try {
//                    thumbnailUrl = item.getSnippet().getThumbnails().getDefault().getUrl();
////                    Log.d("THUMBNAIL", item.getSnippet().getThumbnails().getDefault().getUrl());
////                    globalState.addSubscription(item.getSnippet().getTitle(), thumbnailUrl);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                Log.d("MainActivity", "getListOfSubscribers: " + subscriberList.get(index));
                index++;
            }
            return subscriberList;
        }

//        protected JSONArray generateSubscriberJSON(String name, String)
    }

    private void openLogoutDialogue() {
        // The following code was copied almost entirely from this useful link:
        // http://www.apnatutorials.com/android/android-alert-confirm-prompt-dialog.php?categoryId=2&subCategoryId=34&myPath=android/android-alert-confirm-prompt-dialog.php
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm logout");
        builder.setMessage("You are about to logout from the app. Do you really want to proceed? The app's permissions to view your Google account will be revoked.");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "You have logged out of the app.", Toast.LENGTH_SHORT).show();
                revokePermission();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "You will not be logged out.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }

    private void openRenameDialogue() {

    }

    private void deleteButtonPressed() {

    }

    private void revokePermission() {
        // need to get the sign in client
        mGoogleSignInClient.revokeAccess();
        mGoogleSignInClient.signOut();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        Log.d(TAG, (account == null)?"worked":"didnt work??");
        launchAuthenticate();
    }

    private void setGoogleSignInClient() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope("https://www.googleapis.com/auth/youtube.readonly"))
                .requestIdToken(getString(R.string.client_id)) // added this from stackoverflow answer
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void launchAuthenticate() {
        Intent intent = new Intent(this, Authenticate.class);
        startActivity(intent);
    }

    private void beginAddCategory() {
        // The following code was copied almost entirely from this useful link:
        // http://www.apnatutorials.com/android/android-alert-confirm-prompt-dialog.php?categoryId=2&subCategoryId=34&myPath=android/android-alert-confirm-prompt-dialog.php
        final EditText edtText = new EditText(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create new category");
        builder.setMessage("Enter category name:");
        builder.setCancelable(false);
        builder.setView(edtText);
        builder.setNeutralButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                launchNewCategory(edtText.getText().toString());
            }
        });
        builder.show();
    }

    private void launchNewCategory(String title) {
        Intent intent = new Intent(this, NewCategory.class);
        intent.putExtra("title", title);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE)
        {
            Log.d(TAG, "result " + Integer.toString(resultCode));
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    Log.d(TAG, "onActivityResult: DATA WAS NULL?");
                    return;
                }
                Bundle bundle = data.getExtras();
    
                String categoryName = (String) bundle.getString("title");

//                List<com.example.subscriptionmanager.Subscription> newSubscriptionList =
//                        (List<com.example.subscriptionmanager.Subscription>) bundle.get("list");
                String title2 = myCategories.getMyCategories().get(1).getTitle();
                for(Category category: myCategories.getMyCategories()) {
                    Log.d("PLEASEWORK",category.getTitle());
                }

//                Category newCategory = new Category(categoryName, false);
//                newCategory.addList(newSubscriptionList);
//                myCategories.addCategory(newCategory);

                Log.d(TAG, "selection confirmed");
            }
            if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "onActivityResult: BAD THING");
            }


        }
    }

    private void displayCategories() {
        adapter = new CategoryListAdapter();
        categoryListView.setAdapter(adapter);
        categoryListView.setVisibility(View.VISIBLE);
//        listView.setOnItemClickListener(messageClickedHandler);
    }

    private class CategoryListAdapter extends BaseAdapter {
        // override other abstract methods here

        @Override
        public int getCount() {
            return myCategories.getMyCategories().size();
        }

        @Override
        public com.example.subscriptionmanager.Category getItem(int position) {
            return myCategories.getMyCategories().get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0L;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.default_list_item, container, false);
            }

            ImageView imgView = convertView.findViewById(R.id.category_folder_image);
            TextView textView = convertView.findViewById(R.id.category_text);
            TextView numberView = convertView.findViewById(R.id.category_number_text);
            final ImageButton deleteButton = convertView.findViewById(R.id.category_delete_button);
            final ImageButton renameButton = convertView.findViewById(R.id.category_rename_button);

            final Category currentCategory = myCategories.getMyCategories().get(position);
            textView.setText(currentCategory.getTitle());
            numberView.setText(Integer.toString(currentCategory.getSubscriptions().size()));
            Log.d("ListView", Integer.toString(myCategories.getMyCategories().size()));

            if (position == 0) {
                imgView.setImageResource(R.drawable.special_folder);
            } else {
                imgView.setImageResource(R.drawable.folder_image);
            }

            renameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openRenameDialogue(v);
                }

                private void openRenameDialogue(View v){
                    // The following code was copied almost entirely from this useful link:
                    // http://www.apnatutorials.com/android/android-alert-confirm-prompt-dialog.php?categoryId=2&subCategoryId=34&myPath=android/android-alert-confirm-prompt-dialog.php
                    final EditText edtText = new EditText(v.getContext());

                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Rename " + currentCategory.getTitle());
                    builder.setMessage("Enter new name:");
                    builder.setCancelable(true);
                    builder.setView(edtText);
                    builder.setNeutralButton("Change", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            currentCategory.setTitle(edtText.getText().toString());
//                            onResume();
                        }
                    });
                    builder.show();
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDeleteDialogue(v);
                }

                private void openDeleteDialogue(View v) {
                    // The following code was copied almost entirely from this useful link:
                    // http://www.apnatutorials.com/android/android-alert-confirm-prompt-dialog.php?categoryId=2&subCategoryId=34&myPath=android/android-alert-confirm-prompt-dialog.php
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Delete " + currentCategory.getTitle());
                    builder.setMessage("Are you sure you want to delete this category? This cannot be undone.");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "Deleted " + currentCategory.getTitle(), Toast.LENGTH_SHORT).show();
                            myCategories.removeCategory(currentCategory);
                            notifyDataSetChanged();
//                            onResume();
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.show();
                }
            });

            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                boolean isLongClicked = false;

                @Override
                public boolean onLongClick(View v) {
                    isLongClicked = !isLongClicked;
                    Log.d(TAG, "onLongClick: longclicked " + currentCategory.getTitle());
                    editMode(isLongClicked);
                    return true;
                }

                private void editMode(boolean isLongClicked) {
                    // Cant rename main category
                    if (currentCategory.getTitle().equals("All")) {return;}

                    if (isLongClicked) {
                        deleteButton.setVisibility(View.VISIBLE);
                        renameButton.setVisibility(View.VISIBLE);
                    } else {
                        deleteButton.setVisibility(View.GONE);
                        renameButton.setVisibility(View.GONE);
                    }
                }


            });

            return convertView;
        }

    }




    GoogleSignInClient mGoogleSignInClient;
    SubscriptionList mySubscriptions;
    CategoryList myCategories;

    ProgressBar progressBar;
    TextView loadingText;
    ListView categoryListView;
    ListAdapter adapter;
}
