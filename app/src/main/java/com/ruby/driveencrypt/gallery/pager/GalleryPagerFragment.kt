package com.ruby.driveencrypt.gallery.pager

import android.content.Intent
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ruby.driveencrypt.MainActivity
import com.ruby.driveencrypt.R
import com.ruby.driveencrypt.drive.DriveService
import com.ruby.driveencrypt.files.LocalFilesManager
import com.ruby.driveencrypt.files.RemoteFilesManager
import com.ruby.driveencrypt.gallery.GalleryItem
import com.ruby.driveencrypt.gallery.GalleryViewModel
import com.ruby.driveencrypt.share.shareImage
import kotlinx.android.synthetic.main.activity_gallery_pager.*

class GalleryPagerFragment : Fragment() {

    private var driveService: DriveService? = null
    var isSystemUiShowed = true
    private val model: GalleryViewModel by activityViewModels()
    lateinit var imagesPagerAdapter: GalleryPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Avoid a postponeEnterTransition on orientation change, and postpone only of first creation.
        if (savedInstanceState == null) {
            postponeEnterTransition()
        }

        return inflater.inflate(R.layout.activity_gallery_pager, container, false)
    }


    /**
     * Prepares the shared element transition from and back to the grid fragment.
     */
    private fun prepareSharedElementTransition() {
        val transition =
            TransitionInflater.from(context)
                .inflateTransition(R.transition.image_shared_element_transition)
        sharedElementEnterTransition = transition

        // A similar mapping is set at the GridFragment with a setExitSharedElementCallback.
//        setEnterSharedElementCallback(
//            object : SharedElementCallback() {
//                override fun onMapSharedElements(
//                    names: List<String>,
//                    sharedElements: MutableMap<String, View>
//                ) {
//                    // Locate the image view at the primary fragment (the ImageFragment that is currently
//                    // visible). To locate the fragment, call instantiateItem with the selection position.
//                    // At this stage, the method will simply return the fragment at the position and will
//                    // not create a new one.
//                    val currentItem = image_pager.currentItem
////                    imagesPagerAdapter.ite
//                    val currentFragment = currentItem
//                        .instantiateItem(
//                            image_pager,
//                            MainActivity.currentPosition
//                        ) as Fragment
//                    val view = currentFragment.view ?: return
//
//                    // Map the first shared element name to the child ImageView.
//                    sharedElements[names[0]] = view.findViewById(R.id.image)
//                }
//            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val filesManager = RemoteFilesManager.create(requireContext())
        driveService = DriveService.getDriveService(requireContext())

        val path = arguments?.getString(ARG_IMAGE_PATH)

        imagesPagerAdapter = GalleryPagerAdapter()
        setupBottomNavigation()
        imagesPagerAdapter.onTap = { view, galleryItem ->
            if (isSystemUiShowed) {
                (activity as MainActivity).hideBottomAppBar()
//                hideSystemUI()
//                bottom_navigation.visibility = View.GONE
//                gallery_pager_toolbar.visibility = View.GONE
            } else {
//                showSystemUI()
                (activity as MainActivity).showBottomAppBar()

//                bottom_navigation.visibility = View.VISIBLE
//                gallery_pager_toolbar.visibility = View.VISIBLE
            }
            isSystemUiShowed = !isSystemUiShowed
        }

        imagesPagerAdapter.onTapVideo = { view, uri ->
            val intent = Intent(requireActivity(), VideoActivity::class.java)
            intent.putExtra(VideoActivity.ARG_URI, uri)
            startActivity(intent)
        }

        imagesPagerAdapter.startPostponedEnterTransition = {
            startPostponedEnterTransition()
        }

        image_pager.adapter = imagesPagerAdapter

        model.localFilesLiveData.observe(viewLifecycleOwner, Observer {
            imagesPagerAdapter.submitList(it)
            val index = it.indexOfFirst { it.path == path }
            image_pager.setCurrentItem(index, false)
        })

        prepareSharedElementTransition()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = bottom_navigation as BottomNavigationView

        if (driveService == null) {
            bottomNavigationView.menu.removeItem(R.id.pager_upload);
        }

        bottomNavigationView.setOnNavigationItemReselectedListener {
            when (it.itemId) {
                R.id.pager_share -> {
                    val item = getCurrentGalleryItem()
                    shareImage(requireContext(), item.path)
                }

                R.id.pager_upload -> {
//                    upload(filesManager, path)
                }

                R.id.pager_delete -> {
                    val item = getCurrentGalleryItem()
                    LocalFilesManager.deleteLocal(item.path)
                    model.showAllLocalFiles(requireContext())
                }
            }
        }
    }

     fun getCurrentGalleryItem(): GalleryItem {
        val index = image_pager.currentItem
        return imagesPagerAdapter.currentList[index]
    }

    private fun upload(remoteFilesManager: RemoteFilesManager, path: String) {
//        Toast
//            .makeText(this, "start upload...", Toast.LENGTH_SHORT)
//            .show()
//
//        progress.visibility = View.VISIBLE
//        filesManager
//            .uploadFile(path)
//            ?.addOnSuccessListener {
//                progress.visibility = View.GONE
//
//                Toast
//                    .makeText(this, "file uploaded", Toast.LENGTH_SHORT)
//                    .show()
//            }
    }

    companion object {
        const val ARG_IMAGE_PATH = "image_path"

        fun newInstance(path: String) =
            GalleryPagerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_IMAGE_PATH, path)
                }
            }
    }
}
