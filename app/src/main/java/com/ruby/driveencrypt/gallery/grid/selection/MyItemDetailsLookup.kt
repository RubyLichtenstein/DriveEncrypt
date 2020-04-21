package com.ruby.driveencrypt.gallery.grid.selection

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.ruby.driveencrypt.gallery.BaseGalleryAdapter.ImageViewHolder

class MyItemDetailsLookup(
    private val recyclerView: RecyclerView
) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)

        return if (view != null) (recyclerView.getChildViewHolder(view) as ImageViewHolder)
            .getItemDetails()
        else
            null
    }
}