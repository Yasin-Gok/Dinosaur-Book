package com.yasingok.dinosaurbook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.yasingok.dinosaurbook.databinding.ActivityDinoBinding;

import java.io.ByteArrayOutputStream;

public class Dino extends AppCompatActivity {
    private ActivityDinoBinding dinoBinding;

    // Bunlar aktivite sonucu etkileşimleriyle alakalı olan kullanımlar
    ActivityResultLauncher<Intent> activityResultLauncher;      // Galeriye gitmek için
    ActivityResultLauncher<String> permissionLauncher;          // İzin için
    Bitmap selectedImage;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dinoBinding = ActivityDinoBinding.inflate(getLayoutInflater());
        View view = dinoBinding.getRoot();
        setContentView(view);
        registerLauncher();         // Ne iş yapacağımızı belirtmek için

        database = this.openOrCreateDatabase("Dinos", MODE_PRIVATE,null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        if (info.equals("new")){
            // Yeni bilgi eklenmek istenmiş
            dinoBinding.nameText.setText("");
            dinoBinding.categoryText.setText("");
            dinoBinding.descriptionText.setText("");
            dinoBinding.saveButton.setVisibility(View.VISIBLE);
            dinoBinding.imageView.setImageResource(R.drawable.add);
        }
        else{
            dinoBinding.saveButton.setVisibility(View.INVISIBLE);
            int dinoId = intent.getIntExtra("dinoId", 0);

            try {
                Cursor cursor = database.rawQuery("SELECT * FROM dinos WHERE id =?", new String[] {String.valueOf(dinoId)});
                int nameIx = cursor.getColumnIndex("name");
                int categoryIx = cursor.getColumnIndex("category");
                int descriptionIx = cursor.getColumnIndex("description");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()){
                    dinoBinding.nameText.setText(cursor.getString(nameIx));
                    dinoBinding.categoryText.setText(cursor.getString(categoryIx));
                    dinoBinding.descriptionText.setText(cursor.getString(descriptionIx));

                    byte [] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                    dinoBinding.imageView.setImageBitmap(bitmap);
                }
                cursor.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void selectImage(View view){
        // Burada gerekli iznin verilip verilmediğini kontrol ediyoruz
        // Contextcompat olmadan da yazılabilirdi ama onun amacı sdk18 öncesini de işleyebilmek
        // Ayrıca Manifest kısmını seçerken yanında android yazıp yazmadığına dikkat etmeliyiz
        // Sağ tarafta ise yardımcı bir sınıf ile izin cevabı kontrol edilir

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){ // Androdid sdk 33+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){

                // Burada gerekli izin ile alakalı bir açıklama yazdırıp yazdırmama olayı var
                // Bu snackbar'da altta tıklanabilir bir kısım yapıyoruz ve uzunluğu kullanıcı tıklayana kadar bekliyor
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view, "Permission needed", Snackbar.LENGTH_INDEFINITE).setAction("Give permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // İzin al
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();
                }
                else{
                    // İzin al
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
            }
            else{
                // İzin verilmiş galeriye git
                // Burası için gerekli aksiyon seçilmeli, görsel alacağımız için pick seçtik
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);     // Başlatmak için
            }
        }
        else{   // Android sdk 32 ve altı
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                // Burada gerekli izin ile alakalı bir açıklama yazdırıp yazdırmama olayı var
                // Bu snackbar'da altta tıklanabilir bir kısım yapıyoruz ve uzunluğu kullanıcı tıklayana kadar bekliyor
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view, "Permission needed", Snackbar.LENGTH_INDEFINITE).setAction("Give permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // İzin al
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();
                }
                else{
                    // İzin al
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
            else{
                // İzin verilmiş galeriye git
                // Burası için gerekli aksiyon seçilmeli, görsel alacağımız için pick seçtik
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);     // Başlatmak için
            }
        }
    }

    public void save(View view){
        String name = dinoBinding.nameText.getText().toString();
        String category = dinoBinding.categoryText.getText().toString();
        String description = dinoBinding.descriptionText.getText().toString();

        // Hafıza kullanımı açısından boyutu küçülttük
        Bitmap smallImage = decreaseImageSize(selectedImage,300);

        // Görseli 1-0 olan veriye çeviriyoruz
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try {
            database.execSQL("CREATE TABLE IF NOT EXISTS dinos(id INTEGER PRIMARY KEY, name VARCHAR, category VARCHAR, description VARCHAR, image BLOB)");

            String sqlString = ("INSERT INTO dinos(name, category, description, image) VALUES(?, ?, ?, ?)");
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString); // Sonradan veri eklemek için bir sınıf
            sqLiteStatement.bindString(1, name);        // Veritabanı indexleri 1'den başlar
            sqLiteStatement.bindString(2, category);
            sqLiteStatement.bindString(3, description);
            sqLiteStatement.bindBlob(4, byteArray);
            sqLiteStatement.execute();
        }
        catch (Exception saveError){
            saveError.printStackTrace();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);        // Burada flag activity ile çok şey yapılabilir
        startActivity(intent);                                  // Biz diğer aktiviteye geçerken öncekileri kapatıyoruz
    }

    public Bitmap decreaseImageSize(Bitmap image, int max_size){
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = ((float)width / (float)height);       // 1'den büyükse yatay demektir
        if (bitmapRatio > 1){   // Yatay
            // landscape image
            width = max_size;
            height = (int)(width / bitmapRatio);
        }
        else{       // Dikey
            // portrait image
            height = max_size;
            width = (int)(height * bitmapRatio);
        }

        return image.createScaledBitmap(image, width, height, true);
    }
    public void back(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Burada, en üstte yapılan launcher kullanımlarını kaydeden ve böylelikle kullanmamızı sağlayan fonksiyon yazılıyor
    private void registerLauncher(){
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                // Burada galeriye girdikten sonra karşılaşılabilecek iptal, seçmedi vb durumlarını inceliyoruz
                if (o.getResultCode() == RESULT_OK){
                    Intent intentFromResult = o.getData();
                    if(intentFromResult != null){
                        Uri imageData = intentFromResult.getData();     // Bu seçilen görselin yerini veriyor uri olarak
                        //dinoBinding.imageView.setImageURI(imageData);   // Yeri lazımsa bu kullanılabilir

                        // Fakat verisi lazım olduğunda da bu kullanılır, kayıt vb durumları için
                        try{        // Bitmap'e çeviriyoruz
                            if (Build.VERSION.SDK_INT >= 28) {      // Belirli bir sdk seviyesi gerekli olduğu için
                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                dinoBinding.imageView.setImageBitmap(selectedImage);
                            }
                            else{
                                selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(),imageData);
                                dinoBinding.imageView.setImageBitmap(selectedImage);
                            }
                        }
                        catch (Exception e1){
                            e1.printStackTrace();       // Hata mesajı için
                        }
                    }
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean o) {
                if (o){         // İzin verildiyse
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);     // Başlatmak için
                }
                else{
                    Toast.makeText(Dino.this, "Permission needed!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}