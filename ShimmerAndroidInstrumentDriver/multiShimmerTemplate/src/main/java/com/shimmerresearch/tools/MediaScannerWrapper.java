package com.shimmerresearch.tools;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

public class MediaScannerWrapper implements  
MediaScannerConnection.MediaScannerConnectionClient {
    private MediaScannerConnection mConnection;
    private String mPath;
    private String mMimeType;

    // filePath - where to scan; 
    // mime type of media to scan i.e. "image/jpeg". 
    // use "*/*" for any media
    public MediaScannerWrapper(Context ctx, String filePath, String mime){
        mPath = filePath;
        mMimeType = mime;
        mConnection = new MediaScannerConnection(ctx, this);
    }

    // do the scanning
    public void scan() {
        mConnection.connect();
    }

    // start the scan when scanner is ready
    public void onMediaScannerConnected() {
        mConnection.scanFile(mPath, mMimeType);
        Log.d("MediaScannerWrapper", "media file scanned: " + mPath);
    }

	@Override
	public void onScanCompleted(String arg0, Uri arg1) {
		// TODO Auto-generated method stub
		
	}
}