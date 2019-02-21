package com.blackviking.hosh.Notification;

import com.blackviking.hosh.Model.DataMessage;
import com.blackviking.hosh.Model.MyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Scarecrow on 4/1/2018.
 */

public interface APIService {

    @Headers(

            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAY2Jld28:APA91bGw6IKPO7WQsa61ump0eF5vZWOUuYHdviOZ_oW4Ppmh_rZY_OJBX08mhkS2CoZ9Xqjp1Mn1TnAr0QxFLAguzmrq2Jqjf0jAbCk9CyF4HY3b32cQyEMk8al8aXD5NnwAvJ8tw1gK"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body DataMessage body);

}
