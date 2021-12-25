package adapter.audience.adverts;

import android.content.Context;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.ads.AbstractAdListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeAdView;
import com.facebook.ads.NativeBannerAd;
import com.facebook.ads.NativeBannerAdView;
import com.rockerhieu.rvadapter.RecyclerViewAdapterWrapper;

import java.util.ArrayList;
import java.util.List;

import dnp.karthik.audiencenetwork.fbnativeadadapter.R;

/**
 * Created by thuanle on 2/12/17.
 */
public class FBNativeBannerAdapter extends RecyclerViewAdapterWrapper {

    public static final int TYPE_FB_NATIVE_ADS = 900;
    public static final int DEFAULT_AD_ITEM_INTERVAL = 10;

    private final Param mParam;

    private FBNativeBannerAdapter(Param param) {
        super(param.adapter);
        this.mParam = param;

        assertConfig();
        setSpanAds();
    }

    private void assertConfig() {
        if (mParam.gridLayoutManager != null) {
            //if user set span ads
            int nCol = mParam.gridLayoutManager.getSpanCount();
            if (mParam.adItemInterval % nCol != 0) {
                throw new IllegalArgumentException(String.format("The adItemInterval (%d) is not divisible by number of columns in GridLayoutManager (%d)", mParam.adItemInterval, nCol));
            }
        }
    }

    private int convertAdPosition2OrgPosition(int position) {
        return position - (position + 1) / (mParam.adItemInterval + 1);
    }

    @Override
    public int getItemCount() {
        int realCount = super.getItemCount();
        return realCount + realCount / mParam.adItemInterval;
    }

    @Override
    public int getItemViewType(int position) {
        if (isAdPosition(position)) {
            return TYPE_FB_NATIVE_ADS;
        }
        return super.getItemViewType(convertAdPosition2OrgPosition(position));
    }

    private boolean isAdPosition(int position) {
        return (position + 1) % (mParam.adItemInterval + 1) == 0;
    }

