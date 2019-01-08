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

package br.org.sidia.eva;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPerspectiveCamera;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXROpacityAnimation;
import br.org.sidia.eva.mode.BaseEvaView;

public class ViewChoosePlan extends BaseEvaView {
    private SXRNode mViewChoosePlan;

    public ViewChoosePlan(EvaContext evaContext) {
        super(evaContext);

        onInit();
    }

    @Override
    protected void onShow(SXRScene mainScene) {
        SXROpacityAnimation mAnimation;
        mAnimation = new SXROpacityAnimation(mViewChoosePlan, .8f, 1);
        mAnimation.setOnFinish(sxrAnimation -> {
            mEvaContext.getMainScene().getMainCameraRig().addChildObject(mViewChoosePlan);
        });
        mAnimation.start(mEvaContext.getSXRContext().getAnimationEngine());
        setEnable(true);
    }

    @Override
    protected void onHide(SXRScene mainScene) {
        SXROpacityAnimation mAnimation;
        mAnimation = new SXROpacityAnimation(mViewChoosePlan, .8f, 0);
        mAnimation.setOnFinish(sxrAnimation -> {
            mainScene.getMainCameraRig().removeChildObject(mViewChoosePlan);
        });
        mAnimation.start(mEvaContext.getSXRContext().getAnimationEngine());
        setEnable(false);
    }


    private void onInit() {
        final SXRPerspectiveCamera cam = mEvaContext.getMainScene().getMainCameraRig().getCenterCamera();
        final float aspect = cam.getAspectRatio();
        final double fov = Math.toRadians(cam.getFovY());
        final float z = 1.0f;
        final float h = (float) (z * Math.tan(fov * 0.5f));
        final float w = aspect * h;


        SXRTexture tex = mEvaContext.getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(mEvaContext.getSXRContext(), R.drawable.view_tap_plane));
        mViewChoosePlan = new SXRNode(mEvaContext.getSXRContext(), 0.8f * w, 1 * h);
        mViewChoosePlan.getRenderData().getMaterial().setMainTexture(tex);
        mViewChoosePlan.getTransform().setPosition(0.0f, -0.12f, -z);
    }
}
