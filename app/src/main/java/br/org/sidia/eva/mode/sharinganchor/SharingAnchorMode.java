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

package br.org.sidia.eva.mode.sharinganchor;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Log;
import android.widget.Toast;

import com.samsungxr.SXRCameraRig;
import com.samsungxr.mixedreality.SXRAnchor;

import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import br.org.sidia.eva.EvaContext;
import br.org.sidia.eva.R;
import br.org.sidia.eva.constant.EvaConstants;
import br.org.sidia.eva.constant.EvaObjectType;
import br.org.sidia.eva.manager.cloud.anchor.CloudAnchor;
import br.org.sidia.eva.manager.cloud.anchor.CloudAnchorManager;
import br.org.sidia.eva.manager.cloud.anchor.ManagedAnchor;
import br.org.sidia.eva.manager.cloud.anchor.exception.CloudAnchorException;
import br.org.sidia.eva.manager.cloud.anchor.exception.NetworkException;
import br.org.sidia.eva.manager.connection.EvaConnectionManager;
import br.org.sidia.eva.manager.connection.IEvaConnectionManager;
import br.org.sidia.eva.manager.connection.event.EvaConnectionEvent;
import br.org.sidia.eva.mode.BaseEvaMode;
import br.org.sidia.eva.mode.OnBackToHudModeListener;
import br.org.sidia.eva.mode.sharinganchor.view.IConnectionFoundView;
import br.org.sidia.eva.mode.sharinganchor.view.IGuestLookingAtTargetView;
import br.org.sidia.eva.mode.sharinganchor.view.IHostLookingAtTargetView;
import br.org.sidia.eva.mode.sharinganchor.view.ILetsStartView;
import br.org.sidia.eva.mode.sharinganchor.view.ISharingErrorView;
import br.org.sidia.eva.mode.sharinganchor.view.IWaitingForGuestView;
import br.org.sidia.eva.mode.sharinganchor.view.IWaitingForHostView;
import br.org.sidia.eva.mode.sharinganchor.view.impl.SharingAnchorViewController;
import br.org.sidia.eva.service.IMessageService;
import br.org.sidia.eva.service.MessageService;
import br.org.sidia.eva.service.data.RequestStatus;
import br.org.sidia.eva.service.data.ViewCommand;
import br.org.sidia.eva.service.event.EvaAnchorReceivedMessage;
import br.org.sidia.eva.service.event.RequestStatusReceivedMessage;
import br.org.sidia.eva.service.event.ViewCommandReceivedMessage;
import br.org.sidia.eva.service.share.SharedMixedReality;
import br.org.sidia.eva.util.EventBusUtils;
import br.org.sidia.eva.view.IView;
import br.org.sidia.eva.view.shared.IConnectionFinishedView;

import static br.org.sidia.eva.manager.connection.IEvaConnectionManager.EVENT_ALL_CONNECTIONS_LOST;
import static br.org.sidia.eva.manager.connection.IEvaConnectionManager.EVENT_CONNECTION_ESTABLISHED;
import static br.org.sidia.eva.manager.connection.IEvaConnectionManager.EVENT_ENABLE_BLUETOOTH_DENIED;
import static br.org.sidia.eva.manager.connection.IEvaConnectionManager.EVENT_GUEST_CONNECTION_ESTABLISHED;
import static br.org.sidia.eva.manager.connection.IEvaConnectionManager.EVENT_HOST_VISIBILITY_DENIED;
import static br.org.sidia.eva.manager.connection.IEvaConnectionManager.EVENT_NO_CONNECTION_FOUND;
import static br.org.sidia.eva.manager.connection.IEvaConnectionManager.EVENT_ONE_CONNECTION_LOST;
import static br.org.sidia.eva.manager.connection.IEvaConnectionManager.EVENT_ON_LISTENING_TO_GUESTS;
import static br.org.sidia.eva.manager.connection.IEvaConnectionManager.EVENT_ON_REQUEST_CONNECTION_TO_HOST;

public class SharingAnchorMode extends BaseEvaMode {

    private final String TAG = SharingAnchorMode.class.getSimpleName();

    private IEvaConnectionManager mConnectionManager;
    private SharingAnchorViewController mSharingAnchorViewController;
    private IMessageService mMessageService;
    private OnBackToHudModeListener mBackToHudModeListener;
    private SharedMixedReality mSharedMixedReality;
    private EvaAnchorSharingStatusHandler mEvaAnchorSharingStatusHandler;
    private Resources mResources;

    @EvaConstants.ShareMode
    private int mCurrentMode = EvaConstants.SHARE_MODE_NONE;

