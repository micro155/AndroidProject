package com.example.academyapp.RestAPI;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GeocodingResponse {

    @SerializedName("addresses")
    List<RequestAddress> addresses = new ArrayList();

    public List<RequestAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<RequestAddress> addresses) {
        this.addresses = addresses;
    }

    public class RequestAddress {

        @SerializedName("x")
        private double x;

        @SerializedName("y")
        private double y;

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
    }
}
