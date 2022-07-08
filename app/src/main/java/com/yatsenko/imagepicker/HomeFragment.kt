package com.yatsenko.imagepicker

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yatsenko.picropper.core.piCropperFragmentResultListener
import com.yatsenko.picropper.ui.PiCropperFragment

class HomeFragment: Fragment() {

    private val adapter = MediaAdapter()
    private lateinit var recycler: RecyclerView
    private lateinit var crop: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        piCropperFragmentResultListener(::applyMediaResult)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        crop = view.findViewById(R.id.crop)
        crop.setOnClickListener {
            openPiCropperFragment()
        }

        recycler = view.findViewById(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
    }

    private fun openPiCropperFragment() {
        val args = PiCropperFragment.prepareOptions(
            collectCount = 10,
            forceOpenEditor = true
        )
        findNavController().navigate(R.id.piCropperFragment, args)
    }

    private fun openSettings() {
        
    }

    private fun applyMediaResult(list: List<Uri>) {
        adapter.submitList(list)
    }
}