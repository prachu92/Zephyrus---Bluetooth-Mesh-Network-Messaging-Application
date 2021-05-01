package com.blemesh.zephyrus.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.sql.Time;
import java.text.ParseException;

import com.blemesh.zephyrus.MainActivity;
import im.delight.android.identicons.SymmetricIdenticon;
import com.blemesh.zephyrus.R;
import com.blemesh.zephyrus.database.DbManager;
import com.blemesh.zephyrus.database.MessageTable;
import com.blemesh.sdk.DataUtil;
import timber.log.Timber;

public class MessageAdapter extends BaseAbstractRecycleCursorAdapter<MessageAdapter.ViewHolder>{

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private DbManager dbManager;
    private RecyclerView mHost;
    private String localAlias;
    private String senderAlias;
    private int currentView;

    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public View container;
        public TextView senderView;
        public TextView timeView;
        public TextView messageView;
        public SymmetricIdenticon identicon;
        TextView messageText, timeText, nameText;

        public ViewHolder(View itemView) {
            super(itemView);
            container = itemView;

//            messageText = (TextView) itemView.findViewById(R.id.text_gchat_user_other);
//            Timber.d("messageText in View holder is: "+messageText);
//            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_other);
//            nameText = (TextView) itemView.findViewById(R.id.text_gchat_message_other);

//            senderView = (TextView) itemView.findViewById(R.id.sender);
//            timeView = (TextView) itemView.findViewById(R.id.authoredDate);
//            messageView = (TextView) itemView.findViewById(R.id.messageBody);
//            identicon = (SymmetricIdenticon) itemView.findViewById(R.id.identicon_message);
        }
    }


    //
    public MessageAdapter(@NonNull Context context,
                          @NonNull DbManager dbManager,
                          @NonNull String localAlias){
        super(context, dbManager.getChatMessages(localAlias));
        this.dbManager = dbManager;
        this.localAlias = localAlias;
    }


    @Override
    public int getItemViewType(int position) {

        Cursor c = getCursor();
        Timber.d("cursor is:" + c);
        Timber.d("cursor movefirst:", c.moveToFirst());
  String sender_alias = c.getString(c.getColumnIndexOrThrow(MessageTable.COLUMN_NAME_ALIAS));
        String l_alias = MainActivity.local_alias;
        Timber.d("LOCAL ALIAS = " + l_alias);
        Timber.d("SENDER ALIAS = " + sender_alias);
        if (sender_alias.equals(l_alias)) {
            Timber.d("CAME HERE to set message sent");
            // If the current user is the sender of the message
            currentView = VIEW_TYPE_MESSAGE_SENT;
            return currentView;
        } else {
            Timber.d("CAME HERE to set message recieved");
            // If some other user sent the message
            currentView = VIEW_TYPE_MESSAGE_RECEIVED;
            return currentView;
        }
    }
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.message_item, parent, false);
//        return new ViewHolder(v);


        View view;

        Timber.d("Current view" + currentView);
        if (currentView == VIEW_TYPE_MESSAGE_SENT) {
            Timber.d("CAME HERE to MESSAGE ME");
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_me, parent, false);
            return new SenderMessageHolder(view);
        } else if (currentView == VIEW_TYPE_MESSAGE_RECEIVED) {
            Timber.d("CAME HERE to MESSAGE OTHER");
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_other, parent, false);
            return new ReceivedMessageHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(MessageAdapter.ViewHolder holder, Cursor cursor) {


        holder.container.setTag(R.id.view_tag_peer_id, cursor.getInt(cursor.getColumnIndexOrThrow(MessageTable._ID)));
         senderAlias = cursor.getString(cursor.getColumnIndexOrThrow(MessageTable.COLUMN_NAME_ALIAS));
        Timber.d("before calling getItemViewType, senderAlias is:" +senderAlias);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:

            Timber.d("Message is: " + cursor.getString(cursor.getColumnIndex(MessageTable.COLUMN_NAME_MESSAGE_BODY)));
            ((SenderMessageHolder) holder).messageText.setText(cursor.getString(cursor.getColumnIndex(MessageTable.COLUMN_NAME_MESSAGE_BODY)));
            try {
                ((SenderMessageHolder) holder).timeText.setText(DateUtils.getRelativeTimeSpanString(
                        DataUtil.storedDateFormatter.parse(cursor.getString(cursor.getColumnIndex(MessageTable.COLUMN_NAME_MESSAGE_TIME))).getTime()));
            } catch (ParseException e) {
                ((SenderMessageHolder) holder).timeText.setText("");
                e.printStackTrace();
            }
            break;
            // ((SenderMessageHolder) holder).nameText.setText(alias);
        case VIEW_TYPE_MESSAGE_RECEIVED:

            ((ReceivedMessageHolder) holder).messageText.setText(cursor.getString(cursor.getColumnIndex(MessageTable.COLUMN_NAME_MESSAGE_BODY)));
            try {
                ((ReceivedMessageHolder) holder).timeText.setText(DateUtils.getRelativeTimeSpanString(
                        DataUtil.storedDateFormatter.parse(cursor.getString(cursor.getColumnIndex(MessageTable.COLUMN_NAME_MESSAGE_TIME))).getTime()));
            } catch (ParseException e) {
                ((ReceivedMessageHolder) holder).timeText.setText("");
                e.printStackTrace();
            }
            break;
            //((ReceivedMessageHolder) holder).nameText.setText(alias);

        }


