package de.yoyosource.data;

public interface Raster extends RasterSource {
    int width();
    int height();
    double get(int x, int y);

    Raster eval();

    Raster add(double value);
    Raster subtract(double value);
    Raster multiply(double value);
    Raster divide(double value);
    Raster mod(double value);
    Raster clamp(double min, double max);
    Raster min(double value);
    Raster min(double value, double cutOffValue);
    Raster max(double value);
    Raster max(double value, double cutOffValue);

    Raster add(Raster raster);
    Raster subtract(Raster raster);
    Raster multiply(Raster raster);
    Raster divide(Raster raster);
    Raster mod(Raster raster);
    Raster clamp(Raster min, Raster max);
    Raster min(Raster raster);
    Raster max(Raster raster);

    Raster normalize();
    Raster gaussianBlur(int radius);
    Raster edges(double thresholdSquared);
    Raster invert();

    double average();
    double deviation();
    double min();
    double max();
}
