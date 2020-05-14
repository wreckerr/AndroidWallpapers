package com.my.androidwallpapers

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.my.androidwallpapers.R
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(), (WallpapersModel) -> Unit {

    private val firebaseRepository = FirebaseRepository()
    private var navController: NavController? = null


    private var wallpapersList: List<WallpapersModel> = ArrayList()
    private val wallpapersListAdapter: WallpapersListAdapter = WallpapersListAdapter(wallpapersList, this)

    private var isLoading: Boolean = true

    private val wallpapersViewModel: WallpapersViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Initialize and Setup Action Bar
        (activity as AppCompatActivity).setSupportActionBar(main_toolbar)

        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar!!.title = "Wallpapers"

        //Initialize Nav Controller
        navController = Navigation.findNavController(view)

        //Check if User is logged in
        if(firebaseRepository.getUser() == null){
            //User Not Logged in, Go to Register Page
            navController!!.navigate(R.id.action_homeFragment_to_registerFragment)
        }

        //Initialize recycler view
        wallpapers_list_view.setHasFixedSize(true)
        wallpapers_list_view.layoutManager = GridLayoutManager(context, 3)
        wallpapers_list_view.adapter = wallpapersListAdapter

        //Reached Bottom of RecyclerView
        wallpapers_list_view.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE){
                    //Reached at bottom and not scrolling anymore
                    if(!isLoading){
                        //Load Next Page
                        wallpapersViewModel.loadWallpapersData()
                        isLoading = true

                        Log.d("HOME_FRAGMENT_LOG", "Reached Bottom: loading new content")
                    }
                }
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        wallpapersViewModel.getWallpapersList().observe(viewLifecycleOwner, Observer {
            wallpapersList = it
            wallpapersListAdapter.wallpapersList = wallpapersList
            wallpapersListAdapter.notifyDataSetChanged()

            //Loading Complete
            isLoading = false
        })
    }

    override fun invoke(wallpaper: WallpapersModel) {
        //Clicked on wallpaper Item from the list, Navigate to details fragment
        Log.d("HOME_FRAMGNE_LOG", "Clicked on Item : ${wallpaper.name}")

        val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(wallpaper.image)
        navController!!.navigate(action)
    }
}