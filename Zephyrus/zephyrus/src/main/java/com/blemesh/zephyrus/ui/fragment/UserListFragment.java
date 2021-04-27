package com.blemesh.zephyrus.ui.fragment;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blemesh.zephyrus.R;
import com.blemesh.zephyrus.database.DbManager;
import com.blemesh.zephyrus.ui.adapter.UserListAdapter;

public class UserListFragment extends Fragment implements UserListAdapter.MessageSelectedListener{

    public interface UserListFragmentCallback {
        public void onPeerSelected(View identiconView, View usernameView, String peerAddress);
    }

    private UserListFragmentCallback mCallback;
    private DbManager dbManager;
    private RecyclerView mRecyclerView;
    private UserListAdapter mAdapter;
    private View mRoot;

    public UserListFragment(){

    }

    public void setDbManager(DbManager dbManager){
        this.dbManager = dbManager;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (dbManager == null)
            throw new IllegalStateException("MessageListFragment must be equipped with a DataStore. Did you call #setDataStore");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mRoot = inflater.inflate(R.layout.fragment_userlist, container, false);
        mRecyclerView = (RecyclerView) mRoot.findViewById(R.id.userList_recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new UserListAdapter(getActivity(), dbManager, this);
        mRecyclerView.setAdapter(mAdapter);
        return mRoot;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (UserListFragmentCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ChatFragmentCallback");
        }
    }

    @Override
    public void onPeerSelected(View identiconView, View usernameView, String peerAddress) {
        mCallback.onPeerSelected(identiconView,usernameView,peerAddress);
    }
    //</editor-fold>
    public void animateIn() {
        mRoot.setAlpha(0);
        ObjectAnimator animator = ObjectAnimator.ofFloat(mRoot, "alpha", 0f, 1f)
                .setDuration(300);

        animator.setStartDelay(550);
        animator.start();
    }
}
