package com.rap.sheet.retrofit

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface RestInterface {
    // For Create New User
    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("user")
    suspend fun createNewUser(@Body userDetail: JsonObject): Response<ResponseBody>


    // For Add New Contacts
    @Multipart
    @POST("contact/add")
    suspend fun addNewContact(@Part("number") number: RequestBody,
                              @Part("first_name") first_name: RequestBody,
                              @Part("last_name") last_name: RequestBody,
                              @Part("email") email: RequestBody,
                              @Part("weblink") weblink: RequestBody,
                              @Part("instagram") instagram: RequestBody,
                              @Part("facebook") facebook: RequestBody,
                              @Part("linkedin") linkedin: RequestBody,
                              @Part("user_id") user_id: RequestBody,
                              @Part image: MultipartBody.Part): Response<ResponseBody>

    @Multipart
    @POST("contact/add")
    suspend fun addNewContactWithoutImage(@Part("number") number: RequestBody,
                                          @Part("first_name") first_name: RequestBody,
                                          @Part("last_name") last_name: RequestBody,
                                          @Part("email") email: RequestBody,
                                          @Part("weblink") weblink: RequestBody,
                                          @Part("instagram") instagram: RequestBody,
                                          @Part("facebook") facebook: RequestBody,
                                          @Part("linkedin") linkedin: RequestBody,
                                          @Part("user_id") user_id: RequestBody
    ): Response<ResponseBody>


    @Multipart
    @POST("comments/add")
    suspend fun addNewComment(@Part("contact_id") contact_id: RequestBody,
                              @Part("message") message: RequestBody,
                              @Part("rate") rate: RequestBody,
                              @Part("user_id") user_id: RequestBody): Response<ResponseBody>


    // For Search New Contact
    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("contact/search")
    suspend fun searchContacts(@Query("search") searchContact: String): Response<ResponseBody>

    @GET("user/get")
    suspend fun getProfileData(@Query("uuid") searchContact: String): Response<ResponseBody>

    // For Report
    @POST("reports/add")
    @FormUrlEncoded
    suspend fun reportContact(@Field("user_id") user_id: String,
                              @Field("contact_id") contact_id: String,
                              @Field("title") title: String,
                              @Field("message") message: String): Response<ResponseBody>

//    @Headers("Accept: application/json", "Content-Type: application/json")
//    @POST("feedback/send")
//    suspend fun sendFeedback(@Body feedbackDetail: JsonObject): Response<ResponseBody>

    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("feedback/send")
    suspend fun sendFeedback(@Body feedbackDetail: JsonObject): Response<ResponseBody>

//    @Multipart
//    @POST("feedback/send")
//    suspend fun sendFeedback(@Part("message") message: RequestBody,
//                             @Part("first_name") first_name: RequestBody,
//                             @Part("last_name") last_name: RequestBody,
//                             @Part("email") email: RequestBody,
//                             @Part("phone") phone: RequestBody): Response<ResponseBody>

    @Multipart
    @POST("user/update")
    suspend fun updateProfile(@Part("uuid") uuid: RequestBody,
                             @Part("first_name") message: RequestBody,
                             @Part("last_name") first_name: RequestBody,
                             @Part("email") last_name: RequestBody,
                             @Part("phone") email: RequestBody,
                             @Part image: MultipartBody.Part): Response<ResponseBody>

    @Multipart
    @POST("user/update")
    suspend fun updateProfileWithOutImage(@Part("uuid") uuid: RequestBody,
                              @Part("first_name") message: RequestBody,
                              @Part("last_name") first_name: RequestBody,
                              @Part("email") last_name: RequestBody,
                              @Part("phone") email: RequestBody): Response<ResponseBody>


    @PUT("comments/edit")
    @FormUrlEncoded
    suspend fun editComment(@Field("message") message: String,
                            @Field("comment_id") comment_id: String,
                            @Field("rate") rate: String): Response<ResponseBody>

    // For Report
    @GET("reports/get")
    suspend fun allReportedContact(): Response<ResponseBody>

    // For get contact details
    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("contact")
    suspend fun contactDetails(@Query("id") id: String): Response<ResponseBody>

    // For Add new comments
//    @Headers("Accept: application/json", "Content-Type: application/json")
//    @POST("comments/add")
//    suspend fun addNewComment(@Body userDetail: String): Response<ResponseBody>

//    @Headers("Accept: application/json", "Content-Type: application/json")
//    @POST("comments/add")
//    @FormUrlEncoded
//    suspend fun addNewComment(@Field("contact_id") contact_id: String,
//                              @Field("message") message: String,
//                              @Field("rate") rate: String,
//                              @Field("user_id") user_id: String): Response<ResponseBody>

    // For Get All Comment
    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("comments")
    suspend fun getAllComment(@Query("contact_id") id: String): Response<ResponseBody>

    // For get Avg Rate of Contact
    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("contact/rate")
    suspend fun getAvgRateOfContact(@Query("id") id: String): Response<ResponseBody>

    // For get All My Contact
    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("contact/my")
    suspend fun getAllMyContact(@Query("id") id: String): Response<ResponseBody>

    // For delete My Contact
    @Headers("Accept: application/json", "Content-Type: application/json")
    @DELETE("contact/remove/{contact_id}/{user_id}")
    suspend fun deleteMyContact(@Path("contact_id") contact_id: String, @Path("user_id") user_id: String): Response<ResponseBody>

    // For delete Comment
    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("comments/deletecomment")
    suspend fun deleteComment(@Query("comment_id") comment_id: String): Response<ResponseBody>

    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("contact/browse")
    suspend fun browseContact(): Response<ResponseBody>

}

interface EveryOneAPIInterface {
    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("{number}")
    suspend fun everyoneAPI(@Path("number") number: String, @Query("account_sid") account_sid: String, @Query("auth_token") auth_token: String): Response<ResponseBody>
}