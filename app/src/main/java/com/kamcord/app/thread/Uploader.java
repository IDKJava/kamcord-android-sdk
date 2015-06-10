package com.kamcord.app.thread;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.kamcord.app.R;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.ReserveVideoEntity;
import com.kamcord.app.server.model.ReserveVideoResponse;
import com.kamcord.app.server.model.StatusCode;
import com.kamcord.app.server.model.VideoUploadedEntity;
import com.kamcord.app.server.model.builder.ReserveVideoEntityBuilder;
import com.kamcord.app.server.model.builder.VideoUploadedEntityBuilder;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.FileSystemManager;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import retrofit.RetrofitError;

public class Uploader extends Thread {
    private static final String TAG = Uploader.class.getSimpleName();
    private Context mContext;

    enum UploadType {
        VIDEO,
        VOICE
    }

    private RecordingSession mRecordingSession;

    private int mTotalParts = 0;
    private FileInputStream mPartInputStream;

    private static XmlPullParserFactory xmlParserFactory;

    private String mS3SecretKey;
    private String mS3AccessKey;
    private String mS3SessionToken;

    // Amazon parts have to be *at least* 5 megs, except the last one which can be smaller.
    private static final long S3_GENERIC_PART_SIZE = 5 * 1024 * 1024;

    private String mServerVideoId = "";
    private String mVideoBucketName = null;
    private String mVoiceBucketName = null;
    private String mVideoKeyName = null;
    private String mVoiceKeyName = null;
    private long mClockSkew;

    private String[] mVideoEtags;
    private String mS3UploadId = null;

    private static HashSet<WeakReference<UploadStatusListener>> sListeners = new HashSet<>();

    static {
        try {
            xmlParserFactory = XmlPullParserFactory.newInstance();
            xmlParserFactory.setNamespaceAware(true);
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Unexpected exception during XML parser initialization...");
            e.printStackTrace();
        }
    }

    public static void subscribe(UploadStatusListener listener)
    {
        sListeners.add(new WeakReference<>(listener));
    }

    public static boolean unsubscribe(UploadStatusListener unsubscriber) {
        boolean unsubscribed = false;

        for( WeakReference<UploadStatusListener> listenerRef : sListeners ) {
            UploadStatusListener listener = listenerRef.get();
            if( listener != null && listener.equals(unsubscriber) ) {
                sListeners.remove(listenerRef);
                unsubscribed = true;
                break;
            }
        }

        return unsubscribed;
    }

    public Uploader(RecordingSession recordingSession, Context context) {
        mRecordingSession = recordingSession;
        mContext = context;
    }

    @Override
    public void run() {
        AccountManager accountManager = new AccountManager();
        Account account = accountManager.getStoredAccount();
        Map<String, String> videoParams = new HashMap<>();
        videoParams.put(mContext.getResources().getString(R.string.flurryGameName), mRecordingSession.getGamePackageName());
        videoParams.put(mContext.getResources().getString(R.string.flurryGameID), mRecordingSession.getGameServerID());
        videoParams.put(mContext.getResources().getString(R.string.flurryUserName), account.username);
        videoParams.put(mContext.getResources().getString(R.string.flurryUserID), account.id);

        try {
            long start = System.currentTimeMillis();

            notifyUploadStart(mRecordingSession);
            reserveVideoUpload();
            startUploadToS3(UploadType.VIDEO);
            for (int part = 0; part < mTotalParts; part++) {
                uploadPartToS3(part, UploadType.VIDEO);
                notifyUploadProgressed(mRecordingSession, (float) (part+1) / (float) mTotalParts);
            }
            finishUploadToS3(UploadType.VIDEO);
            informKamcordUploadFinished();
            FileSystemManager.markRecordingSession(mRecordingSession, FileSystemManager.Mark.UPLOADED);
            notifyUploadFinished(mRecordingSession, true);

            long end = System.currentTimeMillis();
            videoParams.put(mContext.getResources().getString(R.string.flurryVideoID), mServerVideoId);
            videoParams.put(mContext.getResources().getString(R.string.flurryDuration), Long.toString(end - start));
            videoParams.put(mContext.getResources().getString(R.string.flurrySuccess), "true");
            FlurryAgent.logEvent(mContext.getResources().getString(R.string.flurryVideoShare), videoParams);
            return;

        } catch (Throwable e) {
            Log.e(TAG, "Something unexpected happened during video upload, trying again...");
            e.printStackTrace();
        }

        notifyUploadFinished(mRecordingSession, false);
        videoParams.put(mContext.getResources().getString(R.string.flurrySuccess), "false");
        FlurryAgent.logEvent(mContext.getResources().getString(R.string.flurryVideoShare), videoParams);
        Log.e(TAG, "Unable to upload video, giving up.");
    }

