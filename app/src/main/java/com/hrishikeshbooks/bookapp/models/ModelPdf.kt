package com.hrishikeshbooks.bookapp.models

class ModelPdf {
    var uid:String = ""
    var id:String = ""
    var title:String = ""
    var description:String = ""
    var categoryId:String = ""
    var url:String = ""
    var timestamp:Long = 0
    var viewsCount:Long = 0
    var downloadsCount:Long = 0
    var isFavorite : Boolean= false

    constructor()
    constructor(
        downloadsCount: Long,
        viewsCount: Long,
        timestamp: Long,
        url: String,
        description: String,
        title: String,
        id: String,
        uid: String,
        categoryId: String,
        isFavorite: Boolean
    ) {
        this.downloadsCount = downloadsCount
        this.viewsCount = viewsCount
        this.timestamp = timestamp
        this.url = url
        this.description = description
        this.title = title
        this.id = id
        this.uid = uid
        this.categoryId = categoryId
        this.isFavorite = isFavorite

    }


}