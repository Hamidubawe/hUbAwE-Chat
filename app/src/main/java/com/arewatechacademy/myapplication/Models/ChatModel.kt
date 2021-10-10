package com.arewatechacademy.myapplication.Models

class ChatModel {

    var message: String? = ""
    var receiver: String? = ""
    var sender: String? = ""
    var type: String? = ""
    var timestamp: String? = ""
    var id: String? = ""
    var seen = true


    constructor()


    constructor(
        message: String?,
        receiver: String?,
        sender: String?,
        type: String?,
        timestamp: String?,
        id: String?,
        seen: Boolean
    ) {
        this.message = message
        this.receiver = receiver
        this.sender = sender
        this.type = type
        this.timestamp = timestamp
        this.id = id
        this.seen = seen
    }


}