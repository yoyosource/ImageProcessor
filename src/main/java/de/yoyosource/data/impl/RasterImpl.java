package de.yoyosource.data.impl;

import de.yoyosource.data.Raster;
import de.yoyosource.data.RasterSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class RasterImpl implements Raster {

    private final RasterSource raster;
    private List<BiFunction<Integer, Double, Double>> operators = new ArrayList<>();

    public RasterImpl(RasterSource rasterSource) {
        this.raster = rasterSource;
    }

    private RasterImpl(Raster raster, BiFunction<Integer, Double, Double> operator) {
        this.raster = raster;
        this.operators.add(operator);
    }

    @Override
    public int width() {
        return raster.width();
    }

    @Override
    public int height() {
        return raster.height();
    }

    @Override
    public double get(int x, int y) {
        return get(y * width() + x);
    }

    @Override
    public Raster eval() {
        if (operators.isEmpty()) {
            return new RasterImpl(raster);
        }

        int width = width();
        int height = height();
        AtomicReference<double[]> data = new AtomicReference<>(null);
        return new RasterImpl(new RasterSource() {
            @Override
            public int width() {
                return width;
            }

            @Override
            public int height() {
                return height;
            }

            @Override
            public double get(int index) {
                if (data.get() == null) {
                    double[] data2 = new double[width * height];
                    for (int i = 0; i < data2.length; i++) {
                        data2[i] = RasterImpl.this.get(i);
                    }
                    data.set(data2);
                }
                return data.get()[index];
            }
        });
    }

    @Override
    public Raster add(double value) {
        operators.add((i, d) -> d + value);
        return this;
    }

    @Override
    public Raster subtract(double value) {
        operators.add((i, d) -> d - value);
        return this;
    }

    @Override
    public Raster multiply(double value) {
        operators.add((i, d) -> d * value);
        return this;
    }

    @Override
    public Raster divide(double value) {
        operators.add((i, d) -> d / value);
        return this;
    }

    @Override
    public Raster mod(double value) {
        operators.add((i, d) -> d % value);
        return this;
    }

    @Override
    public Raster clamp(double min, double max) {
        operators.add((i, d) -> Math.min(Math.max(d, min), max));
        return this;
    }

    @Override
    public Raster min(double value) {
        operators.add((i, d) -> Math.min(d, value));
        return this;
    }

    @Override
    public Raster min(double value, double cutOffValue) {
        operators.add((i, d) -> d < value ? cutOffValue : d);
        return this;
    }

    @Override
    public Raster max(double value) {
        operators.add((i, d) -> Math.max(d, value));
        return this;
    }

    @Override
    public Raster max(double value, double cutOffValue) {
        operators.add((i, d) -> d > value ? cutOffValue : d);
        return this;
    }

    @Override
    public Raster add(Raster raster) {
        operators.add((i, d) -> d + raster.get(i));
        return this;
    }

    @Override
    public Raster subtract(Raster raster) {
        operators.add((i, d) -> d - raster.get(i));
        return this;
    }

    @Override
    public Raster multiply(Raster raster) {
        operators.add((i, d) -> d * raster.get(i));
        return this;
    }

    @Override
    public Raster divide(Raster raster) {
        operators.add((i, d) -> d / raster.get(i));
        return this;
    }

    @Override
    public Raster mod(Raster raster) {
        operators.add((i, d) -> d % raster.get(i));
        return this;
    }

    @Override
    public Raster clamp(Raster min, Raster max) {
        operators.add((i, d) -> Math.min(Math.max(d, min.get(i)), max.get(i)));
        return this;
    }

    @Override
    public Raster min(Raster raster) {
        operators.add((i, d) -> Math.min(d, raster.get(i)));
        return this;
    }

    @Override
    public Raster max(Raster raster) {
        operators.add((i, d) -> Math.max(d, raster.get(i)));
        return this;
    }

    @Override
    public Raster normalize() {
        double max = max();
        operators.add((i, d) -> d / max);
        return this;
    }

    @Override
    public Raster gaussianBlur(int radius) {
        if (operators.isEmpty()) {
            operators.add((i, d ) -> {
                int x = i % width();
                int y = i / width();
                return raster.subRaster(x - radius, y - radius, radius * 2 + 1, radius * 2 + 1).average();
            });
            return this;
        } else {
            return eval().gaussianBlur(radius);
        }
    }

    @Override
    public Raster edges(double thresholdSquared) {
        if (operators.isEmpty()) {
            operators.add((i, d) -> {
                int x = i % width();
                int y = i / width();
                double value = raster.get(x + y * width());
                double left = x > 0 ? raster.get(x - 1 + y * width()) : value;
                double right = x < width() - 1 ? raster.get(x + 1 + y * width()) : value;
                double top = y > 0 ? raster.get(x + (y - 1) * width()) : value;
                double bottom = y < height() - 1 ? raster.get(x + (y + 1) * width()) : value;
                double dx = right - left;
                double dy = bottom - top;
                return dx * dx + dy * dy > thresholdSquared ? 1.0 : 0.0;
            });
            return this;
        } else {
            return eval().edges(thresholdSquared);
        }
    }

    @Override
    public Raster invert() {
        double max = max();
        operators.add((i, d) -> max - d);
        return this;
    }

    @Override
    public double average() {
        double sum = 0;
        for (int i = 0; i < width() * height(); i++) {
            sum += get(i);
        }
        return sum / (width() * height());
    }

    @Override
    public double deviation() {
        return 0;
    }

    @Override
    public double min() {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < width() * height(); i++) {
            min = Math.min(min, get(i));
        }
        return min;
    }

    @Override
    public double max() {
        double max = Double.MIN_VALUE;
        for (int i = 0; i < width() * height(); i++) {
            max = Math.max(max, get(i));
        }
        return max;
    }

    @Override
    public double get(int index) {
        double value = raster.get(index);
        for (BiFunction<Integer, Double, Double> operator : operators) {
            value = operator.apply(index, value);
        }
        return value;
    }
}
