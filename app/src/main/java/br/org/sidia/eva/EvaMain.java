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

package br.org.sidia.eva;

import com.samsungxr.ITouchEvents;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPicker;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.io.SXRGazeCursorController;
import com.samsungxr.io.SXRInputManager;
import com.samsungxr.mixedreality.IMixedReality;
import com.samsungxr.mixedreality.IMixedRealityEvents;
import com.samsungxr.mixedreality.SXRMixedReality;
import com.samsungxr.mixedreality.SXRPlane;
import com.samsungxr.nodes.SXRViewNode;
import com.samsungxr.utility.Log;

import org.greenrobot.eventbus.Subscribe;

import java.util.Arrays;
import java.util.EnumSet;

import br.org.sidia.eva.actions.EvaActions;
import br.org.sidia.eva.animations.HandAnimation;
import br.org.sidia.eva.character.CharacterController;
import br.org.sidia.eva.constant.EvaConstants;
import br.org.sidia.eva.custom.TouchEventsAdapter;
import br.org.sidia.eva.mainview.IExitView;
import br.org.sidia.eva.mainview.MainViewController;
import br.org.sidia.eva.manager.connection.event.EvaConnectionEvent;
import br.org.sidia.eva.mode.HudMode;
import br.org.sidia.eva.mode.IEvaMode;
import br.org.sidia.eva.mode.ILoadEvents;
import br.org.sidia.eva.mode.OnBackToHudModeListener;
import br.org.sidia.eva.mode.OnModeChange;
import br.org.sidia.eva.mode.photo.ScreenshotMode;
import br.org.sidia.eva.mode.sharinganchor.SharingAnchorMode;
import br.org.sidia.eva.service.share.SharedMixedReality;
import br.org.sidia.eva.util.EventBusUtils;
import br.org.sidia.eva.view.shared.IConnectionFinishedView;

import static br.org.sidia.eva.manager.connection.IEvaConnectionManager.EVENT_ALL_CONNECTIONS_LOST;


public class EvaMain extends DisableNativeSplashScreen {

    private static final String TAG = "EvaMain";

    private EvaContext mEvaContext;
    private PlaneHandler mPlaneHandler;
    private PointCloudHandler mPointCloudHandler;

    private IEvaMode mCurrentMode;
    private HandlerModeChange mHandlerModeChange;
    private HandlerBackToHud mHandlerBackToHud;

    private CharacterController mEva = null;
    private SXRCursorController mCursorController = null;

    private CurrentSplashScreen mCurrentSplashScreen;
    private SharedMixedReality mSharedMixedReality;

    private MainViewController mMainViewController = null;

    private ViewInitialMessage mViewInitialMessage;
    private HandAnimation mHandAnimation = null;

    EvaMain(EvaContext evaContext) {
        mEvaContext = evaContext;
        EventBusUtils.register(this);
    }

    @Override
    public void onInit(final SXRContext sxrContext) throws Throwable {
        super.onInit(sxrContext);

        mCurrentSplashScreen = new CurrentSplashScreen(sxrContext);
        mCurrentSplashScreen.onShow();

        mEvaContext.init(sxrContext);

        mHandlerModeChange = new HandlerModeChange();
        mHandlerBackToHud = new HandlerBackToHud();

        mPlaneHandler = new PlaneHandler(this, mEvaContext);
        mPointCloudHandler = new PointCloudHandler(mEvaContext);

        mSharedMixedReality = mEvaContext.getMixedReality();

        mEvaContext.registerPlaneListener(mPlaneHandler);
        mSharedMixedReality.getEventReceiver().addListener(mMixedRealityHandler);
        mSharedMixedReality.getEventReceiver().addListener(mPointCloudHandler);
        mEvaContext.getMixedReality().resume();


        mEva = new CharacterController(mEvaContext);
        mEva.load(new ILoadEvents() {
            @Override
            public void onSuccess() {
                // Will wet eva's scene as the main scene
                mCurrentSplashScreen.onHide(mEvaContext.getMainScene());
                //Show initial message
                mViewInitialMessage = new ViewInitialMessage(mEvaContext);
                mViewInitialMessage.onShow(mEvaContext.getMainScene());

                // Set plane handler in eva context
                mEvaContext.setPlaneHandler(mPlaneHandler);

                // Set eva controller in eva context
                mEvaContext.setEvaController(mEva);

            }

            @Override
            public void onFailure() {
                mEvaContext.getActivity().finish();
            }
        });
    }

