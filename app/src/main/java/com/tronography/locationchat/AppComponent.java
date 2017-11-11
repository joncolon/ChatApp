package com.tronography.locationchat;

import com.tronography.locationchat.chatroom.ChatActivity;
import com.tronography.locationchat.lobby.LobbyActivity;
import com.tronography.locationchat.userprofile.UserProfile;
import com.tronography.locationchat.userprofile.UserProfileActivity;

import javax.inject.Singleton;

import dagger.Component;


@Singleton
@Component(modules = {AppModule.class, SharedPrefModule.class})
public interface AppComponent {

    void inject(ChatActivity target);

    void inject(UserProfileActivity target);

    void inject(LobbyActivity target);
}