package com.example.kotlinlibraryapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlinlibraryapp.databinding.ActivityMainBinding

private lateinit var binding: ActivityMainBinding
private lateinit var bookList: ArrayList<Book>
private lateinit var bookAdapter: BookAdapter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookList = ArrayList<Book>()

        bookAdapter= BookAdapter(bookList)
        binding.recyclerView.layoutManager= LinearLayoutManager(this)
        binding.recyclerView.adapter= bookAdapter

        try {
            val database= this.openOrCreateDatabase("Books", MODE_PRIVATE,null)

            val cursor= database.rawQuery("SELECT * FROM books", null)
            val bookNameIx= cursor.getColumnIndex("bookname")
            val idIx= cursor.getColumnIndex("id")

            while (cursor.moveToNext()){
                val name= cursor.getString(bookNameIx)
                val id= cursor.getInt(idIx)
                val book= Book(name,id)
                bookList.add(book)
            }

            bookAdapter.notifyDataSetChanged()

            cursor.close()


        }catch (e:Exception){
            e.printStackTrace()
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.book_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.add_book_item) {
            val intent = Intent(this@MainActivity, BookActivity::class.java)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }


}

