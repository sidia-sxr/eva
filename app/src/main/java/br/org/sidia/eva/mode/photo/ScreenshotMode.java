/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.org.sidia.eva.mode.photo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import com.samsungxr.SXRCameraRig;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.util.AsyncExecutor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import br.org.sidia.eva.BuildConfig;
import br.org.sidia.eva.EvaContext;
import br.org.sidia.eva.R;
import br.org.sidia.eva.context.ActivityResultEvent;
import br.org.sidia.eva.context.RequestPermissionResultEvent;
import br.org.sidia.eva.mode.BaseEvaMode;
import br.org.sidia.eva.mode.OnBackToHudModeListener;
import br.org.sidia.eva.util.EventBusUtils;
import br.org.sidia.eva.util.StorageUtils;
import br.org.sidia.eva.view.OnViewShownCallback;

public class ScreenshotMode extends BaseEvaMode {

    private static final String TAG = ScreenshotMode.class.getSimpleName();

    private SparseArray<SocialAppInfo> mSocialApps = new SparseArray<>();

    private static final String FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

    private static final int REQUEST_STORAGE_PERMISSION = 1000;
    private static final String[] PERMISSION_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private OnBackToHudModeListener mBackToHudModeListener;
    private PhotoViewController mPhotoViewController;
    private File mPhotosDir;
    private OnStoragePermissionGranted mPermissionCallback;
    private File mSavedFile;
    private SoundPool mSoundPool;
    private float mVolume;
    private float mMaxVolume;
    private IPhotoView mView;

    @IntDef({R.id.button_facebook, R.id.button_twitter, R.id.button_instagram, R.id.button_whatsapp})
    @Retention(RetentionPolicy.SOURCE)
    private @interface SocialAppId {
    }

    public ScreenshotMode(EvaContext evaContext, OnBackToHudModeListener listener) {
        super(evaContext, new PhotoViewController(evaContext));
        mBackToHudModeListener = listener;
        mPhotoViewController = (PhotoViewController) mModeScene;

        AudioManager audioManager = (AudioManager) evaContext.getActivity().getSystemService(Context.AUDIO_SERVICE);
        mVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onEnter() {
        EventBusUtils.register(this);
        loadSocialAppsInfo();
        if (!hasStoragePermission()) {
            requestStoragePermission(this::takePhoto);
        } else {
            takePhoto();
        }
    }

    @Override
    protected void onExit() {
        EventBusUtils.unregister(this);
        if (mSoundPool != null) {
            mSoundPool.release();
        }
    }

    private void showPhotoView(Bitmap photo, OnViewShownCallback onViewShownCallback) {
        mView = mPhotoViewController.makeView(IPhotoView.class);
        mView.setOnActionsShareClickListener(this::onShareButtonClicked);
        mView.setOnCancelClickListener(view1 -> backToHudView());
        mView.setPhotoBitmap(photo);
        mView.show(onViewShownCallback);
    }

    private void onShareButtonClicked(View clickedButton) {

        openSocialApp(clickedButton.getId());

        if (mSavedFile != null) {
            backToHudView();
        }
    }

    private void backToHudView() {
        mEvaContext.getSXRContext().runOnGlThread(() -> {
            mEvaContext.getPlaneHandler().getSelectedPlane().setEnable(true);
            mBackToHudModeListener.OnBackToHud();
        });
    }

    private void initPhotosDir() {
        if (mPhotosDir == null) {
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            mPhotosDir = new File(picturesDir, BuildConfig.APPLICATION_ID);
            if (!mPhotosDir.exists()) {
                if (mPhotosDir.mkdirs()) {
                    Log.d(TAG, "Directory created: " + mPhotosDir);
                }
            } else {
                Log.d(TAG, "Using existing directory: " + mPhotosDir);
            }
        }
    }

    private void takePhoto() {
        try {
            mSavedFile = null;
            mEvaContext.getPlaneHandler().getSelectedPlane().setEnable(false);
            mEvaContext.getSXRContext().captureScreenCenter(this::onPhotoCaptured);
        } catch (Throwable t) {
            Log.e(TAG, "Error taking photo", t);
            mEvaContext.getPlaneHandler().getSelectedPlane().setEnable(true);
        }
    }

    private void onPhotoCaptured(Bitmap capturedPhotoBitmap) {
        Log.d(TAG, "Photo captured " + capturedPhotoBitmap);
        if (capturedPhotoBitmap != null) {
            loadSounds();
            showPhotoView(capturedPhotoBitmap, () -> {
                // FIXME: Put a delay to ensure that the photo view is ready before
                // we can get its bitmap
                new Handler().postDelayed(() -> {
                    Bitmap photoBitmap = getBitmapFromView(mView.getPhotoContent());
                    AsyncExecutor.create().execute(() -> savePhoto(photoBitmap));
                }, 150);
            });
        }
    }

    private Bitmap getBitmapFromView(View view) {
        final int viewWidth = view.getLayoutParams().width;
        final int viewHeight = view.getLayoutParams().height;
        Bitmap bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void savePhoto(Bitmap capturedPhotoBitmap) {

        if (StorageUtils.getAvailableExternalStorageSize() <= 0) {
            Log.e(TAG, "There is no free space to save the photo on this device.");
            return;
        }

        initPhotosDir();

        final String fileName = "eva-photo-" + System.currentTimeMillis() + ".jpeg";
        File file = new File(mPhotosDir, fileName);

        try (FileOutputStream output = new FileOutputStream(file)) {
            capturedPhotoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        } catch (IOException e) {
            file = null;
            Log.e(TAG, "Error saving photo", e);
        }

        new Handler(Looper.getMainLooper()).post(() -> mView.showToast());
        mView.enableButtons();
        mSavedFile = file;

        // Scan file to make it available on gallery immediately
        MediaScannerConnection.scanFile(mEvaContext.getActivity(),
                new String[]{mSavedFile.toString()}, null,
                (path, uri) -> {
                });
    }

    @Override
    protected void onHandleOrientation(SXRCameraRig cameraRig) {
    }

    private void requestStoragePermission(OnStoragePermissionGranted callback) {
        mPermissionCallback = callback;
        mEvaContext.getActivity().requestPermissions(PERMISSION_STORAGE, REQUEST_STORAGE_PERMISSION);
    }

    private boolean hasStoragePermission() {
        return ContextCompat.checkSelfPermission(mEvaContext.getActivity(), PERMISSION_STORAGE[0])
                == PackageManager.PERMISSION_GRANTED;
    }

    @Subscribe
    public void handleContextEvent(ActivityResultEvent event) {
        if (event.getRequestCode() == REQUEST_STORAGE_PERMISSION) {
            if (hasStoragePermission()) {
                backToHudView();
            } else {
                showToastPermissionDenied();
                backToHudView();
            }
        }
    }

    @Subscribe
    public void handleContextEvent(RequestPermissionResultEvent event) {
        if (event.getRequestCode() == REQUEST_STORAGE_PERMISSION) {
            if (hasStoragePermission()) {
                mPermissionCallback.onGranted();
            } else {
                if (mEvaContext.getActivity().shouldShowRequestPermissionRationale(PERMISSION_STORAGE[0])) {
                    backToHudView();
                    showToastPermissionDenied();
                } else {
                    showToastPermissionDenied();
                    openAppPermissionsSettings();
                }
            }
        }
    }

    private void showToastPermissionDenied() {
        Toast.makeText(mEvaContext.getActivity(),
                "Storage access not allowed", Toast.LENGTH_LONG).show();
    }

    private void showToastAppIsDisabled(String appName) {
        Toast.makeText(mEvaContext.getActivity(),
                appName + " is disabled and cannot be launched", Toast.LENGTH_LONG).show();
    }

    private void openAppPermissionsSettings() {
        Activity context = mEvaContext.getActivity();
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + context.getPackageName()));
        context.startActivityForResult(intent, REQUEST_STORAGE_PERMISSION);
    }

