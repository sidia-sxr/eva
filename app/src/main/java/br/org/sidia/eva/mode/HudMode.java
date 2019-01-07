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
import android.util.Log;

import com.samsungxr.SXRCameraRig;
import br.org.sidia.eva.PetContext;
import br.org.sidia.eva.character.CharacterController;
import br.org.sidia.eva.constant.EvaObjectType;
import br.org.sidia.eva.constant.PetConstants;
import br.org.sidia.eva.mainview.IAboutView;
import br.org.sidia.eva.mainview.ICleanView;
import br.org.sidia.eva.mainview.MainViewController;
import br.org.sidia.eva.manager.connection.PetConnectionManager;
import br.org.sidia.eva.manager.connection.event.PetConnectionEvent;
import br.org.sidia.eva.movement.IPetAction;
import br.org.sidia.eva.movement.PetActions;
import br.org.sidia.eva.service.share.SharedMixedReality;
import br.org.sidia.eva.util.EventBusUtils;

import org.greenrobot.eventbus.Subscribe;

import static br.org.sidia.eva.manager.connection.IPetConnectionManager.EVENT_ALL_CONNECTIONS_LOST;

public class HudMode extends BasePetMode {
    private OnModeChange mModeChangeListener;
    private HudView mHudView;
    private MainViewController mMainViewController = null;

    private PetConnectionManager mConnectionManager;
    private SharedMixedReality mSharedMixedReality;
    private CharacterController mPetController;
    private VirtualObjectController mVirtualObjectController;

    public HudMode(PetContext petContext, CharacterController petController, OnModeChange listener) {
        super(petContext, new HudView(petContext));
        mModeChangeListener = listener;
        mPetController = petController;

        mHudView = (HudView) mModeScene;
        mHudView.setListener(new OnHudItemClickedHandler());
        mHudView.setDisconnectListener(new OnDisconnectClickedHandler());

        mConnectionManager = (PetConnectionManager) PetConnectionManager.getInstance();
        mSharedMixedReality = petContext.getMixedReality();

        mVirtualObjectController = new VirtualObjectController(petContext, petController);
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
        if (mPetController.isPlaying()) {
            float rotationRoll = cameraRig.getTransform().getRotationRoll();
            if (rotationRoll <= -89.0f || rotationRoll >= 89.0f) {
                mPetContext.getBallThrowHandlerHandler().rotateBone(Configuration.ORIENTATION_PORTRAIT);
            } else {
                mPetContext.getBallThrowHandlerHandler().rotateBone(Configuration.ORIENTATION_LANDSCAPE);
            }
        }
    }

    private class OnHudItemClickedHandler implements OnHudItemClicked {

        @Override
        public void onBoneClicked() {
            mVirtualObjectController.hideObject();
            if (mPetController.isPlaying()) {
                mPetController.stopBone();
                Log.d(TAG, "Stop Bone");
            } else {
                mPetController.playBone();
                Log.d(TAG, "Play Bone");
            }
            mPetController.setCurrentAction(PetActions.IDLE.ID);
        }

        @Override
        public void onBedClicked() {
            Log.d(TAG, "Action: go to bed");
            if (mPetController.isPlaying()) {
                mPetController.stopBone();
                mHudView.deactivateBoneButton();
            }
            mVirtualObjectController.showObject(EvaObjectType.BED);
        }

        @Override
        public void onHydrantClicked() {
            Log.d(TAG, "Action: go to hydrant");
            if (mPetController.isPlaying()) {
                mPetController.stopBone();
                mHudView.deactivateBoneButton();
            }
            mVirtualObjectController.showObject(EvaObjectType.HYDRANT);
        }

        @Override
        public void onBowlClicked() {
            Log.d(TAG, "Action: go to bowl");
            if (mPetController.isPlaying()) {
                mPetController.stopBone();
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
            mPetContext.getActivity().runOnUiThread(() -> {
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
            mMainViewController = new MainViewController(mPetContext);
            mMainViewController.onShow(mPetContext.getMainScene());
            ICleanView iCleanView = mMainViewController.makeView(ICleanView.class);

            iCleanView.setOnCancelClickListener(view -> {
                if (mMainViewController != null) {
                    mMainViewController.onHide(mPetContext.getMainScene());
                    mMainViewController = null;
                }
            });
            iCleanView.setOnConfirmClickListener(view -> {
                Log.d(TAG, "Cleaning scene");
                mPetController.exit();
                mPetContext.getPlaneHandler().resetPlanes();
                if (mMainViewController != null) {
                    mMainViewController.onHide(mPetContext.getMainScene());
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
            mMainViewController = new MainViewController(mPetContext);
            mMainViewController.onShow(mPetContext.getMainScene());
            IAboutView iAboutView = mMainViewController.makeView(IAboutView.class);

            iAboutView.setBackClickListener(view -> {
                if (mMainViewController != null) {
                    mMainViewController.onHide(mPetContext.getMainScene());
                    mMainViewController = null;
                }
            });

            iAboutView.show();
        }
    }

    private class OnDisconnectClickedHandler implements OnDisconnectClicked {
        @Override
        public void onCancel() {
            mPetContext.getActivity().runOnUiThread(() -> {
                mHudView.hideDisconnectView();
                mHudView.showConnectedLabel();
            });
        }

        @Override
        public void onDisconnect() {
            petExit();
            mSharedMixedReality.stopSharing();
            mConnectionManager.disconnect();
            mPetContext.getActivity().runOnUiThread(() -> {
                mHudView.hideDisconnectView();
                mHudView.hideConnectedLabel();
                mHudView.setStateInActionButtons();
                mHudView.setStateInMenuButtons();
            });
            mPetController.stopBone();
        }
    }

    @SuppressLint("SwitchIntDef")
    @Subscribe
    public void handleConnectionEvent(PetConnectionEvent message) {
        if (message.getType() == EVENT_ALL_CONNECTIONS_LOST) {
            petExit();
            mPetContext.getActivity().runOnUiThread(() -> {
                mHudView.hideDisconnectView();
                mHudView.hideConnectedLabel();
                mHudView.setStateInActionButtons();
                mHudView.setStateInMenuButtons();
            });
        }
    }

    @Subscribe
    public void onPetActionChanged(IPetAction action) {
        if (action.id() == PetActions.IDLE.ID) {
            mVirtualObjectController.hideObject();
        }
    }

    private void petExit() {
        if (mPetContext.getMode() == PetConstants.SHARE_MODE_GUEST) {
            //TODO: after finish the sharing anchor experience as guest, the scene will be reseted
            // and the user should be notified to detect planes and positioning the pet again
            mPetController.exit();
        }
    }

}
