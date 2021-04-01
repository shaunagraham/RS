package com.rap.sheet.view.add_contact

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.rap.sheet.BuildConfig
import com.rap.sheet.R
import com.rap.sheet.extenstion.click
import com.rap.sheet.extenstion.displayAlertDialog
import com.rap.sheet.extenstion.makeSnackBar
import com.rap.sheet.extenstion.userID
import com.rap.sheet.utilitys.Constant
import com.rap.sheet.utilitys.FilePathHelper
import com.rap.sheet.utilitys.InternetConnection
import com.rap.sheet.view.common.BaseActivity
import com.rap.sheet.view.contact_detail.ContactDetailActivity
import com.rap.sheet.viewmodel.AddContactViewModel
import kotlinx.android.synthetic.main.activity_add_number.*
import kotlinx.android.synthetic.main.activity_add_number.imageViewProfile
import kotlinx.android.synthetic.main.toolbar_new.*
import kotlinx.android.synthetic.main.top_view_layout.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import java.io.File
import java.io.IOException

class AddContactActivity : BaseActivity(), PermissionCallbacks {

    private var addContact: Boolean = false
    private val mViewModel: AddContactViewModel by viewModel()
    private var id: String? = null
    private var selectedImageFile: File? = null
    private var filePathHelper: FilePathHelper? = null
    private var mobileNo: String? = null
    private var firstName: String? = null
    private var lastName: String? = null
    private var email: String? = null
    private var profile: String? = null
    private var media: MultipartBody.Part? = null
//    private lateinit var binding: ActivityAddNumberBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_number)
        //        setContentView(R.layout.activity_add_number);
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_number)
        filePathHelper = FilePathHelper()

        getIntentData()
        if (addContact) {
            buttonAdd.text = resources.getString(R.string.save)
            editTextFirstName.setText(firstName)
            editTextLastName.setText(lastName)
            editTextPhoneNumber.setText(mobileNo)
            editTextEmail.setText(email)
            Glide.with(this).load(BuildConfig.IMAGE_PATH + this.profile).placeholder(ResourcesCompat.getDrawable(resources, R.drawable.ic_user_new, null)).into((imageViewProfile)!!)
        } else {
            buttonAdd.text = resources.getString(R.string.Add)
        }
        initView()
        setUpToolBar()
        viewClickListener()
        listenToViewModel()
        //checkAdsRemoveOrNot();
    }

    private fun getIntentData() {
        intent.extras?.apply {
            addContact = this.getBoolean("addContact", false)
            firstName = getString("firstName")
            lastName = getString("lastName")
            profile = getString("profile")
            email = getString("email")
            mobileNo = getString("mobile")
        }
    }

    private fun listenToViewModel() {
        mViewModel.addContactSuccessResponse.observe(this, {
            dismissProgressDialog()
            try {
                val result: String = it.string()
                val jsonObjectRoot = JSONObject(result)
                val jsonObjectInfo = JSONObject(jsonObjectRoot.getString("info"))
                editTextFirstName.setText("")
                editTextLastName.setText("")
                editTextEmail.setText("")
                editTextPhoneNumber.setText("")
                editTextWebsite.setText("")
                editTextEmail.setText("")
                editTextFacebook.setText("")
                editTextLinkedIn.setText("")

                id = jsonObjectInfo.getInt("id").toString()
                startActivity(Intent(this@AddContactActivity, ContactDetailActivity::class.java).putExtra("id", jsonObjectInfo.getInt("id").toString()))
                finish()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        })
        mViewModel.addContactErrorResponse.observe(this, {
            dismissProgressDialog()
            try {
                val result: String = it.string()
                val jsonObjectData = JSONObject(result)
                makeSnackBar(coordinatorLayoutRoot, jsonObjectData.getString("error"))
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        })


        mViewModel.noInternetException.observe(this, {
            dismissProgressDialog()
            if (InternetConnection.checkConnection(this)) {
                this.displayAlertDialog(
                        desc = resources.getString(R.string.something_wrong),
                        cancelable = false,
                        positiveText = resources.getString(android.R.string.ok),
                        positiveClick = { dialog, _ ->
                            dialog?.apply {
                                this.dismiss()
                            }
                        }
                )
            } else {
                this.displayAlertDialog(
                        desc = resources.getString(R.string.no_internet_msg),
                        cancelable = false,
                        positiveText = resources.getString(android.R.string.ok),
                        positiveClick = { dialog, _ ->
                            dialog?.apply {
                                this.dismiss()

                            }
                        }
                )
            }
        })
    }

    //    private void checkAdsRemoveOrNot() {
    //        if (!BaseApplication.adsRemovePref.isAdsRemove()) {
    //            View adContainer = findViewById(R.id.adViewContact);
    //            adViewContact = new AdView(AddNumberActivity.this);
    //            adViewContact.setAdSize(AdSize.SMART_BANNER);
    //            adViewContact.setAdUnitId(BuildConfig.BANNER_ID);
    //            ((RelativeLayout) adContainer).addView(adViewContact);
    //            loadBannerAds();
    //        }
    //    }

    // Initialling  view
    private fun initView() {
        editTextPhoneNumber.addTextChangedListener(MyPhoneTextWatcher())
    }

    // For set up toolbar
    private fun setUpToolBar() {
        topView.imgClose.click { onBackPressed() }
        topView.tvTitle.text = resources.getString(R.string.add_contact)
    }

    private fun viewClickListener() {
        buttonCancel.click {
            finish()
        }
        buttonAdd.click {
            if (editTextFirstName!!.text.toString().trim { it <= ' ' }.isEmpty()) {
                makeSnackBar(coordinatorLayoutRoot, resources.getString(R.string.first_name_error_msg))
                editTextFirstName!!.requestFocus()
            } else if (editTextPhoneNumber!!.text.toString().trim { it <= ' ' }.isEmpty()) {
                makeSnackBar(coordinatorLayoutRoot, resources.getString(R.string.phone_name_error_msg))
                editTextPhoneNumber!!.requestFocus()
            } else {
                if (InternetConnection.checkConnection(this@AddContactActivity)) {
                    if (addContact) {
                        ///update profile API
                    } else {
                        callAddNewContactAPI()
                    }
                } else {
                    makeSnackBar(coordinatorLayoutRoot, resources.getString(R.string.no_internet))
                }
            }
        }
        imageViewProfile.click {
            openImageChooseDialog()
        }
    }


    // For Calling new contact API
    private fun callAddNewContactAPI() {
        launchProgressDialog()
        val number: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), editTextPhoneNumber!!.text.toString().trim { it <= ' ' }.replace("-".toRegex(), ""))
        val firstName: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), editTextFirstName!!.text.toString().trim { it <= ' ' })
        val lastname: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), editTextLastName!!.text.toString().trim { it <= ' ' })
        val email: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), editTextEmail!!.text.toString().trim { it <= ' ' })
        val weblink: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), editTextWebsite!!.text.toString().trim { it <= ' ' })
        val instagram: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), editTextInsta!!.text.toString().trim { it <= ' ' })
        val facebook: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), editTextFacebook!!.text.toString().trim { it <= ' ' })
        val linkedin: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), editTextLinkedIn!!.text.toString().trim { it <= ' ' })
        val user_id: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), sharedPreferences.userID)
        mViewModel.addNewContact(number, firstName, lastname, email, weblink, instagram, facebook, linkedin, user_id, media!!)
    }


    private fun openImageChooseDialog() {
        val permission: Array<String> = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        if (EasyPermissions.hasPermissions(this, *permission)) {
//            startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 510);
            openBottomSheetDialog()
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.need_camera_gallery_permission),
                    RC_CAMERA_GALLERY, *permission)
        }
    }

    @SuppressLint("InflateParams")
    private fun openBottomSheetDialog() {
        val chooseImageDialog = BottomSheetDialog(this@AddContactActivity)
        val chooseImageView: View = window.layoutInflater.inflate(R.layout.dialog_choose_option, null)
        chooseImageDialog.setContentView(chooseImageView)
        val textViewCamera: TextView = chooseImageView.findViewById(R.id.textViewCamera)
        val textViewGallery: TextView = chooseImageView.findViewById(R.id.textViewGallery)
        val textViewCancel: TextView = chooseImageView.findViewById(R.id.textViewCancel)
        textViewCamera.click {
            chooseImageDialog.dismiss()
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            selectedImageFile = createTemporaryFile(".png")
            selectedImageFile!!.delete()
            val mImageUri: Uri = FileProvider.getUriForFile(this@AddContactActivity,
                    BuildConfig.APPLICATION_ID + ".provider", (selectedImageFile)!!)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)
            startActivityForResult(intent, CAMERA_CODE)
        }
        textViewGallery.click {
            chooseImageDialog.dismiss()
            startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 510)
        }
        textViewCancel.click { chooseImageDialog.dismiss() }
        chooseImageDialog.show()
    }

    private fun createTemporaryFile(ext: String): File? {
        val tempDir = File(cacheDir.path)
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        try {
            return File.createTempFile(tempDir.name, ext, tempDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    override fun onBackPressed() {
        finish()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        if (requestCode == RC_CAMERA_GALLERY) {
            openBottomSheetDialog()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {}
    inner class MyPhoneTextWatcher : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            val text: String = editTextPhoneNumber.text.toString()
            val textlength: Int = text.length
            if (text.endsWith("-")) return
            if (textlength == 4 || textlength == 8) {
                editTextPhoneNumber.setText(StringBuilder(text).insert(text.length - 1, "-").toString())
                editTextPhoneNumber.setSelection(text.length)
            }
        }

        override fun afterTextChanged(editable: Editable) {
            ///
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.UPDATE_COMMENT) {
            if (data != null) {
                val intent = Intent()
                intent.putExtra("id", id)
                setResult(Constant.UPDATE_COMMENT, intent)
                finish()
            }
        }
        if (requestCode == 510 && resultCode == Activity.RESULT_OK) {
            data?.apply {
                val path: String = getRealPathFromURI(this.data)
                val file = File(path)
                Glide.with(this@AddContactActivity).load(file.absolutePath).into((imageViewProfile)!!)
                val requestBody: RequestBody = RequestBody.create(MediaType.parse("image/*"), file)
                media = MultipartBody.Part.createFormData("profile", file.name, requestBody)
            }

        } else if (requestCode == CAMERA_CODE && resultCode == Activity.RESULT_OK) {
            if (selectedImageFile != null) {
                if (selectedImageFile!!.length() > 0) {
                    Glide.with(this@AddContactActivity).load(selectedImageFile!!.absolutePath).into((imageViewProfile)!!)
                    val requestBody: RequestBody = RequestBody.create(MediaType.parse("image/*"), selectedImageFile)
                    media = MultipartBody.Part.createFormData("profile", selectedImageFile?.name, requestBody)
                }
            }
        }
    }

    private fun getRealPathFromURI(contentUri: Uri?): String {
        var cursor: Cursor? = null
        return try {
            val proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
            cursor = contentResolver.query((contentUri)!!, proj, null, null, null)
            val columnIndex: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } catch (e: Exception) {
            Log.e("Hello", "getRealPathFromURI Exception : $e")
            ""
        } finally {
            cursor?.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.onDetach()
    }

    companion object {
        private val RC_CAMERA_GALLERY: Int = 105
        private val CAMERA_CODE: Int = 102
    }
}