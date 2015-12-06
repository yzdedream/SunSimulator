package com.kelvinyang.sunsimulator.mapping;

/**
 * Created by Kelvin on 2015/12/5.
 */
public class SineMapper {
    private double a;
    private double omega;
    private double b;

    public SineMapper(double a, double omega, double b) {
        this.a = a;
        this.omega = omega;
        this.b = b;
    }

    protected double map(double x) {
        // f(x) = Asin(ωx + b)
        double theta = omega * x + b;
        return Math.sin(theta) * a;
    }
}
