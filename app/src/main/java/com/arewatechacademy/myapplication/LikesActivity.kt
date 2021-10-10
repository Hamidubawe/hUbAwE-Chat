package com.arewatechacademy.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.arewatechacademy.myapplication.Fragments.DislikeFragment
import com.arewatechacademy.myapplication.Fragments.LikesFragment
import com.google.android.material.tabs.TabLayout
import java.util.ArrayList

class LikesActivity : AppCompatActivity() {

    private var viewPager: ViewPager? = null
    private var tabLayout: TabLayout? = null
    companion object{
        lateinit var postId : String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_likes)

        tabLayout = findViewById(R.id.channelTabLayout)
        viewPager = findViewById(R.id.viewPager)

        postId = intent.getStringExtra("postId")!!

        //initializing viewpager
        val viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager
        )
        viewPagerAdapter.addFragments(LikesFragment(), "Likes")
        viewPagerAdapter.addFragments(DislikeFragment(), "Dislikes")

        viewPager!!.adapter = viewPagerAdapter
        tabLayout!!.setupWithViewPager(viewPager)

    }

    class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private val fragments: ArrayList<Fragment> = ArrayList()
        private val title: ArrayList<String> = ArrayList()
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragments(fragment: Fragment, titles: String) {
            fragments.add(fragment)
            title.add(titles)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return title[position]
        }

    }
}