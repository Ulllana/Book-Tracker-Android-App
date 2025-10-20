package com.example.bookcatalogapp.models

data class VolumeResponse(val items: List<VolumeItem>?)

data class VolumeItem(val id: String, val volumeInfo: VolumeInfo)

data class VolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val publisher: String?,
    val publishedDate: String?,
    val description: String?,
    val pageCount: Int?,
    val imageLinks: ImageLinks?,
    val previewLink: String?
)

data class ImageLinks(val thumbnail: String?)