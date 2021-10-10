package com.arewatechacademy.myapplication.Models

class PostModel {

    var postId: String = ""
    var publisher: String? = null
    var postText: String? = null
    var postPhoto: String? = null
    var type: String? = null
    var postTimestamp: String = ""

    constructor()

    constructor(
        postId: String,
        publisher: String?,
        postText: String?,
        postPhoto: String?,
        type: String?,
        postTimestamp: String
    ) {
        this.postId = postId
        this.publisher = publisher
        this.postText = postText
        this.postPhoto = postPhoto
        this.type = type
        this.postTimestamp = postTimestamp
    }


}