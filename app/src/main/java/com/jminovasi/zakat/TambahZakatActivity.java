package com.jminovasi.zakat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.jminovasi.zakat.Util.Config;
import com.jminovasi.zakat.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class TambahZakatActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 888;
    String[] appPermission = {Manifest.permission.READ_EXTERNAL_STORAGE,
                              Manifest.permission.WRITE_EXTERNAL_STORAGE};

    SignaturePad signaturePad;
    Button btnClearPad, btnSubmitZakat;
    EditText edtJumlahNominal, edtNamaMuzzaki, edtAlamatMuzakki;
    Spinner spinner;
    TextView tvFormatZkt;

    AlertDialog alertDialog;

    private String varJenisZakat, varJumlah, varMuzzakki, varAlamatMuzakki;
    private String TAG = TambahZakatActivity.class.getSimpleName();

    private SharedPreferences sharedPreferences;
    private String nama, nohp;

    Bitmap decoded, bitmapDB;

    Uri contentUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_zakat);

        Toolbar toolbar = findViewById(R.id.tbTambahZakat);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        signaturePad = findViewById(R.id.signaturPad);
        edtJumlahNominal = findViewById(R.id.edtNominal);
        edtNamaMuzzaki = findViewById(R.id.edtNamaMuzzaki);
        edtAlamatMuzakki = findViewById(R.id.edtAlamatMuzakki);
        btnClearPad = findViewById(R.id.btnClearPad);
        btnSubmitZakat = findViewById(R.id.btnSubmitZakat);
        spinner = findViewById(R.id.spinnerJenisZakat);
        tvFormatZkt = findViewById(R.id.tvFormatNominal);

        sharedPreferences = getSharedPreferences(Config.TAG_sharedPreferences, Context.MODE_PRIVATE);
        nama = sharedPreferences.getString(Config.TAG_nama, null);
        nohp = sharedPreferences.getString(Config.TAG_noHP, null);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            this, R.array.jenis_zakat, android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setTheme(R.style.LoadingTheme)
                .setCancelable(false)
                .build();

        signaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {

            }

            @Override
            public void onSigned() {
                btnSubmitZakat.setEnabled(true);
                btnClearPad.setEnabled(true);
            }

            @Override
            public void onClear() {
                btnSubmitZakat.setEnabled(false);
                btnClearPad.setEnabled(false);
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                varJenisZakat = spinner.getSelectedItem().toString();

                if(varJenisZakat.equals("Beras")){
                    tvFormatZkt.setText("Kg");
                }else if(varJenisZakat.equals("Uang")){
                    tvFormatZkt.setText("Rp");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnClearPad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signaturePad.clear();
            }
        });

        btnSubmitZakat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cekForm();
            }
        });

        if(checkAndRequestPermission()){

        }

    }

    private void cekForm() {
        varJumlah = edtJumlahNominal.getText().toString().trim();
        varMuzzakki = edtNamaMuzzaki.getText().toString().trim();
        varAlamatMuzakki = edtAlamatMuzakki.getText().toString().trim();
        Bitmap signaturBMP = signaturePad.getSignatureBitmap();

        if(TextUtils.isEmpty(varJenisZakat)){
            Toast.makeText(TambahZakatActivity.this, "Pilih Jenis Zakat !", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(varJumlah)){
            Toast.makeText(TambahZakatActivity.this, "Isikan Jumlah "+varJenisZakat, Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(varAlamatMuzakki)){
            Toast.makeText(TambahZakatActivity.this, "Isikan Alamat", Toast.LENGTH_SHORT).show();
        }else if(signaturePad.isEmpty()){
            Toast.makeText(TambahZakatActivity.this, "Masukkan Tanda Tangan Anda !", Toast.LENGTH_SHORT).show();
        }else if(!addJPGSignatureToGallery(signaturBMP)){
            Toast.makeText(TambahZakatActivity.this, "Gagal Mendapatkan Tanda Tangan, Coba Lagi !", Toast.LENGTH_SHORT).show();
        }else{
            //Toast.makeText(TambahZakatActivity.this, "Sukses Menyimpan TTD", Toast.LENGTH_SHORT).show();
            //Toast.makeText(TambahZakatActivity.this, getStringImage(decoded), Toast.LENGTH_SHORT).show();

            alertDialog.show();
            submitKeDB();
        }
    }

    private boolean addJPGSignatureToGallery(Bitmap signaturBMP) {
        boolean result = false;
        try {

            File photo = new File(getAlbumStorageDir("AZM"), String.format("TTDAZM_%d.jpg", System.currentTimeMillis()));
            saveBitmapToJPG(signaturBMP, photo);
            scanMediaFile(photo);
            result = true;
        }catch (IOException e){
            e.printStackTrace();
        }
        return result;
    }

    private void scanMediaFile(File photo) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        contentUri = Uri.fromFile(photo);
        bitmapDB = BitmapFactory.decodeFile(contentUri.getPath());
        decodeBitmap(bitmapDB);
        mediaScanIntent.setData(contentUri);
        TambahZakatActivity.this.sendBroadcast(mediaScanIntent);
    }

    private void decodeBitmap(Bitmap bitmapDB) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmapDB.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(bytes.toByteArray()));
    }

    private void saveBitmapToJPG(Bitmap bmp, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, 0,0, null);

        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    private File getAlbumStorageDir(String albumName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if(file.mkdirs()){
            Log.e(TAG+"/SignaturePad", "Gagal Membuat Folder");
        }

        return file;
    }

    private void submitKeDB() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.URL_addZakat, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Response : " + response);

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    boolean erorr = jsonObject.getBoolean(Config.TAG_error);

                    if(erorr){

                        String message = jsonObject.getString(Config.TAG_message);
                        alertDialog.dismiss();
                        Toast.makeText(TambahZakatActivity.this, message, Toast.LENGTH_SHORT).show();

                    }else{

                        String message = jsonObject.getString(Config.TAG_message);
                        alertDialog.dismiss();
                        Toast.makeText(TambahZakatActivity.this, message, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(TambahZakatActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(TambahZakatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error : "+error.getMessage());
                alertDialog.dismiss();
                if(error instanceof NoConnectionError){
                    Log.e(TAG, "Eror : "+error.getMessage());
                    Toast.makeText(TambahZakatActivity.this, "Nyalakan Jaringan Anda !", Toast.LENGTH_SHORT).show();
                }else if(error instanceof NetworkError){
                    Log.e(TAG, "Eror : "+error.getMessage());
                    Toast.makeText(TambahZakatActivity.this, "Terjadi Kesalaham Pada Jaringan, Coba Lagi", Toast.LENGTH_SHORT).show();
                }else if(error instanceof TimeoutError){
                    Log.e(TAG, "Eror : "+error.getMessage());
                    Toast.makeText(TambahZakatActivity.this, "Gagal Menggapai Server, Coba Lagi", Toast.LENGTH_SHORT).show();
                }else if(error instanceof ServerError){
                    Log.e(TAG, "Eror : "+error.getMessage());
                    Toast.makeText(TambahZakatActivity.this, "Terjadi Kesalahan Pada Server", Toast.LENGTH_SHORT).show();
                }else if(error instanceof ParseError){
                    Log.e(TAG, "Eror : "+error.getMessage());
                    Toast.makeText(TambahZakatActivity.this, "Terjadi Kesalahan Membaca Data", Toast.LENGTH_SHORT).show();
                }else{
                    Log.e(TAG, "Eror : "+error.getMessage());
                    Toast.makeText(TambahZakatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();

                params.put(Config.TAG_noHP, nohp);
                params.put(Config.TAG_tipe, varJenisZakat);
                params.put(Config.TAG_jumlah, varJumlah);
                params.put(Config.TAG_muzzaki, varMuzzakki);
                params.put(Config.TAG_alamat, varAlamatMuzakki);
                params.put(Config.TAG_ttd, getStringImage(decoded));

                Log.e(TAG, ""+params);

                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(1000 * 60, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQeue(stringRequest);
    }

    private String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public AlertDialog showDialog(String title, String msg, String positive, DialogInterface.OnClickListener
            positiveClick, String negative, DialogInterface.OnClickListener negativeClick, boolean isCancelAble) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setCancelable(isCancelAble);
        builder.setMessage(msg);
        builder.setPositiveButton(positive, positiveClick);
        builder.setNegativeButton(negative, negativeClick);

        AlertDialog alert = builder.create();
        alert.show();
        return alert;

    }

    private boolean checkAndRequestPermission() {
        List<String> listPermissionNeeded = new ArrayList<>();
        for (String permis : appPermission){
            if (ContextCompat.checkSelfPermission(this, permis) != PackageManager.PERMISSION_GRANTED){
                listPermissionNeeded.add(permis);
            }
        }
        if(!listPermissionNeeded.isEmpty()){
            ActivityCompat.requestPermissions(this,
                    listPermissionNeeded.toArray(new String[listPermissionNeeded.size()]),
                    PERMISSION_CODE);

            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(TambahZakatActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Intent intent = new Intent(TambahZakatActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_CODE){
            HashMap<String, Integer> permissionResult = new HashMap<>();
            int deniedCount = 0;

            for (int i = 0; i < grantResults.length; i++){
                if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                    permissionResult.put(permissions[i], grantResults[i]);
                    deniedCount++;
                }
            }

            if(deniedCount == 0){

            }else{
                for (Map.Entry<String, Integer> entry : permissionResult.entrySet()){
                    String permName = entry.getKey();
                    int permResult = entry.getValue();

                    if(ActivityCompat.shouldShowRequestPermissionRationale(this, permName)){
                        showDialog("", "Aplikasi Ini Membutuhkan Izin Untuk Mengkakses " +
                                "Penyimpanan Agar Aplikasi Berjalan Dengan Baik",
                                "Ya, Izinkan",
                                new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        checkAndRequestPermission();
                                    }
                                },
                                "Tidak, Keluar Aplikasi",
                                new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        finish();
                                    }
                                },
                                false);
                    }
                    else{

                        showDialog("", "Anda Menolak Aplikasi Untuk Mengakses Mengakses" +
                                        " Penyimpanan, Izinkan Semua Akses di [Setting] > [Permissions]",
                                "Ke Pengaturan",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS,
                                                Uri.fromParts("package", getPackageName(), null));
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                },
                                "Tidak, Keluar Aplikasi",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        finish();
                                    }
                                }, false);
                        break;
                    }
                }
            }
        }
    }

}