    private void reserveVideoUpload() throws Exception {
        try {
            ReserveVideoEntity reserveVideoEntity = new ReserveVideoEntityBuilder()
                    .setUserTitle(mRecordingSession.getVideoTitle())
                    .setDescription(mRecordingSession.getVideoDescription())
                    .setDefaultTitle("default title") // TODO: fill this in with something that makes sense.
                    .setGameId(mRecordingSession.getGameServerID())
                    .build();

            GenericResponse<ReserveVideoResponse> genericResponse = null;
            try {
                genericResponse = AppServerClient.getInstance().reserveVideo(reserveVideoEntity);
            } catch (RetrofitError e) {
                e.printStackTrace();
            }

            if (genericResponse == null || genericResponse.response == null) {
                // TODO: notify *someone* that were weren't able to reserve the video.
                return;
            }

            mServerVideoId = genericResponse.response.video_id;
            mVideoBucketName = genericResponse.response.video_location.bucket;
            mVoiceBucketName = genericResponse.response.voice_location.bucket;
            mVideoKeyName = genericResponse.response.video_location.key;
            mVoiceKeyName = genericResponse.response.voice_location.key;
            mClockSkew = 0;
            Log.v(TAG, "Setting clock skew to " + mClockSkew);

            mS3AccessKey = genericResponse.response.credentials.access_key_id;
            mS3SecretKey = genericResponse.response.credentials.secret_access_key;
            mS3SessionToken = genericResponse.response.credentials.session_token;

            // Once we have the video id, we can share to the specified social networks.
            // TODO: handle this when we start sharing to external networks.
        } catch (Exception e) {
            Log.e(TAG, "Something unexpected happened while parsing server's response from requesting a video id...");
            throw e;
        }
    }

