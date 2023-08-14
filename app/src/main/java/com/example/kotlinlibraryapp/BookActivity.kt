package com.example.kotlinlibraryapp

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.Manifest
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.ByteArrayOutputStream

private lateinit var binding: ActivityBookBinding
private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
private lateinit var permissionLauncher: ActivityResultLauncher<String>
var selectedBitmap: Bitmap? = null
private lateinit var database : SQLiteDatabase


class BookActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = this.openOrCreateDatabase("Books", Context.MODE_PRIVATE,null)

        registerLauncher()

        val intent= intent
        val info= intent.getStringExtra("info")

        if (info.equals("new")){
            binding.btnSave.visibility=View.VISIBLE
            binding.edtTxtBookName.setText("")
            binding.edtTxtAuthorName.setText("")
            binding.editTextMultiLineTopic.setText("")
            binding.imgVSelectImage.setImageResource(R.drawable.selectimage)

        }else{
            binding.btnSave.visibility= View.INVISIBLE
            val selectedId= intent.getIntExtra("id", 1)

            val cursor = database.rawQuery("SELECT * FROM books WHERE id = ?", arrayOf(selectedId.toString()) )

            val booknameIx = cursor.getColumnIndex("bookname")
            val authorIx = cursor.getColumnIndex("author")
            val topicIx = cursor.getColumnIndex("topic")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                binding.edtTxtBookName.setText(cursor.getString(booknameIx))
                binding.edtTxtAuthorName.setText(cursor.getString(authorIx))
                binding.editTextMultiLineTopic.setText(cursor.getString(topicIx))

                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imgVSelectImage.setImageBitmap(bitmap)
            }

            cursor.close()

        }


    }

    fun imageViewOnClick(view: View) {

        //Android 33 ve üzerinde ise READ_MEDIA_IMAGES şeklinde izin istemeliyiz
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Bu if koşulu kullanıcı galeri erişim izni vermediği durumlarda çalışır
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                // Bu if koşulu kullanıcı galeriye erişim iznini en az bir kez reddettiğinde çalışır
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    //rationable
                    Snackbar.make(view, "Permission needed for galery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {
                        //request permission
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }).show()
                }
                //eğer daha önce galeri erişim izni reddedilmediği ancak izin de verilmediği durumlarda izin istemek için kullanılır
                else {
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

    fun saveButtonOnClick(view: View) {

        val bookName= binding.edtTxtBookName.text.toString()
        val authorName= binding.edtTxtAuthorName.text.toString()
        val topic= binding.editTextMultiLineTopic.text.toString()

        if (selectedBitmap!=null){

            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,150)

            //görseli kayıt için byte dizisine dönüştürüyoruz
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                database.execSQL("CREATE TABLE IF NOT EXISTS books (id INTEGER PRIMARY KEY, bookname VARCHAR, author VARCHAR, topic VARCHAR, image BLOB)")

                val sqlString= "INSERT INTO books (bookname, author, topic, image) VALUES (?, ?, ?, ?)"
                val statement= database.compileStatement(sqlString)
                statement.bindString(1,bookName)
                statement.bindString(2,authorName)
                statement.bindString(3,topic)
                statement.bindBlob(4,byteArray)
                statement.execute()

            }catch(e : Exception){
                e.printStackTrace()
            }

            val intent = Intent(this@BookActivity, MainActivity::class.java)
            //aşağıdaki komut arkaplanda çalışan ne kadar aktivite varsa kapatarak intent yapacağımız aktiviteye gitmemizi sağlar
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

        }

    }

    //Veri tabanına kaydedilecek görselin boyutunu küçültmek için oluşturuldu.
    private fun makeSmallerBitmap(image:Bitmap, maxSize:Int) : Bitmap{

        var width = image.width
        var height = image.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()
        if (bitmapRatio > 1) {
            width = maxSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        } else {
            height = maxSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image,width,height,true)
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

            if (result) {
                //permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                //permission denied
                Toast.makeText(this@BookActivity, "Permission needed!", Toast.LENGTH_LONG).show()
            }
        }


    }
}