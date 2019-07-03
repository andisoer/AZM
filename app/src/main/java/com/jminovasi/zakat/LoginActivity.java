package com.jminovasi.zakat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.jminovasi.zakat.Util.Config;
import com.jminovasi.zakat.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity {

    TextView tvToDaftar;
    TextInputEditText tiePhone;
    Button btnSubmitLogin;
    CoordinatorLayout coordinatorLayout;

    AlertDialog alertDialog;

    SharedPreferences sharedPreferences;

    private String TAG = LoginActivity.class.getSimpleName();
    private String nama, nohp, idDevice;
    private boolean isSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences(Config.TAG_sharedPreferences, Context.MODE_PRIVATE);
        isSignIn = sharedPreferences.getBoolean(Config.TAG_isSignIn, false);
        nama = sharedPreferences.getString(Config.TAG_nama, null);
        nohp = sharedPreferences.getString(Config.TAG_noHP, null);

        if(isSignIn){
            intentToMain();
        }

        tvToDaftar = findViewById(R.id.tvToDaftar);
        coordinatorLayout = findViewById(R.id.coorLogin);
        tiePhone = findViewById(R.id.tieFormPhoneLogin);
        btnSubmitLogin = findViewById(R.id.btnSubmitLogin);

        idDevice = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setTheme(R.style.LoadingTheme)
                .setCancelable(false)
                .build();

        tvToDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btnSubmitLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cekForm();
            }
        });
    }

    private void cekForm() {
        String varHP = tiePhone.getText().toString().trim();

        if(TextUtils.isEmpty(varHP)){
            Toast.makeText(LoginActivity.this, "Isi Nomor Telepon Anda !", Toast.LENGTH_SHORT).show();
        }else{
            alertDialog.show();
            submitToDb(varHP);

        }
    }

    private void submitToDb(final String varHP) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.URL_login, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Response : " + response);

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    boolean error = jsonObject.getBoolean(Config.TAG_error);

                    if (error) {

                        String message = jsonObject.getString(Config.TAG_message);
                        alertDialog.dismiss();

                        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
                        snackbar.show();

                    } else {

                        String message = jsonObject.getString(Config.TAG_message);

                        nohp = jsonObject.getString(Config.TAG_noHP);
                        nama = jsonObject.getString(Config.TAG_nama);

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(Config.TAG_isSignIn, true);
                        editor.putString(Config.TAG_noHP, nohp);
                        editor.putString(Config.TAG_nama, nama);
                        editor.apply();

                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();

                        intentToMain();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error : "+error.getMessage());
                alertDialog.dismiss();
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();

                params.put(Config.TAG_noHP, varHP);
                params.put(Config.TAG_idDevice, idDevice);

                Log.e(TAG, ""+params);

                return params;
            }
        };
        AppController.getInstance().addToRequestQeue(stringRequest);
    }

    private void intentToMain(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
