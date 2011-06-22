package edu.mit.mitmobile2.facilities;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import edu.mit.mitmobile2.Global;
import edu.mit.mitmobile2.MobileWebApi;
import edu.mit.mitmobile2.Module;
import edu.mit.mitmobile2.ModuleActivity;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.TwoLineActionRow;
import edu.mit.mitmobile2.objs.FacilitiesItem.LocationRecord;

//public class FacilitiesActivity extends ModuleActivity implements OnClickListener {
public class FacilitiesDetailsActivity extends ModuleActivity {

	public static final String TAG = "FacilitiesProblemTypeActivity";
	private static final int MENU_INFO = 0;

	private Context mContext;	
	private TextView problemStringTextView;
	private EditText problemDescription;
	private EditText sendAsEditText;
    private static final int CAMERA_PIC_REQUEST = 1;
    private static final int PIC_SELECTION = 2;
    private TwoLineActionRow addAPhotoActionRow;
    private TwoLineActionRow takePhotoActionRow;
    private TwoLineActionRow chooseExistingPhotoActionRow;
    private TwoLineActionRow cancelActionRow;    
    private TwoLineActionRow submitActionRow;
	private View facilitiesCameraOptionsLayout;
	private ImageView selectedImage;
	private Uri mCapturedImageUri;
	private Uri mSelectedImageUri;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
        this.mContext = this;
        createViews();
	}

	public void createViews() {
        setContentView(R.layout.facilities_details);        
        
    	facilitiesCameraOptionsLayout = (View)findViewById(R.id.facilitiesCameraOptionsLayout);
    	
    	// Set problem string
        problemStringTextView = (TextView)findViewById(R.id.facilitiesProblemString);
        String problemString = "I'm reporting a problem with the " + Global.sharedData.getFacilitiesData().getProblemType();
        
        if (Global.sharedData.getFacilitiesData().getBuildingRoomName().equalsIgnoreCase("INSIDE")) {
        	problemString += " inside " + Global.sharedData.getFacilitiesData().getLocationId();
        }
        else if (Global.sharedData.getFacilitiesData().getBuildingRoomName().equalsIgnoreCase("OUTSIDE")) {
        	problemString += " outside " + Global.sharedData.getFacilitiesData().getLocationId();
        }
        else {
        	problemString += " at " + Global.sharedData.getFacilitiesData().getBuildingNumber() + " in " + Global.sharedData.getFacilitiesData().getBuildingRoomName();        	
        }

        problemStringTextView.setText(problemString);
        
        problemDescription = (EditText) findViewById(R.id.problemDescription);
        sendAsEditText = (EditText) findViewById(R.id.facilitiesSendAs);
        
        // Add A Photo
    	addAPhotoActionRow = (TwoLineActionRow)findViewById(R.id.facilitiesAddAPhotoActionRow);
    	addAPhotoActionRow.setActionIconResource(R.drawable.photoopp);
	
    	addAPhotoActionRow.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TwoLineActionRow addAPhotoActionRow = (TwoLineActionRow)findViewById(R.id.facilitiesAddAPhotoActionRow);
				addAPhotoActionRow.setVisibility(View.GONE);
	            selectedImage = (ImageView)findViewById(R.id.selectedImage);
	            selectedImage.setVisibility(View.GONE);	
				View facilitiesCameraOptionsLayout = findViewById(R.id.facilitiesCameraOptionsLayout);
				facilitiesCameraOptionsLayout.setVisibility(View.VISIBLE);
				mSelectedImageUri = null;
			}
		});
    	
    	// selected Image
    	selectedImage = (ImageView)findViewById(R.id.selectedImage);
    	
    	// Take Photo
    	takePhotoActionRow = (TwoLineActionRow)findViewById(R.id.facilitiesTakePhotoActionRow);

    	takePhotoActionRow.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    ContentValues values = new ContentValues();   
			    mCapturedImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);  
			        
            	Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // Normally you would populate this with your custom intent.
            	cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageUri);
            	startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);  
			}
		});

    	
    	// Use Existing Photo
    	chooseExistingPhotoActionRow = (TwoLineActionRow)findViewById(R.id.facilitiesChooseExistingPhotoActionRow);
    	chooseExistingPhotoActionRow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent choosePhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
				startActivityForResult(choosePhoto, PIC_SELECTION);
			}
    	});
    	
    	
    	// Cancel
    	cancelActionRow = (TwoLineActionRow)findViewById(R.id.facilitiesCancelActionRow);
    	cancelActionRow.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				addAPhotoActionRow.setVisibility(View.VISIBLE);				
	            selectedImage.setVisibility(View.VISIBLE);
				facilitiesCameraOptionsLayout.setVisibility(View.GONE);
			}
		});
    	
    	// Submit form
    	submitActionRow = (TwoLineActionRow)findViewById(R.id.facilitiesSubmitActionRow);
    	submitActionRow.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				submitForm();
			}
		});
	}
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == CAMERA_PIC_REQUEST) {
    			mSelectedImageUri = mCapturedImageUri;          
        	} else if(requestCode == PIC_SELECTION) {
        		mSelectedImageUri = data.getData();
        	}
			
	        selectedImage.setVisibility(View.VISIBLE);
	        long imageId = ContentUris.parseId(mSelectedImageUri);
	        Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), imageId, MediaStore.Images.Thumbnails.MINI_KIND, null);
	        selectedImage.setImageBitmap(thumbnail);
	        facilitiesCameraOptionsLayout.setVisibility(View.GONE);
	        addAPhotoActionRow.setVisibility(View.VISIBLE);	 
    	} 
    }

	@Override
	protected Module getModule() {
		return new FacilitiesModule();
	}

	@Override
	public boolean isModuleHomeActivity() {
		return false;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_INFO:
			Intent intent = new Intent(mContext, FacilitiesInfoActivity.class);					
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void prepareActivityOptionsMenu(Menu menu) { 
	}
	
	void submitForm() {
		FileUploader fileUploader = new FileUploader();
		fileUploader.execute();
	}
	
	private class FileUploader extends AsyncTask<Void, Long, Boolean> implements FileUploadListener {
		ProgressDialog mProgressDialog;
		long mMaxBytes;
		String mSendAs;
		String mProblemDescription;
		CountingMultipartEntity mUploadEntity; 
		
		@Override
		protected void onPreExecute() {
			mProblemDescription = problemDescription.getText().toString();
			mSendAs = sendAsEditText.getText().toString();
			mUploadEntity = new CountingMultipartEntity(this);
			mProgressDialog = new ProgressDialog(mContext);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setMessage("Uploading Data");
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					mUploadEntity.cancel();	
					FileUploader.this.cancel(true);
				}
			});
			mProgressDialog.show();
		}
		
		@Override
		protected Boolean doInBackground(Void... unusedArgs) {
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost("http://" + Global.getMobileWebDomain() + "/api/?module=facilities&command=upload");
				mMaxBytes = 500;
				if(mSelectedImageUri != null) {
					InputStream imageStream = getContentResolver().openInputStream(mSelectedImageUri);
					byte[] imageData = IOUtils.toByteArray(imageStream);
					mMaxBytes += imageData.length; // this an approximation of the total bytes to be transferred
					InputStreamBody imageStreamBody = new InputStreamBody(new ByteArrayInputStream(imageData), "image");
					mUploadEntity.addPart("image", imageStreamBody);
				}
				mUploadEntity.addPart("name", new StringBody(mSendAs));
				mUploadEntity.addPart("message", new StringBody(mProblemDescription));
				publishProgress(new Long(0)); // initialize the progress bar
				
				httpPost.setEntity(mUploadEntity);
				HttpResponse response;
				response = httpClient.execute(httpPost);
				response.getEntity().getContent().close();
				return true;
			} catch (FileNotFoundException fileException)  {
				fileException.printStackTrace();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
			
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean success) {
			mProgressDialog.dismiss();
			if(!success) {
				Toast.makeText(mContext, MobileWebApi.NETWORK_ERROR, Toast.LENGTH_SHORT);
			}
		}
		
		@Override
		public void onBytesTransfered(long transferred) {
			publishProgress(transferred);	
		}
		
		@Override
		public void onProgressUpdate(Long... progress) {
			long progressBytes = progress[0];
			if(progressBytes > mMaxBytes) {
				// this a hack to account for the fact
				// that maxKiloBytes is just an approximation
				progressBytes = mMaxBytes; 
			}
			if(mMaxBytes > 10000) {
				// use kilobytes	
				int maxKiloBytes = (int)(mMaxBytes / 1000);
				mProgressDialog.setMax(maxKiloBytes);
				int progressKiloBytes = (int)(progress[0] / 1000);
				mProgressDialog.setProgress(progressKiloBytes);
			} else {
				mProgressDialog.setMax((int)mMaxBytes);
				mProgressDialog.setProgress((int)progressBytes);
			}
		}
		
	}


	/*
	 * methods and classes to hook into count how many bytes of the image have been uploaded
	 */
	private interface FileUploadListener {
		void onBytesTransfered(long transferred);
	}
	
	private class CountingMultipartEntity extends MultipartEntity {		
		FileUploadListener mFileUploadListener;
		CountingOutputStream mCountingOutputStream;
		
		CountingMultipartEntity(FileUploadListener fileUploadListener) {
			mFileUploadListener = fileUploadListener;
		}
		
		@Override
		public void writeTo(final OutputStream outstream) throws IOException {
			mCountingOutputStream = new CountingOutputStream(outstream, mFileUploadListener);
			super.writeTo(mCountingOutputStream);
		}
		
		public void cancel() {
			mCountingOutputStream.cancel();
		}
	}
	
	public static class CountingOutputStream extends FilterOutputStream {

		private long mTransferred;
		FileUploadListener mFileUploadListener;
		CountingMultipartEntity mUploadEntity;
		boolean isCancelled = false;
		
	    public CountingOutputStream(final OutputStream out, FileUploadListener fileUploadListener) {
	    	super(out);
	        mTransferred = 0;
	        mFileUploadListener = fileUploadListener;
	    }

	    public void cancel() {
	    	isCancelled = true;	
	    }
	    
	    public void write(byte[] b, int off, int len) throws IOException {
	    	if(isCancelled) {
	    		throw new IOException("Upload was cancelled");
	    	}
	    	out.write(b, off, len);
	        mTransferred += len;
	        mFileUploadListener.onBytesTransfered(mTransferred);
	    }

	    public void write(int b) throws IOException {
	    	if(isCancelled) {
	    		throw new IOException("Upload was cancelled");
	    	}
	    	out.write(b);
	        mTransferred++;
	        mFileUploadListener.onBytesTransfered(mTransferred);
	    }
	}
}
	

	
	