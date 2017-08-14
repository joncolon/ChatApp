package com.tronography.locationchat.firebase;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tronography.locationchat.model.MessageModel;
import com.tronography.locationchat.model.UserModel;
import com.tronography.locationchat.chatroom.ChatRoomActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static android.content.ContentValues.TAG;


public class FirebaseMessageUtils {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference messageReference = database.getReference("messages");
    private HashMap<String, UserModel> userMap = new HashMap<>();

    public FirebaseMessageUtils() {
    }

    public HashMap<String, UserModel> getUserMap() {
        return userMap;
    }

    public DatabaseReference getDatabaseReference(@NonNull String path) {
        return database.getReference(path);
    }

    public void addMessageToFirebaseDb(MessageModel messageModel) {
        //creates a unique key identifier
        HashMap<String, Object> uniqueMessageIdentifier = new HashMap<>();

        //appends root with unique key
        messageReference.updateChildren(uniqueMessageIdentifier);
        String messageId = messageReference.push().getKey();
        messageModel.setMessageId(messageId);

        //references the object using the message ID in the database
        DatabaseReference messageRoot = messageReference.child(messageId);

        //assigns values to the children of this new message object
        HashMap<String, Object> messageValueMap = setDatabaseMessageValues(messageModel);

        //confirm changes
        messageRoot.updateChildren(messageValueMap);
    }

    @NonNull
    private HashMap<String, Object> setDatabaseMessageValues(MessageModel messageModel) {
        HashMap<String, Object> messageModelMap = new HashMap<>();
        messageModelMap.put("message_model", messageModel);
        return messageModelMap;
    }

    private void applySenderNameChangesToMessage(MessageModel messageModel, UserModel userModel){
        if(Objects.equals(messageModel.getSenderId(), userModel.getId())){
            applySenderNameChangeInFirebase(messageModel, userModel.getUsername());
        }
    }

    private void applySenderNameChangeInFirebase(MessageModel messagModel, String newUserName) {
        //reference the unique key object in the database
        DatabaseReference memberRoot = messageReference.child(messagModel.getMessageId());
        messagModel.setUsername(newUserName);
        //now we must generate the children of this new object
        HashMap<String, Object> userModelMap = setDatabaseMessageValues(messagModel);
        //confirm changes
        memberRoot.updateChildren(userModelMap);
    }

    public void addMessageChildEventListener(final ChatRoomActivity activity) {
        messageReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(com.google.firebase.database.DataSnapshot dataSnapshot, String s) {
                activity.onChildAdded(dataSnapshot, s);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                activity.fireBaseOnChildChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void updateMessageSenderUsernames(final UserModel userModel) {
        messageReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    String key = child.getKey();
                    Log.e(TAG, "Message key: " + key);
                    MessageModel messageModel = dataSnapshot
                            .child(key)
                            .child("message_model")
                            .getValue(MessageModel.class);
                    Log.e(TAG, "Message Model : " + messageModel );
                    applySenderNameChangesToMessage(messageModel, userModel);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void retrieveMessagesFromFirebase(final RetrieveMessageLogListener listener) {
        final ArrayList<MessageModel> refreshedMessageLog = new ArrayList<>();
        messageReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    String key = child.getKey();
                    MessageModel messageModel = dataSnapshot
                            .child(key)
                            .child("message_model")
                            .getValue(MessageModel.class);
                    Log.e(TAG, "retrieveMessagesFromFirebase: " + messageModel.getMessage());
                    refreshedMessageLog.add(messageModel);
                }
                listener.onMessageLogReceived(refreshedMessageLog);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public interface RetrieveMessageLogListener {
        void onMessageLogReceived(ArrayList<MessageModel> messageLog);
    }
}