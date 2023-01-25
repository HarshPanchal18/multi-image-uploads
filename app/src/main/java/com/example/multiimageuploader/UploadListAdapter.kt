package com.example.multiimageuploader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView


class UploadListAdapter(var fileNameList: List<String>?, var fileDoneList: List<String>?) :
    RecyclerView.Adapter<UploadListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var fileName: TextView
        var fileDone: ImageView

        init {
            fileName = itemView.findViewById(R.id.txtFilename)
            fileDone = itemView.findViewById(R.id.imgLoading)
        }
    }

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(android.R.layout.activity_list_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: ViewHolder, position: Int) {
        val filename= fileNameList?.get(position)
        holder.fileName.text=filename

        val filedone=fileDoneList?.get(position)
        if(filedone == "Uploading")
            holder.fileDone.setImageResource(R.drawable.progress)
        else
            holder.fileDone.setImageResource(R.drawable.checked)
    }

    override fun getItemCount(): Int {
        return fileNameList?.size!!
    }
}
