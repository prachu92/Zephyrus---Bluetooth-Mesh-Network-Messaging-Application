package com.blemesh.zephyrus.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.blemesh.zephyrus.R;
import com.blemesh.zephyrus.database.DbManager;
import com.blemesh.zephyrus.ui.adapter.MessageAdapter;
import com.blemesh.sdk.mesh_graph.Peer;
import timber.log.Timber;

public class MessageFragment extends Fragment {

    public interface MessageFragmentCallback {
        public void onMessageSendRequested(String message,Peer peer);
    }

    private DbManager dbManager;
    private MessageAdapter mAdapter;
    private MessageFragmentCallback callback;

    private RecyclerView mRecyclerView;
    private EditText mMessageEntry;
    private View mRoot;

    private Peer peer;

    public MessageFragment(){}

    public void setDbManager(DbManager dbManager){
        this.dbManager = dbManager;
    }
    public void setPeer(Peer peer){
        this.peer = peer;
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

        mRoot = inflater.inflate(R.layout.fragment_messagelist, container, false);

        mMessageEntry = (EditText) mRoot.findViewById(R.id.messageEntry);
        mMessageEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage(v.getText().toString());
                    v.setText("");
                    return true;
                }
                return false;
            }
        });
        mRoot.findViewById(R.id.sendMessageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendMessageButtonClick(v);
            }
        });

        mRecyclerView = (RecyclerView) mRoot.findViewById(R.id.messagelist_recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new MessageAdapter(getContext(),dbManager,peer.getAlias());
        mRecyclerView.setAdapter(mAdapter);
        return mRoot;
    }


    public void onSendMessageButtonClick(View v) {
        sendMessage(mMessageEntry.getText().toString());
        mMessageEntry.setText("");
    }

    private void sendMessage(String message) {
        if (message.length() == 0) return;
        Timber.i("Sending message %s", message);
        callback.onMessageSendRequested(message,peer);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            callback = (MessageFragmentCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ChatFragmentCallback");
        }
    }

}
