package com.multipay.android.utils;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.OnErrorListener;
import com.facebook.widget.LoginButton.UserInfoChangedCallback;
import com.facebook.widget.ProfilePictureView;
import com.multipay.android.R;

public class FacebookLoginUtils implements UserInfoChangedCallback, OnErrorListener {
	private String TAG = "FacebookLoginUtils";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String USERID = "userid";

	private Context ctx;

	private LoginButton loginBtn;
	private UiLifecycleHelper uiHelper;
	private static final List<String> PERMISSIONS = Arrays.asList("public_profile", "email");
	private FacebookLoginStatus loginstatus;

	private ProfilePictureView profilePictureView;
	private TextView userNameView;
	
	public interface FacebookLoginStatus {
		public void OnSuccessFacebookLogin(Bundle profile);
	}
	
	public FacebookLoginUtils(Activity act) {
		this.ctx = act.getApplicationContext();
		loginBtn = (LoginButton) act.findViewById(R.id.facebook_sign_in_button);
		uiHelper = new UiLifecycleHelper(act, statusCallback);
		loginBtn.setReadPermissions(PERMISSIONS);
		loginBtn.setOnErrorListener(this);
		loginBtn.setUserInfoChangedCallback(this);
		
		//profilePictureView = (ProfilePictureView) act.findViewById(profPicRes);
		profilePictureView = new ProfilePictureView(ctx);
		//userNameView = (TextView) act.findViewById(userNameRes);
		userNameView = new TextView(ctx);
	}
	
	public ProfilePictureView getProfilePictureView() {
		return profilePictureView;
	}

	public void setProfilePictureView(ProfilePictureView profilePictureView) {
		this.profilePictureView = profilePictureView;
	}

	public TextView getUserNameView() {
		return userNameView;
	}

	public void setUserNameView(TextView userNameView) {
		this.userNameView = userNameView;
	}

	public void setEnable(boolean enabled){
		loginBtn.setEnabled(enabled);
	}
	
	public void setLoginStatus(FacebookLoginStatus loginStatus){
		this.loginstatus = loginStatus;
	}
	
	public void onResume(){
		uiHelper.onResume();
	}
	
	public void onPause(){
		uiHelper.onPause();
	}
	
	public void onDestroy(){
		uiHelper.onDestroy();
	}
	
	public void onActivityResult(int requestCode,int responseCode,Intent intent){
		uiHelper.onActivityResult(requestCode, responseCode, intent);
	}

	private Session.StatusCallback statusCallback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			if (state.isOpened()) {
				Log.d(TAG, "Facebook session opened");
				Log.i(TAG, "Access Token" + session.getAccessToken());
				makeMeRequest();
			} else if (state.isClosed()) {
				Log.d(TAG, "Facebook session closed");
			}
		}
	};

	@Override
	public void onUserInfoFetched(GraphUser user) {
		if (user != null) {
			Bundle profile = new Bundle();
			profile.putString(NAME, user.getName());
			profile.putString(EMAIL, user.asMap().get("email").toString());
			profile.putString(USERID, user.getId());
			profile.putString(FIRST_NAME,user.getFirstName());
			profile.putString(LAST_NAME, user.getLastName());
			loginstatus.OnSuccessFacebookLogin(profile);
		} else {
			Log.i(TAG, "You are not logged in");
		}		
	}

	@Override
	public void onError(FacebookException error) {
		Log.i(TAG,"onError: "+ error.getMessage());
		error.printStackTrace();
	}
	
	private void makeMeRequest() {
		Session session = Session.getActiveSession();
		// Make an API call to get user data and define a 
		// new callback to handle the response.
		Request request = Request.newMeRequest(session, 
				new Request.GraphUserCallback() {
			@Override
			public void onCompleted(GraphUser user, Response response) {
				// If the response is successful
				if (user != null) {
					// Set the id for the ProfilePictureView
					// view that in turn displays the profile picture.
					profilePictureView.setProfileId(user.getId());
					// Set the Textview's text to the user's name.
					userNameView.setText(user.getName());
				}

				if (response.getError() != null) {
					// Handle errors, will do so later.
				}
			}
		});
		request.executeAsync();
	}
}