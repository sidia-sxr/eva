/*
 * Copyright 2015 Samsung Electronics Co., LTD
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

package br.org.sidia.eva.healthmonitor;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.ColorRes;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import br.org.sidia.eva.R;
import br.org.sidia.eva.custom.WaveDrawable;

public class HealthLevelIndicator extends ImageView {

    private WaveDrawable mDrawable;
    private View mParent;

    public HealthLevelIndicator(Context context) {
        super(context);
        init();
    }

    public HealthLevelIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HealthLevelIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mDrawable = new WaveDrawable(getContext(), R.drawable.bg_health_level_indicator);
        mDrawable.setOnProgressAnimationListener(this::updateColor);
        super.setBackground(mDrawable);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mParent = (View) getParent();
    }

    public void setOnAnimationEndCallback(WaveDrawable.OnAnimationEndCallback callback) {
        mDrawable.setOnAnimationEndCallback(callback);
    }

    public void setLevel(@FloatRange(from = 0, to = 1) float level, boolean animated) {
        if (mDrawable != null) {
            if (!animated) {
                mDrawable.setProgress(level);
                updateColor(level);
            } else {
                mDrawable.setProgressAnimated(level);
            }
        }
    }

    private void updateColor(float level) {
        mDrawable.setProgressColor(getStatusColor(level));
        if (mParent != null) {
            int bgColor = level > 0 ? android.R.color.transparent : R.color.health_level_critical;
            mParent.getBackground().setTintList(ColorStateList.valueOf(getContext().getColor(bgColor)));
        }
    }

    @ColorRes
    private int getStatusColor(float level) {
        if (level > .5f) {
            return R.color.health_level_normal;
        } else if (level > 0) {
            return R.color.health_level_warning;
        } else {
            return R.color.health_level_critical;
        }
    }
}