    public void onBindAdViewHolder(final RecyclerView.ViewHolder holder) {
        final AdViewHolder adHolder = (AdViewHolder) holder;
        if (mParam.forceReloadAdOnBind || !adHolder.loaded) {
            final NativeBannerAd nativeBannerAd = new NativeBannerAd(adHolder.getContext(), mParam.facebookPlacementId);
            nativeBannerAd.setAdListener(new NativeAdListener() {
                @Override
                public void onMediaDownloaded(Ad ad) {
                    // Native ad finished downloading all assets
                   // Log.e(TAG, "Native ad finished downloading all assets.");
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    // Native ad failed to load
                 //   Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());

                    adHolder.nativeBannerAdContainer.setVisibility(View.GONE);
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    nativeBannerAd.unregisterView();
                    // Race condition, load() called again before last ad was displayed
                    if (nativeBannerAd == null || nativeBannerAd != ad) {
                        return;
                    }
                    View adView = NativeBannerAdView.render(adHolder.getContext(), nativeBannerAd, NativeBannerAdView.Type.HEIGHT_120);

                    adHolder.nativeBannerAdContainer.setVisibility(View.VISIBLE);
                    // Set the Text.
                    adHolder.nativeAdCallToAction.setText(nativeBannerAd.getAdCallToAction());
                    adHolder.nativeAdCallToAction.setVisibility(
                            nativeBannerAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                    adHolder.nativeAdTitle.setText(nativeBannerAd.getAdvertiserName());
                    adHolder.nativeAdSocialContext.setText(nativeBannerAd.getAdSocialContext());
                    adHolder.sponsoredLabel.setText(nativeBannerAd.getSponsoredTranslation());
                    AdChoicesView adChoicesView = new AdChoicesView(adHolder.getContext(), nativeBannerAd, true);
                    adHolder.adChoicesContainer.removeAllViews();
                    adHolder.adChoicesContainer.addView(adChoicesView, 0);

                    // Register the Title and CTA button to listen for clicks.
                    List<View> clickableViews = new ArrayList<>();
                    clickableViews.add(adHolder.nativeAdTitle);
                    clickableViews.add(adHolder.nativeAdCallToAction);
                    nativeBannerAd.registerViewForInteraction(adView, adHolder.nativeAdIconView, clickableViews);
                    adHolder.loaded = true;
                   // Log.d(TAG, "Native ad is loaded and ready to be displayed!");
                }

                @Override
                public void onAdClicked(Ad ad) {
                    // Native ad clicked
                   // Log.d(TAG, "Native ad clicked!");
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                    // Native ad impression
                  //  Log.d(TAG, "Native ad impression logged!");
                }
            });
            // load the ad
            nativeBannerAd.loadAd();
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_FB_NATIVE_ADS) {
            onBindAdViewHolder(holder);
        } else {
            super.onBindViewHolder(holder, convertAdPosition2OrgPosition(position));
        }
    }

    public RecyclerView.ViewHolder onCreateAdViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View adLayoutOutline = inflater
                .inflate(mParam.itemContainerLayoutRes, parent, false);
        ViewGroup vg = (ViewGroup) adLayoutOutline.findViewById(mParam.itemContainerId);

        LinearLayout adLayoutContent = (LinearLayout) inflater
                .inflate(R.layout.banner_ad_outline, parent, false);
        vg.addView(adLayoutContent);
        return new AdViewHolder(adLayoutOutline);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FB_NATIVE_ADS) {
            return onCreateAdViewHolder(parent);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    private void setSpanAds() {
        if (mParam.gridLayoutManager == null) {
            return ;
        }
        final GridLayoutManager.SpanSizeLookup spl = mParam.gridLayoutManager.getSpanSizeLookup();
        mParam.gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (isAdPosition(position)){
                    return spl.getSpanSize(position);
                }
                return 1;
            }
        });
    }

    private static class Param {
        String facebookPlacementId;
        RecyclerView.Adapter adapter;
        int adItemInterval;
        boolean forceReloadAdOnBind;

        @LayoutRes
        int itemContainerLayoutRes;

        @IdRes
        int itemContainerId;

        GridLayoutManager gridLayoutManager;
    }

    public static class Builder {
        private final Param mParam;

        private Builder(Param param) {
            mParam = param;
        }

        public static Builder with(String placementId, RecyclerView.Adapter wrapped) {
            Param param = new Param();
            param.facebookPlacementId = placementId;
            param.adapter = wrapped;

            //default value
            param.adItemInterval = DEFAULT_AD_ITEM_INTERVAL;
            param.itemContainerLayoutRes = R.layout.facebook_native_banner_ad;
            param.itemContainerId = R.id.native_banner_ad_container;
            param.forceReloadAdOnBind = true;
            return new Builder(param);
        }

        public Builder adItemInterval(int interval) {
            mParam.adItemInterval = interval;
            return this;
        }

        public Builder adLayout(@LayoutRes int layoutContainerRes, @IdRes int itemContainerId) {
            mParam.itemContainerLayoutRes = layoutContainerRes;
            mParam.itemContainerId = itemContainerId;
            return this;
        }

        public FBNativeBannerAdapter build() {
            return new FBNativeBannerAdapter(mParam);
        }

        public Builder enableSpanRow(GridLayoutManager layoutManager) {
            mParam.gridLayoutManager = layoutManager;
            return this;
        }

        public Builder forceReloadAdOnBind(boolean forced) {
            mParam.forceReloadAdOnBind = forced;
            return this;
        }
    }

    private static class AdViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout nativeBannerAdContainer;
        boolean loaded;
        RelativeLayout adChoicesContainer;
        TextView nativeAdTitle;
        TextView nativeAdSocialContext;
        TextView sponsoredLabel;
        AdIconView nativeAdIconView;
        Button nativeAdCallToAction;

        AdViewHolder(View view) {
            super(view);
            nativeBannerAdContainer = (RelativeLayout) view.findViewById(R.id.native_banner_ad_container);
            loaded = false;


            // Add the AdChoices icon
         adChoicesContainer =  (RelativeLayout) view.findViewById(R.id.ad_choices_container);

            // Create native UI using the ad metadata.
           nativeAdTitle = (TextView) view.findViewById(R.id.native_ad_title);
          nativeAdSocialContext = (TextView) view.findViewById(R.id.native_ad_social_context);
          sponsoredLabel = (TextView) view.findViewById(R.id.native_ad_sponsored_label);
           nativeAdIconView = (AdIconView) view.findViewById(R.id.native_icon_view);
          nativeAdCallToAction = (Button) view.findViewById(R.id.native_ad_call_to_action);

        }

        public Context getContext() {
            return nativeBannerAdContainer.getContext();
        }
    }
}
