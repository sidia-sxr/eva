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

package br.org.sidia.eva.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import br.org.sidia.eva.R;

public class Scanner extends RelativeLayout {
    RelativeLayout mScanner;

    public Scanner(Context context) {
        super(context, null);
        initView();
    }

    public Scanner(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public Scanner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        mScanner = (RelativeLayout) inflate(getContext(), R.layout.scanner, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        startAnimation();
    }

    private void startAnimation() {
        final ImageView mScannerMovement = findViewById(R.id.ic_scanner_movement);
        final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.scanner);
        mScannerMovement.startAnimation(animation);
        animation.setRepeatMode(Animation.REVERSE);
    }
}
