package com.example.myapplication

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    val TAG: String = "MainActivity"
    val url = "https://api.stability.ai/v1/generation/stable-diffusion-xl-1024-v1-0/text-to-image"

    lateinit var button: Button
    lateinit var editText: TextInputEditText
    lateinit var imageView: ImageView

    lateinit var noDataRL: RelativeLayout
    lateinit var dataRL: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button = findViewById(R.id.idTVQuery)
        editText = findViewById(R.id.idEdtQuery)
        imageView = findViewById(R.id.idIVImage)

        noDataRL = findViewById(R.id.idNoDataLayout)
        dataRL = findViewById(R.id.idRLData)

        button.setOnClickListener {
            Log.d("MainActivity", "Button clicked.")
            Log.i(TAG, "Text from editText: ${editText.text.toString()}")
            if (editText.text.toString().isEmpty()) {
                Toast.makeText(this, "Text cannot be empty", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "User input is empty.")
            } else {
                Log.d("MainActivity", "User input is valid. Proceeding with API request.")
                getResponse(editText.text.toString())
            }
        }
    }

    private fun getResponse(prompt: String) {
        Log.d("MainActivity", "API request started with prompt: $prompt")
        noDataRL.visibility = View.GONE
        dataRL.visibility = View.GONE

        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)

        val jsonObject = JSONObject().apply {
            put("steps", 40)
            put("width", 1024)
            put("height", 1024)
            put("seed", 0)
            put("cfg_scale", 5)
            put("samples", 1)
            put("text_prompts", JSONArray().apply {
                put(JSONObject().apply {
                    put("text", prompt)
                    put("weight", 1)
                })
                put(JSONObject().apply {
                    put("text", "blurry, bad")
                    put("weight", -1)
                })
            })
        }

        val postRequest = object : JsonObjectRequest(Method.POST, url, jsonObject,
            Response.Listener { response ->
                Log.d("MainActivity", "Received successful response from API.")
                noDataRL.visibility = View.GONE
                dataRL.visibility = View.VISIBLE
                val imageBase64 = response.optJSONArray("artifacts")?.getJSONObject(0)?.optString("base64")
                if (imageBase64 != null) {
                    Log.d("MainActivity", "Base64 image data found in response.")
                    saveImageToStorage(imageBase64)
                } else {
                    Log.e("MainActivity", "Base64 image data not found in response.")
                    Toast.makeText(applicationContext, "Image not found in response", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Log.e("MainActivity", "Error occurred while fetching image: ${error.message}")
                Toast.makeText(applicationContext, "Failed to generate image: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/json"
                params["Authorization"] = "Bearer sk-XcoKYwfTwxoM7a0IZKLThwvF6JR0bNxwCtrOF1NitfOfIuby"  // Replace with your actual API key
                return params
            }
        }

        postRequest.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000  // Timeout in milliseconds
            }

            override fun getCurrentRetryCount(): Int {
                return 50000  // Retry count
            }

            override fun retry(error: VolleyError?) {
                Log.e("MainActivity", "Error during retry: ${error?.message}")
            }
        }

        queue.add(postRequest)  // Add the request to the queue
    }

    private fun saveImageToStorage(base64Image: String) {
        Log.d("MainActivity", "Starting to save image to storage.")
        try {
            val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
            val file = File(filesDir, "cat_painting.png")
            FileOutputStream(file).use { fos ->
                fos.write(imageBytes)
                Log.d("MainActivity", "Image saved to storage successfully.")
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                Log.d("MainActivity", "Image set to ImageView successfully.")
            }
        } catch (e: IOException) {
            Log.e("MainActivity", "Failed to save image: ${e.message}")
            Toast.makeText(applicationContext, "Failed to save image.", Toast.LENGTH_SHORT).show()
        }
    }
}
