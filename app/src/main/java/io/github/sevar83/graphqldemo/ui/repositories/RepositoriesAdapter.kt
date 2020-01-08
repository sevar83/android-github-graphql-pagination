package io.github.sevar83.graphqldemo.ui.repositories

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import io.github.sevar83.graphqldemo.fragment.RepositoryFragment

class RepositoriesAdapter(private val onClick: ((RepositoryFragment) -> Unit)?) :
    PagedListAdapter<RepositoryFragment, RepositoryViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryViewHolder {
        return RepositoryViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RepositoryViewHolder, position: Int) {
        return holder.bind(getItem(position), onClick)
    }

    companion object {
        /**
         * This diff callback informs the [PagedListAdapter] how to compute list differences when new
         * PagedLists arrive.
         *
         * When you add a [RepositoryFragment], the [PagedListAdapter] uses diffCallback to
         * detect there's only a single item difference from before, so it only needs to animate and
         * rebind a single view.
         *
         * @see [DiffUtil]
         */
        private val diffCallback = object : DiffUtil.ItemCallback<RepositoryFragment>() {
            override fun areItemsTheSame(oldItem: RepositoryFragment, newItem: RepositoryFragment): Boolean =
                oldItem.id == newItem.id

            /**
             * Note that in kotlin, == checking on data classes compares all contents, but in Java,
             * typically you'll implement Object#equals, and use it to compare object contents.
             */
            override fun areContentsTheSame(oldItem: RepositoryFragment, newItem: RepositoryFragment): Boolean =
                oldItem == newItem
        }
    }
}