    public SharingAnchorMode(EvaContext evaContext, OnBackToHudModeListener listener) {
        super(evaContext, new SharingAnchorViewController(evaContext));

        mConnectionManager = EvaConnectionManager.getInstance();
        mSharingAnchorViewController = (SharingAnchorViewController) mModeScene;

        mResources = mEvaContext.getActivity().getResources();
        showViewLetsStart();

        mBackToHudModeListener = listener;
        mMessageService = MessageService.getInstance();
        mSharedMixedReality = evaContext.getMixedReality();
    }

    private void showViewLetsStart() {
        ILetsStartView view = mSharingAnchorViewController.makeView(ILetsStartView.class);
        view.setBackClickListener(v -> cancelSharing());
        view.setHostClickListener(v -> mConnectionManager.startInvitation());
        view.setGuestClickListener(v -> {
            // Start to accept invitation from the host
            mConnectionManager.findInvitationThenConnect();
        });
        view.show();
    }

    private void showViewWaitingForGuest() {
        mCurrentMode = EvaConstants.SHARE_MODE_HOST;
        IWaitingForGuestView view = mSharingAnchorViewController.makeView(IWaitingForGuestView.class);
        view.setCancelClickListener(v -> cancelSharing());
        view.setContinueClickListener(v -> mConnectionManager.stopInvitation());
        view.show();
    }

    private void showViewWaitingForHost() {
        mCurrentMode = EvaConstants.SHARE_MODE_GUEST;
        IWaitingForHostView view = mSharingAnchorViewController.makeView(IWaitingForHostView.class);
        view.setCancelClickListener(v -> cancelSharing());
        view.show();
    }

    private void showViewHostLookingAtTarget(@StringRes int stringId, String color) {
        IHostLookingAtTargetView view;
        String text = mResources.getString(stringId);
        if (mSharingAnchorViewController.getCurrentView() instanceof IHostLookingAtTargetView) {
            view = (IHostLookingAtTargetView) mSharingAnchorViewController.getCurrentView();
            view.setStatusText(text);
            view.setColor(color);
        } else {
            view = mSharingAnchorViewController.makeView(IHostLookingAtTargetView.class);
            view.setStatusText(text);
            view.setColor(color);
            view.show();
        }
    }

    private void showViewHostLookingAtTarget(@StringRes int stringId) {
        showViewHostLookingAtTarget(stringId, null);
    }

    private void showViewGuestLookingAtTarget() {
        IGuestLookingAtTargetView view = mSharingAnchorViewController.makeView(IGuestLookingAtTargetView.class);
        view.show();
    }

    private void showViewSharingError(@NonNull OnCancelCallback cancelCallback, @NonNull OnRetryCallback retryCallback) {
        ISharingErrorView view = mSharingAnchorViewController.makeView(ISharingErrorView.class);
        view.setCancelClickListener(v -> cancelCallback.onCancel());
        view.setRetryClickListener(v -> retryCallback.onRetry());
        view.show();
    }

    private void showViewSharingFinished(String text) {
        IConnectionFinishedView view = mSharingAnchorViewController.makeView(IConnectionFinishedView.class);
        view.setOkClickListener(v -> cancelSharing());
        view.setStatusText(text);
        view.show();
    }

    private void hostEvaAnchor() {

        final AtomicBoolean isHosting = new AtomicBoolean(true);

        new Handler().postDelayed(() -> {
            if (isHosting.get()) {
                showViewHostLookingAtTarget(R.string.move_around);
            }
        }, 5000);

        ManagedAnchor<SXRAnchor> managedAnchor;
        try {
            // Get the model matrix from the actual eva's position and create an anchor to be
            // hosted by Cloud Anchor service
            float[] anchorMatrix = mEvaContext.getEvaController().getView().getTransform().getModelMatrix();
            SXRAnchor anchor = mSharedMixedReality.createAnchor(anchorMatrix);
            managedAnchor = new ManagedAnchor<>(EvaObjectType.EVA, anchor);
        } catch (Throwable throwable) {
            isHosting.set(false);
            onHostingError(new CloudAnchorException(throwable));
            return;
        }

        Log.d(TAG, "Hosting eva anchor");
        new CloudAnchorManager(mEvaContext).hostAnchors(managedAnchor, new CloudAnchorManager.OnCloudAnchorCallback<SXRAnchor>() {
            @Override
            public void onResult(ManagedAnchor<SXRAnchor> managedAnchor) {
                isHosting.set(false);
                showViewHostLookingAtTarget(R.string.stay_in_position, "#5cffba");
                shareEvaAnchorWithGuests(managedAnchor.getAnchor());
            }

            @Override
            public void onError(CloudAnchorException e) {
                isHosting.set(false);
                onHostingError(e);
            }
        });
    }

