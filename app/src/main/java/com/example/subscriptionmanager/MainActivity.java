package com.example.subscriptionmanager;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;

public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity"; // For debugging
    AsyncTask<Context, Void, SubscriptionListResponse> getSubscriptionsAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Progress bar and loading text will display while
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        loadingText = findViewById(R.id.loading_text);
        loadingText.setVisibility(View.INVISIBLE);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            // Launch sign-in activity
            Intent intent = new Intent(this, Authenticate.class);
            startActivity(intent);
        }
        Log.d(TAG, account.getDisplayName() );

        // Get the list of subscriptions
        // If loading from memory is implemented, should add some sort of check to make sure that
        // progressbar is made visible while loading and invisible when done. Currently only becomes
        // invisible after API call is finished.
        progressBar.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.VISIBLE);
        new getSubscriptionAsync().execute(this);
    }

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
        if (id == R.id.action_settings) {
            return true;
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
            getListOfSubscribers(response);
            Log.d("MainActivity", "nextPageToken: " + nextPageToken);
            while (nextPageToken != "ERROR") {
                SubscriptionListResponse nextResponse;
                nextResponse = getNextSubscriptionPage(youTube, nextPageToken);
                getListOfSubscribers(nextResponse);
                nextPageToken = getNextPageToken(nextResponse);
            }
            return response;
        }

        protected void onPostExecute(SubscriptionListResponse result) {
            Log.d("ASYNC", "onPostExecute: " + result);
            progressBar.setVisibility(View.INVISIBLE);
            loadingText.setVisibility(View.INVISIBLE);
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
//
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
    ProgressBar progressBar;
    TextView loadingText;
}
