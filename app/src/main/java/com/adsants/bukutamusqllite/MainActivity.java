package com.adsants.bukutamusqllite;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.adsants.bukutamusqllite.helper.ConfigURL;
import com.adsants.bukutamusqllite.helper.SqliteHelper;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.leavjenn.smoothdaterangepicker.date.SmoothDateRangePickerFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    ListView list_bukutamu;
    TextView jumlah_laki, jumlah_perempuan, jumlah_keseluruhan;
    SwipeRefreshLayout swipe_refresh;

    ArrayList<HashMap<String, String>>  data_buku_tamu;

    public static String transaksi_id,tgl_mulai,tgl_akhir,tgl_mulai_post,tgl_akhir_post;
    public static TextView text_hasil_pencarian;
    public static boolean query_pencarian;
    public static String nama,jenis_kelamin,alamat,tanggal_input,tanggal_indo;

    String query_data,query_total;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle("Buku Tamu API by adsants");
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //// untuk onclick tombol add
                startActivity(new Intent(MainActivity.this, AddActivity.class));
            }
        });

        list_bukutamu           = findViewById(R.id.list_view);
        data_buku_tamu          = new ArrayList<>();

        jumlah_perempuan        = findViewById(R.id.jumlah_perempuan);
        jumlah_laki             = findViewById(R.id.jumlah_laki);
        jumlah_keseluruhan      = findViewById(R.id.jumlah_keseluruhan);
        text_hasil_pencarian    = findViewById(R.id.text_hasil_pencarian);

        swipe_refresh           = findViewById(R.id.swipe_refresh);

        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
            text_hasil_pencarian.setVisibility(View.GONE);
            tgl_mulai_post = "";
            tgl_akhir_post = "";
            _readMysql();

            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();

        tgl_mulai_post = "";
        tgl_akhir_post = "";

        if(query_pencarian){
            tgl_mulai_post = tgl_mulai;
            tgl_akhir_post = tgl_akhir;
        }

        _readMysql();
    }

    private void _readMysql(){

        data_buku_tamu.clear();
        list_bukutamu.setAdapter(null);

        AndroidNetworking.post(ConfigURL.Domain + "list.php")
            .addBodyParameter("tgl_mulai_post", tgl_mulai_post)
            .addBodyParameter("tgl_akhir_post", tgl_akhir_post)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(new JSONObjectRequestListener() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        NumberFormat rupiahFormat = NumberFormat.getInstance(Locale.GERMANY);

                        jumlah_laki.setText(rupiahFormat.format( response.getDouble("JUMLAH_LAKI") ));
                        jumlah_perempuan.setText(rupiahFormat.format( response.getDouble("JUMLAH_PEREMPUAN") ));
                        jumlah_keseluruhan.setText(rupiahFormat.format( response.getDouble("JUMLAH_TOTAL") ));

                        //// Mulai List Array
                        JSONArray jsonArray = response.getJSONArray("result");
                        for (int i = 0;  i < jsonArray.length(); i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            HashMap<String, String > map = new HashMap<>();

                            map.put("transaksi_id", jsonObject.getString("transaksi_id"));
                            map.put("nama", jsonObject.getString("nama"));
                            map.put("jkl", jsonObject.getString("jenis_kelamin"));
                            map.put("alamat", jsonObject.getString("alamat"));
                            map.put("tanggal",jsonObject.getString("tanggal_indo"));

                            data_buku_tamu.add(map);

                            _toAdapter();
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

        swipe_refresh.setRefreshing(false);
    }

    private void _toAdapter(){
        SimpleAdapter simpleAdapter =  new SimpleAdapter( this, data_buku_tamu, R.layout.list_view_adapter,
                new String[] {"transaksi_id" , "nama", "jkl", "alamat", "tanggal"},
                new int[] {R.id.text_transaksi_id, R.id.text_nama, R.id.text_jkl, R.id.text_alamat, R.id.text_tanggal}
        );
        list_bukutamu.setAdapter(simpleAdapter);

        list_bukutamu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                transaksi_id = ((TextView)view.findViewById(R.id.text_transaksi_id)).getText().toString();
                nama = ((TextView)view.findViewById(R.id.text_nama)).getText().toString();
                popUpAction();
            }
        });

    }

    private void popUpAction(){
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.popup_action);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        TextView text_hapus =   dialog.findViewById(R.id.text_hapus);
        TextView text_edit  =   dialog.findViewById(R.id.text_edit);

        text_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, EditActivity.class));
            }
        });

        text_hapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TampilPesanHapusData();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void TampilPesanHapusData(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pesan Konfirmasi");
        builder.setMessage(Html.fromHtml("Anda yakin akan menghapus Data <b>"+nama+"</b> ..?"));
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                hapus_data();
            }
        });
        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void hapus_data() {
        /*SQLiteDatabase db = sqliteHelper.getWritableDatabase();
        db.execSQL("delete from transaksi where transaksi_id='"+transaksi_id+"'");

        Toast.makeText(getApplicationContext(),"Data Berhasil dihapus", Toast.LENGTH_LONG).show();
        kasAdapter();*/


        AndroidNetworking.post(ConfigURL.Domain + "delete.php")
            .addBodyParameter("transaksi_id", transaksi_id)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(new JSONObjectRequestListener() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if(response.getString("response").equals("success")){
                            Toast.makeText(getApplicationContext(),"Data Berhasil dihapus", Toast.LENGTH_LONG).show();
                            _readMysql();

                            //Log.e("pesan_hapus",  response.getString("response"));

                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Maaf, Data Gagal dihapus !", Toast.LENGTH_LONG).show();

                            //Log.e("pesan_hapus",  response.getString("response"));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
           // startActivity(new Intent(MainActivity.this, SearchActivity.class));
            _tampilDateRange();
        }
        return super.onOptionsItemSelected(item);



    }

    private void _tampilDateRange(){
        SmoothDateRangePickerFragment smoothDateRangePickerFragment = SmoothDateRangePickerFragment.newInstance(
                new SmoothDateRangePickerFragment.OnDateRangeSetListener() {
                    @Override
                    public void onDateRangeSet(SmoothDateRangePickerFragment view,
                                               int yearStart, int monthStart,
                                               int dayStart, int yearEnd,
                                               int monthEnd, int dayEnd ) {
                        // grab the date range, do what you want

                        tgl_mulai_post = yearStart+"-"+(monthStart + 1)+"-"+dayStart;
                        tgl_akhir_post = yearEnd+"-"+(monthEnd + 1)+"-"+dayEnd;



                        text_hasil_pencarian.setText("Hasil Pencarian");
                        text_hasil_pencarian.setVisibility(View.VISIBLE);
                        _readMysql();

                        //Log.e("_dateRange", "dateMulai "+ tgl_mulai_post +" DateAkhir "+ tgl_akhir_post);

                    }
                });

        smoothDateRangePickerFragment.show(getFragmentManager(), "smoothDateRangePicker");
    }



}
