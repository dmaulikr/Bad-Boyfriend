package com.clarifai.androidstarter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;
import com.clarifai.api.exception.ClarifaiException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.provider.MediaStore.Images.Media;


/**
 * A simple Activity that performs recognition using the Clarifai API.
 */
public class RecognitionActivity extends Activity {
    private static final String TAG = RecognitionActivity.class.getSimpleName();

    private static final int CODE_PICK = 1;

    private final ClarifaiClient client = new ClarifaiClient(Credentials.CLIENT_ID,
            Credentials.CLIENT_SECRET);
    private Button selectButton;
    private ImageView imageView;
    private TextView textView;
    private Bitmap currentBitmap;
    private ProgressDialog progressDialog;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);
        ArrayList<String> imagePaths = getImagesPath(this);
        //textView = (TextView) findViewById(R.id.text_view);

        for (int i = 1; i < imagePaths.size(); i++) {
            File imgFile = new File(imagePaths.get(i));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), bmOptions);
            //imageView = (ImageView) findViewById(R.id.image_view);
            //View layout = findViewById(R.id.lolwtf);
            myBitmap = Bitmap.createScaledBitmap(myBitmap, 200, 200, true);
            //imageView.setImageBitmap(myBitmap);
        }

        //Creates loading button, and displays loading bar
        Button downloadButton = (Button)findViewById(R.id.select_button_displayProgressDialog);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                progressDialog = new ProgressDialog(RecognitionActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setTitle("Processing");
                progressDialog.setMessage("Analysing... Please Wait");
                progressDialog.show();
                progressThread thread = new progressThread();
                thread.start();
            }
        });
    }

    //Progress Thread for loading screen
    private class progressThread extends Thread{
        public void run(){
            for (int count=0;count<=100;count++){
                try {
                    Thread.sleep(100);
                    progressDialog.setProgress(count);
                }catch(InterruptedException ex){
                    ex.printStackTrace();
                }
            }
            //Enter what you want the program to do after loading screen
            //Insert Code Here *****
            final Intent intent = new Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, CODE_PICK);








            //
        }
    }
   /* textView = (TextView) findViewById(R.id.text_view);
    ArrayList<String> imagePaths = getImagesPath(this);
    textView = (TextView) findViewById(R.id.text_view);
    for (int i = 1; i < imagePaths.size(); i++) {
      textView.setText(imagePaths.get(i-1)+ " \n" + imagePaths.get(i));
    }
*/


    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == CODE_PICK && resultCode == RESULT_OK) {

            Bitmap bitmap = loadBitmapFromUri(intent.getData());
            currentBitmap = bitmap;
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                textView.setText(intent.toString());
                selectButton.setEnabled(false);

                // Run recognition on a background thread since it makes a network call.
                new AsyncTask<Bitmap, Void, RecognitionResult>() {
                    @Override
                    protected RecognitionResult doInBackground(Bitmap... bitmaps) {
                        return recognizeBitmap(bitmaps[0], "nsfw-v0.1");
                    }

                    @Override
                    protected void onPostExecute(RecognitionResult result) {
                        updateUIForResult(result);
                    }
                }.execute(bitmap);
            } else {
                textView.setText("Unable to load selected image.");
            }
        }
    }


    /**
     * Loads a Bitmap from a content URI returned by the media picker.
     */
    private Bitmap loadBitmapFromUri(Uri uri) {
        try {
            // The image may be large. Load an image that is sized for display. This follows best
            // practices from http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, opts);
            int sampleSize = 1;
            while (opts.outWidth / (2 * sampleSize) >= imageView.getWidth() &&
                    opts.outHeight / (2 * sampleSize) >= imageView.getHeight()) {
                sampleSize *= 2;
            }

            opts = new BitmapFactory.Options();
            opts.inSampleSize = sampleSize;
            return BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, opts);
        } catch (IOException e) {
            Log.e(TAG, "Error loading image: " + uri, e);
        }
        return null;
    }
    //"nsfw-v0.1"

    /**
     * Sends the given bitmap to Clarifai for recognition and returns the result.
     */
    private RecognitionResult recognizeBitmap(Bitmap bitmap, String model) {
        try {
            // Scale down the image. This step is optional. However, sending large images over the
            // network is slow and  does not significantly improve recognition performance.
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 320,
                    320 * bitmap.getHeight() / bitmap.getWidth(), true);

            // Compress the image as a JPEG.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, out);
            byte[] jpeg = out.toByteArray();

            // Send the JPEG to Clarifai and return the result.
            return client.recognize(new RecognitionRequest(jpeg).setModel(model)).get(0);
        } catch (ClarifaiException e) {
            Log.e(TAG, "Clarifai error", e);
            return null;
        }
    }

    /**
     * Updates the UI by displaying tags for the given result.
     */
    private void updateUIForResult(RecognitionResult result) {
        //String bestHashTag = "#";
        if (result != null) {
            if (result.getStatusCode() == RecognitionResult.StatusCode.OK) {
                // Display the list of tags in the UI.
                StringBuilder b = new StringBuilder();

                for (Tag tag : result.getTags()) {
                    Double SFWProb = tag.getProbability();
                    b.append(b.length() > 0 ? "" : "").append(tag.getName() + SFWProb);
                    b.append(nsfwProbability(SFWProb));

                    Double NSFWProb = tag.getProbability();
                    b.append(b.length() > 0 ? "" : "").append(tag.getName() + NSFWProb);

                    if (nsfwProbability(NSFWProb)) {

                        tagsRequest(currentBitmap);
                    }
                    b.append(nsfwProbability(NSFWProb));

                }
                textView.setText("Tags:\n#" + b);
            } else {
                Log.e(TAG, "Clarifai: " + result.getStatusMessage());
                textView.setText("Sorry, there was an error recognizing your image.");
            }
        } else {
            textView.setText("Sorry, there was an error recognizing your image.");
        }
        selectButton.setEnabled(true);
    }

    public static ArrayList<String> getImagesPath(Activity activity) {
        Uri uri;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        String PathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};


        cursor = activity.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            PathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(PathOfImage);
        }
        return listOfAllImages;
    }

    private Boolean nsfwProbability(double probability) {
        if (probability >= 0.85) {
            return true;
        }
        return false;
    }


    /* below here*/
    private void tagsRequest(Bitmap bitmap) {

        // Run recognition on a background thread since it makes a network call.
        new AsyncTask<Bitmap, Void, RecognitionResult>() {
            @Override
            protected RecognitionResult doInBackground(Bitmap... bitmaps) {
                return recognizeNSWFBitmap(bitmaps[0], "");
            }

        }.execute(bitmap);

    }


    //"nsfw-v0.1"

    /**
     * Sends the given bitmap to Clarifai for recognition and returns the result.
     */
    private RecognitionResult recognizeNSWFBitmap(Bitmap bitmap, String model) {
        try {
            // Scale down the image. This step is optional. However, sending large images over the
            // network is slow and  does not significantly improve recognition performance.
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 320,
                    320 * bitmap.getHeight() / bitmap.getWidth(), true);

            // Compress the image as a JPEG.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, out);
            byte[] jpeg = out.toByteArray();

            // Send the JPEG to Clarifai and return the result.
            return client.recognize(new RecognitionRequest(jpeg).setModel(model)).get(0);
        } catch (ClarifaiException e) {
            Log.e(TAG, "Clarifai error", e);
            return null;
        }
    }
}
