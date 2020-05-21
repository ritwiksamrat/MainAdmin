package com.example.samratritwik.mainadmin;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class NoticeActivity extends AppCompatActivity {

    Uri pdfUri;
    Button selectfile, upload;
    TextView notification;
    FirebaseStorage firebaseStorage;
    FirebaseDatabase database;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.noticeactivity);

        firebaseStorage = FirebaseStorage.getInstance();
        selectfile = findViewById(R.id.selectfile);
        upload = findViewById(R.id.upload);
        notification = findViewById(R.id.notification);

        selectfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(NoticeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){

                    selectPdf();
                }
                else
                    ActivityCompat.requestPermissions(NoticeActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},9);
            }
        });

            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                        if(pdfUri!=null)
                            uploadFile(pdfUri);
                        else
                            Toast.makeText(NoticeActivity.this,"Select a File",Toast.LENGTH_SHORT).show();
                }
            });


    }

    private void uploadFile(Uri pdfUri) {


        progressDialog=new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading...");
        progressDialog.setProgress(0);
        progressDialog.show();

        final String fileName=System.currentTimeMillis()+"";
        StorageReference storageReference= firebaseStorage.getReference();
        storageReference.child("upload").child(fileName).putFile(pdfUri)
            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){

                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot){

                    String url=taskSnapshot.getDownloadUrl().toString();
                    DatabaseReference reference=database.getReference();

                    reference.child(fileName).setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                Toast.makeText(NoticeActivity.this,"Successfully Uploaded",Toast.LENGTH_SHORT).show();
                            }
                            else
                                Toast.makeText(NoticeActivity.this,"File is not Successfully Uploaded",Toast.LENGTH_SHORT).show();


                        }
                    });

                }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NoticeActivity.this,"File is not successfully uploaded",Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                int currentProgress= (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
          if (requestCode==9 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
          {
              selectPdf();
          }
          else
              Toast.makeText(NoticeActivity.this,"Please Provide Permission",Toast.LENGTH_SHORT).show();
    }

    private void selectPdf() {

        Intent intent=new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,86);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==86 && resultCode==RESULT_OK && data!=null)
        {
            pdfUri=data.getData();
            notification.setText("A File is Selected : "+data.getData().getLastPathSegment());
        }
        else
        {
            Toast.makeText(NoticeActivity.this,"Please Select File", Toast.LENGTH_SHORT).show();
        }
    }
}
