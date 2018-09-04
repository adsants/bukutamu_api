package com.adsants.bukutamusqllite;

import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.adsants.bukutamusqllite.helper.ConfigURL;
import com.adsants.bukutamusqllite.helper.SqliteHelper;
import com.andexert.library.RippleView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

public class AddActivity extends AppCompatActivity {

    RadioGroup radio_kelamin;
    RadioButton input_radio_cewek,input_radio_cowok;
    EditText input_nama,input_alamat;
    Button btn_simpan;
    RippleView ripple_simpan;

    String jenisKelamin;

    SqliteHelper sqliteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        jenisKelamin = "";

        sqliteHelper = new SqliteHelper(this);

        radio_kelamin       = findViewById(R.id.radio_kelamin);
        input_radio_cowok   = findViewById(R.id.input_radio_cowok);
        input_radio_cewek   = findViewById(R.id.input_radio_cewek);
        input_nama          = findViewById(R.id.input_nama);
        input_alamat        = findViewById(R.id.input_alamat);
        btn_simpan          = findViewById(R.id.btn_simpan);
        ripple_simpan       = findViewById(R.id.ripple_simpan);

        radio_kelamin.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i){
                    case R.id.input_radio_cewek: jenisKelamin = "Perempuan";
                        break;
                    case R.id.input_radio_cowok: jenisKelamin = "Laki-Laki";
                        break;
                }
            }
        });

        btn_simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /// diganti di Ripple onclick
            }
        });

        ripple_simpan.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {

                if(input_nama.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),  "Masukkan Nama terlebih dahulu." , Toast.LENGTH_LONG).show();
                    input_nama.requestFocus();
                }
                else if(jenisKelamin.equals("")){
                    Toast.makeText(getApplicationContext(),  "Pilih Jenis Kelamin." , Toast.LENGTH_LONG).show();
                }
                else if(input_alamat.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),  "Masukkan Alamat terlebih dahulu.." , Toast.LENGTH_LONG).show();
                    input_alamat.requestFocus();
                }
                else {
                    simpan_data();
                    //Toast.makeText(getApplicationContext(), "Nama : " + input_nama.getText().toString() + ", Alamatku adalah  : " + input_alamat.getText().toString(), Toast.LENGTH_LONG).show();

                }
            }
        });

        getSupportActionBar().setTitle("Tambah Data");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    private void simpan_data(){

        /**
         SQLiteDatabase database = sqliteHelper.getWritableDatabase();
        database.execSQL("insert into transaksi (nama,jenis_kelamin,alamat) values
         ('"+input_nama.getText().toString()+"','"+jenisKelamin+"', '"+input_alamat.getText().toString()+"')");

        Toast.makeText(getApplicationContext(), "Data berhasil disimpan.", Toast.LENGTH_LONG).show();
        **/

        AndroidNetworking.post(ConfigURL.Domain + "add.php")
        .addBodyParameter("jenis_kelamin", jenisKelamin)
        .addBodyParameter("alamat",  input_alamat.getText().toString())
        .addBodyParameter("nama", input_nama.getText().toString())
        .setPriority(Priority.MEDIUM)
        .build()
        .getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getString("response").equals("success")){
                        Toast.makeText(getApplicationContext(), "Data berhasil disimpan.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Maaf, Data Gagal disimpan !", Toast.LENGTH_LONG).show();
                    }
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(ANError error) {
                // handle error
            }
        });


    }
}
