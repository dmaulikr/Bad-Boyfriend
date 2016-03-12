package com.clarifai.androidstarter;

import android.app.Activity;
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

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_recognition);
    textView = (TextView) findViewById(R.id.text_view);
    ArrayList<String> imagePaths = getImagesPath(this);
    textView = (TextView) findViewById(R.id.text_view);

    for (int i = 1; i < imagePaths.size(); i++) {
      textView.setText(imagePaths.get(i-1)+ " \n" + imagePaths.get(i));

    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (requestCode == CODE_PICK && resultCode == RESULT_OK) {
      // The user picked an image. Send it to Clarifai for recognition.
      Log.d(TAG, "User picked image: " + intent.getData());
      Bitmap bitmap = loadBitmapFromUri(intent.getData());
      if (bitmap != null) {
        imageView.setImageBitmap(bitmap);
        textView.setText("Recognizing...");
        selectButton.setEnabled(false);

        // Run recognition on a background thread since it makes a network call.
        new AsyncTask<Bitmap, Void, RecognitionResult>() {
          @Override protected RecognitionResult doInBackground(Bitmap... bitmaps) {
            return recognizeBitmap(bitmaps[0]);
          }
          @Override protected void onPostExecute(RecognitionResult result) {
            updateUIForResult(result);
          }
        }.execute(bitmap);
      } else {
        textView.setText("Unable to load selected image.");
      }
    }
  }

  /** Loads a Bitmap from a content URI returned by the media picker. */
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

  /** Sends the given bitmap to Clarifai for recognition and returns the result. */
  private RecognitionResult recognizeBitmap(Bitmap bitmap) {
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
      return client.recognize(new RecognitionRequest(jpeg).setModel("nsfw-v0.1")).get(0);
    } catch (ClarifaiException e) {
      Log.e(TAG, "Clarifai error", e);
      return null;
    }
  }

  /** Updates the UI by displaying tags for the given result. */
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

          b.append(nsfwProbability(NSFWProb));
          /*if (tag.toString() != ""){
            bestHashTag = bestHashTag + tag;
          }*/
          //b.append(b.length() > 0 ? "" : "").append(tag.getName());
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

  private String nsfwProbability(double probability){
    if(probability >= 0.85){

      return "Not the safest thing to put on the internet!";
    }
    else if (probability <= 0.15){
      return "Okie Dokie Artichokie!";
    }
    else {
      return "idk bro!";
    }
  }
}
