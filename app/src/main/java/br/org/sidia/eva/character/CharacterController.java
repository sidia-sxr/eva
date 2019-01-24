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

package br.org.sidia.eva.character;

import android.util.SparseArray;

import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRDrawFrameListener;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTransform;
import com.samsungxr.utility.Log;

import org.greenrobot.eventbus.Subscribe;
import org.joml.Vector2f;

import br.org.sidia.eva.BallThrowHandler;
import br.org.sidia.eva.EvaContext;
import br.org.sidia.eva.constant.EvaConstants;
import br.org.sidia.eva.mode.BaseEvaMode;
import br.org.sidia.eva.mode.ILoadEvents;
import br.org.sidia.eva.actions.EvaActionType;
import br.org.sidia.eva.actions.EvaActions;
import br.org.sidia.eva.actions.IEvaAction;
import br.org.sidia.eva.service.IMessageService;
import br.org.sidia.eva.service.MessageService;
import br.org.sidia.eva.service.data.EvaActionCommand;
import br.org.sidia.eva.service.event.EvaActionCommandReceivedMessage;
import br.org.sidia.eva.service.share.SharedMixedReality;
import br.org.sidia.eva.util.EventBusUtils;

public class CharacterController extends BaseEvaMode {

    private IEvaAction mCurrentAction = null; // default action IDLE
    private final SparseArray<IEvaAction> mEvaActions;
    private SXRDrawFrameListener mDrawFrameHandler;
    private BallThrowHandler mBallThrowHandler;

    private SharedMixedReality mMixedReality;
    private IMessageService mMessageService;
    private boolean mIsPlaying = false;

    private SXRNode mBedTarget;
    private SXRNode mBowlTarget;
    private SXRNode mHydrantNode;

    public CharacterController(EvaContext evaContext) {
        super(evaContext, new CharacterView(evaContext));

        mEvaActions = new SparseArray<>();
        mDrawFrameHandler = null;
        mMixedReality = mEvaContext.getMixedReality();
        mBallThrowHandler = evaContext.getBallThrowHandlerHandler();

        mMessageService = MessageService.getInstance();

        mBedTarget = new SXRNode(evaContext.getSXRContext());
        mBowlTarget = new SXRNode(evaContext.getSXRContext());
        mHydrantNode = new SXRNode(evaContext.getSXRContext());

        initEva((CharacterView) mModeScene);
    }

    @Subscribe
    public void handleReceivedMessage(EvaActionCommandReceivedMessage message) {
        onSetCurrentAction(message.getEvaActionCommand().getType());
    }

    @Override
    protected void onEnter() {
        EventBusUtils.register(this);
    }

    @Override
    protected void onExit() {
        EventBusUtils.unregister(this);
        if (isPlaying()) {
            Log.d(TAG, "stop bone");
            stopBone();
        }
    }

    @Override
    public void load(ILoadEvents listener) {
        super.load(listener);

        mModeScene.load(listener);
    }

    @Override
    public void unload() {
        super.unload();

        mModeScene.unload();
    }

    @Override
    protected void onHandleOrientation(SXRCameraRig cameraRig) {
    }

    private void initEva(CharacterView eva) {
        addAction(new EvaActions.IDLE(mEvaContext, eva));

        addAction(new EvaActions.TO_BALL(eva, mBallThrowHandler.getBall(), (action, success) -> {
            if (success) {
                setCurrentAction(EvaActions.GRAB.ID);
            } else {
                setCurrentAction(EvaActions.IDLE.ID);
            }
        }));

        addAction(new EvaActions.TO_PLAYER(eva, mEvaContext.getPlayer(), (action, success) -> {
            setCurrentAction(EvaActions.IDLE.ID);
        }));

        addAction(new EvaActions.GRAB(eva, mBallThrowHandler.getBall(), (action, success) -> {
            setCurrentAction(EvaActions.TO_PLAYER.ID);

        }));

        addAction(new EvaActions.TO_TAP(eva, eva.getTapObject(), (action, success) -> {
            setCurrentAction(EvaActions.IDLE.ID);
        }));

        addAction(new EvaActions.TO_BED(eva, mBedTarget, (action, success) -> setCurrentAction(EvaActions.SLEEP_ENTER.ID)));

        addAction(new EvaActions.TO_BOWL(eva, mBowlTarget, (action, success) -> setCurrentAction(EvaActions.DRINK_ENTER.ID)));

        addAction(new EvaActions.TO_HYDRANT(eva, mHydrantNode, (action, success) -> setCurrentAction(EvaActions.HYDRANT_ENTER.ID)));

        addAction(new EvaActions.DRINK_ENTER(eva, mBowlTarget, (action, success) -> setCurrentAction(EvaActions.DRINK_LOOP.ID)));

        addAction(new EvaActions.DRINK_EXIT(eva, mBowlTarget, (action, success) -> {
            ((CharacterView) mModeScene).setTapPosition(0, 0, 0);
            setCurrentAction(EvaActions.TO_TAP.ID);
        }));

        addAction(new EvaActions.DRINK_LOOP(eva, mBowlTarget, (action, success) -> setCurrentAction(EvaActions.DRINK_EXIT.ID)));

        addAction(new EvaActions.HYDRANT_ENTER(eva, mHydrantNode, (action, success) -> setCurrentAction(EvaActions.HYDRANT_LOOP.ID)));

        addAction(new EvaActions.HYDRANT_EXIT(eva, mHydrantNode, (action, success) -> {
            ((CharacterView) mModeScene).setTapPosition(0, 0, 0);
            setCurrentAction(EvaActions.TO_TAP.ID);
        }));

        addAction(new EvaActions.HYDRANT_LOOP(eva, mHydrantNode, (action, success) -> setCurrentAction(EvaActions.HYDRANT_EXIT.ID)));

        addAction(new EvaActions.SLEEP_ENTER(eva, mBedTarget, (action, success) -> setCurrentAction(EvaActions.SLEEP_LOOP.ID)));

        addAction(new EvaActions.SLEEP_EXIT(eva, mBedTarget, (action, success) -> {
            ((CharacterView) mModeScene).setTapPosition(0, 0, 0);
            setCurrentAction(EvaActions.TO_TAP.ID);
        }));

        addAction(new EvaActions.SLEEP_LOOP(eva, mBedTarget, (action, success) -> {
            setCurrentAction(EvaActions.SLEEP_EXIT.ID);
        }));

        setCurrentAction(EvaActions.IDLE.ID);
    }

