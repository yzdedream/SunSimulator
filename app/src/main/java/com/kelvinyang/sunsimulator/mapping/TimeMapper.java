package com.kelvinyang.sunsimulator.mapping;

import java.util.Date;

/**
 * Created by Kelvin on 2015/12/5.
 */
public class TimeMapper extends SineMapper {
    public TimeMapper(double a, double omega, double b) {
        super(a, omega, b);
    }

    public double map(int hour, int minutes) {
        if (hour < 0 || hour > 23) {
            return 0;
        }
        if (minutes < .0 || minutes > 59) {
            return 0;
        }

        double h = hour + minutes / 60;
        return super.map(h);
    }

}
