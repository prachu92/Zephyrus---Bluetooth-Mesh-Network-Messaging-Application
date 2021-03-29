package com.blemesh.sdk.app.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.blemesh.sdk.R;
import com.blemesh.sdk.app.BleMeshService;
import com.blemesh.sdk.app.adapter.PeerAdapter;
import com.blemesh.sdk.mesh_graph.Peer;
import com.blemesh.sdk.transport.Transport;

/**
 * A Fragment that supports discovering peers and sending or receiving data to/from them.
 *
 * The three static creators instantiate the Fragment in one of three modes:
 *
 * <ul>
 *     <li> SEND : The Fragment is created with a username, service name and data payload that will
 *          be sent to a peer the user selects. Completion will be indicated by the callback method
 *          {@link com.blemesh.sdk.app.ui.PeerFragment.PeerFragmentListener#onFinished(Exception)}
 *     </li>
 *
 *     <li> RECEIVE : The Fragment is created with a username and service name and will await transfer
 *          from a sending peer. Completion will be indicated by the callback method
 *          {@link com.blemesh.sdk.app.ui.PeerFragment.PeerFragmentListener#onFinished(Exception)}
 *     </li>
 *
 *     <li> BOTH : The Fragment is created with a username and service name and will await transfer
 *          from a sending peer and request data to send when a receiving peer is selected.
 *          Completion will only be indicated in case of error by the callback method
 *          {@link com.blemesh.sdk.app.ui.PeerFragment.PeerFragmentListener#onFinished(Exception)}
 *     </li>
 * </ul>
 *
 * An Activity that hosts PeerFragment must implement
 * {@link com.blemesh.sdk.app.ui.PeerFragment.PeerFragmentListener}
 */
