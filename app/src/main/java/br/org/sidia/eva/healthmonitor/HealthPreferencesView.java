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

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import br.org.sidia.eva.R;
import br.org.sidia.eva.view.BaseView;
import br.org.sidia.eva.view.IViewController;

public class HealthPreferencesView extends BaseView implements IHealthPreferencesView {

    private HealthPreferencesAdapter mAdapter;

    private Button mResetButton;
    private Button mCloseButton;
    private Button mApplyButton;
    private Button mApplyAndCloseButton;

    public HealthPreferencesView(View view, IViewController viewController) {
        super(view, viewController);

        mResetButton = view.findViewById(R.id.button_reset);
        mCloseButton = view.findViewById(R.id.button_close);
        mApplyButton = view.findViewById(R.id.button_apply);
        mApplyAndCloseButton = view.findViewById(R.id.button_apply_close);

        RecyclerView preferences = view.findViewById(R.id.view_preferences);
        preferences.setHasFixedSize(false);
        preferences.setNestedScrollingEnabled(true);
        preferences.setLayoutManager(new LinearLayoutManager(
                view.getContext(), LinearLayoutManager.HORIZONTAL, false));

        mAdapter = new HealthPreferencesAdapter(new ArrayList<>());
        preferences.setAdapter(mAdapter);
    }

    @Override
    public Button getResetButton() {
        return mResetButton;
    }

    @Override
    public Button getCloseButton() {
        return mCloseButton;
    }

    @Override
    public Button getApplyButton() {
        return mApplyButton;
    }

    @Override
    public Button getApplyAndCloseButton() {
        return mApplyAndCloseButton;
    }

    @Override
    public List<HealthPreferenceViewModel> getPreferences() {
        return mAdapter.getPreferences();
    }

    @Override
    public void setPreferences(List<HealthPreferenceViewModel> preferences) {
        mAdapter.setPreferences(preferences);
    }
}
