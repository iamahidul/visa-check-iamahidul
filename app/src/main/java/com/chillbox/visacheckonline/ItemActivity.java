package com.chillbox.visacheckonline;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSize;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;


import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

public class ItemActivity extends AppCompatActivity implements MaxAdListener, MaxAdViewAdListener {

    private MaxInterstitialAd interstitialAd;
    private MaxAdView adView;
    private int retryAttempt;
    AlertDialog.Builder builder;
    public  ProgressBar progressBar;
    public  RecyclerView mPeopleRV;
    public  DatabaseReference mDatabase;
    public FirebaseRecyclerAdapter<Item, ItemViewHolder> mPeopleRVAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        //ads start
        AppLovinSdk.getInstance(this).setMediationProvider("max");
        AppLovinSdk.initializeSdk(this, new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration) {

            }

        });


        //
        adView = new MaxAdView( (getString(R.string.banner_home_footer)), this );
        adView.setListener( this );
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int heightPx = getResources().getDimensionPixelSize( R.dimen.banner_height );
        adView.setLayoutParams( new FrameLayout.LayoutParams( width, heightPx , Gravity.BOTTOM) );
        ViewGroup rootView = findViewById( android.R.id.content );
        rootView.addView( adView );
        adView.loadAd();



        interstitialAd = new MaxInterstitialAd( (getString(R.string.interstitial_full_screen)), this );
        interstitialAd.setListener( this );
        // Load the first ad
        interstitialAd.loadAd();
        // first ad end








        progressBar = findViewById(R.id.progressBar);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Mahi_APP");
        mDatabase.keepSynced(true);
        mDatabase = FirebaseDatabase.getInstance().getReference();


        mDatabase.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                progressBar.setVisibility(View.GONE);
                Toasty.success(ItemActivity.this, "Data Loaded Successfully !", Toast.LENGTH_SHORT, true).show();
                //admob.requestAdMob();
                if ( interstitialAd.isReady() )
                {
                    interstitialAd.showAd();
                }

            }



            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Toasty.info(ItemActivity.this, "Database Updated", Toast.LENGTH_SHORT, true).show();

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Toasty.error(getApplicationContext(), "Database Error ?", Toast.LENGTH_SHORT, true).show();
            }

        });


        mPeopleRV = (RecyclerView) findViewById(R.id.myRecycleView);
        DatabaseReference personsRef1 = FirebaseDatabase.getInstance().getReference().child("Mahi_APP");
        DatabaseReference personsRef = personsRef1.child("Mahi_DATA");
        Query personsQuery = personsRef.orderByKey();



        mPeopleRV.hasFixedSize();
        mPeopleRV.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions personsOptions = new FirebaseRecyclerOptions.Builder<Item>().setQuery(personsQuery, Item.class).build();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        mPeopleRV.setLayoutManager(gridLayoutManager);
        mPeopleRVAdapter = new FirebaseRecyclerAdapter<Item, ItemActivity.ItemViewHolder>(personsOptions) {
            @Override
            protected void onBindViewHolder(ItemActivity.ItemViewHolder holder, final int position, final Item model) {
                holder.setTitle(model.getTitle());
                holder.setImage(getBaseContext(), model.getImage());
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String url = model.getUrl();
                        Intent intent = new Intent(getApplicationContext(), ItemWebView.class);
                        intent.putExtra("id", url);
                        startActivity(intent);

                        if ( interstitialAd.isReady() )
                        {
                            interstitialAd.showAd();
                        }


                    }
                });
            }

            @Override
            public ItemActivity.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_row, parent, false);

                return new ItemActivity.ItemViewHolder(view);
            }
        };

        mPeopleRV.setAdapter(mPeopleRVAdapter);
    }



    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Thank You!");
        builder.setMessage("Please Give Us Your Suggestions and Feedback");
        builder.setPositiveButton("QUIT", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                ItemActivity.this.finish();
            }
        });
        builder.setNegativeButton("RATE US", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent("android.intent.action.VIEW");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("https://play.google.com/store/apps/details?id=");
                stringBuilder.append(ItemActivity.this.getPackageName());
                intent.setData(Uri.parse(stringBuilder.toString()));
                ItemActivity.this.startActivity(intent);
                Toast.makeText(ItemActivity.this, "Thank you for your Rating", 0).show();
            }
        });
        builder.create().show();
    }


    @Override
    public void onStart() {
        super.onStart();
        mPeopleRVAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPeopleRVAdapter.stopListening();


    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public ItemViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }
        public void setTitle(String title){
            TextView post_title = (TextView)mView.findViewById(R.id.post_title);
            post_title.setText(title);
        }
        public void setImage(Context ctx, String image){
            ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.get().load(image).into(post_image);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_noti) {
            Toasty.warning(getApplicationContext(), "Not yet implemented !.", Toast.LENGTH_SHORT, true).show();
            Intent intent = new Intent(this,Notifications.class);
            this.startActivity(intent);
            return true;
        }

        if (id == R.id.action_search) {

            Toasty.warning(getApplicationContext(), "Not yet implemented !.", Toast.LENGTH_SHORT, true).show();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }




    @Override
    protected void onResume() {
        super.onResume();
        App.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.activityPaused();
    }


    // MAX Ad Listener 2nd
    @Override
    public void onAdLoaded(final MaxAd maxAd)
    {
        // Interstitial ad is ready to be shown. interstitialAd.isReady() will now return 'true'

        // Reset retry attempt
        retryAttempt = 0;
    }

    @Override
    public void onAdLoadFailed(final String adUnitId, final MaxError error)
    {
        // Interstitial ad failed to load
        // We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds)

        retryAttempt++;
        long delayMillis = TimeUnit.SECONDS.toMillis( (long) Math.pow( 2, Math.min( 6, retryAttempt ) ) );

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                interstitialAd.loadAd();
            }
        }, delayMillis );
    }

    @Override
    public void onAdDisplayFailed(final MaxAd maxAd, final MaxError error)
    {
        // Interstitial ad failed to display. We recommend loading the next ad
        interstitialAd.loadAd();
    }

    @Override
    public void onAdDisplayed(final MaxAd maxAd) {}

    @Override
    public void onAdClicked(final MaxAd maxAd) {}

    @Override
    public void onAdHidden(final MaxAd maxAd)
    {
        // Interstitial ad is hidden. Pre-load the next ad
        interstitialAd.loadAd();
    }


    /**
     * This method will be invoked when the {@link MaxAdView} has expanded full screen.
     *
     * @param ad An ad for which the ad view expanded for. Guaranteed not to be null.
     */
    @Override
    public void onAdExpanded(MaxAd ad) {

    }

    /**
     * This method will be invoked when the {@link MaxAdView} has collapsed back to its original size.
     *
     * @param ad An ad for which the ad view collapsed for. Guaranteed not to be null.
     */
    @Override
    public void onAdCollapsed(MaxAd ad) {

    }


}






