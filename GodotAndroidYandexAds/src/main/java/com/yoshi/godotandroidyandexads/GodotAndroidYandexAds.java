package com.yoshi.godotandroidyandexads;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;

import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;
import com.yandex.mobile.ads.banner.AdSize;
import com.yandex.mobile.ads.banner.BannerAdEventListener;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.common.InitializationListener;
import com.yandex.mobile.ads.common.MobileAds;
import com.yandex.mobile.ads.interstitial.InterstitialAd;
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener;
import com.yandex.mobile.ads.rewarded.Reward;
import com.yandex.mobile.ads.rewarded.RewardedAd;
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import java.util.Set;

public class GodotAndroidYandexAds extends GodotPlugin {
    private static final String YANDEX_MOBILE_ADS_TAG = "YandexMobileAds";
    private final Activity activity; // The main activity of the game
    private InterstitialAd interstitial = null;
    private BannerAdView banner = null;
    private RewardedAd rewardedVideo = null;
    private FrameLayout layout = null; // Store the layout
    private FrameLayout.LayoutParams adParams = null; // Store the layout params
    public GodotAndroidYandexAds(Godot godot) {
        super(godot);
        this.activity = getActivity();
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "GodotAndroidYandexAds";
    }

    @Override
    public View onMainCreate(Activity activity) {
        layout = new FrameLayout(activity);
        return layout;
    }

    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new ArraySet<>();

        signals.add(new SignalInfo("_on_banner_loaded"));
        signals.add(new SignalInfo("_on_banner_failed_to_load", Integer.class));
        signals.add(new SignalInfo("_on_banner_clicked"));
        signals.add(new SignalInfo("_on_banner_left_application"));
        signals.add(new SignalInfo("_on_returned_to_application_after_banner"));
//        signals.add(new SignalInfo("_on_banner_impression", String.class));

        signals.add(new SignalInfo("_on_interstitial_loaded"));
        signals.add(new SignalInfo("_on_interstitial_failed_to_load", Integer.class));
        signals.add(new SignalInfo("_on_returned_to_application_after_interstitial"));
        signals.add(new SignalInfo("_on_interstitial_ad_shown"));
        signals.add(new SignalInfo("_on_interstitial_ad_dismissed"));
        signals.add(new SignalInfo("_on_interstitial_clicked"));
        signals.add(new SignalInfo("_on_interstitial_left_application"));
//        signals.add(new SignalInfo("_on_interstitial_impression", String.class));

        signals.add(new SignalInfo("_on_rewarded_video_ad_failed_to_load", Integer.class));
        signals.add(new SignalInfo("_on_rewarded_video_ad_loaded"));
        signals.add(new SignalInfo("_on_rewarded", String.class, Integer.class));
        signals.add(new SignalInfo("_on_rewarded_video_ad_left_application"));
        signals.add(new SignalInfo("_on_rewarded_video_ad_clicked"));
        signals.add(new SignalInfo("_on_returned_to_application_after_rewarded_video"));
        signals.add(new SignalInfo("_on_rewarded_video_ad_shown"));
//        signals.add(new SignalInfo("_on_rewarded_video_ad_impression", String.class));
        signals.add(new SignalInfo("_on_rewarded_video_ad_dismissed"));

