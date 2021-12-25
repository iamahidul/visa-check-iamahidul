package adapter.audience.adverts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.ads.AbstractAdListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeAdView;
import com.rockerhieu.rvadapter.RecyclerViewAdapterWrapper;

import java.util.ArrayList;
import java.util.List;

import dnp.karthik.audiencenetwork.fbnativeadadapter.R;

/**
 * Created by thuanle on 2/12/17.
 */
public class FBNativeAdAdapter extends RecyclerViewAdapterWrapper {

    public static final int TYPE_FB_NATIVE_ADS = 900;
    public static final int DEFAULT_AD_ITEM_INTERVAL = 10;

    private final Param mParam;


    private FBNativeAdAdapter(Param param) {
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
           AudienceNetworkAds.initialize(adHolder.getContext());
            final NativeAd nativeAd = new NativeAd(adHolder.getContext(), mParam.facebookPlacementId);
            nativeAd.setAdListener(new NativeAdListener() {
                @Override
                public void onMediaDownloaded(Ad ad) {

                }

                @Override
                public void onAdLoaded(Ad ad) {
                    nativeAd.unregisterView();
                    if (ad != nativeAd) {
                        return;
                    }
                    View adView = NativeAdView.render(adHolder.getContext(), nativeAd, NativeAdView.Type.HEIGHT_300);

                    adHolder.adUnit.setVisibility(View.VISIBLE);
                    // Add the Native Ad View to your ad container
                    adHolder.nativeAdLayout.setVisibility(View.VISIBLE);


                    // Set the Text.
                    adHolder.nativeAdTitle.setText(nativeAd.getAdvertiserName());
                    adHolder.nativeAdBody.setText(nativeAd.getAdBodyText());
                    adHolder.nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
                    adHolder.nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                    adHolder.nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
                    adHolder.sponsoredLabel.setText(nativeAd.getSponsoredTranslation());
                    AdOptionsView adChoicesView = new AdOptionsView(adHolder.getContext(), nativeAd, adHolder.nativeAdLayout);
                    adHolder.adChoicesContainer.removeAllViews();
                    adHolder.adChoicesContainer.addView(adChoicesView, 0);


                    // Create a list of clickable views
                    List<View> clickableViews = new ArrayList<>();
                    clickableViews.add(adHolder.nativeAdTitle);
                    clickableViews.add(adHolder.nativeAdCallToAction);

                    // Register the Title and CTA button to listen for clicks.
                    nativeAd.registerViewForInteraction(
                            adView,
                            adHolder.nativeAdMedia,
                            adHolder.nativeAdIcon,
                            clickableViews);
                    adHolder.loaded = true;
                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    adHolder.nativeAdLayout.setVisibility(View.GONE);
                    adHolder.adUnit.setVisibility(View.GONE);
                 //  Toast.makeText(adHolder.getContext(), adError.toString(), Toast.LENGTH_SHORT).show();
                }
            });
            nativeAd.loadAd();
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
                .inflate(R.layout.item_facebook_native_ad, parent, false);
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
                return 3;
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
            param.itemContainerLayoutRes = R.layout.item_facebook_native_ad_outline;
            param.itemContainerId = R.id.native_ad_container;
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

        public FBNativeAdAdapter build() {
            return new FBNativeAdAdapter(mParam);
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

        //LinearLayout nativeAdContainer;
        boolean loaded;
        AdIconView nativeAdIcon;
        LinearLayout adChoicesContainer;
        AdChoicesView adChoicesView;
        TextView nativeAdTitle;
        MediaView nativeAdMedia;
        TextView nativeAdSocialContext;
        TextView nativeAdBody;
        TextView sponsoredLabel;
        Button nativeAdCallToAction;
        LinearLayout adUnit;
        private NativeAdLayout nativeAdLayout;


        AdViewHolder(View view) {
            super(view);
            nativeAdLayout = (NativeAdLayout) view.findViewById(R.id.native_ad_container);
            loaded = false;


            // Add the AdChoices icon
           adChoicesContainer =(LinearLayout) view.findViewById(R.id.ad_choices_container);
            // Create native UI using the ad metadata.
            nativeAdIcon = (AdIconView) view.findViewById(R.id.native_ad_icon);
            nativeAdTitle = (TextView)view.findViewById(R.id.native_ad_title);
           nativeAdMedia = (MediaView) view.findViewById(R.id.native_ad_media);
         nativeAdSocialContext = (TextView) view.findViewById(R.id.native_ad_social_context);
            nativeAdBody = (TextView) view.findViewById(R.id.native_ad_body);
           sponsoredLabel = (TextView) view.findViewById(R.id.native_ad_sponsored_label);
           nativeAdCallToAction = (Button) view.findViewById(R.id.native_ad_call_to_action);
           adUnit =  (LinearLayout) view.findViewById(R.id.ad_unit);
        }

        public Context getContext() {
            return nativeAdLayout.getContext();
        }
    }
}
