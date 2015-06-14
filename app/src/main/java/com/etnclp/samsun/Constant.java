package com.etnclp.samsun;

/**
 * Created by erdituncalp on 05/06/15.
 */
public class Constant {

    /**
     * Search
     */
    public static String GOOGLE_SEARCH_URL_ROOT = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=";
    public static String GOOGLE_SEARCH_URL_QUERY = "&types=geocode&language=tr&key=AIzaSyC6p_FCmPyos5J5kX4Y5REb5e49zfIpqeE";

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
    public static String GOOGLE_TRAVEL_AND_LANGUAGE_MODE = "&mode=walking&language=tr";

    /**
     * Iki nokta arasindaki durak listesini almak icin kullanilan servis
     */
    public static String BUS_SERVICE = "http://brvsoft.com/Route.asmx/Getroute?StartLong=";
    public static String BUS_SERVICE_PART_1 = "&StartLat=";
    public static String BUS_SERVICE_PART_2 = "&EndLong=";
    public static String BUS_SERVICE_PART_3 = "&EndLat=";

    public static String getSearchUrl(String searchedKey) {
        searchedKey += " samsun";

        /**
         * Url de bosluklari %20 ile degistirelim
         */
        return GOOGLE_SEARCH_URL_ROOT + searchedKey.replace(" ", "%20") + GOOGLE_SEARCH_URL_QUERY;
    }

    public static String getPlaceDetailUrl(String placeId) {
        return GOOGLE_PLACE_DETAIL_ROOT + placeId + GOOGLE_PLACE_DETAIL_QUERY;
    }

    public static String getBusService(double StartLong, double StartLat, Number EndLong, Number EndLat) {
        return BUS_SERVICE + StartLong + BUS_SERVICE_PART_1 + StartLat + BUS_SERVICE_PART_2 + EndLong + BUS_SERVICE_PART_3 + EndLat;
    }

    public static String getPointDistanceInfo(String originLat, String originLon, String destinationLat, String destinationLon) {
        originLat = originLat.replace(",", ".");
        originLon = originLon.replace(",", ".");
        destinationLat = destinationLat.replace(",", ".");
        destinationLon = destinationLon.replace(",", ".");

        return GOOGLE_DISTANCE_ROOT + originLon + "," + originLat + GOOGLE_DISTANCE_DESTINATION + destinationLon + "," + destinationLat + GOOGLE_TRAVEL_AND_LANGUAGE_MODE;
    }
}
