package com.tronography.locationchat.userprofile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tronography.locationchat.R;
import com.tronography.locationchat.firebase.FirebaseMessageUtils;
import com.tronography.locationchat.firebase.FirebaseUserUtils;
import com.tronography.locationchat.model.UserModel;
import com.tronography.locationchat.utils.SharedPrefsUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tronography.locationchat.utils.SharedPrefsUtils.CURRENT_USER_KEY;

public class UserProfileActivity extends AppCompatActivity implements FirebaseUserUtils.RetrieveUserListener {

    private static final String TAG = UserProfileActivity.class.getSimpleName();
    @BindView(R.id.header_username_tv)
    TextView headerUsernameTV;
    @BindView(R.id.edit_option)
    TextView editOptionTV;
    @BindView(R.id.details_bio_et)
    EditText bioDetailsET;
    @BindView(R.id.details_location_et)
    EditText locationDetailsET;
    private UserModel userModel;
    FirebaseUserUtils firebaseUserUtils = new FirebaseUserUtils();
    FirebaseMessageUtils firebaseMessageUtils = new FirebaseMessageUtils();
    public final static String SENDER_ID_KEY = "sender_id";
    private SharedPrefsUtils sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);

        sharedPrefs = new SharedPrefsUtils(this);
        String userId = getIntent().getStringExtra(SENDER_ID_KEY);
        firebaseUserUtils.queryUserByID(userId, this);
    }

    @OnClick(R.id.edit_option)
    public void onClick() {

        if (editOptionTV.getText().equals("EDIT")){
            editOptionTV.setText(R.string.save);
            enableDetailsEditText();
        } else if (editOptionTV.getText().equals("SAVE")){
            editOptionTV.setText(R.string.edit);
            disableDetailsEditText();
            saveChanges();
            firebaseMessageUtils.updateMessageSenderUsernames(userModel);
        }
    }

    private void saveChanges() {
        firebaseUserUtils.applyNewUsernameInFireBase(userModel, headerUsernameTV.getText().toString());
        firebaseUserUtils.applyBioDetailsInFireBase(userModel, bioDetailsET.getText().toString());
        if (CURRENT_USER_KEY.equals(userModel.getId())){
            sharedPrefs.updateUsername(userModel);
        }
    }

    private void enableDetailsEditText() {
        bioDetailsET.setEnabled(true);
        locationDetailsET.setEnabled(true);
        headerUsernameTV.setEnabled(true);
    }

    private void disableDetailsEditText(){
        bioDetailsET.setEnabled(false);
        locationDetailsET.setEnabled(false);
        headerUsernameTV.setEnabled(false);
    }

    @Override
    public void onUserRetrieved(UserModel queriedUser) {
        this.userModel = queriedUser;
        headerUsernameTV.setText(userModel.getUsername());
        bioDetailsET.setText(userModel.getBio());
        if (CURRENT_USER_KEY.equals(userModel.getId())){
            editOptionTV.setVisibility(View.VISIBLE);
        }
    }

    public static Intent provideIntent(Context context, String userName) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra(SENDER_ID_KEY, userName);
        return intent;
    }

}