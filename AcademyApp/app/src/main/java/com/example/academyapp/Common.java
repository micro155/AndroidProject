package com.example.academyapp;

import com.example.academyapp.Model.MemberInfoModel;

public class Common {

    public static final String MEMBER_INFO_REFERENCE = "MemberInfo";

    public static MemberInfoModel currentMember;

    public static String buildWelcomeMessage() {
        if(Common.currentMember != null) {
            return new StringBuilder("환영합니다 ")
                    .append(Common.currentMember.getNickName() + "님").toString();
        } else {
            return "";
        }
    }
}