package com.jminovasi.zakat;

import android.app.AlertDialog;
import android.content.Intent;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText tieHandphone, tieNama, tieAlamat;
    Button btnSubmitRegis;

    CoordinatorLayout coordinatorLayout;
    AlertDialog alertDialog;

    private String idDevice;

    private String TAG = RegisterActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.tbRegister);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tieNama = findViewById(R.id.tieNamaRegis);
        tieAlamat = findViewById(R.id.tieAlamatRegis);
        tieHandphone = findViewById(R.id.tieHandphoneRegis);
        btnSubmitRegis = findViewById(R.id.btnSubmitRegis);

        coordinatorLayout = findViewById(R.id.coorRegister);
        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setTheme(R.style.LoadingTheme)
                .setCancelable(false)
                .build();

        idDevice = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        btnSubmitRegis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cekForm();
            }
        });

    }

    private void cekForm() {
        String varHandphone = tieHandphone.getText().toString().trim();
        String varNama = tieNama.getText().toString().trim();
        String varAlamat = tieAlamat.getText().toString().trim();

        if(TextUtils.isEmpty(varHandphone)){
            Toast.makeText(RegisterActivity.this, "Masukkan Nomor Handphone Anda !", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(varNama)){
            Toast.makeText(RegisterActivity.this, "Masukkan Nama Anda !", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(varAlamat)){
            Toast.makeText(RegisterActivity.this, "Masukkan Alamat Anda !", Toast.LENGTH_SHORT).show();
        }else{
            alertDialog.show();
            submitToDB(varHandphone, varNama, varAlamat);
        }

    }

    private void submitToDB(final String varHandphone, final String varNama, final String varAlamat) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.URL_register, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Response : " + response);

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    boolean error = jsonObject.getBoolean(Config.TAG_error);

                    if (error) {

                        try {

                            String message = jsonObject.getString(Config.TAG_message);
                            alertDialog.dismiss();

                            Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
                            snackbar.show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        }

                    }else{

                        Toast.makeText(RegisterActivity.this, "Pendaftaran Berhasil, Silahkan Login", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error : "+error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();

                params.put(Config.TAG_noHP, varHandphone);
                params.put(Config.TAG_nama, varNama);
                params.put(Config.TAG_alamat, varAlamat);
                params.put(Config.TAG_idDevice, idDevice);

                Log.e(TAG, ""+params);

                return params;
            }
        };
        AppController.getInstance().addToRequestQeue(stringRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
