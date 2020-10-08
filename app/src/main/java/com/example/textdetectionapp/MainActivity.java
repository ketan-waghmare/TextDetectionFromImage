package com.example.textdetectionapp;

import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.textdetectionapp.activity.BaseActivity;
import com.example.textdetectionapp.activity.MyHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import java.util.List;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    private Bitmap myBitmap;
    private TextView myTextView;
    private ImageView myImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myTextView = findViewById(R.id.textView);
        myImageView = findViewById(R.id.imageView);
        findViewById(R.id.checkText).setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case WRITE_STORAGE:
                    checkPermission(requestCode);
                    break;
                case SELECT_PHOTO:
                    Uri dataUri = data.getData();
                    String path = MyHelper.getPath(this, dataUri);
                    if (path == null) {
                        myBitmap = MyHelper.resizePhoto(photo, this, dataUri, myImageView);
                    } else {
                        myBitmap = MyHelper.resizePhoto(photo, path, myImageView);
                    }
                    if (myBitmap != null) {
                        Log.e("bitmap_log"," = "+myBitmap);
                        myTextView.setText(null);
                        myImageView.setImageBitmap(myBitmap);
                    }
                    break;

            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.checkText:
                if (myBitmap != null) {
                    runTextRecog();
                }
                break;

        }
    }

    private void runTextRecog() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(myBitmap);

//        FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

        detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                processText(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Exception = "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

       /* detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText texts) {
                processExtractedText(texts);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure
                    (@NonNull Exception exception) {
                Toast.makeText(MainActivity.this,
                        "Exception", Toast.LENGTH_LONG).show();
            }
        });*/
    }

    private void processText(FirebaseVisionText firebaseVisionText) {
        myTextView.setText(null);
        if (firebaseVisionText.getTextBlocks().size() == 0) {
            myTextView.setText(R.string.no_text);
            return;
        }
        for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
//            myTextView.append(block.getText());
        }


        String resultText = firebaseVisionText.getText();
        for (FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()) {
            String blockText = block.getText();
            Float blockConfidence = block.getConfidence();
            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
            Log.e("blockConfidence_log = "+blockConfidence,"{} blockText_log = "+blockText+" {} blockLanguages_log "+blockLanguages);
//            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            for (FirebaseVisionText.Line line: block.getLines()) {
                String lineText = line.getText();
                Float lineConfidence = line.getConfidence();
                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                myTextView.append("\n"+lineText);
                Log.e("lineConfidence_log = "+lineConfidence," {} lineText_log = "+lineText+" {} lineLanguages_log "+lineLanguages);
//                Point[] lineCornerPoints = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();
                for (FirebaseVisionText.Element element: line.getElements()) {
                    String elementText = element.getText();
                    Float elementConfidence = element.getConfidence();
                    List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                    Log.e("elementC_log = "+elementConfidence," {} elementText_log = "+elementText+" {}lineLanguages_log "+elementLanguages);
//                    Point[] elementCornerPoints = element.getCornerPoints();
                    Rect elementFrame = element.getBoundingBox();
                }
            }
        }
    }
}