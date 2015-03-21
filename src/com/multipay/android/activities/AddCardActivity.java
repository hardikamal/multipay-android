package com.multipay.android.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.Session;
import com.mercadopago.Mercadopago;
import com.mercadopago.model.Card;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.Token;
import com.mercadopago.util.InstallmentAdapter;
import com.mercadopago.util.RetrofitUtil;
import com.multipay.android.R;
import com.multipay.android.utils.Device;
import com.multipay.android.utils.GooglePlusLoginUtils;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AddCardActivity extends Activity implements OnItemSelectedListener {

	// Logcat tag
    private static final String LOGCAT_TAG = "AddCardActivity";
	
    private EditText cardNumber;
    private Spinner month;
    private Spinner year;
    private EditText securityCode;
    private EditText name;
    private Spinner docType;
    private EditText docNumber;
    private Mercadopago mp;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);
        setInputs();
        //Init mercadopago object with public key
        mp = new Mercadopago("841d020b-1077-4742-ad55-7888a0f5aefa", AddCardActivity.this);

        //Get payment methods and show installments spinner
        handleInstallments();
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void submitForm(View view){
        //Create card object with card data
        Card card = new Card(getCardNumber(), getMonth(), getYear(), getSecurityCode(), getName(), getDocType(), getDocNumber());

        //Callback handler
        Callback callback = new Callback<Token>() {
            @Override
            public void success(Token o, Response response) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "ID: " + o.getId() + "    CARDID: " + o.getCardId(), Toast.LENGTH_LONG).show();
                Log.i(LOGCAT_TAG, "ID: " + o.getId());
                Log.i(LOGCAT_TAG, "CARD_ID: " + o.getCardId());
            }
            @Override
            public void failure(RetrofitError error) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), RetrofitUtil.parseErrorBody(error).toString(), Toast.LENGTH_LONG).show();
            }
        };

        //Check valid card data
        if(card.validateCard()) {
            //Send card data to get token id
            dialog = ProgressDialog.show(AddCardActivity.this, "", "Esperá un momento...", true);
            mp.createToken(card, callback);
        }else{
            Toast.makeText(getApplicationContext(), "Ingresaste datos inválidos. Reintentá, por favor", Toast.LENGTH_LONG).show();
        }
    }

    void handleInstallments(){
        cardNumber.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 4) {
                    cardNumber.append(" ");
                }

                    if(s.length() == 6) {
                    Callback cb = new Callback<List<PaymentMethod>>() {
                        @Override
                        public void success(List<PaymentMethod> o, Response response) {
                            InstallmentAdapter adapter = new InstallmentAdapter(AddCardActivity.this, o.get(0).getPayerCosts(), new Double("100"));
                            ((Spinner)findViewById(R.id.spinner)).setAdapter(adapter);
                        }
                        @Override
                        public void failure(RetrofitError error) {
                            Toast.makeText(getApplicationContext(), RetrofitUtil.parseErrorBody(error).toString(), Toast.LENGTH_LONG).show();
                        }
                    };
                    mp.getPaymentMethodByBin(getCardNumber().substring(0,6), cb);
                }
            }
        });
    }

    String getCardNumber() { return this.cardNumber.getText().toString();}

    Integer getMonth() {
    	return (Integer.parseInt((String)this.month.getSelectedItem()));
    }

    Integer getYear() {
    	return (Integer.parseInt((String)this.year.getSelectedItem()));
    }

    String getSecurityCode(){ return this.securityCode.getText().toString();}

    String getName(){ return this.name.getText().toString();}

    String getDocType() {
    	return ((String)this.docType.getSelectedItem()).toUpperCase();
    }

    String getDocNumber(){ return this.docNumber.getText().toString();}

    private Integer getInteger(EditText text) {
        try {
            return Integer.parseInt(text.getText().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    	
    	Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.add_card_form_month)
        {
          //month =(String)spinner.getSelectedItem();                
        }
        else if(spinner.getId() == R.id.add_card_form_year)
        {
          //do this
        }
        else if(spinner.getId() == R.id.add_card_form_document_type)
        {
          //do this
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    //Handle inputs
    void setInputs(){
        cardNumber = (EditText) findViewById(R.id.add_card_form_card_number);
        
        month = (Spinner) findViewById(R.id.add_card_form_month);
        month.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this, R.array.card_month_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        month.setAdapter(monthAdapter);
        
        year = (Spinner) findViewById(R.id.add_card_form_year);
        year.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(this, R.array.card_year_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        year.setAdapter(yearAdapter);
        
        securityCode = (EditText) findViewById(R.id.add_card_form_security_code);
        
        name = (EditText) findViewById(R.id.add_card_form_full_name);
        
        docType = (Spinner) findViewById(R.id.add_card_form_document_type);
        docType.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> documentTypeAdapter = ArrayAdapter.createFromResource(this, R.array.document_types_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        documentTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        docType.setAdapter(documentTypeAdapter);
        
        docNumber = (EditText) findViewById(R.id.add_card_form_document_number);
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
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
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
    	CharSequence text = "2015 - Multipay Co.\nDiríjase a www.multipay.com.ar para más información";;
    	int duration = Toast.LENGTH_LONG;
    	Toast toast = Toast.makeText(getApplicationContext(), text, duration);
    	toast.show();
	}

}