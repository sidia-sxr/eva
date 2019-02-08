/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package br.org.sidia.eva.mode;

import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.samsungxr.IViewEvents;
import com.samsungxr.SXRDrawFrameListener;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.nodes.SXRViewNode;
import com.samsungxr.utility.Log;

import br.org.sidia.eva.BuildConfig;
import br.org.sidia.eva.EvaContext;
import br.org.sidia.eva.R;
import br.org.sidia.eva.connection.socket.ConnectionMode;
import br.org.sidia.eva.constant.EvaConstants;
import br.org.sidia.eva.healthmonitor.HealthId;
import br.org.sidia.eva.healthmonitor.HealthManager;
import br.org.sidia.eva.healthmonitor.HealthStatus;
import br.org.sidia.eva.custom.WaveAnimation;
import br.org.sidia.eva.util.LayoutViewUtils;

import static br.org.sidia.eva.healthmonitor.HealthManager.HEALTH_ID_DRINK;
import static br.org.sidia.eva.healthmonitor.HealthManager.HEALTH_ID_PEE;
import static br.org.sidia.eva.healthmonitor.HealthManager.HEALTH_ID_PLAY;
import static br.org.sidia.eva.healthmonitor.HealthManager.HEALTH_ID_SLEEP;

public class HudView extends BaseEvaView implements View.OnClickListener {
    private static final String TAG = "HudView";

    private View mMenuOptionsHud, mShareAnchorButton, mCameraButton, mCleanButton, mCloseButton, mMenuButton;
    private ImageView mHydrantButton, mBedButton, mBowlButton, mAboutButton, mHealthPreferences;
    private View mPlayBoneButton;
    private LinearLayout mRootLayout;
    private final SXRViewNode mHudMenuObject;
    private final SXRViewNode mStartMenuObject;
    private final SXRViewNode mConnectedLabel;
    private final SXRViewNode mDisconnectViewObject;
    private SXRViewNode mSubmenuObject;
    private Button mConnectedButton, mCancelButton, mDisconnectButton;
    private TextView mDisconnectViewMessage;
    private OnHudItemClicked mListener;
    private OnDisconnectClicked mDisconnectListener;
    private OnClickDisconnectViewHandler mDisconnectViewHandler;
    private Animation mOpenMenuHud;
    private Animation mCloseMenuHud;
    private Animation mBounce;
    private BounceInterpolator interpolator = new BounceInterpolator(0.1, 20);
    private WaveAnimation mNormalWaveAnimation, mWarningWaveAnimation, mCriticalWaveAnimation;

    private final EvaContext mEvaContext;
    private BounceView bounceView = new BounceView();

    public HudView(EvaContext evaContext) {
        super(evaContext);

        // Create a root layout to set the display metrics on it
        mRootLayout = new LinearLayout(evaContext.getActivity());
        final DisplayMetrics metrics = new DisplayMetrics();
        evaContext.getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        mEvaContext = evaContext;
        mRootLayout.setLayoutParams(new LinearLayout.LayoutParams(metrics.widthPixels, metrics.heightPixels));

        View.inflate(evaContext.getActivity(), R.layout.view_disconnect_sharing, mRootLayout);

        mListener = null;
        mDisconnectListener = null;
        mStartMenuObject = new SXRViewNode(evaContext.getSXRContext(),
                R.layout.hud_start_layout, startMenuInitEvents);
        mSubmenuObject = new SXRViewNode(evaContext.getSXRContext(),
                R.layout.actions_submenus_layout, startSubmenuInitEvents);
        mHudMenuObject = new SXRViewNode(evaContext.getSXRContext(),
                R.layout.hud_menus_layout, hudMenuInitEvents);
        mConnectedLabel = new SXRViewNode(evaContext.getSXRContext(),
                R.layout.share_connected_layout, connectButtonInitEvents);
        mDisconnectViewObject = new SXRViewNode(evaContext.getSXRContext(), mRootLayout);
        mRootLayout.post(() -> {
            disconnectViewInitEvents.onInitView(mDisconnectViewObject, mRootLayout);
            disconnectViewInitEvents.onStartRendering(mDisconnectViewObject, mRootLayout);
        });

    }

    @Override
    protected void onShow(SXRScene mainScene) {
        mConnectedLabel.setEnable(mEvaContext.getMode() != EvaConstants.SHARE_MODE_NONE);
        mStartMenuObject.setEnable(mEvaContext.getMode() != EvaConstants.SHARE_MODE_GUEST);
        mainScene.getMainCameraRig().addChildObject(this);
    }

    public void hideDisconnectView() {
        mDisconnectViewObject.setEnable(false);
    }

