package com.example.multiimageuploader

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val RESULT_IMAGE = 10
    private lateinit var fileNameList: ArrayList<String>
    private lateinit var fileDoneList: ArrayList<String>
    private var mAdapter: UploadListAdapter? = null
    private var mStorageReference: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        fileNameList = ArrayList()
        fileDoneList = ArrayList()
        mAdapter = UploadListAdapter(fileNameList, fileDoneList)

        recycler_upload.layoutManager=LinearLayoutManager(this)
        recycler_upload.hasFixedSize()
        recycler_upload.adapter=mAdapter

        mStorageReference = FirebaseStorage.getInstance().reference

        btnUpload.setOnClickListener {
            requestPermission()
            val intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_IMAGE)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RESULT_IMAGE && requestCode== RESULT_OK){
            if(data?.data!=null){
                val totalItems=data.clipData?.itemCount
                for(i in 0..totalItems!!){
                    val fileUri=data.clipData?.getItemAt(i)?.uri
                    val filename=getFileName(fileUri!!)
                    fileNameList.add(filename)
                    fileDoneList.add("Uploading")
                    mAdapter?.notifyDataSetChanged()

                    val fileToUpload=mStorageReference?.child("Images")?.child(filename)
                    fileToUpload?.putFile(fileUri)?.addOnSuccessListener {
                        fileDoneList.removeAt(i);
                        fileDoneList.add(i, "Done");

                        mAdapter?.notifyDataSetChanged();
                    }
                }
            } else if (data?.data != null) {
                Toast.makeText(this, "Selected Single File", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private fun requestPermission() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object:PermissionListener{
                override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse?) {
                    //Toast.makeText(RegisterActivity2.this, "Permission", Toast.LENGTH_SHORT).show();
                }

                override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse?) {
                    DialogOnDeniedPermissionListener.Builder
                        .withContext(this@MainActivity)
                        .withTitle("Read External Storage permission")
                        .withMessage("Read External Storage  permission is needed")
                        .withButtonText("Ok")
                        .withIcon(R.mipmap.ic_launcher)
                        .build()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissionRequest: PermissionRequest?,
                    permissionToken: PermissionToken,
                ) {
                    permissionToken.continuePermissionRequest()
                }
            }).check()
    }

    private fun showSettingsDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Need Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GO TO SETTINGS") { dialog, which ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton("Cancel") {
                dialog, which -> dialog.cancel()
        }
        builder.show()
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    @SuppressLint("Range")
    fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }
}
