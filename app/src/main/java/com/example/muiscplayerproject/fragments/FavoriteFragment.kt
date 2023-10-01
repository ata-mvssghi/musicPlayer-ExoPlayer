package com.example.muiscplayerproject.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.muiscplayerproject.R
import com.example.muiscplayerproject.databinding.FragmentFavoriteBinding
import com.example.muiscplayerproject.databinding.FragmentPreviewBinding

class FavoriteFragment : Fragment() {
    lateinit var binding:FragmentFavoriteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteBinding.inflate(inflater)
        val navigationView=binding.bottomNavigationView2
        navigationView.selectedItemId=R.id.favorites
        return (binding.root)
    }
}