    private void onHostingError(CloudAnchorException e) {
        Log.e(TAG, "Error hosting eva anchor", e);
        showViewSharingError(
                this::cancelSharing,
                () -> {
                    showViewHostLookingAtTarget(R.string.center_eva);
                    new Handler().postDelayed(this::hostEvaAnchor, 1500);
                }
        );
        handleCloudAnchorException(e);
    }

    private void resolveEvaAnchor(EvaAnchorReceivedMessage message) {

        ManagedAnchor<CloudAnchor> managedAnchor = new ManagedAnchor<>(EvaObjectType.EVA, message.getEvaAnchor());

        new CloudAnchorManager(mEvaContext).resolveAnchors(managedAnchor, new CloudAnchorManager.OnCloudAnchorCallback<SXRAnchor>() {
            @Override
            public void onResult(ManagedAnchor<SXRAnchor> managedAnchor) {
                Log.d(TAG, "Anchor resolved successfully");

                RequestStatus requestStatus = message.getRequestStatus();
                requestStatus.setStatus(RequestStatus.STATUS_OK);
                mMessageService.sendRequestStatus(requestStatus);

                mSharedMixedReality.startSharing(
                        managedAnchor.getAnchor(), EvaConstants.SHARE_MODE_GUEST);

                gotToHudView();
            }

            @Override
            public void onError(CloudAnchorException e) {
                showViewSharingError(
                        () -> cancelSharing(),
                        () -> {
                            showViewGuestLookingAtTarget();
                            new Handler().postDelayed(() -> resolveEvaAnchor(message), 1500);
                        });
                handleCloudAnchorException(e);
            }
        });
    }

