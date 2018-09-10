package com.adsants.bukutamusqllite;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import static com.adsants.bukutamusqllite.MainActivity.transaksi_id;

public class EditActivity extends AppCompatActivity {

    RadioGroup radio_kelamin;
    RadioButton input_radio_cewek,input_radio_cowok;
    EditText input_nama,input_alamat,input_tanggal;
    Button btn_simpan;
    RippleView ripple_simpan;

    String jenisKelamin, tanggalUntukSimpan;

    SqliteHelper sqliteHelper;

    Cursor cursor;

    DatePickerDialog datePickerDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getSupportActionBar().setTitle("Edit Data");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        sqliteHelper = new SqliteHelper(this);

        radio_kelamin       = findViewById(R.id.radio_kelamin);
        input_radio_cowok   = findViewById(R.id.input_radio_cowok);
        input_radio_cewek   = findViewById(R.id.input_radio_cewek);
        input_nama          = findViewById(R.id.input_nama);
        input_alamat        = findViewById(R.id.input_alamat);
        btn_simpan          = findViewById(R.id.btn_simpan);
        input_tanggal       = findViewById(R.id.input_tanggal);
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

        /**SQLiteDatabase db =  sqliteHelper.getReadableDatabase();
        cursor = db.rawQuery("select *,strftime('%d-%m-%Y', tanggal_input) AS tgl from transaksi where transaksi_id = '"+MainActivity.transaksi_id+"'", null);
        cursor.moveToFirst();
        **/


        AndroidNetworking.post(ConfigURL.Domain + "get.php")
            .addBodyParameter("transaksi_id", MainActivity.transaksi_id)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(new JSONObjectRequestListener() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if(response.getString("response").equals("success")){
                            input_nama.setText( response.getString("nama"));
                            input_alamat.setText(response.getString("alamat"));
                            input_tanggal.setText(response.getString("tanggal_indo"));

                            jenisKelamin    = response.getString("jenis_kelamin");
                          //  jenisKelamin = "Perempuan";
                            switch (jenisKelamin){
                                case "Perempuan" : input_radio_cewek.setChecked(true);
                                    break;
                                case "Laki-Laki" : input_radio_cowok.setChecked(true);
                                    break;
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Maaf, tidak dapat mengambil Data !", Toast.LENGTH_LONG).show();
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


        input_tanggal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog = new DatePickerDialog(EditActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                else if(input_tanggal.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),  "Masukkan Tanggal terlebih dahulu.." , Toast.LENGTH_LONG).show();
                    input_tanggal.requestFocus();
                }else {
                    update_data();
                    //Toast.makeText(getApplicationContext(), "Nama : " + input_nama.getText().toString() + ", Alamatku adalah  : " + input_alamat.getText().toString(), Toast.LENGTH_LONG).show();

                }
            }
        });

    }

    private void update_data(){
        /**SQLiteDatabase database = sqliteHelper.getWritableDatabase();
        database.execSQL("update transaksi set nama = '"+input_nama.getText().toString()+"', alamat = '"+input_alamat.getText().toString()+"' , " +
                "jenis_kelamin='"+jenisKelamin+"',  tanggal_input = '"+tanggalUntukSimpan+"' " +
                "where transaksi_id = '"+transaksi_id+"'  ");

        Toast.makeText(getApplicationContext(), "Data berhasil disimpan.", Toast.LENGTH_LONG).show();

        finish();
         **/
        AndroidNetworking.post(ConfigURL.Domain + "update.php")
            .addBodyParameter("transaksi_id", MainActivity.transaksi_id)
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
                            Log.e("input_tanggal", input_tanggal.getText().toString() );
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

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

}
