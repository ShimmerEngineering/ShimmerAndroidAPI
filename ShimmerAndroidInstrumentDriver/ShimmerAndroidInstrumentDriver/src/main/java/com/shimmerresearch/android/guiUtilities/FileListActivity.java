package com.shimmerresearch.android.guiUtilities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import androidx.core.content.FileProvider;

import com.shimmerresearch.androidinstrumentdriver.R;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;

import javax.annotation.Nullable;

/**
 * Opens a list of files from a selected folder. Files in the list can be clicked to open an app selector.
 * A file provider that is defined in the Android manifest is needed in order to provide read access to files which are clicked.
 * The file provider's path should be pointing to the directory path or root directory path of the folder that is opened.
 * All file contents are assumed to be text.
 * Note: This Activity is currently an experimental feature and may not be fully functional
 */
public class FileListActivity extends Activity {

    protected Button cancelButton;
    protected ArrayAdapter<String> mFilesArrayAdapter;
    protected File[] filesArray;
    protected String[] fileNamesArray;
    protected ListView filesListView;

    public final static String INTENT_EXTRA_DIR_PATH = "DirectoryPath";
    public final static String INTENT_EXTRA_PROVIDER_AUTHORITY = "FileProviderAuthority";

    /** Replace this with your own file provider's authority.
     * This will be replaced with any String extras with name INTENT_EXTRA_PROVIDER_AUTHORITY that are passed along with the Intent */
    protected String FILE_PROVIDER_AUTHORITY = "com.shimmerresearch.shimmer.fileprovider";
    /** Replace this with the directory you want to open.
     * This will be replaced with any String extras with name INTENT_EXTRA_DIR_PATH that are passed along with the Intent */
    protected String DIRECTORY_PATH = Environment.getExternalStorageDirectory().getPath() + "/Shimmer/";
    /** You can replace this with your own custom MIME type */
    protected final static String FILE_TYPE = "text/";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupFilesList();
    }

    /**
     * Override this method to modify the dialog theme and behaviour
     */
    protected void setupFilesList() {
        retrieveAnyStringExtras();

        // Setup the window
        setContentView(R.layout.file_list);

        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize the button to perform device discovery
        cancelButton = (Button) findViewById(R.id.buttonCancel);

        File fileDir = new File(DIRECTORY_PATH);
        if(!fileDir.exists()) {
            Toast.makeText(this, "Error! Directory does not exist.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            filesArray = fileDir.listFiles();

            if (filesArray != null) {
                ArrayList<String> fileNamesList = new ArrayList<String>();
                for (File file : filesArray) {
                    fileNamesList.add(file.getName());
                }

                fileNamesArray = new String[fileNamesList.size()];
                fileNamesArray = fileNamesList.toArray(fileNamesArray);

                mFilesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name, fileNamesArray);

                filesListView = (ListView) findViewById(R.id.listViewFiles);
                filesListView.setAdapter(mFilesArrayAdapter);
                filesListView.setOnItemClickListener(mFileClickListener);

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                });
            } else {
                Toast.makeText(this, "Error! Could not retrieve files from directory", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void retrieveAnyStringExtras() {
        String dirPath = getIntent().getStringExtra(INTENT_EXTRA_DIR_PATH);
        String providerAuthority = getIntent().getStringExtra(INTENT_EXTRA_PROVIDER_AUTHORITY);
        if(dirPath != null) {
            if(!dirPath.equals("")) {
                DIRECTORY_PATH = dirPath;
            }
        }
        if(providerAuthority != null) {
            if(!providerAuthority.equals("")) {
                FILE_PROVIDER_AUTHORITY = providerAuthority;
            }
        }
    }

    protected AdapterView.OnItemClickListener mFileClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Get the file extension for the MIME type
            String fileExtension = StringUtils.substringAfterLast(fileNamesArray[position], ".");

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(FileProvider.getUriForFile(FileListActivity.this, FILE_PROVIDER_AUTHORITY, filesArray[position]), FILE_TYPE + fileExtension);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open File"));
        }
    };


}
