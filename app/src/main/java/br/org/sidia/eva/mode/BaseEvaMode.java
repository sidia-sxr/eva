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

import com.samsungxr.SXRCameraRig;
import br.org.sidia.eva.EvaContext;
import com.samsungxr.utility.Log;

public abstract class BaseEvaMode implements IEvaMode {
    protected final String TAG;

    protected final EvaContext mEvaContext;
    protected final IEvaView mModeScene;
    protected ILoadEvents mLoadListener;
    protected boolean mIsRunning;
    protected boolean mIsLoaded;

    public BaseEvaMode(EvaContext evaContext, IEvaView sceneMode) {
        TAG = getClass().getSimpleName();
        mEvaContext = evaContext;
        mModeScene = sceneMode;
        mLoadListener = null;
        mIsRunning = false;
        mIsLoaded = false;
    }

    @Override
    public void enter() {
        Log.w(TAG, "enter");
        mModeScene.show(mEvaContext.getMainScene());
        onEnter();

        mIsRunning = true;
    }

    @Override
    public void exit() {
        Log.w(TAG, "exit");
        mModeScene.hide(mEvaContext.getMainScene());
        onExit();

        mIsRunning = false;
    }

    @Override
    public void load(ILoadEvents listener) {
        mLoadListener = listener;
        mIsLoaded = true;
    }

    @Override
    public void unload() {
        mLoadListener = null;
        mIsLoaded = false;
    }

    @Override
    public IEvaView view() {
        return mModeScene;
    }

    public void handleOrientation() {
        onHandleOrientation(mEvaContext.getMainScene().getMainCameraRig());
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public boolean isLoaded() {
        return mIsLoaded;
    }

    abstract protected void onEnter();

    abstract protected void onExit();

    abstract protected void onHandleOrientation(SXRCameraRig cameraRig);
}
