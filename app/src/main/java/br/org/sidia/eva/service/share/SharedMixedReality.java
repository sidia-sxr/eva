package br.org.sidia.eva.service.share;

import android.graphics.Bitmap;
import android.opengl.Matrix;
import android.util.Log;

import com.samsungxr.SXREventReceiver;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPicker;
import com.samsungxr.mixedreality.IMixedReality;
import com.samsungxr.mixedreality.SXRAnchor;
import com.samsungxr.mixedreality.SXRHitResult;
import com.samsungxr.mixedreality.SXRLightEstimate;
import com.samsungxr.mixedreality.SXRMarker;
import com.samsungxr.mixedreality.SXRMixedReality;
import com.samsungxr.mixedreality.SXRPlane;
import com.samsungxr.mixedreality.SXRPointCloud;

import org.greenrobot.eventbus.Subscribe;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import br.org.sidia.eva.EvaContext;
import br.org.sidia.eva.constant.EvaConstants;
import br.org.sidia.eva.constant.EvaObjectType;
import br.org.sidia.eva.service.IMessageService;
import br.org.sidia.eva.service.MessageService;
import br.org.sidia.eva.service.event.UpdatePosesReceivedMessage;
import br.org.sidia.eva.util.EventBusUtils;

public class SharedMixedReality implements IMixedReality {

    private static final String TAG = SharedMixedReality.class.getSimpleName();

    public static final int OFF = 0;
    public static final int HOST = 1;
    public static final int GUEST = 2;

    private final IMixedReality mMixedReality;
    private final EvaContext mEvaContext;
    private final List<SharedSceneObject> mSharedSceneObjects;
    private final IMessageService mMessageService;

    @EvaConstants.ShareMode
    private int mMode = EvaConstants.SHARE_MODE_NONE;
    private SXRAnchor mSharedAnchor = null;
    private SXRNode mSharedAnchorObject;
    private float[] mSpaceMatrix = new float[16];

    public SharedMixedReality(EvaContext evaContext) {
        mMixedReality = new SXRMixedReality(evaContext.getMainScene(), true);
        mEvaContext = evaContext;
        mSharedSceneObjects = new ArrayList<>();
        mMessageService = MessageService.getInstance();
        Matrix.setIdentityM(mSpaceMatrix, 0);
        mSharedAnchorObject = new SXRNode(evaContext.getSXRContext());
    }

    @Override
    public float getARToVRScale() { return mMixedReality.getARToVRScale(); }

    @Override
    public void resume() {
        mMixedReality.resume();
    }

    @Override
    public void pause() {
        mMixedReality.pause();
    }

    public SXREventReceiver getEventReceiver() { return mMixedReality.getEventReceiver(); }

    /**
     * Starts the sharing mode
     *
     * @param mode {@link EvaConstants#SHARE_MODE_HOST} or {@link EvaConstants#SHARE_MODE_GUEST}
     */
    public void startSharing(SXRAnchor sharedAnchor, @EvaConstants.ShareMode int mode) {
        Log.d(TAG, "startSharing => " + mode);

        if (mMode != EvaConstants.SHARE_MODE_NONE) {
            return;
        }

        EventBusUtils.register(this);

        mSharedAnchor = sharedAnchor;
        mSharedAnchorObject.attachComponent(mSharedAnchor);

        mMode = mode;

        if (mode == EvaConstants.SHARE_MODE_HOST) {
            mEvaContext.runOnEvaThread(mSharingLoop);
        } else {
            startGuest();
        }
    }

    public void stopSharing() {
        EventBusUtils.unregister(this);
        mSharedAnchorObject.detachComponent(SXRAnchor.getComponentType());
        if (mMode == EvaConstants.SHARE_MODE_GUEST) {
            stopGuest();
        }
        mMode = EvaConstants.SHARE_MODE_NONE;
    }

    public SXRAnchor getSharedAnchor() {
        return mSharedAnchor;
    }

    private synchronized void startGuest() {
        for (SharedSceneObject shared : mSharedSceneObjects) {
            initAsGuest(shared);
        }
        setPlaneFindingMode(SXRMixedReality.PlaneFindingMode.DISABLED);
    }

    private synchronized void initAsGuest(SharedSceneObject shared) {
        shared.parent = shared.object.getParent();
        if (shared.parent != null) {
            shared.parent.removeChildObject(shared.object);
            mEvaContext.getMainScene().addNode(shared.object);
        }
    }

    private synchronized void stopGuest() {
        Iterator<SharedSceneObject> iterator = mSharedSceneObjects.iterator();
        SharedSceneObject shared;
        while (iterator.hasNext()) {
            shared = iterator.next();
            if (shared.parent != null) {
                shared.object.getTransform().setModelMatrix(shared.localMtx);
                mEvaContext.getMainScene().removeNode(shared.object);
                shared.parent.addChildObject(shared.object);
            }
        }
        mEvaContext.getPlaneHandler().resetPlanes();
        setPlaneFindingMode(SXRMixedReality.PlaneFindingMode.HORIZONTAL);
    }

    public synchronized void registerSharedObject(SXRNode object, @EvaObjectType String type,
                                                  boolean repeat) {
        for (SharedSceneObject shared : mSharedSceneObjects) {
            if (shared.object == object) {
                shared.repeat = repeat;
                return;
            }
        }

        SharedSceneObject newShared = new SharedSceneObject(type, object);
        newShared.repeat = repeat;
        if (mMode == EvaConstants.SHARE_MODE_GUEST) {
            initAsGuest(newShared);
        }
        mSharedSceneObjects.add(newShared);
    }