    public void showDisconnectView(@ConnectionMode int mode) {
        if (mode == ConnectionMode.SERVER) {
            mDisconnectViewMessage.setText(R.string.disconnect_host);
        } else {
            mDisconnectViewMessage.setText(R.string.disconnect_guest);
        }
        mDisconnectViewObject.setEnable(true);
    }

    public void hideConnectedLabel() {
        mConnectedLabel.setEnable(false);
    }

    public void showConnectedLabel() {
        mConnectedLabel.setEnable(true);
    }

    @Override
    protected void onHide(SXRScene mainScene) {
        mainScene.getMainCameraRig().removeChildObject(this);
    }

    public void setListener(OnHudItemClicked listener) {
        mListener = listener;
    }

    public void setDisconnectListener(OnDisconnectClicked listener) {
        mDisconnectListener = listener;
    }

    private class BounceView implements SXRDrawFrameListener {
        private float scaleX, scaleY, scaleZ;
        private float countTime;
        private final float DURATION = 0.5f;
        private final float TIME_OFFSET = 0.34f;
        private final float BOUNCE_LIMIT = 1.2f;
        private final float ACCELERATION = 7.8f;
        private SXRViewNode target;

        void startAnimation(SXRViewNode viewNode) {
            scaleX = viewNode.getTransform().getScaleX();
            scaleY = viewNode.getTransform().getScaleY();
            scaleZ = viewNode.getTransform().getScaleZ();
            countTime = 0f;
            target = viewNode;

            mEvaContext.getSXRContext().registerDrawFrameListener(this);
        }

        @Override
        public void onDrawFrame(float d) {
            if (countTime >= DURATION) {
                mEvaContext.getSXRContext().unregisterDrawFrameListener(this);
                target.getTransform().setScale(scaleX, scaleY, scaleZ);
                target = null;
            } else {
                float t = countTime - TIME_OFFSET;
                float s = -1f * ACCELERATION * t * t + BOUNCE_LIMIT;
                target.getTransform().setScale(scaleX * s, scaleY * s, scaleZ);
                countTime += d;
            }
        }
    }

