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

package br.org.sidia.eva.mode;

import android.annotation.SuppressLint;
import android.content.res.Configuration;

import com.samsungxr.SXRCameraRig;
import com.samsungxr.utility.Log;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import br.org.sidia.eva.EvaContext;
import br.org.sidia.eva.actions.EvaActions;
import br.org.sidia.eva.actions.IEvaAction;
import br.org.sidia.eva.actions.TimerActionEvent;
import br.org.sidia.eva.actions.TimerActionType;
import br.org.sidia.eva.actions.TimerActionsController;
import br.org.sidia.eva.character.CharacterController;
import br.org.sidia.eva.constant.EvaConstants;
import br.org.sidia.eva.constant.EvaObjectType;
import br.org.sidia.eva.mainview.IAboutView;
import br.org.sidia.eva.mainview.ICleanView;
import br.org.sidia.eva.mainview.MainViewController;
import br.org.sidia.eva.manager.connection.EvaConnectionManager;
import br.org.sidia.eva.manager.connection.event.EvaConnectionEvent;
import br.org.sidia.eva.service.share.SharedMixedReality;
import br.org.sidia.eva.util.EventBusUtils;

import static br.org.sidia.eva.manager.connection.IEvaConnectionManager.EVENT_ALL_CONNECTIONS_LOST;

public class HudMode extends BaseEvaMode {
    private OnModeChange mModeChangeListener;
    private HudView mHudView;
    private MainViewController mMainViewController = null;

    private EvaConnectionManager mConnectionManager;
    private SharedMixedReality mSharedMixedReality;
    private CharacterController mEvaController;
    private VirtualObjectController mVirtualObjectController;

    public HudMode(EvaContext evaContext, CharacterController evaController, OnModeChange listener) {
        super(evaContext, new HudView(evaContext));
        mModeChangeListener = listener;
        mEvaController = evaController;

        mHudView = (HudView) mModeScene;
        mHudView.setListener(new OnHudItemClickedHandler());
        mHudView.setDisconnectListener(new OnDisconnectClickedHandler());

        mConnectionManager = (EvaConnectionManager) EvaConnectionManager.getInstance();
        mSharedMixedReality = evaContext.getMixedReality();

        mVirtualObjectController = new VirtualObjectController(evaContext, evaController);
    }

    @Override
    protected void onEnter() {
        EventBusUtils.register(this);
    }

    @Override
    protected void onExit() {
        EventBusUtils.unregister(this);
        mVirtualObjectController.hideObject();
    }

    @Override
    protected void onHandleOrientation(SXRCameraRig cameraRig) {
        if (mEvaController.isPlaying()) {
            float rotationRoll = cameraRig.getTransform().getRotationRoll();
            if (rotationRoll <= -89.0f || rotationRoll >= 89.0f) {
                mEvaContext.getBallThrowHandlerHandler().rotateBone(Configuration.ORIENTATION_PORTRAIT);
            } else {
                mEvaContext.getBallThrowHandlerHandler().rotateBone(Configuration.ORIENTATION_LANDSCAPE);
            }
        }
    }

    private class OnHudItemClickedHandler implements OnHudItemClicked {

        @Override
        public void onBoneClicked() {
            mVirtualObjectController.hideObject();
            if (mEvaController.isPlaying()) {
                mEvaController.stopBone();
                Log.d(TAG, "Stop Bone");
            } else {
                mEvaController.playBone();
                Log.d(TAG, "Play Bone");
            }
            mEvaController.setCurrentAction(EvaActions.IDLE.ID);
        }

        @Override
        public void onBedClicked() {
            Log.d(TAG, "Action: go to bed");
            if (mEvaController.isPlaying()) {
                mEvaController.stopBone();
                mHudView.deactivateBoneButton();
            }
            mVirtualObjectController.showObject(EvaObjectType.BED);
        }

        @Override
        public void onHydrantClicked() {
            Log.d(TAG, "Action: go to hydrant");
            if (mEvaController.isPlaying()) {
                mEvaController.stopBone();
                mHudView.deactivateBoneButton();
            }
            mVirtualObjectController.showObject(EvaObjectType.HYDRANT);
        }

        @Override
        public void onBowlClicked() {
            Log.d(TAG, "Action: go to bowl");
            if (mEvaController.isPlaying()) {
                mEvaController.stopBone();
                mHudView.deactivateBoneButton();
            }
            mVirtualObjectController.showObject(EvaObjectType.BOWL);
        }

        @Override
        public void onShareAnchorClicked() {
            mModeChangeListener.onShareAnchor();
            Log.d(TAG, "Share Anchor Mode");
        }

        @Override
        public void onCleanClicked() {
            showCleanView();
            Log.d(TAG, "Clean button clicked");
        }

        @Override
        public void onCameraClicked() {
            mModeChangeListener.onScreenshot();
            Log.d(TAG, "Camera Mode");
        }