//        Timber.d("ONBIND " + holder);
//        Timber.d("MESSAGE text "+holder.messageText);
//        holder.messageText.setText(cursor.getString(cursor.getColumnIndex(MessageTable.COLUMN_NAME_MESSAGE_BODY)));
//
//        // Format the stored timestamp into a readable String using method.
//        try {
//            holder.timeText.setText(DateUtils.getRelativeTimeSpanString(
//                    DataUtil.storedDateFormatter.parse(cursor.getString(cursor.getColumnIndex(MessageTable.COLUMN_NAME_MESSAGE_TIME))).getTime()));
//        } catch (ParseException e) {
//            holder.timeText.setText("");
//            e.printStackTrace();
//        }
        // holder.nameText.setText(alias);



//        holder.senderView.setText(alias);
//        holder.identicon.show(alias);
//        holder.messageView.setText(cursor.getString(cursor.getColumnIndex(MessageTable.COLUMN_NAME_MESSAGE_BODY)));
//        try {
//            holder.timeView.setText(DateUtils.getRelativeTimeSpanString(
//                    DataUtil.storedDateFormatter.parse(cursor.getString(cursor.getColumnIndex(MessageTable.COLUMN_NAME_MESSAGE_TIME))).getTime()));
//        } catch (ParseException e) {
//            holder.timeView.setText("");
//            e.printStackTrace();
//        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mHost = recyclerView;
    }

    @Override
    protected void onContentChanged() {
        changeCursor(dbManager.getChatMessages(localAlias));
        mHost.smoothScrollToPosition(0);
    }

    private class ReceivedMessageHolder extends MessageAdapter.ViewHolder {
        TextView messageText, timeText, nameText;


        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_other);
            Timber.d("messageText in ReceivedMessage Holder is:" + messageText);
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_other);
            nameText = (TextView) itemView.findViewById(R.id.text_gchat_user_other);

        }

    }

    private class SenderMessageHolder extends MessageAdapter.ViewHolder {
        TextView messageText, timeText, nameText;


        SenderMessageHolder(View itemView) {
            super(itemView);

            Timber.d("CAME To Sender message holder");
            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_me);
            Timber.d("messageText in SenderMessageHolder is: "+messageText);
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_me);
//            nameText = (TextView) itemView.findViewById(R.id.text_gchat_message_other);

        }
    }

}