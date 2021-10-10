package com.arewatechacademy.myapplication.Models

class CommentModel {

    var publisher:String = ""
    var comment:String = ""
    var postId:String = ""
    var commentId = ""

    constructor()

    constructor(publisher: String, comment: String, postId: String, commentId : String) {
        this.publisher = publisher
        this.comment = comment
        this.postId = postId
        this.commentId = commentId
    }
}