    private void handleCloudAnchorException(CloudAnchorException e) {
        mEvaContext.runOnEvaThread(() -> {
            if (e.getCause() instanceof NetworkException) {
                Toast.makeText(mEvaContext.getActivity(),
                        R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            } else if (e.getCause().getClass().getSimpleName().equals("NotTrackingException")) {
                Toast.makeText(mEvaContext.getActivity(),
                        R.string.not_tracking, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onEnter() {
        EventBusUtils.register(this);
    }

    @Override
    protected void onExit() {
        EventBusUtils.unregister(this);
    }

    @Override
    protected void onHandleOrientation(SXRCameraRig cameraRig) {
    }

    private void onConnectionEstablished() {

        showViewConnectionFound();

        if (mCurrentMode == EvaConstants.SHARE_MODE_HOST) {
            new Handler().postDelayed(this::startHostSharingFlow, 3000);
        }
    }

    private void showViewConnectionFound() {

        int total = mConnectionManager.getTotalConnected();

        IConnectionFoundView view = mSharingAnchorViewController.makeView(IConnectionFoundView.class);
        int pluralsText = mCurrentMode == EvaConstants.SHARE_MODE_GUEST ? R.plurals.hosts_found : R.plurals.guests_found;
        view.setStatusText(mResources.getQuantityString(pluralsText, total, total));
        view.show();
    }

    private void startHostSharingFlow() {

        showViewHostLookingAtTarget(R.string.center_eva);

        // Make the guests wait while host prepare eva anchor
        Log.d(TAG, "Request to show remote view: " + ViewCommand.SHOW_VIEW_LOOKING_AT_TARGET);
        mMessageService.sendViewCommand(new ViewCommand(ViewCommand.SHOW_VIEW_LOOKING_AT_TARGET));

        hostEvaAnchor();
    }

    private void shareEvaAnchorWithGuests(SXRAnchor hostedAnchor) {
        Log.d(TAG, "Sharing eva anchor with guests");
        CloudAnchor cloudAnchor = new CloudAnchor(hostedAnchor.getCloudAnchorId(), EvaObjectType.EVA);

        int requestId = mMessageService.shareEvaAnchor(cloudAnchor);
        mEvaAnchorSharingStatusHandler = new EvaAnchorSharingStatusHandler(
                mConnectionManager.getTotalConnected(), requestId, hostedAnchor);
    }

    private void cancelSharing() {

        // Turn sharing OFF
        mSharedMixedReality.stopSharing();

        // Disconnect from remotes
        if (mCurrentMode == EvaConstants.SHARE_MODE_GUEST) {
            mConnectionManager.stopFindInvitationAndDisconnect();
            mConnectionManager.disconnect();
        } else {
            mConnectionManager.stopInvitationAndDisconnect();
        }

        mCurrentMode = EvaConstants.SHARE_MODE_NONE;
        gotToHudView();

        Log.d(TAG, "Sharing canceled");
    }

    private void gotToHudView() {
        mEvaContext.getSXRContext().runOnGlThread(() -> mBackToHudModeListener.OnBackToHud());
    }

    private void updateTotalConnectedUI() {
        if (mCurrentMode == EvaConstants.SHARE_MODE_HOST) {
            IView view = mSharingAnchorViewController.getCurrentView();
            if (view instanceof IWaitingForGuestView) {
                ((IWaitingForGuestView) view).setTotalConnected(
                        mConnectionManager.getTotalConnected());
            }
        }
    }

    private void onNoConnectionFound() {
        if (mCurrentMode == EvaConstants.SHARE_MODE_GUEST) {
            mConnectionManager.findInvitationThenConnect();
        }
    }

    private void onAllConnectionLost() {
        String text = mResources.getString(mCurrentMode == EvaConstants.SHARE_MODE_GUEST
                ? R.string.view_host_disconnected
                : R.string.view_guests_disconnected);
        showViewSharingFinished(text);
        mEvaAnchorSharingStatusHandler = null;
    }

    @SuppressLint("SwitchIntDef")
    @Subscribe
    public void handleConnectionEvent(EvaConnectionEvent message) {
        switch (message.getType()) {
            case EVENT_CONNECTION_ESTABLISHED:
                onConnectionEstablished();
                break;
            case EVENT_NO_CONNECTION_FOUND:
                //showViewNoConnectionFound();
                onNoConnectionFound();
                break;
            case EVENT_GUEST_CONNECTION_ESTABLISHED:
            case EVENT_ONE_CONNECTION_LOST:
                updateTotalConnectedUI();
                if (mEvaAnchorSharingStatusHandler != null) {
                    mEvaAnchorSharingStatusHandler.decrementTotalPending();
                }
                break;
            case EVENT_ALL_CONNECTIONS_LOST:
                onAllConnectionLost();
                break;
            case EVENT_ON_LISTENING_TO_GUESTS:
                showViewWaitingForGuest();
                break;
            case EVENT_ON_REQUEST_CONNECTION_TO_HOST:
                showViewWaitingForHost();
                break;
            case EVENT_ENABLE_BLUETOOTH_DENIED:
                Toast.makeText(mEvaContext.getActivity(),
                        R.string.bluetooth_disabled, Toast.LENGTH_LONG).show();
                break;
            case EVENT_HOST_VISIBILITY_DENIED:
                Toast.makeText(mEvaContext.getActivity(),
                        R.string.device_not_visible, Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    @Subscribe
    public void handleReceivedMessage(ViewCommandReceivedMessage message) {
        ViewCommand command = message.getViewCommand();
        Log.d(TAG, "View command received: " + command);
        switch (command.getType()) {
            case ViewCommand.SHOW_VIEW_LOOKING_AT_TARGET:
                showViewGuestLookingAtTarget();
                break;
            default:
                Log.d(TAG, "Unknown view command: " + command.getType());
                break;
        }
    }

    @Subscribe
    public void handleReceivedMessage(EvaAnchorReceivedMessage message) {
        resolveEvaAnchor(message);
    }

    @Subscribe
    public void handleReceivedMessage(RequestStatusReceivedMessage message) {
        if (mEvaAnchorSharingStatusHandler != null) {
            mEvaAnchorSharingStatusHandler.handle(message.getRequestStatus());
        }
    }

    private class EvaAnchorSharingStatusHandler {

        int mTotalPendingStatus;
        int mRequestId;
        SXRAnchor mResolvedAnchor;
        ReentrantReadWriteLock mLock = new ReentrantReadWriteLock();

        EvaAnchorSharingStatusHandler(int mTotalPendingStatus, int mRequestId, SXRAnchor resolvedAnchor) {
            this.mTotalPendingStatus = mTotalPendingStatus;
            this.mRequestId = mRequestId;
            this.mResolvedAnchor = resolvedAnchor;
        }

        void decrementTotalPending() {
            mLock.writeLock().lock();
            try {
                mTotalPendingStatus--;
            } finally {
                mLock.writeLock().unlock();
            }
        }

        void handle(RequestStatus status) {
            Log.d(TAG, "Request status received: " + status);
            if (status.getRequestId() == mRequestId) {
                decrementTotalPending();
                mLock.readLock().lock();
                try {
                    if (mTotalPendingStatus == 0) {
                        mSharedMixedReality.startSharing(mResolvedAnchor, EvaConstants.SHARE_MODE_HOST);
                        // Sharing succeeded, back host to hud view
                        gotToHudView();
                        mEvaAnchorSharingStatusHandler = null;
                    }
                } finally {
                    mLock.readLock().unlock();
                }
            }
        }
    }

    @FunctionalInterface
    private interface OnCancelCallback {
        void onCancel();
    }

    @FunctionalInterface
    private interface OnRetryCallback {
        void onRetry();
    }
}
