package com.example.academyapp;

import com.example.academyapp.Model.CustomerInfoModel;

public class Common {

    public static final String CUSTOMER_INFO_REFERENCE = "CustomerInfo";

    public static CustomerInfoModel currentCustomer;

    public static String buildWelcomeMessage() {
        if(Common.currentCustomer != null) {
            return new StringBuilder("환영합니다 ")
                    .append(Common.currentCustomer.getNickName() + "님").toString();
        } else {
            return "";
        }
    }
}