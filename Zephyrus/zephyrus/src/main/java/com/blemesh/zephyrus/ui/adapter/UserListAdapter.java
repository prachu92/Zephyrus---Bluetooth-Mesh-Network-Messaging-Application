package com.blemesh.zephyrus.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.delight.android.identicons.SymmetricIdenticon;
import com.blemesh.zephyrus.R;
import com.blemesh.zephyrus.database.DbManager;
import com.blemesh.zephyrus.database.PeerTable;

public class UserListAdapter extends BaseAbstractRecycleCursorAdapter<UserListAdapter.ViewHolder>{

    public interface MessageSelectedListener {
        void onPeerSelected(View identiconView, View usernameView, String peerAddress);
    }

    private DbManager dbManager;
    private RecyclerView mHost;
    private MessageSelectedListener messageSelectedListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View container;
        public TextView alias_view;
        public TextView info_view;
        public TextView last_message;
        public SymmetricIdenticon identicon;
        String peerAddress;
        boolean isAvailable;
        public ViewHolder(View itemView) {
            super(itemView);
            container = itemView;
            alias_view = (TextView) itemView.findViewById(R.id.peer_alias);
            info_view = (TextView) itemView.findViewById(R.id.peer_info);
            last_message = (TextView) itemView.findViewById(R.id.last_message);
            identicon =  (SymmetricIdenticon) itemView.findViewById(R.id.identicon_user);
        }
    }


    public UserListAdapter(@NonNull Context context,
                           @NonNull DbManager dbManager,
                           @Nullable MessageSelectedListener listener)
    {
        super(context, dbManager.getAvailablePeersCursor());
        this.dbManager = dbManager;
        this.messageSelectedListener = listener;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mHost = recyclerView;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        if (holder.peerAddress == null)
            holder.peerAddress = cursor.getString(cursor.getColumnIndexOrThrow(PeerTable.COLUMN_NAME_ALIAS));
        holder.container.setTag(R.id.view_tag_peer_id, holder.peerAddress);
        String alias,user_info,isOnline,lastMessage;
        holder.isAvailable = cursor.getInt(cursor.getColumnIndexOrThrow(PeerTable.COLUMN_NAME_IS_AVAILABLE)) == 1;
        int color;
        if(holder.isAvailable){
            isOnline = mContext.getString(R.string.online);
            color = mContext.getResources().getColor(R.color.remote_online);
        }else{
            isOnline = mContext.getString(R.string.offline);
            color = mContext.getResources().getColor(R.color.remote_offline);
        }
        holder.container.setTag(R.id.view_tag_peer_is_available,holder.isAvailable);

        alias = cursor.getString(cursor.getColumnIndexOrThrow(PeerTable.COLUMN_NAME_ALIAS));

        holder.alias_view.setText(mContext.getString(R.string.user_alias,alias,isOnline));
        holder.alias_view.setTextColor(color);
        user_info = mContext.getString(R.string.user_info,
                cursor.getInt(cursor.getColumnIndexOrThrow(PeerTable.COLUMN_NAME_HOPS)),
                cursor.getInt(cursor.getColumnIndexOrThrow(PeerTable.COLUMN_NAME_RSSI)));
        lastMessage = cursor.getString(cursor.getColumnIndexOrThrow(PeerTable.COLUMN_NAME_LAST_MESSAGE));
        holder.info_view.setText(user_info);
        holder.identicon.show(alias);
        holder.last_message.setText(lastMessage);
    }

    @Override
    protected void onContentChanged() {
        changeCursor(dbManager.getAvailablePeersCursor());
        mHost.smoothScrollToPosition(0);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(boolean)v.getTag(R.id.view_tag_peer_is_available)){
                    Snackbar.make(v,mContext.getString(R.string.peer_status_offline),Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (messageSelectedListener != null )
                    messageSelectedListener.onPeerSelected(v.findViewById(R.id.identicon_user),
                            v.findViewById(R.id.peer_alias),
                            (String) v.getTag(R.id.view_tag_peer_id));
            }
        });
        return new ViewHolder(v);
    }

}
