package com.example.kotlinlibraryapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.kotlinlibraryapp.databinding.ActivityBookBinding

private lateinit var binding: ActivityBookBinding

class BookActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


    }

    fun imageViewOnClick(view : View){

    }

    fun SaveButtonOnClick(view : View){

    }
}