    @Override
    public void onClick(final View view) {
        if (mListener == null) {
            return;
        }

        switch (view.getId()) {
            case R.id.btn_start_menu:
                setStateInMenuButtons();
                mMenuButton.setVisibility(View.GONE);
                mCloseButton.setVisibility(View.VISIBLE);
                bounceView.startAnimation(mStartMenuObject);
                mMenuOptionsHud.startAnimation(mOpenMenuHud);
                mMenuOptionsHud.setVisibility(View.VISIBLE);
                mHudMenuObject.setEnable(true);
                break;
            case R.id.btn_close:
                closeMenu();
                break;
            case R.id.btn_clean:
                mCleanButton.startAnimation(mBounce);
                mBounce.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mEvaContext.getSXRContext().runOnGlThread(() -> mListener.onCleanClicked());
                        mBounce.setAnimationListener(null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mCleanButton.post(this::closeMenu);
                break;
            case R.id.btn_fetchbone:
                mPlayBoneButton.startAnimation(mBounce);
                mPlayBoneButton.setActivated(!mPlayBoneButton.isActivated());
                mBounce.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mEvaContext.getSXRContext().runOnGlThread(() -> mListener.onBoneClicked());
                        mBounce.setAnimationListener(null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                break;
            case R.id.btn_bed:
                mBedButton.startAnimation(mBounce);
                mBounce.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mEvaContext.getSXRContext().runOnGlThread(() -> mListener.onBedClicked());
                        mBounce.setAnimationListener(null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                break;
            case R.id.btn_hydrant:
                mHydrantButton.startAnimation(mBounce);
                mBounce.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mEvaContext.getSXRContext().runOnGlThread(() -> mListener.onHydrantClicked());
                        mBounce.setAnimationListener(null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                break;
            case R.id.btn_bowl:
                mBowlButton.startAnimation(mBounce);
                mBounce.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mEvaContext.getSXRContext().runOnGlThread(() -> mListener.onBowlClicked());
                        mBounce.setAnimationListener(null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                break;
            case R.id.btn_shareanchor:
                mShareAnchorButton.startAnimation(mBounce);
                mBounce.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mEvaContext.getSXRContext().runOnGlThread(() -> mListener.onShareAnchorClicked());
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                break;
            case R.id.btn_camera:
                mCameraButton.startAnimation(mBounce);
                mBounce.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mEvaContext.getSXRContext().runOnGlThread(() -> mListener.onCameraClicked());
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                break;
            case R.id.btn_connected:
                mEvaContext.getSXRContext().runOnGlThread(() -> mListener.onConnectedClicked());
                break;
            case R.id.btn_about:
                mAboutButton.startAnimation(mBounce);
                mBounce.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mEvaContext.getSXRContext().runOnGlThread(() -> mListener.onAbout());
                        mBounce.setAnimationListener(null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mAboutButton.post(this::closeMenu);
                break;
            case R.id.btn_health_preferences:
                mEvaContext.getSXRContext().runOnGlThread(() -> mListener.onHealthPreferencesClicked());
                mAboutButton.post(this::closeMenu);
                break;
            default:
                Log.d(TAG, "Invalid Option");
        }
    }

    public void deactivateBoneButton() {
        mPlayBoneButton.setActivated(false);
    }

    public void setStateInMenuButtons() {
        final int shareMode = mEvaContext.getMode();
        mCleanButton.setEnabled(shareMode == EvaConstants.SHARE_MODE_NONE);
        mCleanButton.setClickable(shareMode == EvaConstants.SHARE_MODE_NONE);
        mShareAnchorButton.setEnabled(shareMode == EvaConstants.SHARE_MODE_NONE);
    }

    public void setStateInActionButtons() {
        final int shareMode = mEvaContext.getMode();

        if (shareMode == EvaConstants.SHARE_MODE_NONE && mSubmenuObject != null) {
            getSXRContext().runOnGlThread(() -> mSubmenuObject = new SXRViewNode(mEvaContext.getSXRContext(),
                    R.layout.actions_submenus_layout, startSubmenuInitEvents));
        }
    }

    private void setDisableButtonActions() {
        final int shareMode = mEvaContext.getMode();
        if (shareMode != EvaConstants.SHARE_MODE_NONE) {
            mHydrantButton.setImageResource(R.drawable.ic_hydrant_disabled);
            mHydrantButton.setClickable(false);
            mHydrantButton.setEnabled(false);
            mBedButton.setImageResource(R.drawable.ic_bed_disabled);
            mBedButton.setClickable(false);
            mBedButton.setEnabled(false);
            mBowlButton.setImageResource(R.drawable.ic_bowl_disabled);
            mBowlButton.setClickable(false);
            mBowlButton.setEnabled(false);
        }
    }

    public void closeMenu() {
        mMenuButton.setVisibility(View.VISIBLE);
        mCloseButton.setVisibility(View.GONE);
        bounceView.startAnimation(mStartMenuObject);
        mMenuOptionsHud.startAnimation(mCloseMenuHud);
        mMenuOptionsHud.setVisibility(View.GONE);
        mHudMenuObject.setEnable(false);
    }

    IViewEvents hudMenuInitEvents = new IViewEvents() {
        @Override
        public void onInitView(SXRViewNode sxrViewNode, View view) {
            mMenuOptionsHud = view.findViewById(R.id.menuHud);
            mCleanButton = view.findViewById(R.id.btn_clean);
            mShareAnchorButton = view.findViewById(R.id.btn_shareanchor);
            mCameraButton = view.findViewById(R.id.btn_camera);
            mAboutButton = view.findViewById(R.id.btn_about);
            mHealthPreferences = view.findViewById(R.id.btn_health_preferences);
            mHealthPreferences.setVisibility(BuildConfig.ENABLE_HEALTH_PREFERENCES
                    ? View.VISIBLE : View.GONE);

            mCleanButton.setOnClickListener(HudView.this);
            mShareAnchorButton.setOnClickListener(HudView.this);
            mCameraButton.setOnClickListener(HudView.this);
            mAboutButton.setOnClickListener(HudView.this);
            mHealthPreferences.setOnClickListener(HudView.this);

            mOpenMenuHud = AnimationUtils.loadAnimation(mEvaContext.getActivity(), R.anim.open);
            mCloseMenuHud = AnimationUtils.loadAnimation(mEvaContext.getActivity(), R.anim.close);
            mBounce = AnimationUtils.loadAnimation(mEvaContext.getActivity(), R.anim.bounce);
            mBounce.setInterpolator(interpolator);
        }

        @Override
        public void onStartRendering(SXRViewNode sxrViewNode, View view) {
            sxrViewNode.setTextureBufferSize(EvaConstants.TEXTURE_BUFFER_SIZE);
            sxrViewNode.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
            LayoutViewUtils.setWorldPosition(mEvaContext.getMainScene(),
                    sxrViewNode, 595f, 20f, 38f, 268f);
            sxrViewNode.setEnable(false);
            addChildObject(sxrViewNode);
        }
    };

    IViewEvents startMenuInitEvents = new IViewEvents() {
        @Override
        public void onInitView(SXRViewNode sxrViewNode, View view) {
            mMenuButton = view.findViewById(R.id.btn_start_menu);
            mCloseButton = view.findViewById(R.id.btn_close);
            mMenuButton.setOnClickListener(HudView.this);
            mCloseButton.setOnClickListener(HudView.this);
        }

        @Override
        public void onStartRendering(SXRViewNode sxrViewNode, View view) {
            sxrViewNode.setTextureBufferSize(EvaConstants.TEXTURE_BUFFER_SIZE);
            sxrViewNode.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
            LayoutViewUtils.setWorldPosition(mEvaContext.getMainScene(),
                    sxrViewNode, 590f, 295f, 48.5f, 48.5f);
            addChildObject(sxrViewNode);
        }
    };

    IViewEvents connectButtonInitEvents = new IViewEvents() {
        @Override
        public void onInitView(SXRViewNode sxrViewNode, View view) {
            mConnectedButton = view.findViewById(R.id.btn_connected);
            mConnectedButton.setOnClickListener(HudView.this);
        }

        @Override
        public void onStartRendering(SXRViewNode sxrViewNode, View view) {
            sxrViewNode.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
            LayoutViewUtils.setWorldPosition(mEvaContext.getMainScene(),
                    sxrViewNode, 4.0f, 4.0f, 144.0f, 44.0f);
            addChildObject(sxrViewNode);
        }
    };

    IViewEvents disconnectViewInitEvents = new IViewEvents() {
        @Override
        public void onInitView(SXRViewNode sxrViewNode, View view) {
            mDisconnectViewMessage = view.findViewById(R.id.disconnect_message_text);
            mCancelButton = view.findViewById(R.id.button_cancel);
            mDisconnectButton = view.findViewById(R.id.button_disconnect);
            mDisconnectViewHandler = new OnClickDisconnectViewHandler();
            mCancelButton.setOnClickListener(mDisconnectViewHandler);
            mDisconnectButton.setOnClickListener(mDisconnectViewHandler);
        }

        @Override
        public void onStartRendering(SXRViewNode sxrViewNode, View view) {
            sxrViewNode.setTextureBufferSize(EvaConstants.TEXTURE_BUFFER_SIZE);
            sxrViewNode.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
            sxrViewNode.getTransform().setPosition(0.0f, 0.0f, -0.74f);
            sxrViewNode.setEnable(false);
            addChildObject(sxrViewNode);
        }
    };

    IViewEvents startSubmenuInitEvents = new IViewEvents() {

        @Override
        public void onInitView(SXRViewNode sxrViewNode, View view) {
            mPlayBoneButton = view.findViewById(R.id.btn_fetchbone);
            mHydrantButton = view.findViewById(R.id.btn_hydrant);
            mBedButton = view.findViewById(R.id.btn_bed);
            mBowlButton = view.findViewById(R.id.btn_bowl);

            setDisableButtonActions();

            mNormalWaveAnimation = new WaveAnimation(mEvaContext.getActivity(), R.drawable.bg_button_normal);

            mBowlButton.setBackground(mNormalWaveAnimation);
            mNormalWaveAnimation.animateTo(1f);

            mWarningWaveAnimation = new WaveAnimation(mEvaContext.getActivity(), R.drawable.bg_button_warning);
            mHydrantButton.setBackground(mWarningWaveAnimation);
            mWarningWaveAnimation.setProgress(1f);
            // new android.os.Handler().postDelayed(() -> mWarningWaveAnimation.animateTo(0.0f), 3000);

            mCriticalWaveAnimation = new WaveAnimation(mEvaContext.getActivity(), R.drawable.bg_button_critical);
            mBedButton.setBackground(mCriticalWaveAnimation);
            mCriticalWaveAnimation.setProgress(1f);


            mPlayBoneButton.setOnClickListener(HudView.this);
            mHydrantButton.setOnClickListener(HudView.this);
            mBedButton.setOnClickListener(HudView.this);
            mBowlButton.setOnClickListener(HudView.this);

        }

        @Override
        public void onStartRendering(SXRViewNode sxrViewNode, View view) {
            sxrViewNode.setTextureBufferSize(EvaConstants.TEXTURE_BUFFER_SIZE);
            sxrViewNode.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.TRANSPARENT);
            LayoutViewUtils.setWorldPosition(mEvaContext.getMainScene(),
                    sxrViewNode, 100f, 187f, 40f, 270f);
            addChildObject(sxrViewNode);
        }
    };

    private class OnClickDisconnectViewHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mDisconnectListener == null) {
                return;
            }

            switch (v.getId()) {
                case R.id.button_cancel:
                    mDisconnectListener.onCancel();
                    break;
                case R.id.button_disconnect:
                    mDisconnectListener.onDisconnect();
                    break;
                default:
                    Log.d(TAG, "invalid ID in disconnect view handler");
            }
        }
    }

    private class BounceInterpolator implements Interpolator {
        private double mAmplitude;
        private double mFrequency;

        BounceInterpolator(double amplitude, double frequency) {
            mAmplitude = amplitude;
            mFrequency = frequency;
        }

        @Override
        public float getInterpolation(float time) {
            return (float) (-1 * Math.pow(Math.E, -time / mAmplitude) *
                    Math.cos(mFrequency * time) + 1);
        }
    }


}