    private void openSocialApp(@SocialAppId int socialAppId) {
        SocialAppInfo info = mSocialApps.get(socialAppId);
        if (mSavedFile != null
                && checkAppInstalled(info.mPackageName)
                && checkAppEnabled(info.mPackageName, info.mAppName)) {
            Intent intent = createIntent();
            if (info.mActivity != null) {
                intent.setClassName(info.mPackageName, info.mActivity);
            } else {
                intent.setPackage(info.mPackageName);
            }
            mEvaContext.getActivity().startActivity(intent);
        }
    }

    private Intent createIntent() {
        Activity context = mEvaContext.getActivity();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, mSavedFile));
        intent.setType("image/jpeg");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
    }

    private boolean checkAppInstalled(String packageName) {
        if (isAppInstalled(packageName)) {
            return true;
        } else {
            installApp(packageName);
            return false;
        }
    }

    private boolean checkAppEnabled(String packageName, String appName) {
        Activity context = mEvaContext.getActivity();
        try {
            boolean enabled = context.getPackageManager().getApplicationInfo(packageName, 0).enabled;
            if (!enabled) {
                showToastAppIsDisabled(appName);
            }
            return enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void installApp(String appName) {
        Activity context = mEvaContext.getActivity();
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName));
            intent.setPackage("com.android.vending");
            context.startActivity(intent);
        } catch (Exception exception) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + appName)));
        }
    }

    private boolean isAppInstalled(String packageName) {
        Activity context = mEvaContext.getActivity();
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void loadSounds() {
        AsyncExecutor.create().execute(() -> {
            mEvaContext.getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            mSoundPool = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(audioAttributes)
                    .build();

            mSoundPool.load("/system/media/audio/ui/camera_click.ogg", 1);
            mSoundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
                // sound was loaded successfully
                if (status == 0) {
                    playClickSound(sampleId);
                } else {
                    Log.e(TAG, "Sound was not loaded successfully");
                }
            });
        });
    }

    private void playClickSound(int soundId) {
        float leftVolume = mVolume / mMaxVolume;
        float rightVolume = mVolume / mMaxVolume;
        int priority = 1;
        int no_loop = 0;
        float normal_playback_rate = 1f;

        mSoundPool.play(soundId, leftVolume, rightVolume,
                priority, no_loop, normal_playback_rate);
    }

    private void loadSocialAppsInfo() {
        mSocialApps.put(R.id.button_facebook, new SocialAppInfo(
                "Facebook",
                "com.facebook.katana",
                "com.facebook.composer.shareintent.ImplicitShareIntentHandlerDefaultAlias"));
        mSocialApps.put(R.id.button_twitter, new SocialAppInfo(
                "Twitter",
                "com.twitter.android",
                "com.twitter.composer.ComposerActivity"));
        mSocialApps.put(R.id.button_instagram, new SocialAppInfo(
                "Instagram",
                "com.instagram.android",
                null));
        mSocialApps.put(R.id.button_whatsapp, new SocialAppInfo(
                "WhatsApp",
                "com.whatsapp",
                null));
    }

    private class SocialAppInfo {

        String mAppName;
        String mPackageName;
        String mActivity;

        SocialAppInfo(String mAppName, String mPackageName, String mActivity) {
            this.mAppName = mAppName;
            this.mPackageName = mPackageName;
            this.mActivity = mActivity;
        }
    }

    @FunctionalInterface
    private interface OnStoragePermissionGranted {
        void onGranted();
    }

}
