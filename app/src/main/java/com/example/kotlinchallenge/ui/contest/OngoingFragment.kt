package com.example.kotlinchallenge.ui.contest

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.kotlinchallenge.R
import com.example.kotlinchallenge.data.db.getDatabase
import com.example.kotlinchallenge.data.network.responses.ArrayDataResponse
import com.example.kotlinchallenge.data.repositories.ContestRepository
import com.example.kotlinchallenge.databinding.FragmentOngoingBinding
import com.example.kotlinchallenge.ui.NetworkListener
import com.example.kotlinchallenge.util.toast
import com.example.kotlinchallenge.util.verifyAvailableNetwork
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.android.synthetic.main.fragment_ongoing.*

class OngoingFragment : Fragment(),NetworkListener {

    val TAG:String = "OngoingFragment"

    private lateinit var viewModel: ContestViewModel
    private lateinit var adapter: ContestRecyclerAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var binding: FragmentOngoingBinding
    @SuppressLint("ResourceType")
    override fun onCreateView (
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG,"Ongoing Fragment")
        binding  = DataBindingUtil.inflate(inflater,R.layout.fragment_ongoing,container,false)
        val db = activity?.let { getDatabase(it) }
        val repository  = db?.let { ContestRepository(it) }
        val modelFactory = repository?.let { ContestViewModelFactory(it) }
        viewModel = ViewModelProviders.of(this,modelFactory).get(ContestViewModel::class.java)
        binding.ongoingvar = viewModel
        binding.lifecycleOwner = this
        binding.shimmerLayout.startShimmer()

        if(activity?.verifyAvailableNetwork(activity as AppCompatActivity) == false){
            binding.listError.visibility = View.VISIBLE
            binding.ongoingRecycler.visibility = View.GONE
            binding.shimmerLayout.visibility = View.GONE

        }else{
            viewModel.callOngoingApi()
            binding.listError.visibility = View.GONE
            binding.ongoingRecycler.visibility = View.VISIBLE
            binding.shimmerLayout.visibility = View.VISIBLE
        }
        //shimmerFrameLayout = activity?.findViewById(R.id.shimmer_layout)!!
        linearLayoutManager = LinearLayoutManager(activity)
        Log.d(TAG,viewModel.liveResult.value?.size.toString())
        activity?.let {
            viewModel.liveResult.observe(it, Observer {
                Log.d(TAG,"Observable " + it.size)
                it.drop(1)
                ongoing_recycler.let {
                    it.layoutManager= linearLayoutManager
                }
                //ongoing_recycler.layoutManager = linearLayoutManager
                adapter = ContestRecyclerAdapter(it,"ongoing")
                ongoing_recycler.adapter = adapter
                val divider = DividerItemDecoration(ongoing_recycler.getContext(), DividerItemDecoration.VERTICAL)
                divider.setDrawable(context?.let { it1 -> ContextCompat.getDrawable(it1, R.layout.custom_divider) }!!)
                ongoing_recycler.addItemDecoration(divider)
            })
        }


        activity?.let {
            viewModel.loading.observe(it, Observer {
                if (it==true){
                    binding.shimmerLayout.visibility - View.VISIBLE
                    //shimmer_layout.visibility = View.VISIBLE
                    binding.ongoingRecycler.visibility = View.GONE
                    binding.shimmerLayout.startShimmer()
                }else{
                    binding.shimmerLayout.visibility = View.GONE
                    binding.ongoingRecycler.visibility = View.VISIBLE
                    binding.shimmerLayout.stopShimmer()
            }
            })
        }

        binding.swipeToRefreshOngoing.setOnRefreshListener {
                binding.swipeToRefreshOngoing.isRefreshing = false
                viewModel.callOngoingApi()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    override fun onStarted() {
        Log.d(TAG,"Network Started")
    }

    override fun onSuccess(response: List<ArrayDataResponse>) {
        activity?.toast("Successfull Network Call")
    }

    override fun onFailure(message: String) {
        activity?.toast(message)
        Log.d(TAG,"Inside Error" + message)
    }
}
