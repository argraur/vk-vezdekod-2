package com.travels.searchtravels.api;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.travels.searchtravels.utils.ImageHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static com.travels.searchtravels.utils.ImageHelper.getBase64EncodedJpeg;

public class VisionApi2 {
    private OnVisionApiListener onVisionApiListener;

    public VisionApi2(OnVisionApiListener listener) {
        this.onVisionApiListener = listener;
    }

    public void findLocation(Image base64EncodedImage, String token) {
        Handler handler = new Handler(Looper.getMainLooper());
        try {
            VisionRequestInitializer requestInitializer =
                    new VisionRequestInitializer("AIzaSyD4N_orCMgITvSXywp9I7XTwOR4nUBLJFs");
            HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            Vision.Builder builder = new Vision.Builder
                    (httpTransport, jsonFactory, null);
            builder.setVisionRequestInitializer(requestInitializer);
            builder.setApplicationName("com.preview.planner");
            Vision vision = builder.build();

            List<Feature> featureList = new ArrayList<>();

            Feature textDetection = new Feature();
            textDetection.setType("WEB_DETECTION");
            textDetection.setMaxResults(10);
            featureList.add(textDetection);

            Feature landmarkDetection = new Feature();
            landmarkDetection.setType("LANDMARK_DETECTION");
            landmarkDetection.setMaxResults(10);
            featureList.add(landmarkDetection);

            List<AnnotateImageRequest> imageList = new ArrayList<>();
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
            annotateImageRequest.setImage(base64EncodedImage);
            annotateImageRequest.setFeatures(featureList);
            imageList.add(annotateImageRequest);

            BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                    new BatchAnnotateImagesRequest();
            batchAnnotateImagesRequest.setRequests(imageList);

            Vision.Images.Annotate annotateRequest =
                    vision.images().annotate(batchAnnotateImagesRequest);
            // Due to a bug: requests to Vision API containing large images fail when GZipped.
            annotateRequest.setDisableGZipContent(true);
            Log.d("VISION_API", "sending request");

            BatchAnnotateImagesResponse response = annotateRequest.execute();
            try {
                if (response != null && response.getResponses() != null && response.getResponses().get(0) != null && response.getResponses().get(0).getLandmarkAnnotations() != null && response.getResponses().get(0).getLandmarkAnnotations().get(0) != null && response.getResponses().get(0).getLandmarkAnnotations().get(0).getLocations() != null && response.getResponses().get(0).getLandmarkAnnotations().get(0).getLocations().get(0) != null && response.getResponses().get(0).getLandmarkAnnotations().get(0).getLocations().get(0).getLatLng() != null) {
                    handler.post(() -> onVisionApiListener.onSuccess(response.getResponses().get(0).getLandmarkAnnotations().get(0).getLocations().get(0).getLatLng()));
                } else if (response != null){
                    if (response.toString().toLowerCase().contains("\"sea\"")){
                        handler.post(() -> onVisionApiListener.onErrorPlace("sea"));

                    } else if (response.toString().toLowerCase().contains("\"beach\"")){
                        handler.post(() -> onVisionApiListener.onErrorPlace("beach"));

                    } else if (response.toString().toLowerCase().contains("\"mountain\"")){
                        handler.post(() -> onVisionApiListener.onErrorPlace("mountain"));

                    } else if (response.toString().toLowerCase().contains("\"snow\"")){
                        handler.post(() -> onVisionApiListener.onErrorPlace("snow"));

                    } else if (response.toString().toLowerCase().contains("\"ocean\"")){
                        handler.post(() -> onVisionApiListener.onErrorPlace("ocean"));

                    } else {
                        handler.post(() -> onVisionApiListener.onError());

                    }

                }

                System.out.println("Cloud Vision success = " + response);



            } catch (Error e){
                e.printStackTrace();
                handler.post(() -> onVisionApiListener.onError());
            }

        } catch (GoogleJsonResponseException e) {
            handler.post(() -> onVisionApiListener.onError());
            Log.e("VISION_API", "Request failed: " + e.getContent());
        } catch (IOException e) {
            handler.post(() -> onVisionApiListener.onError());
            Log.d("VISION_API", "Request failed: " + e.getMessage());
        }
    }
    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp=android.util.Base64.encodeToString(b, android.util.Base64.DEFAULT);
        return temp;
    }
}
