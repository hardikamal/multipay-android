package com.multipay.android.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.multipay.android.R;
import com.multipay.android.dtos.LoginRequestDTO;
import com.multipay.android.dtos.LoginResponseDTO;
import com.multipay.android.helpers.Caller;
import com.multipay.android.helpers.SessionManager;
import com.multipay.android.tasks.LoginTask;
import com.multipay.android.utils.Device;
import com.multipay.android.utils.FacebookLoginUtils;
import com.multipay.android.utils.GooglePlusLoginUtils;
import com.multipay.android.utils.FacebookLoginUtils.FacebookLoginStatus;
import com.multipay.android.utils.GooglePlusLoginUtils.GooglePlusLoginStatus;
 
public class MainActivity extends Activity implements FacebookLoginStatus, GooglePlusLoginStatus, Caller<Boolean> {
 
    private static final String LOGCAT_TAG = "MainActivity";
    
    private FacebookLoginUtils fLogin;
    private GooglePlusLoginUtils gpLogin;
    
    private EditText email;
    private EditText password;
 
    public static ProgressDialog mConnectionProgressDialog;
    
    private SessionManager session;
    private String mobileId;
    private LoginResponseDTO loginResponse;
    
    @Override
    public void OnSuccessFacebookLogin(Bundle profile) {
    	final String SOCIAL_NETWORK = "FACEBOOK ";
        Log.i(LOGCAT_TAG, SOCIAL_NETWORK + profile.getString(FacebookLoginUtils.USERID));
        Log.i(LOGCAT_TAG, SOCIAL_NETWORK + profile.getString(FacebookLoginUtils.EMAIL));
        Log.i(LOGCAT_TAG, SOCIAL_NETWORK + profile.getString(FacebookLoginUtils.NAME));
        Log.i(LOGCAT_TAG, SOCIAL_NETWORK + profile.getString(FacebookLoginUtils.FIRST_NAME));
        Log.i(LOGCAT_TAG, SOCIAL_NETWORK + profile.getString(FacebookLoginUtils.LAST_NAME));
        
		/*SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString("loginType", "FB");
		editor.putString("name", profile.getString(FacebookLoginUtils.NAME));
		editor.commit();*/
        
		this.session.createLoginSession(profile.getString(FacebookLoginUtils.NAME), profile.getString(FacebookLoginUtils.EMAIL), "FB");
		
        Intent intent = new Intent(this, MenuActivity.class);
		startActivity(intent);
    }
    
    @Override
	public void OnSuccessGooglePlusLogin(Bundle profile) {
    	final String SOCIAL_NETWORK = "GPLUS ";
    	Log.i(LOGCAT_TAG, SOCIAL_NETWORK + profile.getString(GooglePlusLoginUtils.USERID));
        Log.i(LOGCAT_TAG, SOCIAL_NETWORK + profile.getString(GooglePlusLoginUtils.EMAIL));
        Log.i(LOGCAT_TAG, SOCIAL_NETWORK + profile.getString(GooglePlusLoginUtils.NAME));
        Log.i(LOGCAT_TAG, SOCIAL_NETWORK + profile.getString(GooglePlusLoginUtils.FIRST_NAME));
        Log.i(LOGCAT_TAG, SOCIAL_NETWORK + profile.getString(GooglePlusLoginUtils.LAST_NAME));
        
		/*SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString("loginType", "G+");
		editor.putString("name", profile.getString(GooglePlusLoginUtils.NAME));
		editor.commit();*/
		
		this.session.createLoginSession(profile.getString(GooglePlusLoginUtils.NAME), profile.getString(GooglePlusLoginUtils.EMAIL), "G+");
		
		Intent intent = new Intent(this, MenuActivity.class);
		startActivity(intent);
	}
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        session = new SessionManager(this.getApplicationContext());
        mobileId = Device.getDevice(getApplicationContext()).getMACAddress();
        
        fLogin = new FacebookLoginUtils(this);
        fLogin.setLoginStatus(this);
        fLogin.setEnable(true);
 
