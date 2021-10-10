package com.arewatechacademy.myapplication.Fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arewatechacademy.hubawe.Adapters.RequestAdapter
import com.arewatechacademy.myapplication.Models.FriendsModel
import com.arewatechacademy.myapplication.Models.UsersModel

import com.arewatechacademy.myapplication.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * A simple [Fragment] subclass.
 */
class RequestsFragment : Fragment() {
    private lateinit var requestAdapter: RequestAdapter
    private var mUsers : List<UsersModel>? = null
    private lateinit var mFriendsList : List<FriendsModel>
    private lateinit var recyclerView: RecyclerView

    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var noRequests: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_requests, container, false)

        recyclerView = view.findViewById(R.id.requestRecycler)
        recyclerView.layoutManager = LinearLayoutManager(context)

        shimmerLayout = view.findViewById(R.id.shimmer_layout)
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        noRequests = view.findViewById(R.id.noRequests)

        mUsers = ArrayList()
        mFriendsList = ArrayList()

        val  firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val ref: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Friend_Requests")
            .child("received_request").child(firebaseUser.uid)

        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()){
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    noRequests.visibility = View.VISIBLE
                }

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })

        val options: FirebaseRecyclerOptions<UsersModel?> =
            FirebaseRecyclerOptions.Builder<UsersModel>()
                .setQuery(ref, UsersModel::class.java)
                .build()

        requestAdapter = RequestAdapter(options)
        recyclerView.adapter = requestAdapter
        requestAdapter.notifyDataSetChanged()
        shimmerLayout.stopShimmer()
        shimmerLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE


        return view
    }

    override fun onStart() {
        super.onStart()
        requestAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        requestAdapter.stopListening()
    }

}