    public synchronized void unregisterSharedObject(SXRNode object) {
        Iterator<SharedSceneObject> iterator = mSharedSceneObjects.iterator();
        SharedSceneObject shared;
        while (iterator.hasNext()) {
            shared = iterator.next();
            if (shared.object == object) {
                iterator.remove();
            }
        }
    }
    @Override
    public float getScreenDepth() { return mMixedReality.getScreenDepth(); }

    @Override
    public SXRNode getPassThroughObject() {
        return mMixedReality.getPassThroughObject();
    }

    @Override
    public ArrayList<SXRPlane> getAllPlanes() {
        return mMixedReality.getAllPlanes();
    }

    @Override
    public SXRAnchor createAnchor(float[] pose) {
        return mMixedReality.createAnchor(pose);
    }

    @Override
    public SXRNode createAnchorNode(float[] pose) {
        return mMixedReality.createAnchorNode(pose);
    }

    @Override
    public void hostAnchor(SXRAnchor sxrAnchor, CloudAnchorCallback cloudAnchorCallback) {
        mMixedReality.hostAnchor(sxrAnchor, cloudAnchorCallback);
    }

    @Override
    public void updateAnchorPose(SXRAnchor sxrAnchor, float[] pose) {
        mMixedReality.updateAnchorPose(sxrAnchor, pose);
    }

    @Override
    public void removeAnchor(SXRAnchor sxrAnchor) {
        mMixedReality.removeAnchor(sxrAnchor);
    }

    @Override
    public void resolveCloudAnchor(String anchorId, IMixedReality.CloudAnchorCallback cb) {
        mMixedReality.resolveCloudAnchor(anchorId, cb);
    }

    @Override
    public void setEnableCloudAnchor(boolean enableCloudAnchor) {
        mMixedReality.setEnableCloudAnchor(enableCloudAnchor);
    }

    @Override
    public SXRHitResult hitTest(SXRPicker.SXRPickedObject sxrPickedObject) {
        return null;
    }

    @Override
    public SXRHitResult hitTest(float x, float y) {
        return mMixedReality.hitTest(x, y);
    }

    @Override
    public SXRLightEstimate getLightEstimate() {
        return mMixedReality.getLightEstimate();
    }

    @Override
    public void setMarker(Bitmap bitmap) {
        mMixedReality.setMarker(bitmap);
    }

    @Override
    public void setMarkers(ArrayList<Bitmap> arrayList) {
        mMixedReality.setMarkers(arrayList);
    }

    @Override
    public ArrayList<SXRMarker> getAllMarkers() {
        return mMixedReality.getAllMarkers();
    }

    @Override
    public float[] makeInterpolated(float[] poseA, float[] poseB, float t) {
        return mMixedReality.makeInterpolated(poseA, poseB, t);
    }

    @Override
    public SXRPointCloud acquirePointCloud() {
        return mMixedReality.acquirePointCloud();
    }

    @Override
    public void setPlaneFindingMode(SXRMixedReality.PlaneFindingMode planeFindingMode) {
        mMixedReality.setPlaneFindingMode(planeFindingMode);
    }

    @EvaConstants.ShareMode
    public int getMode() {
        return mMode;
    }

    private synchronized void sendSharedSceneObjects() {
        Matrix.invertM(mSpaceMatrix, 0,
                mSharedAnchor.getTransform().getModelMatrix(), 0);

        List<SharedObjectPose> poses = new ArrayList<>();

        for (SharedSceneObject shared : mSharedSceneObjects) {
            float[] result = new float[16];
            Matrix.multiplyMM(result, 0, mSpaceMatrix, 0,
                    shared.object.getTransform().getModelMatrix(), 0);
            poses.add(new SharedObjectPose(shared.type, result));
        }

        mMessageService.updatePoses(poses.toArray(new SharedObjectPose[0]));
    }

    private synchronized void onUpdatePosesReceived(SharedObjectPose[] poses) {
        mSpaceMatrix = mSharedAnchor.getTransform().getModelMatrix();

        for (SharedObjectPose pose : poses) {
            for (SharedSceneObject shared : mSharedSceneObjects) {
                if (shared.type.equals(pose.getObjectType())) {
                    float[] result = new float[16];
                    Matrix.multiplyMM(result, 0, mSpaceMatrix, 0, pose.getModelMatrix(), 0);
                    shared.object.getTransform().setModelMatrix(result);

                    if (!shared.repeat) {
                        mSharedSceneObjects.remove(shared);
                    }
                    break;
                }
            }
        }
    }

    private Runnable mSharingLoop = new Runnable() {

        final int LOOP_TIME = 500;

        @Override
        public void run() {
            if (mMode != EvaConstants.SHARE_MODE_NONE) {
                sendSharedSceneObjects();
                mEvaContext.runDelayedOnEvaThread(this, LOOP_TIME);
            }
        }
    };

    private static class SharedSceneObject {

        @EvaObjectType
        String type;

        // Shared object
        SXRNode object;
        // Parent of shared object.
        SXRNode parent;
        // Local matrix to be used in guest mode after the share experience has been finished
        Matrix4f localMtx;

        boolean repeat;

        SharedSceneObject(String type, SXRNode object) {
            this.type = type;
            this.object = object;
            this.repeat = true;
            this.localMtx = object.getTransform().getLocalModelMatrix4f();
        }

        @Override
        public String toString() {
            return "SharedSceneObject{" +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    @Subscribe
    public void handleReceivedMessage(UpdatePosesReceivedMessage message) {
        onUpdatePosesReceived(message.getSharedObjectPoses());
    }
}
