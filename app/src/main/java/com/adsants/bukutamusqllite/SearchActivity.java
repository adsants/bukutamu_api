package com.adsants.bukutamusqllite;

import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.adsants.bukutamusqllite.helper.CurrentDate;
import com.andexert.library.RippleView;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class SearchActivity extends AppCompatActivity {

    EditText input_mulai,input_akhir;
    RippleView ripple_cari;


    DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setTitle("Pencarian Data");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        input_mulai = findViewById(R.id.input_mulai);
        input_akhir = findViewById(R.id.input_akhir);
        ripple_cari = findViewById(R.id.ripple_cari);

        input_mulai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog = new DatePickerDialog(SearchActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month_of_year, int day_of_month) {
                        // set day of month , month and year value in the edit text
                        NumberFormat numberFormat = new DecimalFormat("00");
                        MainActivity.tgl_mulai = year + "-" + numberFormat.format(( month_of_year +1 )) + "-" +
                                numberFormat.format(day_of_month);
                        input_mulai.setText(numberFormat.format(day_of_month) + "/" + numberFormat.format(( month_of_year +1 )) +
                                "/" + year );

                        Log.e("mulai", MainActivity.tgl_mulai );
                    }
                }, CurrentDate.year, CurrentDate.month, CurrentDate.day);
                datePickerDialog.show();
            }
        });

        input_akhir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog = new DatePickerDialog(SearchActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month_of_year, int day_of_month) {
                        // set day of month , month and year value in the edit text
                        NumberFormat numberFormat   = new DecimalFormat("00");
                        MainActivity.tgl_akhir      = year + "-" + numberFormat.format(( month_of_year +1 )) + "-" +
                                numberFormat.format(day_of_month);
                        input_akhir.setText(numberFormat.format(day_of_month) + "/" + numberFormat.format(( month_of_year +1 )) +
                                "/" + year );
                    }
                }, CurrentDate.year, CurrentDate.month, CurrentDate.day);
                datePickerDialog.show();
            }
        });

        ripple_cari.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                if(input_mulai.getText().toString().equals("") || input_akhir.getText().toString().equals("") ){
                    Toast.makeText(getApplicationContext(),"Isi Data dengan benar", Toast.LENGTH_LONG).show();
                }
                else{
                    MainActivity.query_pencarian = true;
                    MainActivity.text_hasil_pencarian.setText(input_mulai.getText().toString() +" s/d "+input_akhir.getText().toString());
                    MainActivity.text_hasil_pencarian.setVisibility(View.VISIBLE);

                    finish();
                }
            }
        });

    }



    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
