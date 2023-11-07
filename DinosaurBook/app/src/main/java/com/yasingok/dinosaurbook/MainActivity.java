package com.yasingok.dinosaurbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.yasingok.dinosaurbook.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding mainBinding;
    ArrayList<Dinosaur> dinosaurArrayList;
    DinoAdapter dinoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();
        setContentView(view);

        dinosaurArrayList = new ArrayList<>();

        mainBinding.recycler.setLayoutManager(new LinearLayoutManager(this));
        dinoAdapter = new DinoAdapter(dinosaurArrayList);
        mainBinding.recycler.setAdapter(dinoAdapter);
        getData();
    }

    private void getData(){
        try {
            SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("Dinos", MODE_PRIVATE, null);
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM dinos", null);

            int nameIx = cursor.getColumnIndex("name");
            int idIx = cursor.getColumnIndex("id");

            while (cursor.moveToNext()){
                String nameData = cursor.getString(nameIx);
                int idData = cursor.getInt(idIx);
                Dinosaur dinosaur = new Dinosaur(nameData, idData);
                dinosaurArrayList.add(dinosaur);
            }
            dinoAdapter.notifyDataSetChanged();     // Veri değiştiğinde güncellemek için

            cursor.close();
        }catch (Exception g1){
            g1.printStackTrace();
        }
    }

    // Burada üst menü için override ediyoruz ve menüyü bağlıyoruz
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();

        // Burada nereden nereye bağlamak istediğimizi seçiyoruz
        menuInflater.inflate(R.menu.dino_add_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Burada da tıklanınca ne olacağını tanımlıyoruz
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.ustMenu){
            Intent intent = new Intent(this, Dino.class);

            intent.putExtra("info", "new");     // Karşı tarafa yeni bilgi gittiğini göstermenin ilkel yolu
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}