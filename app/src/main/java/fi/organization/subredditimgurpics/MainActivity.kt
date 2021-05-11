package fi.organization.subredditimgurpics

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.net.URL
import kotlin.concurrent.thread


@JsonIgnoreProperties(ignoreUnknown = true)
data class Content(var link: String? = null, var type: String? = null, var in_gallery: Boolean? = false, var title: String? = null, val height: Int? = null, val width: Int? = null)


@JsonIgnoreProperties(ignoreUnknown = true)
data class Response(var data: MutableList<Content>? = null)


class MainActivity : AppCompatActivity() {
    lateinit var searchText: EditText
    lateinit var searchButton: Button
    lateinit var searchRandom: Button
    lateinit var container: LinearLayout
    lateinit var scrollingView: ScrollView
    lateinit var notFound: TextView
    lateinit var spinner: Spinner
    var containerHeight = 200
    var customTitle = "Imgur content from subreddit"
    val itemsToShow = 50
    val clientID = "5ab4b9f856bbf8c"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.searchText = findViewById(R.id.searchText)
        this.container = findViewById(R.id.container)
        this.scrollingView = findViewById(R.id.scrollingView)
        this.searchRandom = findViewById(R.id.searchRandom)
        this.searchButton = findViewById(R.id.searchButton)
        this.notFound = findViewById(R.id.notFound)
        this.spinner = findViewById(R.id.spinner1)
        spinner.prompt = "How many items to show"
        title = customTitle
        containerHeight = container.height - 200
    }

    // Called when pressing Go button. Search for images in a subreddit based on user input in the edit text field
    @RequiresApi(Build.VERSION_CODES.R)
    fun getInput(view: View) {
        val input = this.searchText.text.toString()
        getImages(view, input)
        buttonsEnabled(false)
    }

    // Called when pressing "Random" button. Search for images in a random subreddit given by randomReddit function
    @RequiresApi(Build.VERSION_CODES.R)
    fun getRandom(view: View) {
        getImages(view, randomReddit())
        buttonsEnabled(false)
    }

    // Initiates asynchronous httpconnection, jsonparsing and ui update
    @RequiresApi(Build.VERSION_CODES.R)
    fun getImages(view: View, inputText: String) {
        clear()
        customTitle = "/r/$inputText"
        downloadUrlAsync(this, inputText) {
            var contentList = parseJson(it)
            updateUI(contentList) { it1 ->
                buttonsEnabled(it1)
            }
        }
    }

    fun parseJson(string: String?): MutableList<Content>? {
        val mp = ObjectMapper()
        val myObject: Response = mp.readValue(string, Response::class.java)
        if (myObject.data?.size!! >= 1) {
            redditFound(true)
        } else {
            redditFound(false)
        }
        return myObject.data?.filter { it.in_gallery == false }?.take(itemsToShow)?.toMutableList()
    }

    fun httpConnection(inputText: String): String? {
        val client = OkHttpClient()
        val url = URL("https://api.imgur.com/3/gallery/r/$inputText")
        val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Client-ID $clientID")
                .get()
                .build()
        val response = client.newCall(request).execute()
        return response.body?.string()
    }

    private fun downloadUrlAsync(activity: Activity, s: String, callback: (result: String?) -> Unit) {
        thread() {
            var result = httpConnection(s)
            runOnUiThread {
                callback(result)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun updateUI(contentList: MutableList<Content>?, callback: (result: Boolean) -> Unit) {
        var done = 0
        Log.d("TEST", "$done")
        Log.d("TEST", "${contentList?.size}")
        contentList?.forEach {
            val url = it.link
            val type = it.type
            val title = it.title
            val height = it.height
            val width = it.width

            if (url != null && type?.contains("image") == true) {
                thread() {
                    var textView = TextView(this)
                    var imageView = ImageView(this)
                    textViewParameters(textView, title)
                    imageViewParameters(imageView, url)
                    if (type.contains("gif")) {
                        runOnUiThread {
                            Glide.with(this).load(url).into(imageView)
                        }
                    } else {
                        var myBitmap: Bitmap? = null
                        myBitmap?.recycle()
                        val input: InputStream = URL(url).openStream()
                        if (height != null && width != null) {
                            if (height > 5000 || width > 5000) {
                                val o = BitmapFactory.Options()
                                o.inSampleSize = 14
                                myBitmap = BitmapFactory.decodeStream(input, null, o)
                                imageView.setImageBitmap(myBitmap)
                            }
                            else if (height > 4000 || width > 4000) {
                                val o = BitmapFactory.Options()
                                o.inSampleSize = 8
                                myBitmap = BitmapFactory.decodeStream(input, null, o)
                                imageView.setImageBitmap(myBitmap)
                            }
                            else if (height > 3000 || width > 3000) {
                                val o = BitmapFactory.Options()
                                o.inSampleSize = 3
                                myBitmap = BitmapFactory.decodeStream(input, null, o)
                                imageView.setImageBitmap(myBitmap)
                            }
                            else if (height > 2000 || width > 2000) {
                                val o = BitmapFactory.Options()
                                o.inSampleSize = 2
                                myBitmap = BitmapFactory.decodeStream(input, null, o)
                                imageView.setImageBitmap(myBitmap)

                            } else {
                                myBitmap = BitmapFactory.decodeStream(input)
                                imageView.setImageBitmap(myBitmap)
                            }
                        }
                    }
                    runOnUiThread {
                        this.container.addView(textView)
                        this.container.addView(imageView)
                        done++
                        if(done == contentList.size) {
                            callback(true)
                        }
                    }
                }
            }

            if (url != null && type?.contains("video") == true) {
                var videoView = VideoView(this)
                val textView = TextView(this)
                textViewParameters(textView)
                videoView.setVideoURI(Uri.parse(url))
                videoView.setOnPreparedListener { video ->
                    video.setVolume(0.0F, 0.0F)
                    video.start()
                }
                videoView.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                this.container.addView(textView)
                this.container.addView(videoView)
                done++
                if(done == contentList.size) {
                    callback(true)
                }
            }
        }
        scrollingView.fullScroll((ScrollView.FOCUS_UP))
    }


    fun randomReddit(): String {
        val randomReddit = listOf("javascript", "java", "machinelearning", "howtohack", "dogs", "cats", "birds", "corgi")
        return randomReddit.shuffled().take(1)[0]
    }

    fun buttonsEnabled(status: Boolean) {
        searchButton.isEnabled = status
        searchRandom.isEnabled = status
    }

    fun clear() {
        container.removeAllViewsInLayout()
        container.removeAllViews()
    }

    fun redditFound(status: Boolean) {
        if(status) {
            title = customTitle
            notFound.visibility = View.INVISIBLE
        } else {
            notFound.visibility = View.VISIBLE
            title = "Imgur content from subreddit"
            buttonsEnabled(true)
        }
        searchText.setText("")
    }

    fun textViewParameters(textView: TextView, title: String? = null) {
        textView.typeface = Typeface.DEFAULT_BOLD;
        textView.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        textView.gravity = 1
        textView.setTextColor(Color.parseColor("#FFFFFF"))
        textView.text = title
    }

    fun imageViewParameters(imageView: ImageView, url : String) {
        imageView.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        (imageView.layoutParams as LinearLayout.LayoutParams).setMargins(0, 20, 0, 200)
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE

        imageView.adjustViewBounds = true
        imageView.isClickable = true
        imageView.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}