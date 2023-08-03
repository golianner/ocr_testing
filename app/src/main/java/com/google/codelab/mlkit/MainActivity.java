// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.codelab.mlkit;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.codelab.mlkit.GraphicOverlay.Graphic;
import com.google.codelab.mlkit.ocr.GenerateKTPData;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "MainActivity";
    private ImageView mImageView;
    private Button mTextButton;
    private Button mFaceButton;
    private Bitmap mSelectedImage;
    private GraphicOverlay mGraphicOverlay;
    // Max width (portrait mode)
    private Integer mImageMaxWidth;
    // Max height (portrait mode)
    private Integer mImageMaxHeight;

    /**
     * Number of results to show in the UI.
     */
    private static final int RESULTS_TO_SHOW = 3;
    /**
     * Dimensions of inputs.
     */
    private static final int DIM_IMG_SIZE_X = 224;
    private static final int DIM_IMG_SIZE_Y = 224;

    private final PriorityQueue<Map.Entry<String, Float>> sortedLabels =
            new PriorityQueue<>(
                    RESULTS_TO_SHOW,
                    new Comparator<Map.Entry<String, Float>>() {
                        @Override
                        public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float>
                                o2) {
                            return (o1.getValue()).compareTo(o2.getValue());
                        }
                    });

    private static final int REQUEST_CAMERA = 11;
    private static final int REQUEST_GALLERY = 12;

    GenerateKTPData ktpData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.image_view);

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogChoose();
            }
        });

        mTextButton = findViewById(R.id.button_text);
        mFaceButton = findViewById(R.id.button_face);

        mGraphicOverlay = findViewById(R.id.graphic_overlay);
        mTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTextRecognition();
            }
        });
        mFaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runFaceContourDetection();
            }
        });
        Spinner dropdown = findViewById(R.id.spinner);
        String[] items = new String[]{
                "Test Image 1 (Text)",
                "Test Image 2 (Face)",
                "KTP",
                "Sample KTP",
                "Sample KTP2",
                "Sample KTP AXA 1",
                "Sample KTP AXA 2",
                "Sample KTP AXA 3",
                "Sample KTP AXA 4",
                "Sample KTP AXA 5"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout
                .simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);
    }

    private void showDialogChoose(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_choose);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);

        ImageView imgCamera = dialog.findViewById(R.id.imgCamera);
        ImageView imgGallery = dialog.findViewById(R.id.imgGallery);

        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openCamera(MainActivity.this, REQUEST_CAMERA);
                dialog.dismiss();
            }
        });

        imgGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openGallery(MainActivity.this, REQUEST_GALLERY);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new EasyImage.Callbacks() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource imageSource, int i) {
                e.printStackTrace();
            }

            @Override
            public void onImagePicked(File file, EasyImage.ImageSource imageSource, int i) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                if (i == REQUEST_CAMERA){
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    mImageView.setImageBitmap(rotatedBitmap);
                    mSelectedImage = rotatedBitmap;
                } else {
                    mImageView.setImageBitmap(bitmap);
                    mSelectedImage = bitmap;
                }
                runTextRecognition();
            }

            @Override
            public void onCanceled(EasyImage.ImageSource imageSource, int i) {

            }
        });
    }

    private void runTextRecognition() {
        InputImage image = InputImage.fromBitmap(mSelectedImage, 0);
        TextRecognizerOptions.Builder textRecognizerOptions = new TextRecognizerOptions.Builder();
        TextRecognizerOptions options = textRecognizerOptions.build();
        TextRecognizer recognizer = TextRecognition.getClient(options);
        mTextButton.setEnabled(false);
        recognizer.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text texts) {
                                mTextButton.setEnabled(true);
//                                processTextRecognitionResult(texts);
                                processTextRecognitionResultNew(texts);
//                                ktpData = new GenerateKTPData(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                mTextButton.setEnabled(true);
                                e.printStackTrace();
                            }
                        });
    }

    private void processTextRecognitionResultNew(Text texts){
        List<String> results = new ArrayList<>();
//        Log.d("Semua", texts.getText());
        for (Text.TextBlock block : texts.getTextBlocks()){
//            results.add(block.getText());
            for (Text.Line line : block.getLines()){
                results.add(line.getText());
            }
        }
        ktpData = new GenerateKTPData(results);
//        Log.d("Hasilnya", Arrays.toString(results.toArray()));
    }

    private void processTextRecognitionResult(Text texts) {
        List<Text.TextBlock> blocks = texts.getTextBlocks();
//        String dataAwal = texts.getText();
        if (blocks.size() == 0) {
            showToast("No text found");
            return;
        }
        mGraphicOverlay.clear();
        for (int i = 0; i < blocks.size(); i++) {
            String blockData = blocks.get(i).getText();
            Point[] blockCornerPoints = blocks.get(i).getCornerPoints();
            Rect blockFrame = blocks.get(i).getBoundingBox();
//            Log.d("Corner point block", Arrays.toString(blockCornerPoints));
//            Log.d("frame block", blockFrame.toString());
//            Log.d("Blok ke-", blockData);
            List<Text.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                String text = lines.get(j).getText();
                Point[] lineCornerPoints = lines.get(j).getCornerPoints();
                Rect lineFrame = lines.get(j).getBoundingBox();
//                Log.d("Corner point", Arrays.toString(lineCornerPoints));
//                Log.d("frame", lineFrame.toString());
                Log.d("Baris ke-", text);
                List<Text.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    Graphic textGraphic = new TextGraphic(mGraphicOverlay, elements.get(k));
                    mGraphicOverlay.add(textGraphic);
                }
            }
        }
    }

    private void runFaceContourDetection() {
        InputImage image = InputImage.fromBitmap(mSelectedImage, 0);
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .build();

        mFaceButton.setEnabled(false);
        FaceDetector detector = FaceDetection.getClient(options);
        detector.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<List<Face>>() {
                            @Override
                            public void onSuccess(List<Face> faces) {
                                mFaceButton.setEnabled(true);
                                processFaceContourDetectionResult(faces);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                mFaceButton.setEnabled(true);
                                e.printStackTrace();
                            }
                        });

    }

    private void processFaceContourDetectionResult(List<Face> faces) {
        // Task completed successfully
        if (faces.size() == 0) {
            showToast("No face found");
            return;
        }
        mGraphicOverlay.clear();
        for (int i = 0; i < faces.size(); ++i) {
            Face face = faces.get(i);
            FaceContourGraphic faceGraphic = new FaceContourGraphic(mGraphicOverlay);
            mGraphicOverlay.add(faceGraphic);
            faceGraphic.updateFace(face);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Functions for loading images from app assets.

    // Returns max image width, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxWidth() {
        if (mImageMaxWidth == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxWidth = mImageView.getWidth();
        }

        return mImageMaxWidth;
    }

    // Returns max image height, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxHeight() {
        if (mImageMaxHeight == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxHeight =
                    mImageView.getHeight();
        }

        return mImageMaxHeight;
    }

    // Gets the targeted width / height.
    private Pair<Integer, Integer> getTargetedWidthHeight() {
        int targetWidth;
        int targetHeight;
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        targetWidth = maxWidthForPortraitMode;
        targetHeight = maxHeightForPortraitMode;
        return new Pair<>(targetWidth, targetHeight);
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        mGraphicOverlay.clear();
        switch (position) {
            case 0:
                mSelectedImage = getBitmapFromAsset(this, "Please_walk_on_the_grass.jpg");
                break;
            case 1:
                // Whatever you want to happen when the thrid item gets selected
                mSelectedImage = getBitmapFromAsset(this, "grace_hopper.jpg");
                break;
            case 2:
                mSelectedImage = getBitmapFromAsset(this, "ktp.png");
                break;
            case 3:
                mSelectedImage = getBitmapFromAsset(this, "sample_ktp.png");
                break;
            case 4:
                mSelectedImage = getBitmapFromAsset(this, "ktp2.jpeg");
                break;
            case 5:
                mSelectedImage = getBitmapFromAsset(this, "ktp1.jpg");
                break;
            case 6:
                mSelectedImage = getBitmapFromAsset(this, "ktp2.jpg");
                break;
            case 7:
                mSelectedImage = getBitmapFromAsset(this, "ktp3.jpg");
                break;
            case 8:
                mSelectedImage = getBitmapFromAsset(this, "ktp4.jpg");
                break;
            case 9:
                mSelectedImage = getBitmapFromAsset(this, "ktp5.jpg");
                break;
        }
        if (mSelectedImage != null) {
            // Get the dimensions of the View
            Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

            int targetWidth = targetedSize.first;
            int maxHeight = targetedSize.second;

            // Determine how much to scale down the image
            float scaleFactor =
                    Math.max(
                            (float) mSelectedImage.getWidth() / (float) targetWidth,
                            (float) mSelectedImage.getHeight() / (float) maxHeight);

            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(
                            mSelectedImage,
                            (int) (mSelectedImage.getWidth() / scaleFactor),
                            (int) (mSelectedImage.getHeight() / scaleFactor),
                            true);

            mImageView.setImageBitmap(resizedBitmap);
            mSelectedImage = resizedBitmap;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream is;
        Bitmap bitmap = null;
        try {
            is = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}
