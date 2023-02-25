package com.thinkers.whiteboard.wastebin

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thinkers.whiteboard.R

class WasteBinFragment : Fragment() {

    companion object {
        fun newInstance() = WasteBinFragment()
    }

    private lateinit var viewModel: WasteBinViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_waste_bin, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(WasteBinViewModel::class.java)
        // TODO: Use the ViewModel
    }

}