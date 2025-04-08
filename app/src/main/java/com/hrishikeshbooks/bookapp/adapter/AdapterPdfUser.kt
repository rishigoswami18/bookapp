
package com.hrishikeshbooks.bookapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.hrishikeshbooks.bookapp.MyApplication
import com.hrishikeshbooks.bookapp.activities.PdfDetailActivity
import com.hrishikeshbooks.bookapp.databinding.RowPdfAdminBinding
import com.hrishikeshbooks.bookapp.filters.FilterPdfUser
import com.hrishikeshbooks.bookapp.models.ModelPdf

class AdapterPdfUser(
    private val context: Context,
    var pdfArrayList: ArrayList<ModelPdf>
) : RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser>(), Filterable {

    var filterList: ArrayList<ModelPdf> = pdfArrayList
    private var filter: FilterPdfUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        val binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfUser(binding)
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        val model = pdfArrayList[position]
        val binding = holder.binding

        // Set text
        binding.titleTv.text = model.title
        binding.descriptionTv.text = model.description
        binding.dateTv.text = MyApplication.formatTimeStamp(model.timestamp)

        binding.pdfView.recycle()

        // Load PDF thumbnail
        MyApplication.loadPdfFromUrlSinglePage(
            model.url,
            model.title,
            binding.pdfView,
            binding.progressBar,
            null
        )

        // Load PDF size
        MyApplication.loadPdfSize(
            model.url,
            model.title,
            binding.sizeTv
        )

        // Item click
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", model.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterPdfUser(filterList, this)
        }
        return filter!!
    }

    inner class HolderPdfUser(val binding: RowPdfAdminBinding) :
        RecyclerView.ViewHolder(binding.root)
}