    public void goToTap(float x, float y, float z) {
        if (mCurrentAction == null
                || mCurrentAction.id() == EvaActions.IDLE.ID
                || mCurrentAction.id() == EvaActions.TO_TAP.ID) {
            Log.d(TAG, "goToTap(%f, %f, %f)", x, y, z);
            ((CharacterView) mModeScene).setTapPosition(x, y, z);
            setCurrentAction(EvaActions.TO_TAP.ID);
        }
    }

    public void goToBed(float x, float y, float z) {
        mBedTarget.getTransform().setPosition(x, y, z);
        setCurrentAction(EvaActions.TO_BED.ID);
    }

    public void goToBowl(float x, float y, float z) {
        mBowlTarget.getTransform().setPosition(x, y, z);
        setCurrentAction(EvaActions.TO_BOWL.ID);
    }

    public void goToHydrant(float x, float y, float z) {
        SXRTransform t = getView().getTransform();
        float x0 = t.getPositionX();
        float z0 = t.getPositionZ();
        Vector2f vecHydrant = new Vector2f(x - x0, z - z0);
        Vector2f perpendicular = new Vector2f(vecHydrant.y, vecHydrant.x * -1);

        perpendicular.normalize();
        perpendicular.mul(getView().getBoundingVolume().radius * 0.5f);

        mHydrantNode.getTransform().setPosition(x + perpendicular.x, y, z + perpendicular.y);

        setCurrentAction(EvaActions.TO_HYDRANT.ID);
    }

    public void playBone() {
        mIsPlaying = true;
        mBallThrowHandler.enable();
    }

    public void stopBone() {
        mIsPlaying = false;
        mBallThrowHandler.disable();
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    public void setPlane(SXRNode plane) {
        CharacterView view = (CharacterView) view();

        view.setBoundaryPlane(plane);
    }

    public SXRNode getPlane() {
        CharacterView view = (CharacterView) view();

        return view.getBoundaryPlane();
    }

    public CharacterView getView() {
        return (CharacterView) view();
    }

    public void setCurrentAction(@EvaActionType int action) {
        onSetCurrentAction(action);
        onSendCurrentAction(action);
    }

    private void onSetCurrentAction(@EvaActionType int action) {
        mCurrentAction = mEvaActions.get(action);

        if (mCurrentAction != null) {
            if (mIsPlaying || mEvaContext.getMode() == EvaConstants.SHARE_MODE_GUEST) {
                if (mCurrentAction.id() == EvaActions.IDLE.ID) {
                    mBallThrowHandler.reset();
                } if (mCurrentAction.id() == EvaActions.GRAB.ID) {
                    mBallThrowHandler.disableBallsPhysics();
                }
            }
            EventBusUtils.post(mCurrentAction);
        }
    }

    private void onSendCurrentAction(@EvaActionType int action) {
        if (mEvaContext.getMode() == EvaConstants.SHARE_MODE_HOST) {
            mMessageService.sendEvaActionCommand(new EvaActionCommand(action));
        }
    }

    public void addAction(IEvaAction action) {
        mEvaActions.put(action.id(), action);
    }

    public void removeAction(@EvaActionType int action) {
        mEvaActions.remove(action);
    }

    public void enableActions() {
        if (mDrawFrameHandler == null) {
            Log.w(TAG, "On actions enabled");
            mDrawFrameHandler = new DrawFrameHandler();
            mEvaContext.getSXRContext().registerDrawFrameListener(mDrawFrameHandler);
        }
    }

    public void disableActions() {
        if (mDrawFrameHandler != null) {
            Log.w(TAG, "On actions disabled");
            mEvaContext.getSXRContext().unregisterDrawFrameListener(mDrawFrameHandler);
            mDrawFrameHandler = null;
        }
    }

    public void setInitialScale() {
        CharacterView view = (CharacterView) view();
        view.setInitialScale();
    }

    private class DrawFrameHandler implements SXRDrawFrameListener {
        IEvaAction activeAction = null;

        @Override
        public void onDrawFrame(float frameTime) {
            if (mCurrentAction != activeAction) {
                if (activeAction != null) {
                    activeAction.exit();
                }
                activeAction = mCurrentAction;
                activeAction.entry();
            } else if (activeAction != null) {
                activeAction.run(frameTime);
            }
        }
    }
}
