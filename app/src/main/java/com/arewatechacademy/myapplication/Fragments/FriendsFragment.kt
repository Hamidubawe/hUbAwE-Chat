package com.arewatechacademy.myapplication.Fragments


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arewatechacademy.hubawe.Adapters.FriendsAdapter
import com.arewatechacademy.myapplication.Models.FriendsModel
import com.arewatechacademy.myapplication.Models.UsersModel

import com.arewatechacademy.myapplication.R
import com.arewatechacademy.myapplication.UsersActivity
import com.facebook.shimmer.ShimmerFrameLayout
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * A simple [Fragment] subclass.
 */
class FriendsFragment : Fragment() {
    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var ref: DatabaseReference
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var noFriends: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View =  inflater.inflate(R.layout.fragment_friends, container, false)

        recyclerView = view.findViewById(R.id.friendsRecycler)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        shimmerLayout = view.findViewById(R.id.shimmer_layout)
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()
        noFriends = view.findViewById(R.id.noFriends)

        val  currentUser = FirebaseAuth.getInstance().currentUser!!
        ref = FirebaseDatabase.getInstance().reference.child("Friends")
            .child(currentUser.uid)

        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()){
                    shimmerLayout.visibility = View.GONE
                    shimmerLayout.startShimmer()
                    noFriends.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })

        val options: FirebaseRecyclerOptions<UsersModel?> =
            FirebaseRecyclerOptions.Builder<UsersModel>()
                .setQuery(ref, UsersModel::class.java)
                .build()

        friendsAdapter = FriendsAdapter(options)
        friendsAdapter.notifyDataSetChanged()
        recyclerView.adapter = friendsAdapter
        shimmerLayout.visibility = View.GONE
        shimmerLayout.stopShimmer()
        recyclerView.visibility = View.VISIBLE



        val fabBtn : FloatingActionButton =  view.findViewById(R.id.usersButton)
        fabBtn.setOnClickListener {
            val intent = Intent(context, UsersActivity::class.java)
            startActivity(intent)
        }


        return view

    }

    override fun onStart() {
        super.onStart()
        friendsAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        friendsAdapter.stopListening()
    }


}
