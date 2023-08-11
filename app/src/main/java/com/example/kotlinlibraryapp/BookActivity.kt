package com.example.kotlinlibraryapp

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kotlinlibraryapp.databinding.ActivityBookBinding
import com.google.android.material.snackbar.Snackbar

private lateinit var binding: ActivityBookBinding
private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
private lateinit var permissionLauncher: ActivityResultLauncher<String>
var selectedBitmap: Bitmap? = null


class BookActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        registerLauncher()


    }

    fun imageViewOnClick(view: View) {

        //Android 33 ve üzerinde ise READ_MEDIA_IMAGES şeklinde izin istemeliyiz
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {


            println("deneme1")


            // Bu if koşulu kullanıcı galeri erişim izni vermediği durumlarda çalışır
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {

                println("deneme2")

                // Bu if koşulu kullanıcı galeriye erişim iznini en az bir kez reddettiğinde çalışır
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    //rationable

                    println("deneme3")

                    Snackbar.make(view, "Permission needed for galery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {
                        //request permission
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }).show()
                }
                //eğer daha önce galeri erişim izni reddedilmediği ancak izin de verilmediği durumlarda izin istemek için kullanılır
                else {

                    println("deneme4")

                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            //galeri izni verildiğinde çalıştırılacak komutlar
            else {
                //kullanıcının galerisine ulaşmak için intent yapıyoruz
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }


        } else {
            //Android SDK 32 ya da altında bir sürüm ile çalışıyorsak READ_EXTERNAL_STORAGE şeklinde izin istemeliyiz

            // Bu if koşulu kullanıcı galeri erişim izni vermediği durumlarda çalışır
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Bu if koşulu kullanıcı galeriye erişim iznini en az bir kez reddettiğinde çalışır
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //rationable
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission", View.OnClickListener {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                }
                //eğer daha önce galeri erişim izni reddedilmediği ancak izin de verilmediği durumlarda izin istemek için kullanılır
                else {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            //galeri izni verildiğinde çalıştırılacak komutlar
            else {
                //kullanıcının galerisine ulaşmak için intent yapıyoruz
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }

        }


    }

    fun SaveButtonOnClick(view: View) {

    }




    private fun registerLauncher() {

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    val imageData = intentFromResult.data
                    if (imageData != null) {
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(this@BookActivity.contentResolver, imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imgVSelectImage.setImageBitmap(selectedBitmap)
                            } else {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageData)
                                binding.imgVSelectImage.setImageBitmap(selectedBitmap)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                }

            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->

            println("deneme5")

            if (result) {

                println("deneme6")

                //permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {

                println("deneme7")

                //permission denied
                Toast.makeText(this@BookActivity, "Permission needed!", Toast.LENGTH_LONG).show()
            }
        }


    }
}