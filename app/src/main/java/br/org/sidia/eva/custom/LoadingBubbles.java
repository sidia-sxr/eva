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
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Timer;
import java.util.TimerTask;

import br.org.sidia.eva.R;

public class LoadingBubbles extends LinearLayout {
    Timer mTimer;
    TimerTask mTimerTask;
    LinearLayout mLoadingBubble;
    int mTotal;
    int mCurrent;

    public LoadingBubbles(Context context) {
        super(context, null);
        initView();
    }

    public LoadingBubbles(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LoadingBubbles(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        mLoadingBubble = (LinearLayout) inflate(getContext(), R.layout.progress_bubbles, this);
        mLoadingBubble = (LinearLayout) mLoadingBubble.getChildAt(0);
        mTotal = mLoadingBubble.getChildCount();
        Log.d("xx", "total: " + mTotal);
        mTimer = new Timer();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            startAnimation();
        } else {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        startAnimation();
    }

    private void startAnimation() {
        if (mTimerTask != null) {
            return;
        }

        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(
                        () -> {
                            ImageView mImageView;
                            if (mCurrent > 0) {
                                LinearLayout parent = (LinearLayout) mLoadingBubble.getChildAt(mCurrent - 1);
                                mImageView = (ImageView) parent.getChildAt(0);
                                mImageView.setImageResource(R.drawable.white_bubble_white);
                            } else {
                                LinearLayout parent = (LinearLayout) mLoadingBubble.getChildAt(mTotal - 1);
                                mImageView = (ImageView) parent.getChildAt(0);
                                mImageView.setImageResource(R.drawable.white_bubble_white);
                            }
                            LinearLayout parent = (LinearLayout) mLoadingBubble.getChildAt(mCurrent);
                            mImageView = (ImageView) parent.getChildAt(0);
                            mImageView.setImageResource(R.drawable.bubble_blue);

                            mCurrent = (mCurrent + 1) % mTotal;

                        }
                );
            }
        };
        mTimer.schedule(mTimerTask, 0, 500);
    }
}
