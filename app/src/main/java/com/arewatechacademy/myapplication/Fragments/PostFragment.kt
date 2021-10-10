package com.arewatechacademy.myapplication.Fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.arewatechacademy.myapplication.Adapters.PostAdapter
import com.arewatechacademy.myapplication.AddPostActivity
import com.arewatechacademy.myapplication.Models.PostModel
import com.arewatechacademy.myapplication.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_post.view.*

class PostFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    lateinit var postList: List<PostModel>
    private lateinit var reference: DatabaseReference
    private lateinit var postAdapter: PostAdapter
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var linearLayoutManager: LinearLayoutManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_post, container, false)

        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true

        shimmerLayout = view.findViewById(R.id.shimmer_layout)
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = true
            setUpRefreshLayout()

            Handler().postDelayed({
                swipeRefresh.isRefreshing = false
                Toast.makeText(context, "Refreshed", Toast.LENGTH_SHORT).show()
            }, 2000)
        }

        recyclerView = view.findViewById(R.id.postRecycler)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        view.newPostEtx.setOnClickListener {
            val i = Intent(context, AddPostActivity::class.java)
            startActivity(i)
        }


        reference = FirebaseDatabase.getInstance().reference.child("Post")
        postList = ArrayList()

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){

                    for (ds in snapshot.children) {

                        val post: PostModel? = ds.getValue(PostModel::class.java)
                        (postList as ArrayList<PostModel>).add(post!!)

                        if (context != null) {
                            setUpRefreshLayout()
                            shimmerLayout.stopShimmer()
                            shimmerLayout.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE

                        }


                    }

                }else{
                    view.noPost.visibility = View.VISIBLE
                    shimmerLayout.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                }


            }

            override fun onCancelled(error: DatabaseError) {


            }
        })

        return view
    }

    private fun setUpRefreshLayout(){

        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)
        postAdapter = PostAdapter(context!!, postList)
        recyclerView.adapter = postAdapter
        postAdapter.notifyDataSetChanged()

    }

}