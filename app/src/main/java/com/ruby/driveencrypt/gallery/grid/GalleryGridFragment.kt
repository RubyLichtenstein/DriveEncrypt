package com.ruby.driveencrypt.gallery.grid

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.ruby.driveencrypt.R
import com.ruby.driveencrypt.files.FilesManager
import com.ruby.driveencrypt.gallery.GalleryViewModel
import com.ruby.driveencrypt.gallery.grid.selection.MyItemDetailsLookup
import com.ruby.driveencrypt.gallery.grid.selection.MyItemKeyProvider
import com.ruby.driveencrypt.gallery.pager.GalleryPagerActivity
import com.ruby.driveencrypt.share.shareMultipleImage
import kotlinx.android.synthetic.main.fragment_gallery_grid.*

class GalleryGridFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewGridAdapter: GalleryGridAdapter
    private val SPAN_COUNT = 3
    lateinit var filesManager: FilesManager
    lateinit var selectionTracker: SelectionTracker<Long>

    val model: GalleryViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filesManager = FilesManager.create(requireActivity())
        setupViewModel(model)
    }

    private fun setupViewModel(model: GalleryViewModel) {
        model.showAllLocalFiles(requireActivity())
        model.localFilesLiveData.observe(this, Observer {
            if (it.isEmpty()) {
                empty_state.visibility = View.VISIBLE
            } else {
                empty_state.visibility = View.GONE
            }

            viewGridAdapter.addAll(it)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery_grid, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewGridAdapter = GalleryGridAdapter()

        viewGridAdapter.onClick = { view, item ->
            GalleryPagerActivity.startWithTransition(
                requireActivity(),
                item.path,
                view
            )
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
        delete_selected.setOnClickListener {
            viewGridAdapter.getSelectedItems()?.forEach {
                filesManager.deleteLocal(it.path)
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
                .data
                .map {
//                    Log.d("TAG", it.id().toString())
                    it.key()
                }
                .asIterable()

            selectionTracker.setItemsSelected(
                keys,
                true
            )
        }
    }
}