    private void startUploadToS3(UploadType uploadType) throws Exception {
        // First, create the request.
        HttpEntityEnclosingRequestBase request;
        switch (uploadType) {
            case VOICE:
                request = makeRequestForS3("POST", "audio/mp4a-latm", mVoiceBucketName, mVoiceKeyName, "uploads");
                break;
            default:
            case VIDEO:
                request = makeRequestForS3("POST", "video/mp4", mVideoBucketName, mVideoKeyName, "uploads");
                break;
        }

        // Then, execute the request, and receive the response from Amazon.
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = null;
        try {
            response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                Log.e(TAG, "Invalid status code returned while attempting to start upload to S3...");
                Log.e(TAG, EntityUtils.toString(response.getEntity()));
                throw new Exception();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while executing request to start upload!");
            e.printStackTrace();
            throw e;
        }

        // Finally, handle the response string.
        String responseString = EntityUtils.toString(response.getEntity());
        String uploadId = "";
        boolean gettingUploadId = false;

        try {
            XmlPullParser xpp = xmlParserFactory.newPullParser();

            xpp.setInput(new StringReader(responseString));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;

                    case XmlPullParser.START_TAG:
                        String tag = xpp.getName();
                        if (tag.equals("UploadId")) {
                            gettingUploadId = true;
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        gettingUploadId = false;
                        break;

                    case XmlPullParser.TEXT:
                        if (gettingUploadId) {
                            uploadId = xpp.getText();
                        }
                        break;
                }
                eventType = xpp.next();
            }

            if (uploadId != null && !uploadId.equals("")) {
                File file;
                switch (uploadType) {
                    case VOICE:
                        // TODO: handle the voice track differently if we're goint to be doing that.
                        file = new File(
                                FileSystemManager.getRecordingSessionCacheDirectory(mRecordingSession),
                                FileSystemManager.MERGED_VIDEO_FILENAME);
                        break;
                    default:
                    case VIDEO:
                        file = new File(
                                FileSystemManager.getRecordingSessionCacheDirectory(mRecordingSession),
                                FileSystemManager.MERGED_VIDEO_FILENAME);
                        break;
                }
                long fileSize = file.length();

                mTotalParts = (int) (fileSize / S3_GENERIC_PART_SIZE);
                if (fileSize > mTotalParts * S3_GENERIC_PART_SIZE)
                    mTotalParts++;

                mVideoEtags = new String[mTotalParts];
                mS3UploadId = uploadId;
            }
        } catch (Exception e) {
            Log.e(TAG, "Something unexpected happened while parsing Amazon's response from starting a video upload...");
            throw e;
        }
    }

    private void uploadPartToS3(int partNumber, UploadType uploadType) throws Exception {
        if (mVideoEtags[partNumber] != null) {
            Log.v(TAG, "Already uploaded part " + partNumber + ", continuing with next part");
            return;
        }

        int amazonPartNumber = partNumber + 1; // Amazon 1-indexes like chumps.

        // First, create the request.
        HttpEntityEnclosingRequestBase request;
        switch (uploadType) {
            case VOICE:
                request = makeRequestForS3("PUT", "application/x-www-form-urlencoded; charset=utf-8", mVoiceBucketName, mVoiceKeyName, "partNumber=" + amazonPartNumber + "&uploadId=" + mS3UploadId);
                break;
            default:
            case VIDEO:
                request = makeRequestForS3("PUT", "application/x-www-form-urlencoded; charset=utf-8", mVideoBucketName, mVideoKeyName, "partNumber=" + amazonPartNumber + "&uploadId=" + mS3UploadId);
                break;
        }

        // Then, add the entity.
        File file;
        switch (uploadType) {
            case VOICE:
                // TODO: handle the voice track differently, if we're going to do that.
                file = new File(
                        FileSystemManager.getRecordingSessionCacheDirectory(mRecordingSession),
                        FileSystemManager.MERGED_VIDEO_FILENAME);
                break;
            default:
            case VIDEO:
                file = new File(
                        FileSystemManager.getRecordingSessionCacheDirectory(mRecordingSession),
                        FileSystemManager.MERGED_VIDEO_FILENAME);
                break;
        }
        try {
            mPartInputStream = new FileInputStream(file);
            long fileSize = mPartInputStream.getChannel().size();
            long startingPoint = partNumber * S3_GENERIC_PART_SIZE;

            long partSize = fileSize - startingPoint;
            if (partSize > S3_GENERIC_PART_SIZE) {
                partSize = S3_GENERIC_PART_SIZE;
            }

            int skipped = 0;
            int count = 0;
            while (skipped < startingPoint && count < 100) {
                skipped += mPartInputStream.skip(startingPoint - skipped);
                count++;
            }

            InputStreamEntity inStreamEntity = new InputStreamEntity(mPartInputStream, partSize);
            request.setEntity(inStreamEntity);
        } catch (Exception e) {
            Log.e(TAG, "Something unexpected happened while setting the entity for uploading a part of the file to Amazon...");
            throw e;
        }

        // Then, execute the request, and receive the response from Amazon.
        HttpClient httpclient = new DefaultHttpClient();

        HttpResponse response = httpclient.execute(request);
        if (response.getStatusLine().getStatusCode() != 200) {
            Log.e(TAG, "Invalid status code returned while attempting to upload file part...");
            throw new Exception();
        }

        mPartInputStream.close();

        String etag = null;
        Header etagHeaders[] = response.getHeaders("etag");
        if (etagHeaders.length > 0)
            etag = etagHeaders[0].getValue();

        if (etag != null) {
            mVideoEtags[partNumber] = etag;
        } else {
            Log.e(TAG, "Something unexpected happened when handling Amazon's response while uploading part of a file...");
            throw new Exception();
        }

    }

    private void finishUploadToS3(UploadType uploadType) throws Exception {
        // First, create the request.
        HttpEntityEnclosingRequestBase request;
        switch (uploadType) {
            case VOICE:
                request = makeRequestForS3("POST", "text/xml", mVoiceBucketName, mVoiceKeyName, "uploadId=" + mS3UploadId);
                break;
            default:
            case VIDEO:
                request = makeRequestForS3("POST", "text/xml", mVideoBucketName, mVideoKeyName, "uploadId=" + mS3UploadId);
                break;
        }

        // Then, add the entity.
        String body = "<CompleteMultipartUpload>\n";
        for (int i = 0; i < mVideoEtags.length; i++) {
            body += "<Part><PartNumber>" + (i + 1) + "</PartNumber>\n" +
                    "<ETag>" + mVideoEtags[i] + "</ETag></Part>\n";
        }
        body += "</CompleteMultipartUpload>";
        request.setEntity(new StringEntity(body, "utf-8"));

        // Then, execute the request, and receive the response from Amazon.
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(request);
        if (response.getStatusLine().getStatusCode() != 200) {
            Log.e(TAG, "Invalid status code returned while attempting to finish file upload...");
            throw new Exception();
        }
    }

    private void informKamcordUploadFinished() throws Exception {
        VideoUploadedEntity videoUploadedEntity = new VideoUploadedEntityBuilder()
                .setVideoId(mServerVideoId)
                .build();

        GenericResponse<?> genericResponse = null;
        try {
            genericResponse = AppServerClient.getInstance().videoUploaded(videoUploadedEntity);
        } catch (RetrofitError e) {
        }
        if (genericResponse == null || genericResponse.status == null || !genericResponse.status.equals(StatusCode.OK)) {
            Log.e(TAG, "Invalid status code returned while attempting to complete upload.");
            throw new Exception();
        }
    }

    private HttpEntityEnclosingRequestBase makeRequestForS3(String method, String contentType, String bucketName, String onlineId, String suffix) throws Exception {
        HttpEntityEnclosingRequestBase request;

        // Create the request.
        if (method.equals("POST")) {
            request = new HttpPost();
        } else if (method.equals("PUT")) {
            request = new HttpPut();
        } else {
            return null;
        }

        // Set the uri.
        String uriString = "https://" + bucketName + ".s3.amazonaws.com/" + onlineId;
        if (suffix != null && suffix.length() > 0) {
            uriString += "?" + suffix;
        }
        request.setURI(new URI(uriString));

        // Add the headers.
        String dateString = makeDateStringForS3();
        String signature = makeSignatureForS3(dateString, method, contentType, bucketName, onlineId, suffix);
        request.addHeader("Authorization", "AWS " + mS3AccessKey + ":" + signature);
        request.addHeader("User-Agent", "");
        request.addHeader("Date", dateString);
        request.addHeader("Host", bucketName + ".s3.amazonaws.com");
        request.addHeader("Content-Type", contentType);
        request.addHeader("x-amz-security-token", mS3SessionToken);

        return request;
    }

    private String makeDateStringForS3() {
        Date nowWithSkew = new Date(System.currentTimeMillis() - mClockSkew);
        SimpleDateFormat format =
                new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(nowWithSkew);
    }

    private String makeSignatureForS3(String dateString, String method, String contentType, String bucketName, String onlineId, String suffix) throws Exception {
        String signature = null;

        String value = method + "\n\n" + contentType + "\n" + dateString + "\n" +
                "x-amz-security-token:" + mS3SessionToken + "\n" +
                "/" + bucketName + "/" + onlineId;

        if (suffix != null && !suffix.equals(""))
            value += "?" + suffix;

        SecretKeySpec key = new SecretKeySpec(mS3SecretKey.getBytes(), "HmacSHA1");

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(key);
            byte[] rawHmac = mac.doFinal(value.getBytes());
            signature = Base64.encodeToString(rawHmac, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "Something unexpected happened while making a signature for S3...");
            throw e;
        }

        return signature;
    }

    private class InputStreamEntity extends org.apache.http.entity.InputStreamEntity {
        private OutputStreamProgress outstream;

        public InputStreamEntity(FileInputStream instream, long length) {
            super(instream, length);
        }

        @Override
        public void writeTo(OutputStream outstream) throws IOException {
            this.outstream = new OutputStreamProgress(outstream);
            super.writeTo(this.outstream);
        }

    }

    private class OutputStreamProgress extends OutputStream {
        private final OutputStream outstream;

        public OutputStreamProgress(OutputStream outstream) {
            this.outstream = outstream;
        }

        @Override
        public void write(int b) throws IOException {
            outstream.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            outstream.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            outstream.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            outstream.flush();
        }

        @Override
        public void close() throws IOException {
            outstream.close();
        }
    }

    private void notifyUploadStart(final RecordingSession session) {
        for( WeakReference<UploadStatusListener> listenerRef : sListeners ) {
            final UploadStatusListener listener = listenerRef.get();
            if( listener != null ) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onUploadStart(session);
                    }
                });
            }
        }
    }

    private void notifyUploadProgressed(final RecordingSession session, final float progress) {
        for( WeakReference<UploadStatusListener> listenerRef : sListeners ) {
            final UploadStatusListener listener = listenerRef.get();
            if( listener != null ) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onUploadProgress(session, progress);
                    }
                });
            }
        }
    }

    private void notifyUploadFinished(final RecordingSession session, final boolean success) {
        for( WeakReference<UploadStatusListener> listenerRef : sListeners ) {
            final UploadStatusListener listener = listenerRef.get();
            if( listener != null ) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onUploadFinish(session, success);
                    }
                });
            }
        }
    }

    public interface UploadStatusListener
    {
        void onUploadStart(RecordingSession recordingSession);
        void onUploadProgress(RecordingSession recordingSession, float progress);
        void onUploadFinish(RecordingSession recordingSession, boolean success);
    }
}
