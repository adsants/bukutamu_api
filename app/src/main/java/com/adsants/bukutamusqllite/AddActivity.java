package com.adsants.bukutamusqllite;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.adsants.bukutamusqllite.helper.ConfigURL;
import com.adsants.bukutamusqllite.helper.CurrentDate;
import com.adsants.bukutamusqllite.helper.SqliteHelper;
import com.andexert.library.RippleView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import butterknife.BindView;

public class AddActivity extends AppCompatActivity {

    @BindView(R.id.radio_kelamin) RadioGroup radio_kelamin;
    @BindView(R.id.input_radio_cewek) RadioButton input_radio_cewek;
    @BindView(R.id.input_radio_cowok) RadioButton input_radio_cowok;
    @BindView(R.id.input_nama) EditText input_nama;
    @BindView(R.id.input_alamat) EditText input_alamat;
    @BindView(R.id.input_tanggal) EditText input_tanggal;

    RippleView ripple_simpan;
    String jenisKelamin,tanggalUntukSimpan;
    DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        jenisKelamin = "";
        radio_kelamin       = findViewById(R.id.radio_kelamin);
        input_radio_cowok   = findViewById(R.id.input_radio_cowok);
        input_radio_cewek   = findViewById(R.id.input_radio_cewek);
        input_nama          = findViewById(R.id.input_nama);
        input_alamat        = findViewById(R.id.input_alamat);
        input_tanggal        = findViewById(R.id.input_tanggal);
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

        input_tanggal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog = new DatePickerDialog(AddActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month_of_year, int day_of_month) {


                        // set day of month , month and year value in the edit text
                        NumberFormat numberFormat = new DecimalFormat("00");
                        tanggalUntukSimpan = year + "-" + numberFormat.format(( month_of_year +1 )) + "-" +
                                numberFormat.format(day_of_month);
                        input_tanggal.setText(numberFormat.format(day_of_month) + "/" + numberFormat.format(( month_of_year +1 )) +
                                "/" + year );
                    }
                }, CurrentDate.year, CurrentDate.month, CurrentDate.day);
                datePickerDialog.show();
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
        .addBodyParameter("tanggal_input", input_tanggal.getText().toString())
        .setPriority(Priority.MEDIUM)
        .build()
        .getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getString("response").equals("success")){
                        Toast.makeText(getApplicationContext(), "Data berhasil disimpan.", Toast.LENGTH_LONG).show();
                        finish();

                        //Log.e("logTglAdd",input_tanggal.getText().toString() );
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
