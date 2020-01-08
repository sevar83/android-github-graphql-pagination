package io.github.sevar83.graphqldemo.ui.repositories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.sevar83.graphqldemo.R
import io.github.sevar83.graphqldemo.fragment.RepositoryFragment
import kotlinx.android.synthetic.main.item_repository.view.*

class RepositoryViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_repository, parent, false)
) {
    fun bind(repositoryFragment: RepositoryFragment?, onClick: ((RepositoryFragment) -> Unit)? = null) {
        itemView.run {
            tvRepositoryName?.text = repositoryFragment?.name
            if (repositoryFragment?.description == null) {
                tvRepositoryDescription.visibility = View.GONE
            } else {
                tvRepositoryDescription.text = repositoryFragment.description
            }

            rootLayout.setOnClickListener {
                repositoryFragment?.let { onClick?.invoke(it) }
            }
        }
    }
}