package com.fredcodecrafts.lab_week_05

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.fredcodecrafts.lab_week_05.api.CatApiService
import com.fredcodecrafts.lab_week_05.model.ImageData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    companion object {
        private const val MAIN_ACTIVITY = "MainActivity"
    }

    // Retrofit instance with Moshi
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/v1/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    // API service
    private val catApiService by lazy {
        retrofit.create(CatApiService::class.java)
    }

    // Views
    private val apiResponseView: TextView by lazy {
        findViewById(R.id.api_response)
    }

    private val imageResultView: ImageView by lazy {
        findViewById(R.id.image_result)
    }

    // Glide loader
    private val imageLoader: ImageLoader by lazy {
        GlideLoader(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Trigger API request
        getCatImageResponse()
    }
    private fun getCatImageResponse() {
        val call = catApiService.searchImages(1, "full")
        call.enqueue(object : Callback<List<ImageData>> {
            override fun onFailure(call: Call<List<ImageData>>, t: Throwable) {
                Log.e(MAIN_ACTIVITY, "Failed to get response", t)
            }

            override fun onResponse(
                call: Call<List<ImageData>>,
                response: Response<List<ImageData>>
            ) {
                if (response.isSuccessful) {
                    val images = response.body()

                    // First, check if images is null or empty
                    if (!images.isNullOrEmpty()) {
                        val firstImage = images[0] // safe because list is not empty

                        // Load image safely
                        imageLoader.loadImage(firstImage.imageUrl, imageResultView)

                        // Safely get breed name or fallback to "Unknown"
                        val breedName = firstImage.breeds?.firstOrNull()?.name ?: "Unknown"
                        apiResponseView.text = "Breed: $breedName"

                    } else {
                        Log.d(MAIN_ACTIVITY, "Response body is null or empty")
                        apiResponseView.text = "Breed: Unknown"
                    }

                } else {
                    Log.e(
                        MAIN_ACTIVITY,
                        "Failed to get response\n${response.errorBody()?.string().orEmpty()}"
                    )
                }
            }
        })
    }


}
