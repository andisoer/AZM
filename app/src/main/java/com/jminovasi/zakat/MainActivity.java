package com.jminovasi.zakat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.jminovasi.zakat.Util.Config;
import com.jminovasi.zakat.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    CardView cvAddZakat, cvRekapZakat;
    TextView tvNama, tvNoHP, tvUang, tvBeras;

    SwipeRefreshLayout swipeRefreshLayout;

    AlertDialog alertDialog;

    SharedPreferences sharedPreferences;
    private String varNoHP, varNama;

    private String TAG = MainActivity.class.getSimpleName();
    private double jumlah_uang, jumlah_beras;

    NumberFormat formatRupiah;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cvAddZakat = findViewById(R.id.cvAddZakat);
        cvRekapZakat = findViewById(R.id.cvRekapZakat);

        tvNama = findViewById(R.id.tvNamaHome);
        tvNoHP = findViewById(R.id.tvNoHPHome);
        tvUang = findViewById(R.id.tvJumlahUangHome);
        tvBeras = findViewById(R.id.tvJumlahBerasHome);
        swipeRefreshLayout = findViewById(R.id.srlHome);

        sharedPreferences = getSharedPreferences(Config.TAG_sharedPreferences, Context.MODE_PRIVATE);
        varNama = sharedPreferences.getString(Config.TAG_nama, null);
        varNoHP = sharedPreferences.getString(Config.TAG_noHP, null);

        Locale locale = new Locale("in", "ID");
        formatRupiah = NumberFormat.getCurrencyInstance(locale);

        tvNama.setText(varNama);
        tvNoHP.setText(varNoHP);

        cvAddZakat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, TambahZakatActivity.class);
                startActivity(i);
            }
        });

        cvRekapZakat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, RekapActivity.class);
                startActivity(i);
            }
        });

        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .setMessage("Tunggu Sebentar")
                .build();

        alertDialog.show();
        getJumlahZakat();

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryLight, R.color.colorPrimaryDarkLight);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getJumlahZakat();
            }
        });
    }

    private void getJumlahZakat() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.URL_jumlahZakat, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Response : " + response);

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    boolean error = jsonObject.getBoolean(Config.TAG_error);

                    if (error) {
                        alertDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Terjadi Kesalahan", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);

                    } else {

                        try {

                            String jumlahUang = jsonObject.getString(Config.TAG_jumlahUang);
                            String jumlahBeras = jsonObject.getString(Config.TAG_jumlahBeras);

                            if(jumlahUang.equals("null")){
                                tvUang.setText("Belum Ada Data");
                                jumlah_beras = jsonObject.getDouble(Config.TAG_jumlahBeras);
                                tvBeras.setText(Double.toString(jumlah_beras)+" Kg Beras");
                                swipeRefreshLayout.setRefreshing(false);
                                alertDialog.dismiss();
                            }else if(jumlahBeras.equals("null")){
                                tvBeras.setText("Belum Ada Data");
                                jumlah_uang = jsonObject.getDouble(Config.TAG_jumlahUang);
                                tvUang.setText(formatRupiah.format((double)jumlah_uang));
                                swipeRefreshLayout.setRefreshing(false);
                                alertDialog.dismiss();
                            }else{
                                jumlah_beras = jsonObject.getDouble(Config.TAG_jumlahBeras);
                                jumlah_uang = jsonObject.getDouble(Config.TAG_jumlahUang);
                                setToTV();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                    swipeRefreshLayout.setRefreshing(false);

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error : "+error.getMessage());
                alertDialog.dismiss();
                swipeRefreshLayout.setRefreshing(false);
                if(error instanceof NoConnectionError){
                    Log.e(TAG, "Eror : "+error.getMessage());
                    Toast.makeText(MainActivity.this, "Nyalakan Jaringan Anda !", Toast.LENGTH_SHORT).show();
                }else if(error instanceof NetworkError){
                    Log.e(TAG, "Eror : "+error.getMessage());
                    Toast.makeText(MainActivity.this, "Terjadi Kesalaham Pada Jaringan, Coba Lagi", Toast.LENGTH_SHORT).show();
                }else if(error instanceof TimeoutError){
                    Log.e(TAG, "Eror : "+error.getMessage());
                    Toast.makeText(MainActivity.this, "Gagal Menggapai Server, Coba Lagi", Toast.LENGTH_SHORT).show();
                }else if(error instanceof ServerError){
                    Log.e(TAG, "Eror : "+error.getMessage());
                    Toast.makeText(MainActivity.this, "Terjadi Kesalahan Pada Server", Toast.LENGTH_SHORT).show();
                }else if(error instanceof ParseError){
                    Log.e(TAG, "Eror : "+error.getMessage());
                    Toast.makeText(MainActivity.this, "Terjadi Kesalahan Membaca Data", Toast.LENGTH_SHORT).show();
                }else{
                    Log.e(TAG, "Eror : "+error.getMessage());
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();

                params.put(Config.TAG_noHP, varNoHP);

                Log.e(TAG, ""+params);

                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(1000 * 60, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQeue(stringRequest);
    }

    private void setToTV() {
        tvBeras.setText(Double.toString(jumlah_beras)+" Kg Beras");
        tvUang.setText(formatRupiah.format((double)jumlah_uang));
        swipeRefreshLayout.setRefreshing(false);
        alertDialog.dismiss();
    }
}