        gpLogin = new GooglePlusLoginUtils(this);
        gpLogin.setLoginStatus(this);
        
        email = (EditText) findViewById(R.id.email_input);
        password = (EditText) findViewById(R.id.password_input);
        
        mConnectionProgressDialog = new ProgressDialog(this);
        mConnectionProgressDialog.setMessage("Conectándose...\nTu teléfono se está contactando con Google.\nÉsta acción puede demorar hasta 5 minuntos.");
    }
    
    // se llama despues de onPause()
    @Override
    protected void onResume() {
        super.onResume();
        fLogin.onResume();
    };

    // se llama cuando justo antes de que A llame a B
    @Override
    protected void onPause() {
        super.onPause();
        fLogin.onPause();
    }

    // se llama justo antes de que alguien haga finish() d esta actividad o el sistema la mate.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        fLogin.onDestroy();
    }
 
    // se llama despues de onCreate() y antes de onResume()
    @Override
    protected void onStart() {
        super.onStart();
        gpLogin.connect();
    }
 
    // se llama antes de onDestroy()
    @Override
    protected void onStop() {
        super.onStop();
        // TODO no se si va el discon
        //gpLogin.disconnect();
    }
 
    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
    	fLogin.onActivityResult(requestCode, responseCode, intent);
    	gpLogin.onActivityResult(requestCode, responseCode, intent);
    }
    
    public void signIn(View view) {
    	//	TODO obtener el usuario de la db
    	/*SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString("loginType", "NATIVE");
		editor.commit();*/
		
		
		Boolean cancel = false;
        String userEmail = this.email.getText().toString();
        String userPassword = this.password.getText().toString();
        View focusView = null;

        // Se validan los campos obligatorios
        if (TextUtils.isEmpty(userPassword)) {
                this.password.setError("El campo contraseña es obligatorio");
                focusView = this.password;
                cancel = true;
        }
        if (TextUtils.isEmpty(userEmail)) {
                this.email.setError("El campo email es obligatorio");
                focusView = this.email;
                cancel = true;
        }

        if (!cancel) {
                LoginRequestDTO userLogin = new LoginRequestDTO();
                userLogin.setUserEmail(userEmail);
                userLogin.setUserPassword(userPassword);
                userLogin.setMobileId(this.mobileId);

                new LoginTask(this).execute(userLogin);

        } else {
                // Se devuelve el foco al campo que no fue completado
                focusView.requestFocus();
        }
		
    	/*Intent intent = new Intent(this, MenuActivity.class);
		startActivity(intent);*/
    }
    
    public void setLoginResponse(LoginResponseDTO loginResponse) {
        this.loginResponse = loginResponse;
    }
    
    @Override
    public void afterCall(Boolean result) {
    	Boolean valid = false;

    	if (this.loginResponse != null) {
    		valid = this.loginResponse.getValid();
    		if (valid) {
    			this.session.createLoginSession(this.loginResponse.getUserName(), this.email.getText().toString(), "NATIVE");
    			Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
    			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    			startActivity(intent);
    			this.finish();
    		} else {
    			Toast.makeText(this.getApplicationContext(), this.loginResponse.getMessage(), Toast.LENGTH_LONG).show();
    			this.email.setText("");
    			this.password.setText("");
    			this.email.requestFocus();
    		}
    	} else {
    		Toast.makeText(this.getApplicationContext(), "Multipay no ha podido autenticar. Intente nuevamente.", Toast.LENGTH_LONG).show();
    	}
    }
    
    public void signUp(View view) {
    	Intent intent = new Intent(this, SignUpActivity.class);
		startActivity(intent);
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.menu_activity_main, menu);
    	    return super.onCreateOptionsMenu(menu);
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items.
        switch (item.getItemId()) {
            case R.id.action_about:
                openAbout();
                return true;
            case R.id.action_help:
                help();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onBackPressed() {
        // Do Here what ever you want do on back press;
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
}