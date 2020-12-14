package com.example.uber;

import com.example.uber.Model.DriverInfoModel;

public class Common {
    public static final String DRIVER_INFO_REFERENCE = "DriverInfo";
    public static final String DRIVERS_LOCATION_REFERENCES = "DriversLocation";

    public static DriverInfoModel currentUser;

    public static String buildWelcomMessage() {
        if(Common.currentUser != null) {
            return new StringBuilder("Welcome ")
                    .append(Common.currentUser.getFirstName())
                    .append(" ")
                    .append(Common.currentUser.getLastName()).toString();
        } else {
            return "";
        }
    }
}
