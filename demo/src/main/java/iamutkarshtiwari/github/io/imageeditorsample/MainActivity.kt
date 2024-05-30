package iamutkarshtiwari.github.io.imageeditorsample

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.loader.content.CursorLoader
import iamutkarshtiwari.github.io.ananas.BaseActivity
import iamutkarshtiwari.github.io.ananas.editimage.EditImageActivity
import iamutkarshtiwari.github.io.ananas.editimage.ImageEditorIntentBuilder
import iamutkarshtiwari.github.io.ananas.editimage.utils.BitmapUtils
import iamutkarshtiwari.github.io.imageeditorsample.imagepicker.utils.generateEditFile
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class MainActivity: AppCompatActivity(), View.OnClickListener {
    private var imgView: ImageView? = null
    private var mainBitmap: Bitmap? = null
    private var loadingDialog: Dialog? = null
    private var imageWidth = 0
    private var imageHeight = 0
    private var path: String? = null
    private val compositeDisposable = CompositeDisposable()
    var editResultLauncher: ActivityResultLauncher<Intent>? = null
    var pickResultLauncher: ActivityResultLauncher<Intent>? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupActivityResultLaunchers()
        initView()
    }

    val IMAGE_TYPES = arrayOf(
        "image/jpeg",
        "image/png",
        "image/jpg"
    )

    private val choosePhotosIntent =
        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable()) {
            Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                type = IMAGE_TYPES.first()
                putExtra(Intent.EXTRA_MIME_TYPES, IMAGE_TYPES)
            }
        } else {
            // For older devices running KitKat and higher and devices running Android 12
            // and 13 without the SDK extension that includes the Photo Picker, rely on the
            // ACTION_OPEN_DOCUMENT intent
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = IMAGE_TYPES.first()
                putExtra(Intent.EXTRA_MIME_TYPES, IMAGE_TYPES)
            }
        }

    private fun setupActivityResultLaunchers() {
        pickResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                handleSelectFromAlbum(data)
            }
        }
        editResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                handleEditorImage(data)
            }
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun initView() {
        val metrics = resources.displayMetrics
        imageWidth = metrics.widthPixels
        imageHeight = metrics.heightPixels
        imgView = findViewById(R.id.img)
        val selectAlbum = findViewById<View>(R.id.photo_picker)
        val editImage = findViewById<View>(R.id.edit_image)
        selectAlbum.setOnClickListener(this)
        editImage.setOnClickListener(this)
        loadingDialog = BaseActivity.getLoadingDialog(
            this, R.string.iamutkarshtiwari_github_io_ananas_loading,
            false
        )
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.edit_image -> editImageClick()
            R.id.photo_picker -> selectFromAlbum()
        }
    }

    private fun editImageClick() {
        val outputFile = generateEditFile()
        try {
            val intent = ImageEditorIntentBuilder(this, path!!, outputFile!!.absolutePath)
                .withAddText()
                .withPaintFeature()
                .withFilterFeature()
                .withRotateFeature()
                .withCropFeature()
                .withBrightnessFeature()
                .withSaturationFeature()
                .withBeautyFeature()
                .withStickerFeature()
                .withEditorTitle("Photo Editor")
                .forcePortrait(true)
                .setSupportActionBarVisibility(false)
                .build()
            EditImageActivity.start(editResultLauncher, intent, this)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                R.string.iamutkarshtiwari_github_io_ananas_not_selected,
                Toast.LENGTH_SHORT
            ).show()
            Log.e("Demo App", e.message!!)
        }
    }

    private fun selectFromAlbum() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            openAlbumWithPermissionsCheck()
//        } else {
            openAlbum()
//        }
    }

    private fun openAlbum() {
        pickResultLauncher!!.launch(choosePhotosIntent)
    }

    private fun openAlbumWithPermissionsCheck() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_STORAGE
            )
            return
        }
        openAlbum()
    }

    private fun handleEditorImage(data: Intent?) {
        var newFilePath = data!!.getStringExtra(ImageEditorIntentBuilder.OUTPUT_PATH)
        val isImageEdit = data.getBooleanExtra(EditImageActivity.IS_IMAGE_EDITED, false)
        if (isImageEdit) {
            Toast.makeText(
                this,
                getString(R.string.ananas_image_editor_save_path, newFilePath),
                Toast.LENGTH_LONG
            ).show()
        } else {
            newFilePath = data.getStringExtra(ImageEditorIntentBuilder.SOURCE_PATH)
        }
        loadImage(newFilePath)
    }

    private fun handleSelectFromAlbum(data: Intent?) {
        data?.let {
            path = getRealPathFromURI(it.data!!)
            loadImage(path)
        }
    }

    private fun getRealPathFromURI(contentUri: Uri): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val loader = CursorLoader(this, contentUri, proj, null, null, null)
        val cursor: Cursor = loader.loadInBackground()!!
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val result = cursor.getString(column_index)
        cursor.close()
        return result
    }

    private fun loadImage(imagePath: String?) {
        val applyRotationDisposable = loadBitmapFromFile(imagePath)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { subscriber: Disposable? -> loadingDialog!!.show() }
            .doFinally { loadingDialog!!.dismiss() }
            .subscribe(
                { sourceBitmap: Bitmap -> setMainBitmap(sourceBitmap) }
            ) { e: Throwable ->
                e.printStackTrace()
                Toast.makeText(
                    this, R.string.iamutkarshtiwari_github_io_ananas_load_error, Toast.LENGTH_SHORT
                ).show()
            }
        compositeDisposable.add(applyRotationDisposable)
    }

    private fun setMainBitmap(sourceBitmap: Bitmap) {
        if (mainBitmap != null) {
            mainBitmap!!.recycle()
            mainBitmap = null
            System.gc()
        }
        mainBitmap = sourceBitmap
        imgView!!.setImageBitmap(mainBitmap)
    }

    private fun loadBitmapFromFile(filePath: String?): Single<Bitmap> {
        return Single.fromCallable {
            BitmapUtils.getSampledBitmap(
                filePath,
                imageWidth / 4,
                imageHeight / 4
            )
        }
    }

    companion object {
        const val REQUEST_PERMISSION_STORAGE = 1
        const val ACTION_REQUEST_EDITIMAGE = 9
    }
}