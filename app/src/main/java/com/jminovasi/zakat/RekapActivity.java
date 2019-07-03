package com.jminovasi.zakat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import com.jminovasi.zakat.adapter.AdapterRekapZakat;
import com.jminovasi.zakat.app.AppController;
import com.jminovasi.zakat.model.ModelRekapZakat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RekapActivity extends AppCompatActivity {

    ProgressBar progressBar;

    View noData;

    RecyclerView rvRekapZakat;
    LinearLayoutManager linearLayoutManager;

    SwipeRefreshLayout swipeRefreshLayout;

    AdapterRekapZakat adapter;
    List<ModelRekapZakat> listZakat;

    SharedPreferences sharedPreferences;
    private String nohp, nama;

    private String TAG = RekapActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rekap);

        Toolbar toolbar = findViewById(R.id.tbRekapZakat);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = getSharedPreferences(Config.TAG_sharedPreferences, Context.MODE_PRIVATE);
        nohp = sharedPreferences.getString(Config.TAG_noHP, null);
        nama = sharedPreferences.getString(Config.TAG_nama, null);

        progressBar = findViewById(R.id.pbRekap);
        noData = findViewById(R.id.noDataRekap);
        swipeRefreshLayout = findViewById(R.id.srlRekap);

        rvRekapZakat = findViewById(R.id.rvRekapZakat);
        linearLayoutManager = new LinearLayoutManager(RekapActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        listZakat = new ArrayList<>();
        adapter = new AdapterRekapZakat(this, listZakat);

        rvRekapZakat.setLayoutManager(linearLayoutManager);
        rvRekapZakat.setAdapter(adapter);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryDarkLight, R.color.colorPrimaryLight);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getRekap();
            }
        });

        getRekap();

    }

    private void getRekap() {
        listZakat.clear();
        adapter.notifyDataSetChanged();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.URL_rekapZakat, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Response : " + response);

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    boolean error = jsonObject.getBoolean(Config.TAG_error);

                    if (error) {

                        String message = jsonObject.getString(Config.TAG_message);
                        Toast.makeText(RekapActivity.this, message, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        noData.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setRefreshing(false);
                    } else {

                        try {

                            JSONArray jsonArray = jsonObject.getJSONArray(Config.TAG_data);

                            for(int i = 0; i < jsonArray.length(); i++){

                                try {

                                    JSONObject objectZakat = jsonArray.getJSONObject(i);

                                    ModelRekapZakat item = new ModelRekapZakat();
                                    item.setId(objectZakat.getString(Config.TAG_id));
                                    item.setTipe(objectZakat.getString(Config.TAG_tipe));
                                    item.setJumlah(objectZakat.getString(Config.TAG_jumlah));
                                    item.setTanggal(objectZakat.getString(Config.TAG_tanggal));
                                    item.setMuzakki(objectZakat.getString(Config.TAG_muzzaki));
                                    item.setAmil(objectZakat.getString(Config.TAG_amil));
                                    item.setAlamat(objectZakat.getString(Config.TAG_alamat));

                                    listZakat.add(item);

                                }catch (JSONException e){
                                    e.printStackTrace();
                                    Toast.makeText(RekapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }

                            }
                            adapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                            swipeRefreshLayout.setRefreshing(false);

                        }catch (JSONException e){
                            e.printStackTrace();
                            Toast.makeText(RekapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(RekapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error : "+error.getMessage());
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if(error instanceof NoConnectionError){
                    Log.e(TAG, "Eror : "+error.getMessage());
                    Toast.makeText(RekapActivity.this, "Nyalakan Jaringan Anda !", Toast.LENGTH_SHORT).show();
                }else if(error instanceof NetworkError){
                    Log.e(TAG, "Eror : "+error.getMessage());
                    Toast.makeText(RekapActivity.this, "Terjadi Kesalaham Pada Jaringan, Coba Lagi", Toast.LENGTH_SHORT).show();
                }else if(error instanceof TimeoutError){
                    Log.e(TAG, "Eror : "+error.getMessage());
                    Toast.makeText(RekapActivity.this, "Gagal Menggapai Server, Coba Lagi", Toast.LENGTH_SHORT).show();
                }else if(error instanceof ServerError){
                    Log.e(TAG, "Eror : "+error.getMessage());
                    Toast.makeText(RekapActivity.this, "Terjadi Kesalahan Pada Server", Toast.LENGTH_SHORT).show();
                }else if(error instanceof ParseError){
                    Log.e(TAG, "Eror : "+error.getMessage());
                    Toast.makeText(RekapActivity.this, "Terjadi Kesalahan Membaca Data", Toast.LENGTH_SHORT).show();
                }else{
                    Log.e(TAG, "Eror : "+error.getMessage());
                    Toast.makeText(RekapActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<>();

                params.put(Config.TAG_noHP, nohp);

                Log.e(TAG, ""+params);

                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(1000 * 60, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQeue(stringRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Intent intent = new Intent(RekapActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RekapActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
