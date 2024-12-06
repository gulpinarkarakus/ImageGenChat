package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.android.volley.VolleyError
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    var url = "https://api.openai.com/v1/image/generations"
    lateinit var queryTV: TextView
    lateinit var imageView: ImageView
    lateinit var queryEdt: TextInputEditText  // TextInputEditText türünde olmalı
    lateinit var loadingPB: ProgressBar
    lateinit var noDataRL: RelativeLayout
    lateinit var dataRL: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        queryTV = findViewById(R.id.idTVQuery)
        imageView = findViewById(R.id.idIVImage)

        // TextInputEditText ile erişim yapılmalı
        queryEdt = findViewById(R.id.idEdtQuery) // .editText ile TextInputEditText'e erişim

        loadingPB = findViewById(R.id.idPBLoading)
        noDataRL = findViewById(R.id.idNoDataLayout)
        dataRL = findViewById(R.id.idRLData)

        queryEdt.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEND) {
                if (queryEdt.text.toString().isNotEmpty()) {
                    queryTV.text = queryEdt.text.toString()
                    getResponse(queryEdt.text.toString())
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Please enter your query.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun getResponse(query: String) {
        queryEdt.setText("")  // Clear the input field
        loadingPB.visibility = View.VISIBLE  // Show the loading progress bar
        noDataRL.visibility = View.GONE  // Hide the no data layout
        dataRL.visibility = View.GONE  // Hide the data layout initially

        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        val jsonObject = JSONObject().apply {
            put("prompt", query)
            put("n", 1)
            put("size", "256x256")
        }

        // POST request to generate image from API
        val postRequest = object : JsonObjectRequest(
            Method.POST, url, jsonObject,
            Response.Listener { response ->
                // Handle successful response here
                noDataRL.visibility = View.GONE  // Hide the no data layout
                dataRL.visibility = View.VISIBLE  // Show the data layout
                // Example: Handle the image response here (e.g., imageView.setImageURI(response))
            },
            Response.ErrorListener { error ->
                // Handle error
                Toast.makeText(applicationContext, "Failed to generate image.", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/json"
                params["Authorization"] = "Bearer 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"  // Replace with your actual API key
                return params
            }
        }

        // Set retry policy for the request
        postRequest.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000  // Timeout in milliseconds
            }

            override fun getCurrentRetryCount(): Int {
                return 50000  // Retry count
            }

            override fun retry(error: VolleyError?) {
                // You can implement custom retry behavior here if needed
            }
        }

        queue.add(postRequest)  // Add the request to the queue
    }
}
