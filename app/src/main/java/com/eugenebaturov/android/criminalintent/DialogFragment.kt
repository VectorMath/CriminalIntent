package com.eugenebaturov.android.criminalintent

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import java.io.File


class DialogFragment : Fragment() {
    private lateinit var photoView: ImageView
    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        photoUri = arguments?.getParcelable("crime_photo")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dialog, container, false)

        photoView = view.findViewById(R.id.crime_photo)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoView.setImageURI(photoUri)
    }

    companion object {
        fun newInstance(crimeUri: Uri): DialogFragment {
            val args = Bundle().apply {
                putParcelable("crime_photo", crimeUri)
            }
            return DialogFragment().apply {
                arguments = args
            }
        }
    }
}