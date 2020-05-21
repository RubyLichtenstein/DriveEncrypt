package com.ruby.driveencrypt.gallery.grid

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ruby.driveencrypt.MainActivity
import com.ruby.driveencrypt.R
import com.ruby.driveencrypt.files.LocalFilesManager
import com.ruby.driveencrypt.files.RemoteFilesManager
import com.ruby.driveencrypt.gallery.GalleryViewModel
import com.ruby.driveencrypt.gallery.grid.selection.MyItemDetailsLookup
import com.ruby.driveencrypt.gallery.grid.selection.MyItemKeyProvider
import com.ruby.driveencrypt.share.shareMultipleImage
import kotlinx.android.synthetic.main.fragment_gallery_grid.*

class GalleryGridFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewGridAdapter: GalleryGridAdapter
    private val SPAN_COUNT = 3
    private var remoteFilesManager: RemoteFilesManager? = null
    lateinit var selectionTracker: SelectionTracker<Long>

    val model: GalleryViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        remoteFilesManager = RemoteFilesManager.create(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showBottomAppBar()
        (activity as MainActivity).pagerMenuItems(false)
    }

    private fun setupViewModel(model: GalleryViewModel) {
        model.showAllLocalFiles(requireActivity())
        model.localFilesLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) {
                empty_state.visibility = View.VISIBLE
            } else {
                empty_state.visibility = View.GONE
            }

            viewGridAdapter.submitList(it)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        postponeEnterTransition()
        return inflater.inflate(R.layout.fragment_gallery_grid, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel(model)
        viewGridAdapter = GalleryGridAdapter()

        viewGridAdapter.onClick = { imageView, item ->
            (activity as MainActivity).openGalleryPager(imageView, item.path)
        }

        val gridLayoutManager = GridLayoutManager(activity, SPAN_COUNT)

        recyclerView = my_recycler_view.apply {
            layoutManager = gridLayoutManager
            adapter = viewGridAdapter
        }

        selectionTracker = SelectionTracker.Builder(
            "mySelection",
            recyclerView,
            MyItemKeyProvider(viewGridAdapter),
            MyItemDetailsLookup(recyclerView),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        selectionTracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                val isItemsSelected = !selectionTracker.selection.isEmpty
                model.isInSelectionModeLiveData.value = isItemsSelected

                multiselect_toolbar.visibility = if (isItemsSelected) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                selected_count.text = selectionTracker.selection.size().toString()
            }
        })

        viewGridAdapter.tracker = selectionTracker

        viewGridAdapter.startPostponedEnterTransition = {
            startPostponedEnterTransition()
        }

        delete_selected.setOnClickListener {
            viewGridAdapter.getSelectedItems()?.forEach {
                LocalFilesManager.deleteLocal(it.path)
            }

            selectionTracker.clearSelection()
            model.showAllLocalFiles(requireActivity())
        }

        share_selected.setOnClickListener {
            val paths = viewGridAdapter.getSelectedItems()
                .orEmpty()
                .map { it.path }

            shareMultipleImage(requireContext(), paths)
        }

        clear_selection.setOnClickListener {
            selectionTracker.clearSelection()
        }

        select_all.setOnClickListener {
            val keys = viewGridAdapter
                .currentList
                .map {
                    it.key()
                }
                .asIterable()

            selectionTracker.setItemsSelected(
                keys,
                true
            )
        }

        prepareTransitions()
    }

    /**
     * Prepares the shared element transition to the pager fragment, as well as the other transitions
     * that affect the flow.
     */
    private fun prepareTransitions() {
        exitTransition = TransitionInflater.from(context)
            .inflateTransition(R.transition.grid_exit_transition)

        // A similar mapping is set at the ImagePagerFragment with a setEnterSharedElementCallback.
        setExitSharedElementCallback(
            object : SharedElementCallback() {
                override fun onMapSharedElements(
                    names: List<String>,
                    sharedElements: MutableMap<String, View>
                ) {
                    // Locate the ViewHolder for the clicked position.
                    val selectedViewHolder = recyclerView
                        .findViewHolderForAdapterPosition(MainActivity.currentPosition)
                        ?: return

                    // Map the first shared element name to the child ImageView.
                    sharedElements[names[0]] =
                        selectedViewHolder.itemView.findViewById(R.id.gallery_image)
                }
            })
    }
}
