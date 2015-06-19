package com.kamcord.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kamcord.app.R;
import com.kamcord.app.activity.LoginActivity;
import com.kamcord.app.adapter.ProfileAdapter;
import com.kamcord.app.model.ProfileItem;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.PaginatedVideoList;
import com.kamcord.app.server.model.StatusCode;
import com.kamcord.app.server.model.User;
import com.kamcord.app.server.model.Video;
import com.kamcord.app.service.UploadService;
import com.kamcord.app.thread.Uploader;
import com.kamcord.app.utils.AccountListener;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.ActiveRecordingSessionManager;
import com.kamcord.app.view.DynamicRecyclerView;
import com.kamcord.app.view.utils.ProfileLayoutSpanSizeLookup;
import com.kamcord.app.view.utils.ProfileViewItemDecoration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by donliang1 on 5/6/15.
 */
public class ProfileFragment extends Fragment implements AccountListener, Uploader.UploadStatusListener {

    private static final int HEADER_EXISTS = 1;

    @InjectView(R.id.signInPromptContainer)
    ViewGroup signInPromptContainer;
    @InjectView(R.id.signInPromptButton)
    Button signInPromptButton;
    @InjectView(R.id.profilefragment_refreshlayout)
    SwipeRefreshLayout videoFeedRefreshLayout;
    @InjectView(R.id.profile_recyclerview)
    DynamicRecyclerView profileRecyclerView;