        return signals;
    }

    /* Init */
    @UsedByGodot
    public void init(final String API_key) { // The API key is a unique application identifier that is issued in the AppMetrics web interface during app registration
        if (!API_key.isEmpty()) {
            // initialization yandex metrics
            // Creating an extended library configuration.
            YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder(API_key).build();
            // Initializing the AppMetrics SDK.
            YandexMetrica.activate(activity.getApplicationContext(), config);
            // Automatic tracking of user activity.
            YandexMetrica.enableActivityAutoTracking(activity.getApplication());
        }
        // initialization will speed up the loading of ads
        MobileAds.initialize(activity.getApplicationContext(), new InitializationListener() {
            @Override
            public void onInitializationCompleted() {
                Log.d(YANDEX_MOBILE_ADS_TAG, "SDK initialized");
            }
        });
    }

    private AdRequest getAdRequest() {
        AdRequest.Builder adBuilder = new AdRequest.Builder();
        AdRequest adRequest;
        adRequest = adBuilder.build();
        return adRequest;
    }

    /* Rewarded Video */
    private RewardedAd initRewardedVideo(final String id) {
        RewardedAd rewarded = new RewardedAd(activity);
        rewarded.setAdUnitId(id);
        rewarded.setRewardedAdEventListener(new RewardedAdEventListener() {

            @Override
            public void onLeftApplication() {
                Log.w("godot", "YandexAds: onRewardedVideoAdLeftApplication");
                emitSignal("_on_rewarded_video_ad_left_application");
            }

            @Override
            public void onAdFailedToLoad(@NonNull final AdRequestError error) {
                Log.w("godot", "YandexAds: onRewardedVideoAdFailedToLoad. Error: " + error.getCode());
                emitSignal( "_on_rewarded_video_ad_failed_to_load", error.getCode());
            }

            @Override
            public void onAdLoaded() {
                Log.w("godot", "YandexAds: onRewardedVideoAdLoaded");
                emitSignal( "_on_rewarded_video_ad_loaded");
            }

            @Override
            public void onAdClicked() {
                Log.w("godot", "YandexAds: onRewardedVideoAdClicked");
                emitSignal( "_on_rewarded_video_ad_clicked");
            }

            @Override
            public void onRewarded(@NonNull final Reward reward) {
                Log.w("godot", "YandexAds: " + String.format(" onRewarded! currency: %s amount: %d", reward.getType(), reward.getAmount()));
                emitSignal( "_on_rewarded", reward.getType(), reward.getAmount() );
            }

            @Override
            public void onReturnedToApplication() {
                Log.w("godot", "YandexAds: onReturnedToApplicationAfterRewardedVideo");
                emitSignal( "_on_returned_to_application_after_rewarded_video");
            }

            @Override
            public void onAdShown() {
                Log.w("godot", "YandexAds: onRewardedVideoAdShown");
                emitSignal( "_on_rewarded_video_ad_shown");
            }

            @Override
            public void onImpression(@Nullable ImpressionData impressionData) {
//                Log.w("godot", "YandexAds: onRewardedVideoAdImpression");
//                emitSignal( "_on_rewarded_video_ad_impression", impressionData.getRawData());
            }

            @Override
            public void onAdDismissed() {
                Log.w("godot", "YandexAds: onRewardedVideoAdDismissed");
                emitSignal( "_on_rewarded_video_ad_dismissed");
            }

        });

        rewarded.loadAd(getAdRequest());
        return rewarded;
    }

    @UsedByGodot
    public void loadRewardedVideo(final String id) {
        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                try {
                    rewardedVideo = initRewardedVideo(id);
                } catch (Exception e) {
                    Log.e("godot", e.toString());
                    e.printStackTrace();
                }
            }
        });
    }

    @UsedByGodot
    public void showRewardedVideo() {
        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                if(rewardedVideo != null) {
                    if (rewardedVideo.isLoaded()) {
                        rewardedVideo.show();
                    } else {
                        Log.w("godot", "YandexAds: showRewardedVideo - rewarded not loaded");
                    }
                }
            }
        });
    }

    /* Banner */
    private AdSize getAdSize(final String bannerSize) {
        switch (bannerSize) {
            case "BANNER_240x400":
                return AdSize.BANNER_240x400;
            case "BANNER_300x250":
                return AdSize.BANNER_300x250;
            case "BANNER_300x300":
                return AdSize.BANNER_300x300;
            case "BANNER_320x100":
                return AdSize.BANNER_320x100;
            case "BANNER_400x240":
                return AdSize.BANNER_400x240;
            case "BANNER_728x90":
                return AdSize.BANNER_728x90;
            default:
                return AdSize.BANNER_320x50;
        }
    }

    private BannerAdView initBanner(final String id, final boolean isOnTop, final String bannerSize) {
        layout = (FrameLayout)activity.getWindow().getDecorView().getRootView();
        adParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        if(isOnTop) adParams.gravity = Gravity.TOP;
        else adParams.gravity = Gravity.BOTTOM;

        BannerAdView banner = new BannerAdView(activity);
        banner.setAdUnitId(id);

        banner.setBackgroundColor(Color.TRANSPARENT);

        banner.setAdSize(getAdSize(bannerSize));
        banner.setBannerAdEventListener(new BannerAdEventListener() {
            @Override
            public void onAdLoaded() {
                Log.w("godot", "YandexAds: onBannerAdLoaded");
                emitSignal("_on_banner_loaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull final AdRequestError error)
            {
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
//                Log.w("godot", "YandexAds: onBannerAdImpression");
//                emitSignal("_on_banner_impression", impressionData.getRawData());
            }
        });
        layout.addView(banner, adParams);

        banner.loadAd(getAdRequest());
        return banner;
    }

    @UsedByGodot
    public void loadBanner(final String id, final boolean isOnTop, final String bannerSize) {
        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                if(banner == null) {
                    banner = initBanner(id, isOnTop, bannerSize);
                } else {
                    banner.loadAd(getAdRequest());
                    //Log.w("godot", "YandexAds: Banner already created: "+id);
                }
            }
        });
    }

    @UsedByGodot
    public void showBanner() {
        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                if(banner != null) {
                    banner.setVisibility(View.VISIBLE);
                    Log.d("godot", "YandexAds: Show Banner");
                } else {
                    Log.w("godot", "YandexAds: Banner not found");
                }
            }
        });
    }

    @UsedByGodot
    public void removeBanner() {
        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                if (layout == null || adParams == null)	{
                    return;
                }

                if(banner != null) {
                    layout.removeView(banner); // Remove the banner
                    Log.d("godot", "YandexAds: Remove Banner");
                } else {
                    Log.w("godot", "YandexAds: Banner not found");
                }
            }
        });
    }

    @UsedByGodot
    public void hideBanner() {
        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                if(banner != null) {
                    banner.setVisibility(View.GONE);
                    Log.d("godot", "YandexAds: Hide Banner");
                } else {
                    Log.w("godot", "YandexAds: Banner not found");
                }
            }
        });
    }

    @UsedByGodot
    public int getBannerWidth() {
        if(banner != null) {
            return banner.getWidth();
        } else {
            return 0;
        }
    }

    @UsedByGodot
    public int getBannerHeight() {
        if(banner != null) {
            return banner.getHeight();
        } else {
            return 0;
        }
    }

    /* Interstitial */
    private InterstitialAd initInterstitial(final String id) {
        InterstitialAd interstitial = new InterstitialAd(activity);
        interstitial.setAdUnitId(id);
        interstitial.setInterstitialAdEventListener(new InterstitialAdEventListener() {
            @Override
            public void onAdLoaded() {
                Log.w("godot", "YandexAds: onInterstitialAdLoaded");
                emitSignal("_on_interstitial_loaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError error) {
                Log.w("godot", "YandexAds: onInterstitialAdFailedToLoad. Error: " + error.getCode());
                emitSignal("_on_interstitial_failed_to_load", error.getCode());
            }

            @Override
            public void onAdShown() {
                Log.w("godot", "YandexAds: onInterstitialAdShown");
                emitSignal("_on_interstitial_ad_shown");
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
            public void onLeftApplication() {
                Log.w("godot", "YandexAds: onInterstitialAdLeftApplication");
                emitSignal("_on_interstitial_left_application");
            }

            @Override
            public void onReturnedToApplication() {
                Log.w("godot", "YandexAds: onReturnedToApplicationAfterInterstitial");
                emitSignal("_on_returned_to_application_after_interstitial");
            }

            @Override
            public void onImpression(@Nullable ImpressionData impressionData) {
//                Log.w("godot", "YandexAds: onInterstitialAdImpression");
//                emitSignal("_on_interstitial_impression", impressionData.getRawData());
            }
        });

        interstitial.loadAd(getAdRequest());
        return interstitial;
    }

    @UsedByGodot
    public void loadInterstitial(final String id) {
        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                interstitial = initInterstitial(id);
            }
        });
    }

    @UsedByGodot
    public void showInterstitial() {
        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                if(interstitial != null) {
                    if (interstitial.isLoaded()) {
                        interstitial.show();
                    } else {
                        Log.w("godot", "YandexAds: showInterstitial - interstitial not loaded");
                    }
                }
            }
        });
    }
}
