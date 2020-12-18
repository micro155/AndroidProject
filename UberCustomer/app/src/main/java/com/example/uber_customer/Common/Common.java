package com.example.uber_customer.Common;

import android.widget.TextView;

import com.example.uber_customer.Model.AnimationModel;
import com.example.uber_customer.Model.DriverGeoModel;
import com.example.uber_customer.Model.RiderModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Common {
    public static final String RIDER_INFO_REFERENCE = "RiderInfo";
    public static final String DRIVERS_LOCATION_REFERENCES = "DriversLocation";
    public static final String DRIVER_INFO_REFERENCE = "DriverInfo";
    public static RiderModel currentRider;
    public static Set<DriverGeoModel> driversFound = new HashSet<DriverGeoModel>();
    public static HashMap<String, Marker> markerList = new HashMap<>();
    public static HashMap<String, AnimationModel> driverLocationSubscribe = new HashMap<String, AnimationModel>();

    public static String buildWelcomeMessage() {
        if(Common.currentRider != null) {
            return new StringBuilder("Welcome ")
                    .append(Common.currentRider.getFirstName())
                    .append(" ")
                    .append(Common.currentRider.getLastName()).toString();
        } else {
            return "";
        }
    }

    public static String buildName(String firstName, String lastName) {
        return new StringBuilder(firstName).append("").append(lastName).toString();
    }

    public static List<LatLng> decodePoly(String encoded) { //주행 경로 생성 코드
        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while(index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++)-63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++)-63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double)lat / 1E5)),
            (((double)lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    public static float getBearing(LatLng begin, LatLng end) {

        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude) {
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        } else if (begin.latitude >= end.latitude && begin.longitude < end.longitude) {
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        } else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude) {
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        } else if (begin.latitude < end.latitude && begin.longitude >= end.longitude) {
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        }
        return -1;
    }

    public static void setWelcomeMessage(TextView txt_welcome) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 1 && hour <= 12) {
            txt_welcome.setText(new StringBuilder("Good morning."));
        } else if (hour >= 13 && hour <= 17){
            txt_welcome.setText(new StringBuilder("Good afternoon."));
        } else {
            txt_welcome.setText(new StringBuilder("Good evening."));
        }
    }

    public static String formatDuration(String duration) {
        if(duration.contains("mins")) {
            return duration.substring(0, duration.length() - 1); //remove letter "s"
        } else {
            return duration;
        }
    }

    public static String formatAddress(String start_address) {
        int firstIndexOfComma = start_address.indexOf(",");
        return start_address.substring(0, firstIndexOfComma); //get only address
    }
}