    private static final String TAG = ProfileFragment.class.getSimpleName();
    private List<ProfileItem> mProfileList = new ArrayList<>();
    private List<RecordingSession> currentUploads = new ArrayList<>();
    private ProfileAdapter mProfileAdapter;
    private ProfileItem<User> userHeader;
    private String nextPage;
    private int totalItems = 0;
    private boolean footerVisible = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.profile_tab, container, false);
        AccountManager.addListener(this);
        ButterKnife.inject(this, root);
        initKamcordProfileFragment();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        AccountManager.removeListener(this);
        }

    @Override
    public void onResume() {
        super.onResume();
        marshalActiveSessions();
        Uploader.subscribe(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Uploader.unsubscribe(this);
    }

    public void initKamcordProfileFragment() {

        if(AccountManager.isLoggedIn()) {
            userHeader = new ProfileItem<>(ProfileItem.Type.HEADER, (User) null);
            mProfileList.add(userHeader);
            signInPromptContainer.setVisibility(View.GONE);
            Account myAccount = AccountManager.getStoredAccount();
            AppServerClient.getInstance().getUserInfo(myAccount.id, new GetUserInfoCallBack());
            AppServerClient.getInstance().getUserVideoFeed(myAccount.id, null, new GetUserVideoFeedCallBack());
        } else {
            signInPromptContainer.setVisibility(View.VISIBLE);
        }

        mProfileAdapter = new ProfileAdapter(getActivity(), mProfileList);
        profileRecyclerView.setAdapter(mProfileAdapter);
        profileRecyclerView.setSpanSizeLookup(new ProfileLayoutSpanSizeLookup(profileRecyclerView));
        profileRecyclerView.addItemDecoration(new ProfileViewItemDecoration(getResources().getDimensionPixelSize(R.dimen.grid_margin)));

        videoFeedRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(R.dimen.refreshEnd));
        videoFeedRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.refreshColor));
        videoFeedRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (AccountManager.isLoggedIn()) {
                    videoFeedRefreshLayout.setRefreshing(true);
                    marshalActiveSessions();
                    Account myAccount = AccountManager.getStoredAccount();
                    AppServerClient.AppServer client = AppServerClient.getInstance();
                    client.getUserInfo(myAccount.id, new GetUserInfoCallBack());
                    client.getUserVideoFeed(myAccount.id, null, new SwipeToRefreshVideoFeedCallBack());
                    checkProcessingSessions();
                } else {
                    videoFeedRefreshLayout.setRefreshing(false);
                }
            }
        });
        
        profileRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int state) {
                }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (profileRecyclerView.getChildAt(0) != null) {
                    int tabsHeight = getResources().getDimensionPixelSize(R.dimen.tabsHeight);
                    videoFeedRefreshLayout.setEnabled(profileRecyclerView.getChildAdapterPosition(profileRecyclerView.getChildAt(0)) == 0
                            && profileRecyclerView.getChildAt(0).getTop() == tabsHeight);
                } else {
                    videoFeedRefreshLayout.setEnabled(true);
                }

                if ((mProfileList.size() - 1) < totalItems
                        && nextPage != null
                        && ((GridLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() == (mProfileList.size() - 1)
                        && !footerVisible) {
                    loadMoreItems();
                }
            }
        });
    }

    private void marshalActiveSessions() {
        boolean modified = false;
        {
            Iterator<ProfileItem> iterator = mProfileList.iterator();
            while (iterator.hasNext()) {
                ProfileItem item = iterator.next();
                if (item.getType() == ProfileItem.Type.UPLOAD_PROGRESS) {
                    iterator.remove();
                    modified = true;
                }
            }
        }

        Set<RecordingSession> activeSessions = ActiveRecordingSessionManager.getActiveSessions();
        Set<RecordingSession> queuedSessions = new HashSet<>();
        RecordingSession currentSession = null;
        UploadService uploadService = UploadService.getInstance();
        if (uploadService != null) {
            queuedSessions = new HashSet<>(uploadService.getQueuedSessions());
            currentSession = uploadService.getCurrentlyUploadingSession();
        }

        for (RecordingSession session : activeSessions) {
            if (session.getState() == RecordingSession.State.SHARED
                    && !queuedSessions.contains(session)
                    && !session.equals(currentSession)) {
                session.setUploadProgress(RecordingSession.UPLOAD_FAILED_PROGRESS);
                addToProfileList(new ProfileItem<>(ProfileItem.Type.UPLOAD_PROGRESS, session));
                modified = true;

            } else if (session.getState() == RecordingSession.State.UPLOADED) {
                session.setUploadProgress(RecordingSession.UPLOAD_PROCESSING_PROGRESS);
                addToProfileList(new ProfileItem<>(ProfileItem.Type.UPLOAD_PROGRESS, session));
                modified = true;
            }
        }

        if (uploadService != null) {
            for (RecordingSession queuedSession : uploadService.getQueuedSessions()) {
                queuedSession.setUploadProgress(-1f);
                addToProfileList(new ProfileItem<>(ProfileItem.Type.UPLOAD_PROGRESS, queuedSession));
                modified = true;
            }
            addToProfileList(new ProfileItem<>(ProfileItem.Type.UPLOAD_PROGRESS, uploadService.getCurrentlyUploadingSession()));
        }

        if (modified) {
            mProfileAdapter.notifyDataSetChanged();
        }
    }

    public void addToProfileList(ProfileItem item)
    {
        if(mProfileList.size() > 0)
            mProfileList.add(1, item);
        else
            mProfileList.add(item);
    }

    public void checkProcessingSessions() {
        Set<RecordingSession> activeSessions = ActiveRecordingSessionManager.getActiveSessions();
        for (RecordingSession activeSession : activeSessions) {
            if (activeSession.getState() == RecordingSession.State.UPLOADED) {
                AppServerClient.getInstance().getVideoInfo(activeSession.getGlobalId(), new VideoProcessingDoneCallback(activeSession));
            }
        }
    }

    private void removeProcessedSession(RecordingSession session) {
        int index = 0;
        Iterator<ProfileItem> iterator = mProfileList.iterator();
        while (iterator.hasNext()) {
            ProfileItem item = iterator.next();
            if (item.getType() == ProfileItem.Type.UPLOAD_PROGRESS
                    && item.getSession().equals(session)) {
                iterator.remove();
                mProfileAdapter.notifyItemRemoved(index);
                break;
            }
            index++;
        }
    }

    public void loadMoreItems() {
        footerVisible = true;
        mProfileList.add(new ProfileItem<>(ProfileItem.Type.FOOTER, null));
        mProfileAdapter.notifyItemInserted(mProfileAdapter.getItemCount());
        Account myAccount = AccountManager.getStoredAccount();
        AppServerClient.getInstance().getUserVideoFeed(myAccount.id, nextPage, new GetUserVideoFeedCallBack());
    }

    @Override
    public void onLoggedInChanged(boolean state) {
        if (state) {
            userHeader = new ProfileItem<User>(ProfileItem.Type.HEADER, null);
            mProfileList.add(userHeader);
            signInPromptContainer.setVisibility(View.GONE);
            Account myAccount = AccountManager.getStoredAccount();
            AppServerClient.getInstance().getUserInfo(myAccount.id, new GetUserInfoCallBack());
            AppServerClient.getInstance().getUserVideoFeed(myAccount.id, null, new GetUserVideoFeedCallBack());
        } else {
            signInPromptContainer.setVisibility(View.VISIBLE);
        }
    }

    private class GetUserInfoCallBack implements Callback<GenericResponse<User>> {
        @Override
        public void success(GenericResponse<User> userResponse, Response response) {
            if (userResponse != null && userResponse.response != null && userHeader != null && isResumed()) {
                userHeader.setUser(userResponse.response);
                if (userHeader.getUser() != null) {
                totalItems = userHeader.getUser().video_count;
                } else {
                    totalItems = 0;
                }
                mProfileAdapter.notifyItemChanged(0);
                videoFeedRefreshLayout.setRefreshing(false);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG, "  " + error.toString());
            if( isResumed() ) {
                videoFeedRefreshLayout.setRefreshing(false);
            }
        }
    }

    private class SwipeToRefreshVideoFeedCallBack implements Callback<GenericResponse<PaginatedVideoList>> {
        @Override
        public void success(GenericResponse<PaginatedVideoList> paginatedVideoListGenericResponse, Response response) {
            if (paginatedVideoListGenericResponse != null
                    && paginatedVideoListGenericResponse.response != null
                    && paginatedVideoListGenericResponse.response.video_list != null
                    && isResumed()) {
                Iterator<ProfileItem> iterator = mProfileList.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().getType() == ProfileItem.Type.VIDEO) {
                        iterator.remove();
                    }
                }
                nextPage = paginatedVideoListGenericResponse.response.next_page;
                for (Video video : paginatedVideoListGenericResponse.response.video_list) {
                ProfileItem profileViewModel = new ProfileItem<>(ProfileItem.Type.VIDEO, video);
                    mProfileList.add(profileViewModel);
                }
                footerVisible = false;
                mProfileAdapter.notifyDataSetChanged();
                videoFeedRefreshLayout.setRefreshing(false);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG, "  " + error.toString());
            if( isResumed() ) {
                videoFeedRefreshLayout.setRefreshing(false);
            }
        }
    }

    private class GetUserVideoFeedCallBack implements Callback<GenericResponse<PaginatedVideoList>> {
        @Override
        public void success(GenericResponse<PaginatedVideoList> paginatedVideoListGenericResponse, Response response) {
            if (paginatedVideoListGenericResponse != null
                    && paginatedVideoListGenericResponse.response != null
                    && paginatedVideoListGenericResponse.response.video_list != null
                    && isResumed() ) {
                nextPage = paginatedVideoListGenericResponse.response.next_page;
                if (mProfileList.get(mProfileAdapter.getItemCount() - 1).getType() == ProfileItem.Type.FOOTER) {
                    mProfileList.remove(mProfileAdapter.getItemCount() - 1);
                }
                for (Video video : paginatedVideoListGenericResponse.response.video_list) {
                    ProfileItem profileViewModel = new ProfileItem<>(ProfileItem.Type.VIDEO, video);
                    mProfileList.add(profileViewModel);
                }
                footerVisible = false;
                mProfileAdapter.notifyDataSetChanged();
                videoFeedRefreshLayout.setRefreshing(false);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG, "  " + error.toString());
            if( isResumed() ) {
                videoFeedRefreshLayout.setRefreshing(false);
            }
        }
    }

    private class VideoProcessingDoneCallback implements Callback<GenericResponse<Video>> {

        private RecordingSession session;

        public VideoProcessingDoneCallback(RecordingSession session) {
            this.session = session;
        }

        @Override
        public void success(GenericResponse<Video> responseWrapper, Response response) {
            if (responseWrapper != null && responseWrapper.response != null
                    && responseWrapper.status != null && responseWrapper.status.equals(StatusCode.OK)
                    && isResumed()) {
                if (responseWrapper.response.video_state == Video.State.PROCESSED) {
                    session.setState(RecordingSession.State.PROCESSED);
                    ActiveRecordingSessionManager.updateActiveSession(session);
                    removeProcessedSession(session);
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {
            // Do nothing.
        }
    }

    @OnClick(R.id.signInPromptButton)
    public void showSignInPrompt() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onUploadStart(RecordingSession recordingSession) {
        updateUploadingSessionProgress(recordingSession, 0f);
}

    @Override
    public void onUploadProgress(RecordingSession recordingSession, float progress) {
        updateUploadingSessionProgress(recordingSession, progress);
    }

    @Override
    public void onUploadFinish(RecordingSession recordingSession, boolean success) {
        if (success) {
            updateUploadingSessionProgress(recordingSession, RecordingSession.UPLOAD_PROCESSING_PROGRESS);
        } else {
            updateUploadingSessionProgress(recordingSession, RecordingSession.UPLOAD_FAILED_PROGRESS);
        }
    }

    private void updateUploadingSessionProgress(RecordingSession session, float progress) {
        boolean updated = false;
        int index = 0;
        for (ProfileItem item : mProfileList) {
            if (item.getType() == ProfileItem.Type.UPLOAD_PROGRESS
                    && session.equals(item.getSession())) {
                item.getSession().setUploadProgress(progress);
                mProfileAdapter.notifyItemChanged(index);
                updated = true;
                break;
            }
            index++;
        }
        if (!updated) {
            marshalActiveSessions();
        }
    }
}
