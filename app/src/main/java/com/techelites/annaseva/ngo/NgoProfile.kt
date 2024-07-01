package com.techelites.annaseva.ngo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
    import android.view.ViewGroup
    import android.widget.Button
    import android.widget.ImageButton
    import android.widget.ProgressBar
    import android.widget.TextView
    import com.techelites.annaseva.R
    import com.techelites.annaseva.auth.Start

    class NgoProfile : Fragment() {
        private lateinit var userName: TextView
        private lateinit var email: TextView
        private lateinit var logout : Button
        private lateinit var edit : Button
        private lateinit var progressBar: ProgressBar
        private lateinit var setting : ImageButton
        //for recycler
        private lateinit var  userId : String



        @SuppressLint("MissingInflatedId")
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.fragment_ngo_profile, container, false)
            edit = view.findViewById(R.id.editBtn)
            userName = view.findViewById(R.id.usernameP)
            email = view.findViewById(R.id.emailP)
            logout= view.findViewById(R.id.logoutbtn)
            progressBar = view.findViewById(R.id.progessId)
            progressBar.visibility = View.INVISIBLE
            setting = view.findViewById(R.id.settings)

            // Access SharedPreferences using the context of the Fragment
            val pref: SharedPreferences = requireActivity().getSharedPreferences("login",
                Context.MODE_PRIVATE
            )
            val savedUserName = pref.getString("username", "")
            val savedEmail = pref.getString("email", "")
            userId = pref.getString("userid","").toString()
            // Display the saved data in TextViews
            userName.text = savedUserName
            email.text = savedEmail




//        setting.setOnClickListener(View.OnClickListener {
//            val intentStart = Intent(requireContext(),Contact::class.java)
//            startActivity(intentStart)
//        })


            logout.setOnClickListener(View.OnClickListener {
                progressBar.visibility = View.VISIBLE
                val handler = Handler(Looper.getMainLooper())
                val runnable = Runnable{
                    val editor : SharedPreferences.Editor = pref.edit()
                    editor.putBoolean("flag",false)
                    editor.apply()
                    progressBar.visibility = View.GONE
                    val intentStart = Intent(requireContext(), Start::class.java)
                    startActivity(intentStart)

                }
                handler.postDelayed(runnable,2000)

            })
            edit.setOnClickListener(View.OnClickListener {

            })

            return view
        }


    }
