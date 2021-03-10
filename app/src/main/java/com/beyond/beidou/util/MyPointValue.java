package com.beyond.beidou.util;

public class MyPointValue {
    private double x;
    private double y;
    private double h;

    public MyPointValue() {
        this.x = 0.0;
        this.y = 0.0;
        this.h = 0.0;
    }

    public MyPointValue(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public MyPointValue(double x, double y, double h) {
        this.x = x;
        this.y = y;
        this.h = h;
    }

    public MyPointValue(MyPointValue pointValue) {
        this.x = pointValue.x;
        this.y = pointValue.y;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getH() {
        return this.h;
    }

    public MyPointValue set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public MyPointValue set(float x, float y, float h) {
        this.x = x;
        this.y = y;
        this.h = h;
        return this;
    }
}