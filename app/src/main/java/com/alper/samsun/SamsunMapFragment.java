package com.alper.samsun;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class SamsunMapFragment extends SupportMapFragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap googleMap;
    private Location mLocation;
    private Marker mMarker;
    private Polyline mLine;
    private OnLocationFoundListener mListener;
    private boolean isLocationFounded = false;
    private int ZOOM_LEVEL = 14;

    public interface OnLocationFoundListener {
        void onLocationFounded(Location location);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnLocationFoundListener) activity;
        } catch (Exception ex) {
            Log.e("Map Fragment", "OnLocationFoundListener cast exception");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                googleMap = map;

                updateLocation();
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e("CONNECTED: ", "YES");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("CONNECTION SUSPEND: ", i + "");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("CONNECTION FAILED: ", connectionResult.getErrorCode() + "");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e("LOCATION LAT: ", location.getLatitude() + "");
        Log.e("LOCATION LON: ", location.getLongitude() + "");

        mLocation = location;

        if (!isLocationFounded && mListener != null) {
            mListener.onLocationFounded(location);

            updateLocation();

            isLocationFounded = true;
        }
    }

    public void stopListener() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    public void startListener() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    public void connectApi() {
        mGoogleApiClient.connect();
    }

    public void disconnectApi() {
        mGoogleApiClient.disconnect();
    }

    private void updateLocation() {
        if (googleMap == null || mLocation == null)
            return;

        if (mMarker != null)
            mMarker.remove();

        mMarker = googleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker))
                .position(getCurrentLocation()));

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                getCurrentLocation(), ZOOM_LEVEL));
    }

    public LatLng getCurrentLocation() {
        return new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
    }

    public void setStationMarker(LatLng targetLocation) {
        googleMap.clear();

        googleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker))
                .position(getCurrentLocation()));

        googleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_station_map_marker))
                .position(targetLocation));
    }

    public void drawPath(String polyline) {
        List<LatLng> list = decodePoly(polyline);

        if (mLine != null)
            mLine.remove();

        for (int z = 0; z < list.size() - 1; z++) {
            LatLng src = list.get(z);
            LatLng dest = list.get(z + 1);

            mLine = googleMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
                    .width(20)
                    .color(getResources().getColor(R.color.orange))
                    .geodesic(true));
        }

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                getCurrentLocation(), ZOOM_LEVEL));
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    public Location getLocation() {
        return mLocation;
    }
}
