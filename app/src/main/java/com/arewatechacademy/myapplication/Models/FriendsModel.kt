package com.arewatechacademy.myapplication.Models

class FriendsModel {
    var date: String? = ""
    var id: String? = ""
    var receiver: String? = ""
    var sender: String? = ""

    constructor()



    constructor(date: String?, id: String?, receiver: String?, sender: String?) {
        this.date = date
        this.id = id
        this.receiver = receiver
        this.sender = sender
    }


}