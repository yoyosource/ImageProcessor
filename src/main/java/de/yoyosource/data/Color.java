package de.yoyosource.data;

public class Color {
    private double red;
    private double green;
    private double blue;
    private double alpha;

    public Color(double red, double green, double blue, double alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public Color(double red, double green, double blue) {
        this(red, green, blue, 255);
    }

    public double red() {
        return red;
    }

    public double green() {
        return green;
    }

    public double blue() {
        return blue;
    }

    public double alpha() {
        return alpha;
    }
}
