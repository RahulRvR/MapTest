package com.rahulrvr.maptest;

import com.rahulrvr.maptest.pojo.BikeLocation;

import retrofit.http.GET;
import rx.Observable;

/**
 *
 *
 */
public interface BikeLocationApi {

    @GET("/bike-share-stations/v1")
    Observable<BikeLocation> getBikeLocation();

}
