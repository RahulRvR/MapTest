package com.rahulrvr.maptest;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.rahulrvr.maptest.pojo.BikeLocation;
import com.rahulrvr.maptest.pojo.Feature;
import com.rahulrvr.maptest.pojo.Geometry;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import retrofit.RestAdapter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    public static final String END_POINT = "https://api.phila.gov";

    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(
                        R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(final GoogleMap map) {



        RestAdapter adapter = new RestAdapter.Builder().
                setEndpoint(END_POINT).build();
        map.setMyLocationEnabled(true);
        BikeLocationApi api = adapter.create(BikeLocationApi.class);

        api.getBikeLocation().observeOn(AndroidSchedulers.mainThread()).map(
                new Func1<BikeLocation, List<Feature>>() {

                    @Override
                    public List<Feature> call(BikeLocation bikeLocation) {
                        return bikeLocation.getFeatures();
                    }
                }).take(10).subscribe(new Subscriber<List<Feature>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<Feature> features) {
                Observable.from(features).take(5).subscribe(new Subscriber<Feature>() {
                    @Override
                    public void onCompleted() {
                        LatLng point = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                        map.setMyLocationEnabled(true);
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 12));
                        map.addMarker(new MarkerOptions()
                                .title("you are here")
                                .anchor(0.0f, 1.0f)
                                .position(point));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("xx","xx");
                    }

                    @Override
                    public void onNext(final Feature feature) {
                        Geometry geometry = feature.getGeometry();
                        Double lat = geometry.getCoordinates().get(0);
                        Double lang = geometry.getCoordinates().get(1);

                        LatLng point = new LatLng(lang, lat);
                        map.setMyLocationEnabled(true);

                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 12));
                        map.addMarker(new MarkerOptions()
                                .title(Integer.toString(feature.getProperties().getTotalDocks()))
                                .snippet(Integer.toString(
                                        feature.getProperties().getBikesAvailable()))
                                        .anchor(0.0f, 1.0f)
                                        .position(point));
                        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                            @Override
                            public View getInfoWindow(Marker marker) {
                                View v = getLayoutInflater().inflate(R.layout.info_window, null);

                                ((TextView)v.findViewById(R.id.totalDocks)).setText(
                                        marker.getTitle());
                                ((TextView)v.findViewById(R.id.bikesAvl)).setText(marker.getSnippet());
                                return v;
                            }

                            @Override
                            public View getInfoContents(Marker marker) {
                                return null;
                            }
                        });
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 12.0f));
                    }
                });
            }
        });


    }

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