public class PeerFragment extends BleMeshFragment implements BleMeshService.Callback,
                                                              BleMeshFragment.Callback {

    /** Bundle parameters */
    // BleMeshFragment provides username, servicename
    private static final String ARG_MODE    = "mode";
    private static final String ARG_PAYLOAD = "payload";

    public enum Mode { SEND, RECEIVE, BOTH }

    public interface PeerFragmentListener {
        /**
         * A transfer was received from a peer.
         * Called when mode is {@link Mode#RECEIVE} or {@link Mode#BOTH}
         */
        void onDataReceived(@NonNull PeerFragment fragment,
                            @Nullable byte[] payload,
                            @NonNull String sourceAddress,
                            @NonNull Peer sender);

        /**
         * A transfer was sent to a peer.
         * Called when mode is {@link Mode#SEND} or {@link Mode#BOTH}
         */
        void onDataSent(@NonNull PeerFragment fragment,
                        @Nullable byte[] data,
                        @NonNull Peer recipient,
                        @NonNull Peer desc);

        /**
         * The user selected recipient to receive data. Provide that data in a call
         * to {@link #sendDataToPeer(byte[], Peer)}
         * Called when mode is {@link Mode#BOTH}
         */
        void onDataRequestedForPeer(@NonNull PeerFragment fragment,
                                    @NonNull Peer recipient);

        /**
         * The fragment is complete and should be removed by the host Activity.
         *
         * If exception is null, the fragment has completed it's requested operation,
         * else an error occurred.
         */
        void onFinished(@NonNull PeerFragment fragment,
                        @Nullable Exception exception);

        void onNewLog(@NonNull String logText);
    }

    private ViewGroup emptyContainer;
    private RecyclerView recyclerView;
    private PeerAdapter peerAdapter;
    private PeerFragmentListener callback;
    private BleMeshService.ServiceBinder serviceBinder;

    private Mode mode;

    private byte[] payload;

    public static PeerFragment toSend(@NonNull byte[] toSend,
                                      @NonNull String username,
                                      @NonNull String serviceName) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_PAYLOAD, toSend);
        return init(bundle, Mode.SEND, username, serviceName);
    }

    public static PeerFragment toReceive(@NonNull String username,
                                         @NonNull String serviceName) {
        return init(null, Mode.RECEIVE, username, serviceName);
    }

    public static PeerFragment toSendAndReceive(@NonNull String username,
                                                @NonNull String serviceName) {
        return init(null, Mode.BOTH, username, serviceName);
    }

    private static PeerFragment init(@Nullable Bundle bundle,
                                     @NonNull Mode mode,
                                     @NonNull String username,
                                     @NonNull String serviceName) {

        if (bundle == null) bundle = new Bundle();

        bundle.putSerializable(ARG_MODE, mode);
        bundle.putString(BleMeshFragment.ARG_USERNAME, username);
        bundle.putString(BleMeshFragment.ARG_SERVICENAME, serviceName);

        PeerFragment fragment = new PeerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public PeerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBleMeshCallback(this);
        if (getArguments() != null) {
            mode = (Mode) getArguments().getSerializable(ARG_MODE);
            payload = (byte[]) getArguments().getSerializable(ARG_PAYLOAD);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Context context = getActivity();
        View root = inflater.inflate(R.layout.fragment_peer, container, false);
        peerAdapter = new PeerAdapter(context, new ArrayList<Peer>());
        emptyContainer = (ViewGroup) root.findViewById(R.id.empty_container);
        recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(peerAdapter);

        peerAdapter.setOnPeerViewClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPeerSelected((Peer) v.getTag());
            }
        });
        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (PeerFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PeerFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    /**
     * Send data to recipient. Used when mode is {@link Mode#BOTH}.
     *
     * Should be called by a client in response to the PeerFragmentCallback method:
     * {@link com.blemesh.sdk.app.ui.PeerFragment.PeerFragmentListener#onDataRequestedForPeer(PeerFragment, Peer)}
     */
    public void sendDataToPeer(byte[] data, Peer recipient) {
        serviceBinder.send(data, recipient);
    }

    /** An available peer was selected from {@link com.blemesh.sdk.app.adapter.PeerAdapter} */
    public void onPeerSelected(Peer peer) {
        switch (mode) {
            case SEND:

                serviceBinder.send(payload, peer);
                break;

            case BOTH:

                callback.onDataRequestedForPeer(this, peer);
                break;

            case RECEIVE:
                // do nothing
                break;
        }
    }

    @Override
    public void onDataRecevied(@NonNull BleMeshService.ServiceBinder binder, byte[] data, Date date, @NonNull String sourceAddress, @NonNull Peer sender, Exception exception) {
        if (callback == null) return; // Fragment was detached but not destroyed

        callback.onDataReceived(this, data, sourceAddress,sender);

/*        if (mode == Mode.RECEIVE)
            callback.onFinished(this, null);*/
    }

    @Override
    public void onDataSent(@NonNull BleMeshService.ServiceBinder binder, byte[] data, @NonNull Peer recipient, @NonNull Peer desc, Exception exception) {
        if (callback == null) return; // Fragment was detached but not destroyed
        callback.onDataSent(this, data, recipient, desc);

/*        if (mode == Mode.SEND)
            callback.onFinished(this, null);*/
    }

    @Override
    public void onPeerStatusUpdated(@NonNull BleMeshService.ServiceBinder binder, @NonNull Peer peer, @NonNull Transport.ConnectionStatus newStatus, boolean peerIsHost) {
        switch (newStatus) {
            case CONNECTED:
                peerAdapter.notifyPeerAdded(peer);

                emptyContainer.setVisibility(View.GONE);
                break;

            case DISCONNECTED:
                peerAdapter.notifyPeerRemoved(peer);
                if (peerAdapter.getItemCount() == 0) {
                    emptyContainer.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public void onPeersStatusUpdated(@NonNull BleMeshService.ServiceBinder binder,
                                     @NonNull Map<String, Peer> vertexes,
                                     boolean isJoinAction){
        peerAdapter.notifyPeersUpdated(vertexes,isJoinAction);
        if(isJoinAction){
            emptyContainer.setVisibility(View.GONE);
        }else{
            if (peerAdapter.getItemCount() == 0) { //only local node left;
                emptyContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onNewLog(@NonNull String logText) {
        callback.onNewLog(logText);
    }

    @Override
    public void onServiceReady(@NonNull BleMeshService.ServiceBinder serviceBinder) {
        this.serviceBinder = serviceBinder;
        this.serviceBinder.setCallback(this);

        switch (mode) {
            case SEND:
                this.serviceBinder.scanForOtherUsers();
                break;

            case RECEIVE:
                this.serviceBinder.advertiseLocalUser();
                break;

            case BOTH:
                this.serviceBinder.startTransport();
        }
    }

    @Override
    public void onFinished(Exception e) {
        if (callback == null) return; // Fragment was detached but not destroyed
        callback.onFinished(this, e);
    }

}
