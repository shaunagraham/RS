package com.rap.sheet.view.profile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.rap.sheet.BuildConfig
import com.rap.sheet.R
import com.rap.sheet.extenstion.*
import com.rap.sheet.extenstion.uID
import com.rap.sheet.model.Profile.GetProfileData
import com.rap.sheet.model.Profile.InfoX
import com.rap.sheet.utilitys.FilePathHelper
import com.rap.sheet.utilitys.InternetConnection
import com.rap.sheet.view.common.BaseActivity
import com.rap.sheet.viewmodel.GetProfileViewModel
import com.rap.sheet.viewmodel.ProfileViewModel
import kotlinx.android.synthetic.main.activity_add_number.*
import kotlinx.android.synthetic.main.activity_contact_detail.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.constraintLayoutNoInternet
import kotlinx.android.synthetic.main.no_internet_view.*
import kotlinx.android.synthetic.main.progress_dialog_view.*
import kotlinx.android.synthetic.main.toolbar_new.*
import kotlinx.android.synthetic.main.top_view_layout.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.IOException

class ProfileActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {

    //    private lateinit var binding: ActivityProfileBinding
    private var selectedImageFile: File? = null
    private var filePathHelper: FilePathHelper? = null

    private var media: MultipartBody.Part? = null
    private val TAG = ProfileActivity::class.java.simpleName

