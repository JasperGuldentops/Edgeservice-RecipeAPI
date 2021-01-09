package com.jasperg.edge.helper;

public class Helper {

    public static String intToTimeString(int time) {
        String timeString = "";

        if(time / 60 > 0) {
            timeString += time / 60 + "uur ";
        }
        if(time % 60 != 0) {
            if(time % 60 < 10) {
                timeString += "0";
            }
            timeString += time % 60 + " minuten";
        }

        return timeString;
    }

}