    void onARInit(IMixedReality mr) {
        mCursorController = null;
        SXRInputManager inputManager = mEvaContext.getSXRContext().getInputManager();
        final int cursorDepth = 5;
        final EnumSet<SXRPicker.EventOptions> eventOptions = EnumSet.of(
                SXRPicker.EventOptions.SEND_PICK_EVENTS,
                SXRPicker.EventOptions.SEND_TOUCH_EVENTS,
                SXRPicker.EventOptions.SEND_TO_LISTENERS,
                SXRPicker.EventOptions.SEND_TO_HIT_OBJECT);

        inputManager.selectController((newController, oldController) -> {
            if (mCursorController != null) {
                mCursorController.removePickEventListener(mTouchEventsHandler);
            }
            newController.addPickEventListener(mTouchEventsHandler);
            newController.setCursorDepth(cursorDepth);
            newController.setCursorControl(SXRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
            newController.getPicker().setPickClosest(false);
            newController.getPicker().setEventOptions(eventOptions);
            mCursorController = newController;
            if (newController instanceof SXRGazeCursorController) {
                ((SXRGazeCursorController) newController).setTouchScreenDepth(mr.getScreenDepth());
                // Don't show any cursor
                newController.setCursor(null);
            }
        });

        mr.setPlaneFindingMode(SXRMixedReality.PlaneFindingMode.HORIZONTAL);
    }

    void resume() {
        EventBusUtils.register(this);
    }

    void pause() {
        EventBusUtils.unregister(this);
    }

    private void showViewExit() {
        if (mMainViewController == null) {
            mMainViewController = new MainViewController(mEvaContext);
            mMainViewController.onShow(mEvaContext.getMainScene());
            IExitView iExitView = mMainViewController.makeView(IExitView.class);

            iExitView.setOnCancelClickListener(view -> {
                if (mMainViewController != null) {
                    mMainViewController.onHide(mEvaContext.getMainScene());
                    mMainViewController = null;
                }
            });
            iExitView.setOnConfirmClickListener(view -> {
                getSXRContext().getActivity().finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            });

            iExitView.show();
        }
    }

    private void showViewConnectionFinished(@EvaConstants.ShareMode int mode) {

        mMainViewController = new MainViewController(mEvaContext);
        mMainViewController.onShow(mEvaContext.getMainScene());

        IConnectionFinishedView iFinishedView =
                mMainViewController.makeView(IConnectionFinishedView.class);

        iFinishedView.setOkClickListener(view -> {
            mMainViewController.onHide(mEvaContext.getMainScene());
            mMainViewController = null;
            if (mode == EvaConstants.SHARE_MODE_GUEST) {
                onShowHandAnimate();
            }
        });

        String text = getSXRContext().getActivity().getString(
                mode == EvaConstants.SHARE_MODE_GUEST
                        ? R.string.view_host_disconnected
                        : R.string.view_guests_disconnected);
        iFinishedView.setStatusText(text);
        iFinishedView.show();
    }

    @Override
    public boolean onBackPress() {
        if (mCurrentMode instanceof SharingAnchorMode || mCurrentMode instanceof ScreenshotMode) {
            getSXRContext().runOnGlThread(() -> mHandlerBackToHud.OnBackToHud());
        }

        if (mCurrentMode instanceof HudMode || mCurrentMode == null) {
            if (mCurrentMode != null && !((HudMode) mCurrentMode).isPromptEnabled()) {
                getSXRContext().runOnGlThread(this::showViewExit);
            }
        }
        return true;
    }

    @Subscribe
    public void handleConnectionEvent(EvaConnectionEvent message) {
        if (message.getType() == EVENT_ALL_CONNECTIONS_LOST) {
            if (mCurrentMode instanceof HudMode) {
                int mode = mSharedMixedReality.getMode();
                getSXRContext().runOnGlThread(() -> showViewConnectionFinished(mode));
                mSharedMixedReality.stopSharing();
                mEva.stopBone();
                if (mode == EvaConstants.SHARE_MODE_GUEST) {
                    mEva.exit();
                }
            }
        }
    }

    @Subscribe
    public void handlePlaneDetected(SXRPlane plane) {
        mViewInitialMessage.onHide(mEvaContext.getMainScene());

        if (mHandAnimation == null && mCurrentMode == null) {
            onShowHandAnimate();
        }
    }

    @Override
    public void onStep() {
        super.onStep();
        if (mCurrentMode != null) {
            mCurrentMode.handleOrientation();
        }
    }

    @Subscribe
    public void handleBallEvent(BallThrowHandlerEvent event) {
        if (event.getPerformedAction().equals(BallThrowHandlerEvent.THROWN)) {
            mEva.setCurrentAction(EvaActions.TO_BALL.ID);
        }
    }

    private void onShowHandAnimate() {
        mHandAnimation = new HandAnimation(mEvaContext.getSXRContext(), 500);
        mHandAnimation.setLightPosition(0, 0, -0.74f);
        mHandAnimation.setLightSize(1);
        mEvaContext.getSXRContext().getAnimationEngine().start(mHandAnimation);
    }

    public class HandlerModeChange implements OnModeChange {

        @Override
        public void onShareAnchor() {
            if (mCurrentMode instanceof SharingAnchorMode) {
                return;
            }

            if (mCurrentMode != null) {
                mCurrentMode.exit();
            }

            mCurrentMode = new SharingAnchorMode(mEvaContext, mHandlerBackToHud);
            mCurrentMode.enter();
            mEva.stopBone();
            mEva.setCurrentAction(EvaActions.IDLE.ID);
        }

        @Override
        public void onScreenshot() {

            if (mCurrentMode instanceof ScreenshotMode) {
                return;
            }

            if (mCurrentMode != null) {
                mCurrentMode.exit();
            }

            mCurrentMode = new ScreenshotMode(mEvaContext, mHandlerBackToHud);
            mCurrentMode.enter();
        }
    }

    public class HandlerBackToHud implements OnBackToHudModeListener {

        @Override
        public void OnBackToHud() {
            if (mCurrentMode instanceof ScreenshotMode) {
                mCursorController.addPickEventListener(mTouchEventsHandler);
            }

            mCurrentMode.exit();
            mCurrentMode = new HudMode(mEvaContext, mEva, mHandlerModeChange);
            mCurrentMode.enter();

            mEva.setCurrentAction(EvaActions.IDLE.ID);
        }
    }

    /**
     * Checks if the given picked object contains some {@link SXRViewNode}
     *
     * @param sxrPickedObject Holds the picked objects array
     * @return Whether exists some clicked object of type {@link SXRViewNode}
     */
    private boolean hasViewNode(SXRPicker.SXRPickedObject sxrPickedObject) {
        SXRPicker.SXRPickedObject[] picked = sxrPickedObject.getPicker().getPicked();
        if (picked != null) {
            return Arrays.stream(picked).anyMatch(p -> p.hitObject instanceof SXRViewNode);
        }
        return false;
    }

    private ITouchEvents mTouchEventsHandler = new TouchEventsAdapter() {

        @Override
        public void onTouchEnd(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {

            // Ignores if is playing with bone
            if (mEva.isPlaying()) {
                return;
            }

            // Ignores if some view is clicked
            if (hasViewNode(sxrPickedObject)) {
                return;
            }

            // The MainViewController manages views in full screen
            if (mMainViewController != null && mMainViewController.isEnabled()) {
                return;
            }

            if (sxrNode == null || sxrNode.getParent() == null) {
                return;
            }

            Log.d(TAG, "onTouchEnd " + sxrNode.getName());

            SXRPlane selectedPlane = (SXRPlane) sxrNode.getParent().getComponent(SXRPlane.getComponentType());

            // TODO: Improve this if
            if (selectedPlane != null) {
                if (mHandAnimation != null) {
                    mHandAnimation.onHide();
                    mHandAnimation = null;
                }

                final float[] modelMtx = sxrNode.getTransform().getModelMatrix();

                if (!mEva.isRunning()) {
                    mEva.setPlane(sxrNode);
                    mEva.getView().getTransform().setPosition(modelMtx[12], modelMtx[13], modelMtx[14]);
                    mEva.enter();
                    mEva.setInitialScale();
                    mEva.enableActions();

                    if (mCurrentMode == null) {
                        mCurrentMode = new HudMode(mEvaContext, mEva, mHandlerModeChange);
                        mCurrentMode.enter();
                    } else if (mCurrentMode instanceof HudMode) {
                        mCurrentMode.view().show(mEvaContext.getMainScene());
                    }

                    mPlaneHandler.setSelectedPlane(selectedPlane, sxrNode);

                    // remove point cloud
                    mPointCloudHandler.removeFromScene();
                    mSharedMixedReality.getEventReceiver().removeListener(mPointCloudHandler);
                }

                if (sxrNode == mEva.getPlane() && mCurrentMode instanceof HudMode) {
                    final float[] hitPos = sxrPickedObject.hitLocation;
                    mEva.goToTap(hitPos[0], hitPos[1], hitPos[2]);
                }
            }
        }
    };

    private IMixedRealityEvents mMixedRealityHandler = new IMixedRealityEvents() {
        @Override
        public void onMixedRealityStart(IMixedReality mixedReality) {
            onARInit(mixedReality);
        }

        @Override
        public void onMixedRealityStop(IMixedReality mixedReality) {

        }

        @Override
        public void onMixedRealityUpdate(IMixedReality mixedReality) {

        }
    };
}