        @Override
        public void onConnectedClicked() {
            Log.d(TAG, "Connected label clicked");
            mEvaContext.getActivity().runOnUiThread(() -> {
                mHudView.showDisconnectView(mConnectionManager.getConnectionMode());
                mHudView.hideConnectedLabel();
            });
        }

        @Override
        public void onAbout() {
            Log.d(TAG, "About clicked");
            showAboutView();
        }
    }

    private void showCleanView() {
        if (mMainViewController == null) {
            mMainViewController = new MainViewController(mEvaContext);
            mMainViewController.onShow(mEvaContext.getMainScene());
            ICleanView iCleanView = mMainViewController.makeView(ICleanView.class);

            iCleanView.setOnCancelClickListener(view -> {
                if (mMainViewController != null) {
                    mMainViewController.onHide(mEvaContext.getMainScene());
                    mMainViewController = null;
                }
            });
            iCleanView.setOnConfirmClickListener(view -> {
                Log.d(TAG, "Cleaning scene");
                mEvaController.exit();
                mEvaContext.getPlaneHandler().resetPlanes();
                if (mMainViewController != null) {
                    mMainViewController.onHide(mEvaContext.getMainScene());
                    mMainViewController = null;
                }
            });

            iCleanView.show();
        }
    }

    public boolean isPromptEnabled(){
        return mMainViewController != null;
    }

    private void showAboutView() {
        if (mMainViewController == null) {
            mMainViewController = new MainViewController(mEvaContext);
            mMainViewController.onShow(mEvaContext.getMainScene());
            IAboutView iAboutView = mMainViewController.makeView(IAboutView.class);

            iAboutView.setBackClickListener(view -> {
                if (mMainViewController != null) {
                    mMainViewController.onHide(mEvaContext.getMainScene());
                    mMainViewController = null;
                }
            });

            iAboutView.show();
        }
    }

    private class OnDisconnectClickedHandler implements OnDisconnectClicked {
        @Override
        public void onCancel() {
            mEvaContext.getActivity().runOnUiThread(() -> {
                mHudView.hideDisconnectView();
                mHudView.showConnectedLabel();
            });
        }

        @Override
        public void onDisconnect() {
            evaExit();
            mSharedMixedReality.stopSharing();
            mConnectionManager.disconnect();
            mEvaContext.getActivity().runOnUiThread(() -> {
                mHudView.hideDisconnectView();
                mHudView.hideConnectedLabel();
                mHudView.setStateInActionButtons();
                mHudView.setStateInMenuButtons();
            });
            mEvaController.stopBone();
        }
    }

    @SuppressLint("SwitchIntDef")
    @Subscribe
    public void handleConnectionEvent(EvaConnectionEvent message) {
        if (message.getType() == EVENT_ALL_CONNECTIONS_LOST) {
            evaExit();
            mEvaContext.getActivity().runOnUiThread(() -> {
                mHudView.hideDisconnectView();
                mHudView.hideConnectedLabel();
                mHudView.setStateInActionButtons();
                mHudView.setStateInMenuButtons();
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvaActionChanged(IEvaAction action) {
        switch (action.id()) {
            case EvaActions.IDLE.ID:
                mVirtualObjectController.hideObject();
                break;
            case EvaActions.DRINK_LOOP.ID:
                TimerActionsController.startTimer(EvaActions.DRINK_LOOP.ID, TimerActionType.DRINK_NORMAL);
                break;
            case EvaActions.HYDRANT_LOOP.ID:
                TimerActionsController.startTimer(EvaActions.HYDRANT_LOOP.ID, TimerActionType.PEE_NORMAL);
                break;
            case EvaActions.SLEEP_LOOP.ID:
                TimerActionsController.startTimer(EvaActions.SLEEP_LOOP.ID, TimerActionType.SLEEP_NORMAL);
                break;
        }
    }

    @Subscribe
    public void onActionTimerFired(TimerActionEvent event) {
        switch (event.getActionType()) {
            case EvaActions.DRINK_LOOP.ID:
                mEvaController.setCurrentAction(EvaActions.DRINK_EXIT.ID);
                break;
            case EvaActions.HYDRANT_LOOP.ID:
                mEvaController.setCurrentAction(EvaActions.HYDRANT_EXIT.ID);
                break;
            case EvaActions.SLEEP_LOOP.ID:
                mEvaController.setCurrentAction(EvaActions.SLEEP_EXIT.ID);
                break;
            default:
                Log.w(TAG, "event type not handled: " + event.getActionType());
        }
    }

    private void evaExit() {
        if (mEvaContext.getMode() == EvaConstants.SHARE_MODE_GUEST) {
            //TODO: after finish the sharing anchor experience as guest, the scene will be reseted
            // and the user should be notified to detect planes and positioning the Eva again
            mEvaController.exit();
        }
    }

}
