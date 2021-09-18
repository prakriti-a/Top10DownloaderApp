package com.prakriti.top10downloader

// here we put our model class, no restrictions in kotlin
class FeedEntry { // new object created for every entry in xml data

    // properties for data extracted from rss feed
    private var name: String = ""
    private var artist: String = ""
    private var releaseDate: String = ""
    private var summary: String = ""
    private var imageURL: String = ""

    fun getName(): String {
        return name
    }
    fun getArtist(): String {
        return artist
    }
    fun getReleaseDate(): String {
        return releaseDate
    }
    fun getSummary(): String {
        return summary
    }
    fun getImageURL(): String {
        return imageURL
    }

    fun setName(name: String) {
        this.name = name
    }
    fun setArtist(artist: String) {
        this.artist = artist
    }
    fun setReleaseDate(releaseDate: String) {
        this.releaseDate = releaseDate
    }
    fun setSummary(summary: String) {
        this.summary =  summary
    }
    fun setImageURL(imageURL: String) {
        this.imageURL = imageURL
    }

    override fun toString(): String {
        // this method is used by adapter when pulling data directly to textview
        return "Name: $name\nArtist: $artist\nRelease Date: $releaseDate\nSummary:\n${summary.substring(0, summary.indexOf("\n"))}\n"
        // Image: $imageURL\n" // or use """.trimIndent()
    }
}