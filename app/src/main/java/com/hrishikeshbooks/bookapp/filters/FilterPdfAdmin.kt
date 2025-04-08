package com.hrishikeshbooks.bookapp.filters

import android.widget.Filter
import com.hrishikeshbooks.bookapp.adapter.AdapterPdfAdmin
import com.hrishikeshbooks.bookapp.models.ModelPdf

class FilterPdfAdmin : Filter {
    var filterList:ArrayList<ModelPdf>

    var adapterPdfAdmin: AdapterPdfAdmin

    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin):super() {

        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint:CharSequence? = constraint
        val results =  FilterResults()
        if(constraint!=null && constraint.isEmpty()){
            constraint = constraint.toString().lowercase()
            var filterModels = ArrayList<ModelPdf>()

            for(i in filterList.indices){
                if(filterList[i].title.lowercase().contains(constraint)){
                    filterModels.add(filterList[i])
                }
            }
            results.count = filterModels.size
            results.values = filterModels
        }else{
            results.count = filterList.size
            results.values = filterList

        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
       adapterPdfAdmin.pdfArrayList = results!!.values as ArrayList<ModelPdf>
        adapterPdfAdmin.notifyDataSetChanged()

    }


}