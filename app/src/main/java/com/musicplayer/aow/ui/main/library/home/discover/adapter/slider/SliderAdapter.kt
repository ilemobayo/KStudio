package com.musicplayer.aow.ui.main.library.home.discover.adapter.slider

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.ViewGroup
import com.musicplayer.aow.R.id.linearLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.os.Build
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.GsonBuilder
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.softcode.adapters.placeholder.PlaceholderData
import com.musicplayer.aow.ui.main.library.home.browse.BrowseActivity
import org.jetbrains.anko.doAsync


class SliderAdapter(private val context: Context, private val itemsList: PlaceholderData?) : PagerAdapter() {

    var items = itemsList?.member.apply {
        val placeholder = PlaceholderData()
        placeholder.name = "BrilaFM"
        placeholder.location = "https://ice31.securenetsystems.net/BRILAMP3"
        placeholder.picture = ""
        placeholder._id = "brilaFm"
        placeholder.picture = ""
        placeholder.owner = "brila"
        placeholder.type = "radio"
        this?.add(placeholder)
    }

    override fun getCount(): Int {
        if (items?.size!! > 10){
            return 15
        }
        return items?.size!!
    }

    override fun isViewFromObject(view: View, nView: Any): Boolean {
        return view === nView
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.slider_item, null)

        val textView = view.findViewById(R.id.textView) as TextView
        val imageView = view.findViewById<ImageView>(R.id.itemImage)
        val linearLayout = view.findViewById(R.id.linearLayout) as LinearLayout

        textView.text = items?.get(position)?.name
        Glide.with(context)
        .load(items?.get(position)?.picture)
        .apply(
                RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .dontTransform()
        )
        .into(imageView)

        //linearLayout.setBackgroundColor(color[position])
        val gsonBuilder = GsonBuilder().create()
        val jsonFromPojo = gsonBuilder.toJson(items?.get(position))
        val intent = Intent(context, BrowseActivity::class.java)
        intent.putExtra("data", jsonFromPojo)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        linearLayout.setOnClickListener {
                ContextCompat.startActivity(context, intent, null)
        }

        val viewPager = container as ViewPager
        viewPager.addView(view, 0)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, nView: Any) {
        val viewPager = container as ViewPager
        val view = nView as View
        viewPager.removeView(view)
    }
}