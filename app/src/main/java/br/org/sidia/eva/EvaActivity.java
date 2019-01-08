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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.samsungxr.SXRActivity;
import br.org.sidia.eva.context.ActivityResultEvent;
import br.org.sidia.eva.context.RequestPermissionResultEvent;
import br.org.sidia.eva.manager.permission.OnPermissionResultListener;
import br.org.sidia.eva.manager.permission.PermissionManager;
import br.org.sidia.eva.util.EventBusUtils;
import com.samsungxr.utility.Log;

public class EvaActivity extends SXRActivity {

    private static final String TAG = "EvaActivity";

    private EvaMain mMain;
    private EvaContext mEvaContext;
    private PermissionManager mPermissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        mPermissionManager = new PermissionManager(this);
        mPermissionManager.setPermissionResultListener(new PermissionListener());
    }

    private void startEvaMain() {
        mEvaContext = new EvaContext(this);
        mMain = new EvaMain(mEvaContext);
        setMain(mMain, "sxr.xml");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mPermissionManager.hasPermissions()) {
            mPermissionManager.requestPermissions();
        } else if (mEvaContext == null) {
            startEvaMain();
        } else {
            mEvaContext.resume();
            mMain.resume();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EventBusUtils.post(new RequestPermissionResultEvent(requestCode, permissions, grantResults));
        mPermissionManager.handlePermissionResults(requestCode);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mEvaContext != null) {
            mEvaContext.pause();
        }
        if (mMain != null) {
            mMain.pause();
        }
    }

    private class PermissionListener implements OnPermissionResultListener {
        @Override
        public void onPermissionGranted() {
            startEvaMain();
        }

        @Override
        public void onPermissionDenied() {
            Log.d(TAG, "on permission denied");
            showMessage(getString(R.string.application_permissions));
            // TODO: maybe we need to call settings here to enable permission again
            finish();
        }

        private void showMessage(String text) {
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        EventBusUtils.post(new ActivityResultEvent(requestCode, resultCode, data));
    }

}