    //    private var contactDetailRootModel: GetProfileData? = null
    private var profileList: MutableList<InfoX> = mutableListOf()
    private val mViewModel: ProfileViewModel by viewModel()
    private val mViewModelProfile: GetProfileViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        filePathHelper = FilePathHelper()
        setView()
        listenToViewModelProfile()
        launchContactDetailAPI()
        listenToViewModel()
    }

    private fun listenToViewModelProfile() {

        mViewModelProfile.getProfileSuccessResponse.observe(this, {
            profileList.clear()
            val result: String = it.string()
            val getProfileData = Gson().fromJson(result, GetProfileData::class.java)
            getProfileData?.info?.apply {
                profileList.addAll(this)
            }
//            contactDetailRootModel = GetProfileData()
//            contactDetailRootModel = Gson().fromJson(result, ContactDetailRootModel::class.java)
            setUpContactDetailsData()
        })

        mViewModel.unAuthorizationException.observe(this, {
            displayAlertDialog(
                    desc = resources.getString(R.string.contact_delete_msg),
                    positiveText = resources.getString(android.R.string.ok),
                    positiveClick = { dialog, _ ->
                        dialog?.apply {
                            dismiss()
                            finish()
                        }
                    }
            )
            dismissProgressDialog()

        })

        mViewModelProfile.getProfileErrorResponse.observe(this, {
            dismissProgressDialog()
            reLoadAPI()
        })

        mViewModelProfile.noInternetException.observe(this, {
            if (it == "detail") {
                progressBar.beGone()
                if (InternetConnection.checkConnection(this@ProfileActivity)) {
                    reLoadAPI()
                }
            } else {
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
                                    finish()
                                }
                            }
                    )
                }
            }
        })

    }

    private fun setUpContactDetailsData() {
//        linearLayoutOtherOption.beVisible()
//        floatingButtonComment!!.show()
        dismissProgressDialog()
        editTextFirstNameProfile.setText(profileList.get(0).first_name)
        editTextLastNameProfile.setText(profileList.get(0).last_name)

//            val stringBuilderPhone: StringBuilder = StringBuilder(profileList.get(0).phone.toString().replace("-".toRegex(), ""))
//            if (stringBuilderPhone.length > 3) {
//                stringBuilderPhone.insert(3, "-")
//            }
//            if (stringBuilderPhone.length > 7) {
//                stringBuilderPhone.insert(7, "-")
//            }

        if (this.profileList.get(0).email != "") {
            editTextEmailProfile.setText(profileList.get(0).email)
        }

        if (profileList.get(0).profile != "") {
            Glide.with(this@ProfileActivity).load(BuildConfig.IMAGE_PATH + profileList.get(0).profile).into((imageViewProfileadd)!!)
        }

        if (this.profileList.get(0).weblink != "") {
            editTextWebsiteProfile.setText(profileList.get(0).weblink)
        }

        if (this.profileList.get(0).instagram != "") {
            editTextInstaProfile.setText(profileList.get(0).instagram)
        }

        if (this.profileList.get(0).twitter != "") {
            editTextTwitterProfile.setText(profileList.get(0).twitter)
        }

        if (this.profileList.get(0).facebook != "") {
            editTextFacebookProfile.setText(profileList.get(0).facebook)
        }

        if (this.profileList.get(0).linkedin != "") {
            editTextLinkedInProfile.setText(profileList.get(0).linkedin)
        }

        editTextPhoneNumberProfile.setText(profileList.get(0).phone)

    }


    private fun reLoadAPI() {
        displayAlertDialog(
                desc = resources.getString(R.string.something_wrong),
                positiveText = resources.getString(R.string.retry),
                positiveClick = { dialog, _ ->
                    dialog?.apply {
                        dismiss()
                        launchContactDetailAPI()
                        launchProgressDialog()
                    }
                },
                negativeText = resources.getString(R.string.cancel),
                negativeClick = { dialog, _ ->
                    dialog?.apply {
                        dismiss()
                    }
                }
        )
    }

    private fun launchContactDetailAPI() {
        launchProgressDialog()
        mViewModelProfile.getUserProfile(sharedPreferences.uID)
//        contactDetails = restInterface!!.contactDetails(id)
//        contactDetails!!.enqueue(contactDetailsCallBack)
    }

    private fun listenToViewModel() {
        mViewModel.uploadProfileSuccessResponse.observe(this, {
            dismissProgressDialog()
            val result: String = it.string()
            val jsonObjectResult = JSONObject(result)
            Toast.makeText(this, "" + jsonObjectResult.getString("success"), Toast.LENGTH_SHORT)
            finish()
//            Handler().postDelayed({
//                finish()
//                //doSomethingHere()
//            }, 1000)

        })
        mViewModel.uploadProfileErrorResponse.observe(this, {
            dismissProgressDialog()
            finish()
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

    private fun setView() {
        setUpToolBar()
        buttonClose.click {
            onBackPressed()
        }

        imageViewProfileadd.click {
            openImageChooseDialog()
        }

        buttonSave.click {
            if (editTextFirstNameProfile!!.text.toString().trim { it <= ' ' }.isEmpty()) {
                makeSnackBar(editTextFirstNameProfile, resources.getString(R.string.first_name_error_msg))
                editTextFirstNameProfile!!.requestFocus()
            }
//            else if (editTextLastNameProfile!!.text.toString().trim { it <= ' ' }.isEmpty()) {
//                makeSnackBar(editTextLastNameProfile, resources.getString(R.string.first_name_error_msg))
//                editTextLastNameProfile!!.requestFocus()
//            }
            else if (editTextPhoneNumberProfile!!.text.toString().trim { it <= ' ' }.isEmpty()) {
                makeSnackBar(editTextPhoneNumberProfile, resources.getString(R.string.phone_name_error_msg))
                editTextPhoneNumberProfile!!.requestFocus()
            } else {
                if (InternetConnection.checkConnection(this@ProfileActivity)) {
                    callUplaodProfile()
                } else {
                    makeSnackBar(constraintLayoutNoInternet, resources.getString(R.string.no_internet))
                }
            }
        }

    }

    // For Calling upload Profile
    private fun callUplaodProfile() {
        launchProgressDialog()
        val number: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), editTextPhoneNumberProfile!!.text.toString().trim { it <= ' ' }.replace("-".toRegex(), ""))
        val firstName: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), editTextFirstNameProfile!!.text.toString().trim { it <= ' ' })
        val lastname: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), editTextLastNameProfile!!.text.toString().trim { it <= ' ' })
        val email: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), editTextEmailProfile!!.text.toString().trim { it <= ' ' })
        val weblink: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), editTextWebsiteProfile!!.text.toString().trim { it <= ' ' })
        val instagram: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), editTextInstaProfile!!.text.toString().trim { it <= ' ' })
        val twitter: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), editTextTwitterProfile!!.text.toString().trim { it <= ' ' })
        val faceBook: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), editTextFacebookProfile!!.text.toString().trim { it <= ' ' })
        val linkdin: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), editTextLinkedInProfile!!.text.toString().trim { it <= ' ' })
        val uuid: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), sharedPreferences.uID.trim { it <= ' ' })

        if (media != null) {
            mViewModel.uploadProfile(uuid, firstName, lastname, number, email,weblink,instagram,twitter,faceBook,linkdin, media!!)
        } else {
            mViewModel.uploadProfileWithout(uuid, firstName, lastname, number, email,weblink,instagram,twitter,faceBook,linkdin)
        }

    }

    private fun setUpToolBar() {
        toolbar1.imgClose.click {
            onBackPressed()
        }
        toolbar1.tvTitle.text = resources.getString(R.string.my_profile)
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

    companion object {
        private val RC_CAMERA_GALLERY: Int = 105
        private val CAMERA_CODE: Int = 102
    }

    @SuppressLint("InflateParams")
    private fun openBottomSheetDialog() {
        val chooseImageDialog = BottomSheetDialog(this@ProfileActivity)
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
            val mImageUri: Uri = FileProvider.getUriForFile(this@ProfileActivity,
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

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == RC_CAMERA_GALLERY) {
            openBottomSheetDialog()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        TODO("Not yet implemented")
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 510 && resultCode == Activity.RESULT_OK) {
            data?.apply {
                val path: String = getRealPathFromURI(this.data)
                val file = File(path)
                Glide.with(this@ProfileActivity).load(file.absolutePath).into((imageViewProfileadd)!!)
                val requestBody: RequestBody = RequestBody.create(MediaType.parse("image/*"), file)
                media = MultipartBody.Part.createFormData("profile", file.name, requestBody)
            }

        } else if (requestCode == CAMERA_CODE && resultCode == Activity.RESULT_OK) {
            if (selectedImageFile != null) {
                if (selectedImageFile!!.length() > 0) {
                    Glide.with(this@ProfileActivity).load(selectedImageFile!!.absolutePath).into((imageViewProfileadd)!!)
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
}