package com.polsri.projek1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;

public class InsertAndViewActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_CODE_STORAGE = 100;
    int eventID = 0;
    EditText edtFileName, edtContent;
    Button btnSimpan;
    boolean isEditable = false;
    String fileName = "";
    String tempCatatan = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_and_view);

        // button back
        ActionBar menu = getSupportActionBar();
        menu.setDisplayShowHomeEnabled(true);
        menu.setDisplayHomeAsUpEnabled(true);

        // input
        edtFileName = findViewById(R.id.editFilename);
        edtContent = findViewById(R.id.editContent);

        // button
        btnSimpan = findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            fileName = extras.getString("filename");
            edtFileName.setText(fileName);
            getSupportActionBar().setTitle("Ubah Catatan");
        } else {
            getSupportActionBar().setTitle("Tambah Catatan");
        }

        eventID = 1;
        if (Build.VERSION.SDK_INT >= 23) {
            if (periksaIzin()) {
                bacaFile();
            }
        } else {
            bacaFile();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSimpan :
                eventID = 2;
                if (!tempCatatan.equals(edtContent.getText().toString())) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        konfirmasiSave();
                    }
                } else {
                    konfirmasiSave();
                }
                break;
        }
    }

    public boolean periksaIzin() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == getPackageManager().PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case  REQUEST_CODE_STORAGE :
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (eventID == 1) {
                        bacaFile();
                    } else {
                        konfirmasiSave();
                    }
                }
                break;
        }
    }

    void bacaFile() {
        String path = Environment.getExternalStorageDirectory().toString() + "/halimlab.catatan";
        File file = new File(path, edtFileName.getText().toString());
        if (file.exists()) {
            StringBuilder text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = br.readLine();
                while (line != null) {
                    text.append(line);
                    line = br.readLine();
                }
                br.close();
            } catch (Exception e) {
                System.out.println("ERROR" + e.getMessage());
            }
            tempCatatan = text.toString();
            edtContent.setText(text.toString());
        }
    }

    void buatDanUbah() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return;
        }
        String path = Environment.getExternalStorageDirectory().toString() + "/halimlab.catatan";
        File parent = new File(path);
        if (parent.exists()) {
            File file = new File(path, edtFileName.getText().toString());
            FileOutputStream outputStream = null;
            try {
                file.createNewFile();
                outputStream = new FileOutputStream(file);
                OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream);
                streamWriter.append(edtContent.getText());
                streamWriter.flush();
                streamWriter.close();
                streamWriter.flush();
                streamWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            parent.mkdir();
            File file = new File(path, edtFileName.getText().toString());
            FileOutputStream outputStream = null;
            try {
                file.createNewFile();
                outputStream = new FileOutputStream(file, false);
                outputStream.write(edtContent.getText().toString().getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onBackPressed();
    }

    void konfirmasiSave () {
        new AlertDialog.Builder(this)
                .setTitle("Simpan Catatan")
                .setMessage("Apakah anda akan menyimpan catatan ini ?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        buatDanUbah();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    @Override
    public void onBackPressed() {
        if (!tempCatatan.equals(edtContent.getText().toString())) {
            konfirmasiSave();
        } else if (tempCatatan.equals(edtContent.getText().toString())){
            super.onBackPressed(); // Do that in the else part.
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
