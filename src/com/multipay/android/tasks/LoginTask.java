package com.multipay.android.tasks;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import android.os.AsyncTask;
import android.util.Log;

import com.multipay.android.activities.MainActivity;
import com.multipay.android.dtos.LoginRequestDTO;
import com.multipay.android.dtos.LoginResponseDTO;
import com.multipay.android.helpers.ConnectionHelper;

public class LoginTask extends AsyncTask<LoginRequestDTO, Void, Boolean> {

        private final ConnectionHelper connectionHelper = new ConnectionHelper();
        private final ObjectMapper mapper = new ObjectMapper();
        private final MainActivity login;

        public LoginTask(MainActivity login) {
                this.login = login;
        }

        @Override
        protected Boolean doInBackground(LoginRequestDTO... params) {
                String result = null;
                LoginResponseDTO loginResponse = null;

                try {
                        result = this.connectionHelper.post("login", this.mapper.writeValueAsString(params[0]));
                        loginResponse = this.mapper.readValue(result, new TypeReference<LoginResponseDTO>() {
                        });
                } catch (Exception e) {
                        e.printStackTrace();
                }

                this.login.setLoginResponse(loginResponse);

                return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (this.login != null) {
                        this.login.afterCall(result);
                }
        }

}