package com.multipay.android.activities;

import com.facebook.Session;
import com.multipay.android.R;
import com.multipay.android.utils.Device;
import com.multipay.android.utils.GooglePlusLoginUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PINChangeActivity extends Activity {

    private EditText oldPIN;
    private EditText newPIN;
    private Button PINChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_change);
        setInputs();
    }
    
    /** Called when the user touches the button */
    public void PINChange(View view) {
    	oldPIN.getText();
    }
    
    //	Handle inputs
    private void setInputs(){
    	oldPIN = (EditText) findViewById(R.id.old_PIN);
    	newPIN = (EditText) findViewById(R.id.new_PIN);
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
    
    private void logout() {
    	SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    	String loginType = sharedPref.getString("loginType", "defaultvalue");
    	
		if (loginType.compareTo("FB") == 0) {
			if (Session.getActiveSession() != null) {
				Session.getActiveSession().closeAndClearTokenInformation();
			}
			Session.setActiveSession(null);
		} else {
			GooglePlusLoginUtils.signOutFromGplus();
		}
		Intent intent = new Intent(this, SplashScreenActivity.class);
		//intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		finish();
		startActivity(intent);
	}
    
    private void help() {
    	CharSequence text = "2015 - Multipay Co.\nDir�jase a www.multipay.com.ar para m�s informaci�n";;
    	int duration = Toast.LENGTH_LONG;
    	Toast toast = Toast.makeText(getApplicationContext(), text, duration);
    	toast.show();
	}

}