package com.darkmoonight.godotandroidyandexads;

import android.app.Activity;
import android.graphics.Color;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yandex.mobile.ads.banner.BannerAdEventListener;
import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdError;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestConfiguration;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.common.MobileAds;
import com.yandex.mobile.ads.interstitial.InterstitialAd;
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader;
import com.yandex.mobile.ads.rewarded.Reward;
import com.yandex.mobile.ads.rewarded.RewardedAd;
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener;
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener;
import com.yandex.mobile.ads.rewarded.RewardedAdLoader;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import java.util.Set;

import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.AppMetricaConfig;

public class GodotAndroidYandexAds extends GodotPlugin {
    private static final String YANDEX_MOBILE_ADS_TAG = "YandexMobileAds";
    private final Activity activity;
    @Nullable
    private BannerAdView bannerAdView = null;
    @Nullable
    private RewardedAd rewardedAd = null;
    @Nullable
    private InterstitialAd interstitialAd = null;
    private FrameLayout layout = null;
    private FrameLayout.LayoutParams adParams = null;

    public GodotAndroidYandexAds(Godot godot) {
        super(godot);
        this.activity = getActivity();
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "GodotAndroidYandexAds";
    }

    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new ArraySet<>();
        addBannerSignals(signals);
        addRewardedSignals(signals);
        addInterstitialSignals(signals);
        return signals;
    }

    private void addBannerSignals(Set<SignalInfo> signals) {
        signals.add(new SignalInfo("_on_banner_loaded"));
        signals.add(new SignalInfo("_on_banner_failed_to_load", Integer.class));
        signals.add(new SignalInfo("_on_banner_clicked"));
        signals.add(new SignalInfo("_on_banner_left_application"));
        signals.add(new SignalInfo("_on_returned_to_application_after_banner"));
    }

    private void addRewardedSignals(Set<SignalInfo> signals) {
        signals.add(new SignalInfo("_on_rewarded_video_ad_loaded"));
        signals.add(new SignalInfo("_on_rewarded_video_ad_failed_to_load", Integer.class));
        signals.add(new SignalInfo("_on_rewarded_video_ad_show"));
        signals.add(new SignalInfo("_on_rewarded_video_ad_failed_to_show", String.class));
        signals.add(new SignalInfo("_on_rewarded_video_ad_dismissed"));
        signals.add(new SignalInfo("_on_rewarded_video_ad_clicked"));
        signals.add(new SignalInfo("_on_rewarded", String.class, Integer.class));
    }

    private void addInterstitialSignals(Set<SignalInfo> signals) {
        signals.add(new SignalInfo("_on_interstitial_loaded"));
        signals.add(new SignalInfo("_on_interstitial_failed_to_load", Integer.class));
        signals.add(new SignalInfo("_on_interstitial_ad_show"));
        signals.add(new SignalInfo("_on_interstitial_failed_to_show", String.class));
        signals.add(new SignalInfo("_on_interstitial_ad_dismissed"));
        signals.add(new SignalInfo("_on_interstitial_clicked"));
    }

    @UsedByGodot
    public void init(@NonNull final String apiKey) {
        if (!apiKey.isEmpty()) {
            AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey).build();
            AppMetrica.activate(activity.getApplicationContext(), config);
            AppMetrica.enableActivityAutoTracking(activity.getApplication());
        }
        MobileAds.initialize(activity, () -> Log.d(YANDEX_MOBILE_ADS_TAG, "SDK initialized"));
    }

    @Override
    public View onMainCreate(Activity activity) {
        layout = new FrameLayout(activity);
        return layout;
    }

    @NonNull
    private BannerAdSize getAdSize(int width, int height) {
        if (width > 0 && height > 0) {
            return BannerAdSize.fixedSize(activity, width, height);
        }
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        int adWidthPixels = bannerAdView != null ? bannerAdView.getWidth() : displayMetrics.widthPixels;
        int adWidth = Math.round(adWidthPixels / displayMetrics.density);
        return BannerAdSize.stickySize(activity, adWidth);
    }

    @NonNull
    private BannerAdView initBanner(final String id, final boolean isOnTop, int width, int height) {
        layout = (FrameLayout) activity.getWindow().getDecorView().getRootView();
        adParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        adParams.gravity = isOnTop ? Gravity.TOP : Gravity.BOTTOM;

        BannerAdView bannerAdView = new BannerAdView(activity);
        bannerAdView.setAdUnitId(id);
        bannerAdView.setBackgroundColor(Color.TRANSPARENT);
        bannerAdView.setAdSize(getAdSize(width, height));
        bannerAdView.setBannerAdEventListener(createBannerAdEventListener());

        layout.addView(bannerAdView, adParams);
        bannerAdView.loadAd(new AdRequest.Builder().build());
        return bannerAdView;
    }

    private BannerAdEventListener createBannerAdEventListener() {
        return new BannerAdEventListener() {
            @Override
            public void onAdLoaded() {
                Log.w("godot", "YandexAds: onBannerAdLoaded");
                emitSignal("_on_banner_loaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull final AdRequestError error) {
                Log.w("godot", "YandexAds: onBannerAdFailedToLoad. Error: " + error.getCode());
                emitSignal("_on_banner_failed_to_load", error.getCode());
            }

            @Override
            public void onAdClicked() {
                Log.w("godot", "YandexAds: onBannerAdClicked");
                emitSignal("_on_banner_clicked");
            }

            @Override
            public void onLeftApplication() {
                Log.w("godot", "YandexAds: onBannerAdLeftApplication");
                emitSignal("_on_banner_left_application");
            }

            @Override
            public void onReturnedToApplication() {
                Log.w("godot", "YandexAds: onReturnedToApplicationAfterBannerAd");
                emitSignal("_on_returned_to_application_after_banner");
            }

            @Override
            public void onImpression(@Nullable ImpressionData impressionData) {
                // Log.w("godot", "YandexAds: onBannerAdImpression");
                // emitSignal("_on_banner_impression", impressionData.getRawData());
            }
        };
    }

    @UsedByGodot
    public void loadBanner(final String id, final boolean isOnTop, int width, int height) {
        activity.runOnUiThread(() -> {
            if (bannerAdView == null) {
                bannerAdView = initBanner(id, isOnTop, width, height);
            } else {
                bannerAdView.loadAd(new AdRequest.Builder().build());
                Log.w("godot", "YandexAds: Banner already created: " + id);
            }
        });
    }

    @UsedByGodot
    public void showBanner() {
        activity.runOnUiThread(() -> {
            if (bannerAdView != null) {
                bannerAdView.setVisibility(View.VISIBLE);
                Log.d("godot", "YandexAds: Show Banner");
            } else {
                Log.w("godot", "YandexAds: Banner not found");
            }
        });
    }

    @UsedByGodot
    public void removeBanner() {
        activity.runOnUiThread(() -> {
            if (layout == null || adParams == null) {
                return;
            }

            if (bannerAdView != null) {
                layout.removeView(bannerAdView);
                Log.d("godot", "YandexAds: Remove Banner");
            } else {
                Log.w("godot", "YandexAds: Banner not found");
            }
        });
    }

    @UsedByGodot
    public void hideBanner() {
        activity.runOnUiThread(() -> {
            if (bannerAdView != null) {
                bannerAdView.setVisibility(View.GONE);
                Log.d("godot", "YandexAds: Hide Banner");
            } else {
                Log.w("godot", "YandexAds: Banner not found");
            }
        });
    }

    private RewardedAd initRewardedVideo(final String id) {
        RewardedAdLoader rewardedAdLoader = new RewardedAdLoader(activity);
        rewardedAdLoader.setAdLoadListener(new RewardedAdLoadListener() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd newRewardedAd) {
                rewardedAd = newRewardedAd;
                Log.w("godot", "YandexAds: onRewardedVideoAdLoaded");
                emitSignal("_on_rewarded_video_ad_loaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
                Log.w("godot", "YandexAds: onRewardedVideoAdFailedToLoad. Error: " + adRequestError.getCode());
                emitSignal("_on_rewarded_video_ad_failed_to_load", adRequestError.getCode());
            }
        });
        rewardedAdLoader.loadAd(new AdRequestConfiguration.Builder(id).build());
        return rewardedAd;
    }

    @UsedByGodot
    public void loadRewardedVideo(final String id) {
        activity.runOnUiThread(() -> {
            try {
                rewardedAd = initRewardedVideo(id);
            } catch (Exception e) {
                Log.e("godot", e.toString());
            }
        });
    }

    @UsedByGodot
    public void showRewardedVideo() {
        activity.runOnUiThread(() -> {
            if (rewardedAd != null) {
                rewardedAd.setAdEventListener(createRewardedAdEventListener());
                rewardedAd.show(activity);
            }
        });
    }

    private RewardedAdEventListener createRewardedAdEventListener() {
        return new RewardedAdEventListener() {
            @Override
            public void onAdShown() {
                Log.w("godot", "YandexAds: onRewardedVideoAdShown");
                emitSignal("_on_rewarded_video_ad_show");
            }

            @Override
            public void onAdFailedToShow(@NonNull AdError adError) {
                Log.w("godot", "YandexAds: onRewardedVideoAdFailedToShown. Error: " + adError.getDescription());
                emitSignal("_on_rewarded_video_ad_failed_to_show", adError.getDescription());
            }

            @Override
            public void onAdDismissed() {
                Log.w("godot", "YandexAds: onRewardedVideoAdDismissed");
                emitSignal("_on_rewarded_video_ad_dismissed");
            }

            @Override
            public void onAdClicked() {
                Log.w("godot", "YandexAds: onRewardedVideoAdClicked");
                emitSignal("_on_rewarded_video_ad_clicked");
            }

            @Override
            public void onAdImpression(@Nullable ImpressionData impressionData) {
                // Log.w("godot", "YandexAds: onRewardedVideoAdImpression");
                // emitSignal("_on_rewarded_video_ad_impression", impressionData.getRawData());
            }

            @Override
            public void onRewarded(@NonNull Reward reward) {
                Log.w("godot", "YandexAds: " + String.format("onRewarded! currency: %s amount: %d", reward.getType(), reward.getAmount()));
                emitSignal("_on_rewarded", reward.getType(), reward.getAmount());
            }
        };
    }

    private InterstitialAd initInterstitial(final String id) {
        InterstitialAdLoader interstitialAdLoader = new InterstitialAdLoader(activity);
        interstitialAdLoader.setAdLoadListener(new InterstitialAdLoadListener() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd newInterstitialAd) {
                interstitialAd = newInterstitialAd;
                Log.w("godot", "YandexAds: onInterstitialAdLoaded");
                emitSignal("_on_interstitial_loaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
                Log.w("godot", "YandexAds: onInterstitialAdFailedToLoad. Error: " + adRequestError.getCode());
                emitSignal("_on_interstitial_failed_to_load", adRequestError.getCode());
            }
        });
        interstitialAdLoader.loadAd(new AdRequestConfiguration.Builder(id).build());
        return interstitialAd;
    }

    @UsedByGodot
    public void loadInterstitial(final String id) {
        activity.runOnUiThread(() -> {
            try {
                interstitialAd = initInterstitial(id);
            } catch (Exception e) {
                Log.e("godot", e.toString());
            }
        });
    }

    @UsedByGodot
    public void showInterstitial() {
        activity.runOnUiThread(() -> {
            if (interstitialAd != null) {
                interstitialAd.setAdEventListener(createInterstitialAdEventListener());
                interstitialAd.show(activity);
            }
        });
    }

    private InterstitialAdEventListener createInterstitialAdEventListener() {
        return new InterstitialAdEventListener() {
            @Override
            public void onAdShown() {
                Log.w("godot", "YandexAds: onInterstitialAdShown");
                emitSignal("_on_interstitial_ad_show");
            }

            @Override
            public void onAdFailedToShow(@NonNull final AdError adError) {
                Log.w("godot", "YandexAds: onInterstitialAdFailedToShown. Error: " + adError.getDescription());
                emitSignal("_on_interstitial_failed_to_show", adError.getDescription());
            }

            @Override
            public void onAdDismissed() {
                Log.w("godot", "YandexAds: onInterstitialAdDismissed");
                emitSignal("_on_interstitial_ad_dismissed");
            }

            @Override
            public void onAdClicked() {
                Log.w("godot", "YandexAds: onInterstitialAdClicked");
                emitSignal("_on_interstitial_clicked");
            }

            @Override
            public void onAdImpression(@Nullable final ImpressionData impressionData) {
                // Log.w("godot", "YandexAds: onInterstitialAdImpression");
                // emitSignal("_on_interstitial_impression", impressionData.getRawData());
            }
        };
    }
}
