package com.arewatechacademy.myapplication.Models

class UsersModel {

    var name: String? = ""
    var userId: String? = ""
    var profilePhoto: String? = ""
    var email: String? = ""
    var presence: String? = ""
    var status: String? = ""
    var gender: String? = ""
    var typing: String? = ""
    var state: String? = ""
    var phone: String? = ""
    var age: String? = ""
    var relationship: String? = ""
    var coverPhoto: String? = ""
    var currentCity : String? = ""
    var password : String? = ""
    var token : String? = ""
    var verified: Boolean? = false

    constructor()

    constructor(
        name: String?,
        userId: String?,
        profilePhoto: String?,
        email: String?,
        presence: String?,
        status: String?,
        gender: String?,
        token: String?,
        typing: String?,
        state: String?,
        phone: String?,
        relationship: String?,
        age: String?,
        coverPhoto: String?,
        currentCity: String?,
        password: String?,
        verified: Boolean
    ) {
        this.name = name
        this.userId = userId
        this.profilePhoto = profilePhoto
        this.email = email
        this.presence = presence
        this.status = status
        this.gender = gender
        this.typing = typing
        this.state = state
        this.phone = phone
        this.relationship = relationship
        this.age = age
        this.coverPhoto = coverPhoto
        this.currentCity = currentCity
        this.password = password
        this.token = token
        this.verified = verified
    }


}