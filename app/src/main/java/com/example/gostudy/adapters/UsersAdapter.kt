package com.example.gostudy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gostudy.R
import com.example.gostudy.models.AppUser

class UsersAdapter(
    private val users: List<AppUser>,
    private val onUserClick: (AppUser) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPhoto: ImageView = itemView.findViewById(R.id.ivUserPhoto)
        val tvName: TextView = itemView.findViewById(R.id.tvUserName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = users[position]

        holder.tvName.text = item.displayName

        Glide.with(holder.itemView)
            .load(item.photoUrl)
            .placeholder(android.R.drawable.sym_def_app_icon)
            .error(android.R.drawable.sym_def_app_icon)
            .into(holder.ivPhoto)

        holder.itemView.setOnClickListener {
            onUserClick(item)
        }
    }

    override fun getItemCount(): Int = users.size
}
