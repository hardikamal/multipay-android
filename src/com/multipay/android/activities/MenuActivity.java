package com.multipay.android.activities;

import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.multipay.android.R;
import com.multipay.android.helpers.SessionManager;
import com.multipay.android.utils.Device;
import com.multipay.android.utils.GooglePlusLoginUtils;

public class MenuActivity extends Activity {

	private ImageView socialLogo;
	private ProfilePictureView facebookProfilePicture;
	private ImageView gPlusProfilePicture;
	private TextView profileUsername;
	private String loginType;
	private String name;
	GooglePlusLoginUtils gpLogin;
	private WebView current_promos;
	private SessionManager session;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		
		/*Intent intent = getIntent();
		loginType = intent.getStringExtra("loginType");
		String name = intent.getStringExtra("name");
		
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    	loginType = sharedPref.getString("loginType", "defaultvalue");
    	name = sharedPref.getString("name", "defaultvalue");*/
    	
    	session = new SessionManager(this.getApplicationContext());
    	loginType = session.getUserDetails().get("loginType");
    	name = session.getUserDetails().get("name");
		
		socialLogo = (ImageView) findViewById(R.id.social_logo_imageview);
		
		// Find the user's profile picture FB custom view
		facebookProfilePicture = (ProfilePictureView) findViewById(R.id.facebook_profile_pictureview);
		facebookProfilePicture.setCropped(true);
		
		gPlusProfilePicture = (ImageView) findViewById(R.id.gplus_profile_pictureview);

		// Find the user's name view
		profileUsername = (TextView) findViewById(R.id.profile_username);
		
		current_promos = (WebView) findViewById(R.id.current_promos_webView);
		WebSettings webSettings = current_promos.getSettings();
		webSettings.setBuiltInZoomControls(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setUseWideViewPort(true);
		current_promos.loadUrl("https://www.mercadopago.com/mla/credit_card_promos.htm");
		
		// Get the user's data.
		if (loginType.compareTo("FB") == 0) {
			socialLogo.setImageResource(R.drawable.facebook_logo__blue);
			facebookProfilePicture.setVisibility(View.VISIBLE);
			makeMeRequest();
			gPlusProfilePicture.setVisibility(View.GONE);
			profileUsername.setText(name);
		} else if (loginType.compareTo("G+") == 0) {
			socialLogo.setImageResource(R.drawable.gplus_logo);
			facebookProfilePicture.setVisibility(View.GONE);
			gPlusProfilePicture.setVisibility(View.VISIBLE);
			new LoadProfileImage(gPlusProfilePicture).execute(GooglePlusLoginUtils.personPhotoUrl);
			//gPlusProfilePicture.setImageBitmap(GooglePlusLoginUtils.bitmap);
			profileUsername.setText(name);
		} else {
			socialLogo.setImageResource(R.drawable.ic_logo_multipay);
			facebookProfilePicture.setVisibility(View.GONE);
			gPlusProfilePicture.setVisibility(View.VISIBLE);
			gPlusProfilePicture.setImageResource(R.drawable.generic_user);
			profileUsername.setText(name);
		}
	}

	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.menu_activity_menu, menu);
    	    return super.onCreateOptionsMenu(menu);
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_about:
            	openAbout();
            	return true;
            case R.id.action_make_payment:
                makePayment(item.getActionView());
                return true;
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_help:
                help();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void openAbout() {
    	CharSequence text;
    	int duration = Toast.LENGTH_LONG;
    	
    	if (!Device.getDevice(getApplicationContext()).isNFCPresent()) {
    		text = "NFC no disponible!";
    	} else {
    		if (!Device.getDevice(getApplicationContext()).isNFCEnabled()) {
    			text = "Debe activar NFC en ajustes";
    		} else {
    			text = "El equipo funciona correctamente";
    		}
    	}

    	Toast toast = Toast.makeText(getApplicationContext(), text, duration);
    	toast.show();
	}
    
    private void help() {
    	CharSequence text = "2015 - Multipay Co.\nDiríjase a www.multipay.com.ar para más información";;
    	int duration = Toast.LENGTH_LONG;
    	Toast toast = Toast.makeText(getApplicationContext(), text, duration);
    	toast.show();
	}


	@Override
    public void onBackPressed() {
        // Do Here what ever you want do on back press;
		if (current_promos.isShown()) {
			current_promos.setVisibility(View.GONE);
		}
    }
    
    /**
     * Background Async task to load user profile picture from url
     * */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
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
            //bitmap = result;
        }
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
					facebookProfilePicture.setProfileId(user.getId());
					// Set the Textview's text to the user's name.
					//userNameView.setText(user.getName());
				}

				if (response.getError() != null) {
					// Handle errors, will do so later.
				}
			}
		});
		request.executeAsync();
	}

	private void logout() {
		if (loginType.compareTo("FB") == 0) {
			if (Session.getActiveSession() != null) {
				Session.getActiveSession().closeAndClearTokenInformation();
			}
			Session.setActiveSession(null);
		} else if (loginType.compareTo("G+") == 0) {
			GooglePlusLoginUtils.signOutFromGplus();
		}
		Intent intent = new Intent(this, SplashScreenActivity.class);
		//intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		finish();
		startActivity(intent);
		
	}


	public void addCard(View view) {
		Intent addCardIntent = new Intent(this, AddCardActivity.class);
    	startActivity(addCardIntent);
	}
	
	public void paymentHistory(View view) {
		Intent paymentHistoryIntent = new Intent(this, PaymentHistoryActivity.class);
    	startActivity(paymentHistoryIntent);
	}
	
	public void PINChange(View view) {
		Intent PINChangeIntent = new Intent(this, PINChangeActivity.class);
    	startActivity(PINChangeIntent);
	}
	
	public void viewPromos(View view) {
		current_promos.setVisibility(View.VISIBLE);
	}
	
	public void makePayment(View view) {
		Intent makePaymentIntent = new Intent(this, MakePaymentActivity.class);
    	startActivity(makePaymentIntent);
	}
}
