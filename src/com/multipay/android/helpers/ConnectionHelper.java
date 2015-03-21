package com.multipay.android.helpers;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectionHelper {
	
	public static final String WEBAPP_HOST = "http://multipay.ddns.net:8080/api/";

        public String get(String fullUrl) throws Exception {
                URL netUrl = new URL(WEBAPP_HOST + fullUrl);
                HttpURLConnection connection = (HttpURLConnection) netUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setUseCaches(false);

                String response = this.inputStreamToString(connection.getInputStream());
                connection.disconnect();
                return response;
        }

        public String post(String url, String body) throws Exception {
                URL netUrl = new URL(WEBAPP_HOST + url);
                HttpURLConnection connection = (HttpURLConnection) netUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                connection.setUseCaches(false);

                if (body != null) {
                        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                        out.writeBytes(body);
                        out.flush();
                        out.close();
                }

                String response = this.inputStreamToString(connection.getInputStream());
                connection.disconnect();
                return response;
        }

        private String inputStreamToString(InputStream in) throws Exception {
                StringBuffer sb = new StringBuffer();
                Reader reader = new InputStreamReader(in, "UTF-8");
                int c;
                while ((c = reader.read()) != -1) {
                        sb.append((char) c);
                }
                return sb.toString();
        }

}