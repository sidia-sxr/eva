package br.org.sidia.eva.animations;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRHybridObject;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXROpacityAnimation;

import br.org.sidia.eva.R;
import br.org.sidia.eva.shaders.SXRHandShader;

public class HandAnimation extends SXRAnimation {
    private final SXRContext mContext;
    private final SXRNode mLightObject, mHandObject, mLabelObject;
    private final SXRMaterial mLightMaterial;

    public HandAnimation(SXRContext context, float duration) {
        super(null, duration);
        mContext = context;
        mLightObject = createLightObject(context);
        mHandObject = createHand(mContext);
        mLabelObject = createLabel(mContext);
        mLightMaterial = mLightObject.getRenderData().getMaterial();
        mLightObject.addChildObject(mHandObject);
        mLightObject.addChildObject(mLabelObject);
       /* setRepeatMode(SXRRepeatMode.REPEATED);
        setRepeatCount(-1);*/
    }

    public void setLightSize(float size) {
        mLightObject.getTransform().setScale(size, size, 1);
    }

    public void setLightPosition(float x, float y, float z) {
        mLightObject.getTransform().setPosition(x, y, z);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mLightObject.getParent() == null) {
            mContext.getMainScene().getMainCameraRig().addChildObject(mLightObject);

            SXROpacityAnimation mAnimation;
            mAnimation = new SXROpacityAnimation(mLightObject, .8f, 1);
            mAnimation.start(mContext.getMainScene().getSXRContext().getAnimationEngine());
            mLightObject.setEnable(true);

            SXROpacityAnimation mAnimationHand;
            mAnimationHand = new SXROpacityAnimation(mHandObject, .8f, 1);
            mAnimationHand.start(mContext.getMainScene().getSXRContext().getAnimationEngine());

            SXROpacityAnimation mAnimationLabel;
            mAnimationLabel = new SXROpacityAnimation(mLabelObject, .8f, 1);
            mAnimationLabel.start(mContext.getMainScene().getSXRContext().getAnimationEngine());
        }
    }

    @Override
    protected void onFinish() {
        super.onFinish();
        if (mLightObject.getParent() != null) {
            mLightObject.getParent().removeChildObject(mLightObject);
        }
    }

    public void onHide() {
        SXROpacityAnimation mAnimation;
        mAnimation = new SXROpacityAnimation(mLightObject, .8f, 0);
        mAnimation.start(mContext.getMainScene().getSXRContext().getAnimationEngine());

        SXROpacityAnimation mAnimationHand;
        mAnimationHand = new SXROpacityAnimation(mHandObject, .8f, 0);
        mAnimationHand.start(mContext.getMainScene().getSXRContext().getAnimationEngine());

        SXROpacityAnimation mAnimationLabel;
        mAnimationLabel = new SXROpacityAnimation(mLabelObject, .8f, 0);
        mAnimationLabel.start(mContext.getMainScene().getSXRContext().getAnimationEngine());
        mAnimationLabel.setOnFinish(animation -> mContext.getMainScene().getMainCameraRig().removeChildObject(mLightObject));
    }

    @Override
    protected void animate(SXRHybridObject sxrHybridObject, float ratio) {
        mLightMaterial.setFloat("u_time", mElapsedTime);
    }

    private static SXRNode createLightObject(SXRContext context) {
        final SXRTexture tex = context.getAssetLoader().loadTexture(
                new SXRAndroidResource(context, R.drawable.ic_points));
        final SXRMesh mesh = SXRMesh.createQuad(context,
                "float3 a_position float2 a_texcoord", 0.05f, 0.05f);
        final SXRMaterial material = new SXRMaterial(context, new SXRShaderId(SXRHandShader.class));
        final SXRNode lightObj = new SXRNode(context, mesh, material);
        final SXRRenderData renderData = lightObj.getRenderData();
        material.setMainTexture(tex);
        renderData.setAlphaBlend(true);
        material.setOpacity(0f);
        lightObj.getTransform().setPosition(0, -0.14f, 0f);
        return lightObj;
    }

    private static SXRNode createHand(SXRContext context) {
        final SXRTexture tex = context.getAssetLoader().loadTexture(
                new SXRAndroidResource(context, R.drawable.ic_hand));
        final SXRMesh mesh = SXRMesh.createQuad(context,
                "float3 a_position float2 a_texcoord", 0.134f * 0.5f, 0.195f * 0.5f);
        final SXRNode lightObj = new SXRNode(context, mesh, tex);
        lightObj.getTransform().setPosition(0, -0.05f, 0.08f);
        lightObj.getRenderData().getMaterial().setOpacity(0f);
        return lightObj;
    }

    private static SXRNode createLabel(SXRContext context) {
        final SXRTexture tex = context.getAssetLoader().loadTexture(
                new SXRAndroidResource(context, R.drawable.ic_label));
        final SXRMesh mesh = SXRMesh.createQuad(context,
                "float3 a_position float2 a_texcoord", 1.195f * 0.44f, 0.075f * 0.28f);
        final SXRNode lightObj = new SXRNode(context, mesh, tex);
        lightObj.getTransform().setPosition(0, -0.164f, 0.02f);
        lightObj.getRenderData().getMaterial().setOpacity(0f);
        return lightObj;
    }
}
