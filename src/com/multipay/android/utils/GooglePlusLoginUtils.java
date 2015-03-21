package com.multipay.android.utils;

import java.io.InputStream;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.multipay.android.R;
import com.multipay.android.activities.MainActivity;

public class GooglePlusLoginUtils implements ConnectionCallbacks, OnConnectionFailedListener, OnClickListener {
	
	public interface GooglePlusLoginStatus {
		public void OnSuccessGooglePlusLogin(Bundle profile);
	}
	
	private static final int RC_SIGN_IN = 0;
	
    // Logcat tag
    private static final String TAG = "GooglePlusLoginUtils";
    
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String USERID = "userid";
 
    // Profile pic image size in pixels
    private static final int PROFILE_PIC_SIZE = 400;
 
    // Google client to interact with Google API
    private static GoogleApiClient mGoogleApiClient;
    
    private Context ctx;
    private Activity act;
    
    private GooglePlusLoginStatus loginstatus;

    private SignInButton btnSignIn;
    private Button btnSignOut, btnRevokeAccess;
    
    public static String personPhotoUrl;
    
    private TextView userNameView;
    private ImageView imgProfilePic;
    
    public static Bitmap bitmap;
 
    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private boolean mIntentInProgress;
 
    private boolean mSignInClicked;
 
    private ConnectionResult mConnectionResult;

	private String personName;

    public GooglePlusLoginUtils(Activity act) {
		this.ctx = act.getApplicationContext();
		this.act = act;
		mSignInClicked = false;
		/*mGoogleApiClient = new GoogleApiClient.Builder(ctx)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this).addApi(Plus.API)
        .addScope(Plus.SCOPE_PLUS_LOGIN).build();*/
		
		//imgProfilePic = (ImageView) act.findViewById(R.id.gplus_profile_pictureview);
		imgProfilePic = new ImageView(ctx);
		btnSignIn = (SignInButton) act.findViewById(R.id.gplus_sign_in_button);
		btnSignIn.setOnClickListener(this);
		//userNameView = (TextView) act.findViewById(R.id.profile_user_textview);
		userNameView = new TextView(ctx);
	}
    
    public ImageView getImgProfilePic() {
		return imgProfilePic;
	}

	public void setImgProfilePic(ImageView imgProfilePic) {
		this.imgProfilePic = imgProfilePic;
	}

	protected void selectAccount() {
    	 Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
    	         false, null, null, null, null);
    	 act.startActivityForResult(intent, 666);
	}

	public TextView getUserNameView() {
		return userNameView;
	}

	public void setUserNameView(TextView userNameView) {
		this.userNameView = userNameView;
	}

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
	}

	public void setLoginStatus(GooglePlusLoginStatus loginStatus){
		this.loginstatus = loginStatus;
	}
    
	@Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
        	Activity activity = (Activity) ctx;
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), activity,
                    0).show();
            return;
        }
 
        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;
 
            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }
 
    }

	@Override
    public void onConnected(Bundle arg0) {
		MainActivity.mConnectionProgressDialog.dismiss();
        mSignInClicked = false;
        Toast.makeText(ctx, "conectado!", Toast.LENGTH_LONG).show();
 
        // Get user's information
        getProfileInformation();
    }

	@Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }
	
	/**
     * Method to resolve any signin errors
     * */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
				mConnectionResult.startResolutionForResult(act, RC_SIGN_IN);
            } catch (SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }
    
    /**
     * Sign-in into google
     * */
    public void signInWithGplus() {
    	if (!mGoogleApiClient.isConnected()) {
    		// We should always have a connection result ready to resolve,
			// so we can start that process.
			if (mConnectionResult != null) {
				resolveSignInError();
			} else {
				// If we don't have one though, we can start connect in
				// order to retrieve one.
				mGoogleApiClient.connect();
			}
        } 
    	if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }
 
    /**
     * Sign-out from google
     * */
    public static void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
         // Clear the default account in order to allow the user
			// to potentially choose a different account from the
			// account chooser.
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);

			// Disconnect from Google Play Services, then reconnect in
			// order to restart the process from scratch.
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }
    }
    
    /**
     * Fetching user's information name, email, profile pic
     * */
    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            	
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                /*String*/ personPhotoUrl = currentPerson.getImage().getUrl();
                String personGooglePlusProfile = currentPerson.getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
                
                Bundle profile = new Bundle();
    			profile.putString(NAME, personName);
    			profile.putString(EMAIL, email);
    			profile.putString(USERID, currentPerson.getId());
    			profile.putString(FIRST_NAME,currentPerson.getName().getGivenName());
    			profile.putString(LAST_NAME, currentPerson.getName().getFamilyName());
 
                Log.i(TAG, "Name: " + personName + ", plusProfile: "
                        + personGooglePlusProfile + ", email: " + email
                        + ", Image: " + personPhotoUrl);
 
 
                // by default the profile url gives 50x50 px image only
                // we can replace the value with whatever dimension we want by
                // replacing sz=X
                personPhotoUrl = personPhotoUrl.substring(0,
                        personPhotoUrl.length() - 2)
                        + PROFILE_PIC_SIZE;
 
                //new LoadProfileImage(imgProfilePic).execute(personPhotoUrl);
                this.userNameView.setText(personName);
                loginstatus.OnSuccessGooglePlusLogin(profile);
            } else {
                Toast.makeText(ctx.getApplicationContext(),
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    /**
     * Revoking access from google
     * */
    public void revokeGplusAccess() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status arg0) {
                            Log.e(TAG, "User access revoked!");
                            mGoogleApiClient.connect();
                        }
 
                    });
        }
    }
 
    /**
     * Background Async task to load user profile picture from url
     * */
    /*private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
 
        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }
 
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }
 
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            bitmap = result;
        }
    }*/

	public void onActivityResult(int requestCode, int responseCode, Intent intent) {
		if (requestCode == 666 && responseCode == Activity.RESULT_OK) {
	         String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
	         Log.i(TAG, accountName);
	         mGoogleApiClient = new GoogleApiClient.Builder(ctx)
	         .addConnectionCallbacks(this)
	         .addOnConnectionFailedListener(this).addApi(Plus.API)
	         .addScope(Plus.SCOPE_PLUS_LOGIN).setAccountName(accountName).build();
	         signInWithGplus();
	   }
		
		if (requestCode == RC_SIGN_IN) {
            if (responseCode != -1) {
                mSignInClicked = false;
            }
 
            mIntentInProgress = false;
 
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
	}

	public void connect() {
		if (mSignInClicked) {
			mGoogleApiClient.connect();
		}
		
	}

	public void disconnect() {
		mGoogleApiClient.disconnect();		
	}

	@Override
	public void onClick(View v) {
		MainActivity.mConnectionProgressDialog.show();
		mSignInClicked = true;
		selectAccount();
	}
}