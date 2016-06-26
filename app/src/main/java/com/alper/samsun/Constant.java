package com.alper.samsun;

/**
 * Created by erdituncalp on 05/06/15.
 */
public class Constant {

    /**
     * Search
     */
    public static String GOOGLE_SEARCH_URL_ROOT = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=";
    public static String GOOGLE_SEARCH_URL_QUERY = "&types=geocode&language=tr&key=AIzaSyC6p_FCmPyos5J5kX4Y5REb5e49zfIpqeE";

    public static String GOOGLE_CURRENT_LOCATION_URL_ROOT = "http://maps.googleapis.com/maps/api/geocode/json?latlng=";
    public static String GOOGLE_CURRENT_LOCATION_URL_QUERY = "&sensor=true";

    /**
     * Place detayini cekmek icin
     */
    public static String GOOGLE_PLACE_DETAIL_ROOT = "https://maps.googleapis.com/maps/api/place/details/json?placeid=";
    public static String GOOGLE_PLACE_DETAIL_QUERY = "&key=AIzaSyC6p_FCmPyos5J5kX4Y5REb5e49zfIpqeE";

    /**
     * Iki nokta arasi yolu cizdirmek icin kullanilan google servisi
     */
    public static String GOOGLE_DISTANCE_ROOT = "http://maps.googleapis.com/maps/api/directions/json?origin=";
    public static String GOOGLE_DISTANCE_DESTINATION = "&destination=";
    public static String GOOGLE_TRAVEL_AND_LANGUAGE_MODE = "&mode=driving&language=tr";

    public static String getSearchUrl(String searchedKey) {
        return GOOGLE_SEARCH_URL_ROOT + searchedKey.replace(" ", "%20") + GOOGLE_SEARCH_URL_QUERY;
    }

    public static String getCurrentLocationDetail(String originLon, String originLat) {
        return GOOGLE_CURRENT_LOCATION_URL_ROOT + originLon + "," + originLat + GOOGLE_CURRENT_LOCATION_URL_QUERY;
    }

    public static String getPlaceDetailUrl(String placeId) {
        return GOOGLE_PLACE_DETAIL_ROOT + placeId + GOOGLE_PLACE_DETAIL_QUERY;
    }

    public static String getPointDistanceInfo(String originDestination, String destinationDescription) {
        return GOOGLE_DISTANCE_ROOT + originDestination.replace(" ", "%20") + GOOGLE_DISTANCE_DESTINATION + destinationDescription.replace(" ", "%20") + GOOGLE_TRAVEL_AND_LANGUAGE_MODE;
    }
}
