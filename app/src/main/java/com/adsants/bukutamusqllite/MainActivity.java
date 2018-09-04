package com.adsants.bukutamusqllite;

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
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AdapterView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ListView list_bukutamu;
    TextView jumlah_laki, jumlah_perempuan, jumlah_keseluruhan;
    SwipeRefreshLayout swipe_refresh;

    ArrayList<HashMap<String, String>>  data_buku_tamu;

    public static String transaksi_id,tgl_mulai,tgl_akhir;
    public static TextView text_hasil_pencarian;
    public static boolean query_pencarian;
    public static String nama,jenis_kelamin,alamat,tanggal_input,tanggal_indo;

    String query_data,query_total;

    SqliteHelper sqliteHelper;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

//        Belajar Layout

        // log.d adalah untuk debug
        Log.d("contoh","hasilnya satu");

        // log.e adalah untuk error
        Log.e("contoh2","hasilnya Dua");

        toolbar.setTitle("Buku Tamu API by adsants");
        setSupportActionBar(toolbar);

        //Toolbar mToolbar = findViewById(R.id.toolbar);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                //// untuk onclick tombol add
                startActivity(new Intent(MainActivity.this, AddActivity.class));
            }
        });

        list_bukutamu           = findViewById(R.id.list_view);
        data_buku_tamu          = new ArrayList<>();

        sqliteHelper            = new SqliteHelper(this);

        jumlah_perempuan        = findViewById(R.id.jumlah_perempuan);
        jumlah_laki             = findViewById(R.id.jumlah_laki);
        jumlah_keseluruhan      = findViewById(R.id.jumlah_keseluruhan);
        text_hasil_pencarian    = findViewById(R.id.text_hasil_pencarian);

        swipe_refresh           = findViewById(R.id.swipe_refresh);

        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                query_data  =   "select *,strftime('%d-%m-%Y', tanggal_input) AS tgl from transaksi order by tanggal_input desc";
                query_total =   "select count(*) as jumlah_total,(select count(*) from transaksi where jenis_kelamin='Laki-Laki') as jumlah_laki," +
                        "(select count(*) from transaksi where jenis_kelamin='Perempuan') as jumlah_perempuan " +
                        "from transaksi ";

               // kasAdapter();
                _readMysql();
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();

        query_data  =   "select *,strftime('%d-%m-%Y', tanggal_input) AS tgl from transaksi order by tanggal_input desc";
        query_total =   "select count(*) as jumlah_total," +
                "(select count(*) from transaksi where jenis_kelamin='Laki-Laki') as jumlah_laki," +
                "(select count(*) from transaksi where jenis_kelamin='Perempuan') as jumlah_perempuan " +
                "from transaksi ";

        if(query_pencarian){
            query_data  =   "select *,strftime('%d-%m-%Y', tanggal_input) AS tgl from transaksi" +
                    " where (tanggal_input >= '"+tgl_mulai+"') and (tanggal_input <= '"+tgl_akhir+"')" +
                    "order by tanggal_input desc";
            query_total =   "select count(*) as jumlah_total," +
                    "(select count(*) from transaksi where jenis_kelamin='Laki-Laki' and  (tanggal_input >= '"+tgl_mulai+"') " +
                    "and (tanggal_input <= '"+tgl_akhir+"')) as jumlah_laki," +
                    "(select count(*) from transaksi where jenis_kelamin='Perempuan' and  (tanggal_input >= '"+tgl_mulai+"') " +
                    "and (tanggal_input <= '"+tgl_akhir+"')) as jumlah_perempuan " +
                    "from transaksi where (tanggal_input >= '"+tgl_mulai+"') and (tanggal_input <= '"+tgl_akhir+"')";
        }

        //kasAdapter();
        _readMysql();
    }

    private void _readMysql(){

        data_buku_tamu.clear();
        list_bukutamu.setAdapter(null);

        AndroidNetworking.post(ConfigURL.Domain + "list.php")
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
                Log.e("transaksi_id", transaksi_id);

                popUpAction();
            }
        });
    }

    protected void kasAdapter() {

        data_buku_tamu.clear();
        list_bukutamu.setAdapter(null);

        SQLiteDatabase db = sqliteHelper.getReadableDatabase();
        cursor = db.rawQuery(query_data, null);
        cursor.moveToFirst();

        int i;
        for( i=0; i < cursor.getCount(); i++ ){
            cursor.moveToPosition(i);

            HashMap<String, String > map = new HashMap<>();

            map.put("transaksi_id", cursor.getString(0));
            map.put("nama", cursor.getString(2));
            map.put("jkl", cursor.getString(1));
            map.put("alamat", cursor.getString(3));
            map.put("tanggal", cursor.getString(5));

            data_buku_tamu.add(map);
        }

        if (i == 0){
            Toast.makeText(getApplicationContext(), "Tidak ada transaksi untuk ditampilkan",
                    Toast.LENGTH_LONG).show();
        }

        SimpleAdapter simpleAdapter =  new SimpleAdapter( this, data_buku_tamu, R.layout.list_view_adapter,
         new String[] {"transaksi_id" , "nama", "jkl", "alamat", "tanggal"},
         new int[] {R.id.text_transaksi_id, R.id.text_nama, R.id.text_jkl, R.id.text_alamat, R.id.text_tanggal}
        );
        list_bukutamu.setAdapter(simpleAdapter);

        list_bukutamu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                transaksi_id = ((TextView)view.findViewById(R.id.text_transaksi_id)).getText().toString();
                Log.e("transaksi_id", transaksi_id);

                popUpAction();
            }
        });

        hitungJumlah();

        /// menghilangkan loading refresh
        swipe_refresh.setRefreshing(false);

    }

    private void hitungJumlah() {
        NumberFormat rupiahFormat = NumberFormat.getInstance(Locale.GERMANY);

        SQLiteDatabase db = sqliteHelper.getReadableDatabase();
        cursor = db.rawQuery(query_total, null);
        cursor.moveToFirst();


        jumlah_laki.setText(rupiahFormat.format(cursor.getDouble(1)));
        jumlah_perempuan.setText(rupiahFormat.format(cursor.getDouble(2)));
        jumlah_keseluruhan.setText(rupiahFormat.format(cursor.getDouble(0)));

        if(!query_pencarian){
            text_hasil_pencarian.setVisibility(View.GONE);
        }
        query_pencarian = false;
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
        builder.setMessage("Anda yakin akan menghapus Data terpilih ..?");
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
        SQLiteDatabase db = sqliteHelper.getWritableDatabase();
        db.execSQL("delete from transaksi where transaksi_id='"+transaksi_id+"'");

        Toast.makeText(getApplicationContext(),"Data Berhasil dihapus", Toast.LENGTH_LONG).show();
        kasAdapter();
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
            startActivity(new Intent(MainActivity.this, SearchActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
