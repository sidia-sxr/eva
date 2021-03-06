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

package br.org.sidia.eva.mode.sharinganchor.view.impl;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import br.org.sidia.eva.R;
import br.org.sidia.eva.mode.sharinganchor.view.IWaitingForGuestView;
import br.org.sidia.eva.view.BaseView;
import br.org.sidia.eva.view.IViewController;

public class WaitingForGuestView extends BaseView implements IWaitingForGuestView {

    private TextView mTotalConnected;
    private TextView mGuestText;
    private View mCancelButton;
    private View mContinueButton;
    private ImageView mIconGuest;

    public WaitingForGuestView(View view, IViewController controller) {
        super(view, controller);
        this.mTotalConnected = view.findViewById(R.id.text_total);
        this.mGuestText = view.findViewById(R.id.text_guests);
        this.mCancelButton = view.findViewById(R.id.button_cancel);
        this.mContinueButton = view.findViewById(R.id.button_continue);
        this.mIconGuest = view.findViewById(R.id.icon_guest);
        setTotalConnected(0);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void setTotalConnected(int total) {
        int t = Math.max(0, total);
        runOnUiThread(() -> mTotalConnected.setText(String.format("%02d", t)));
        setContinueButtonEnabled(t > 0);
        updatePluralGuestText(t);
    }

    @Override
    public void setCancelClickListener(View.OnClickListener listener) {
        mCancelButton.setOnClickListener(listener);
    }

    @Override
    public void setContinueClickListener(View.OnClickListener listener) {
        mContinueButton.setOnClickListener(listener);
    }

    @Override
    public void setContinueButtonEnabled(boolean enabled) {
        runOnUiThread(() -> mContinueButton.setEnabled(enabled));
    }

    @Override
    public void setIconGuestFinded() {
        runOnUiThread(() -> mIconGuest.setImageResource(R.drawable.icon_guest));
    }

    private void updatePluralGuestText(int total) {
        runOnUiThread(() -> {
            String guestText = mGuestText.getContext().getResources()
                    .getQuantityString(R.plurals.common_text_guest, total);
            mGuestText.setText(guestText.toLowerCase());
        });
    }
}
