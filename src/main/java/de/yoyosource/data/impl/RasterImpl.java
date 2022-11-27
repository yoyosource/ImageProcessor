package de.yoyosource.data.impl;

import de.yoyosource.data.Raster;
import de.yoyosource.data.RasterSource;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class RasterImpl implements Raster {

    private final RasterSource raster;
    private List<BiFunction<Integer, Double, Double>> operators = new ArrayList<>();

    private OptionalDouble min = OptionalDouble.empty();
    private OptionalDouble max = OptionalDouble.empty();

    public RasterImpl(RasterSource rasterSource) {
        this.raster = rasterSource;
    }

    public RasterImpl(RasterSource rasterSource, double min, double max) {
        this.raster = rasterSource;
        this.min = OptionalDouble.of(min);
        this.max = OptionalDouble.of(max);
    }

    private RasterImpl(RasterSource rasterSource, OptionalDouble min, OptionalDouble max) {
        this.raster = rasterSource;
        this.min = min;
        this.max = max;
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
            return new RasterImpl(raster, min, max);
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
        }, min, max);
    }

    @Override
    public Raster add(double value) {
        operators.add((i, d) -> d + value);
        if (min.isPresent()) min = OptionalDouble.of(min.getAsDouble() + value);
        if (max.isPresent()) max = OptionalDouble.of(max.getAsDouble() + value);
        return this;
    }

    @Override
    public Raster subtract(double value) {
        operators.add((i, d) -> d - value);
        if (min.isPresent()) min = OptionalDouble.of(min.getAsDouble() - value);
        if (max.isPresent()) max = OptionalDouble.of(max.getAsDouble() - value);
        return this;
    }

    @Override
    public Raster multiply(double value) {
        operators.add((i, d) -> d * value);
        if (min.isPresent()) min = OptionalDouble.of(min.getAsDouble() * value);
        if (max.isPresent()) max = OptionalDouble.of(max.getAsDouble() * value);
        if (value < 0) {
            OptionalDouble temp = min;
            min = max;
            max = temp;
        }
        return this;
    }

    @Override
    public Raster divide(double value) {
        operators.add((i, d) -> d / value);
        if (min.isPresent()) min = OptionalDouble.of(min.getAsDouble() / value);
        if (max.isPresent()) max = OptionalDouble.of(max.getAsDouble() / value);
        if (value < 0) {
            OptionalDouble temp = min;
            min = max;
            max = temp;
        }
        return this;
    }

    @Override
    public Raster mod(double value) {
        operators.add((i, d) -> d % value);
        min = OptionalDouble.empty();
        max = OptionalDouble.empty();
        return this;
    }

    @Override
    public Raster clamp(double min, double max) {
        operators.add((i, d) -> Math.min(Math.max(d, min), max));
        if (this.min.isPresent()) this.min = OptionalDouble.of(Math.min(Math.max(this.min.getAsDouble(), min), max));
        if (this.max.isPresent()) this.max = OptionalDouble.of(Math.min(Math.max(this.max.getAsDouble(), min), max));
        return this;
    }

    @Override
    public Raster min(double value) {
        operators.add((i, d) -> Math.min(d, value));
        if (min.isPresent()) min = OptionalDouble.of(Math.min(min.getAsDouble(), value));
        if (max.isPresent()) max = OptionalDouble.of(Math.min(max.getAsDouble(), value));
        return this;
    }

    @Override
    public Raster min(double value, double cutOffValue) {
        operators.add((i, d) -> d < value ? cutOffValue : d);
        if (min.isPresent()) min = OptionalDouble.of(min.getAsDouble() < value ? cutOffValue : min.getAsDouble());
        if (max.isPresent()) max = OptionalDouble.of(max.getAsDouble() < value ? cutOffValue : max.getAsDouble());
        return this;
    }

    @Override
    public Raster max(double value) {
        operators.add((i, d) -> Math.max(d, value));
        if (min.isPresent()) min = OptionalDouble.of(Math.max(min.getAsDouble(), value));
        if (max.isPresent()) max = OptionalDouble.of(Math.max(max.getAsDouble(), value));
        return this;
    }

    @Override
    public Raster max(double value, double cutOffValue) {
        operators.add((i, d) -> d > value ? cutOffValue : d);
        if (min.isPresent()) min = OptionalDouble.of(min.getAsDouble() > value ? cutOffValue : min.getAsDouble());
        if (max.isPresent()) max = OptionalDouble.of(max.getAsDouble() > value ? cutOffValue : max.getAsDouble());
        return this;
    }

    @Override
    public Raster add(Raster raster) {
        operators.add((i, d) -> d + raster.get(i));
        min = OptionalDouble.empty();
        max = OptionalDouble.empty();
        return this;
    }

    @Override
    public Raster subtract(Raster raster) {
        operators.add((i, d) -> d - raster.get(i));
        min = OptionalDouble.empty();
        max = OptionalDouble.empty();
        return this;
    }

    @Override
    public Raster multiply(Raster raster) {
        operators.add((i, d) -> d * raster.get(i));
        min = OptionalDouble.empty();
        max = OptionalDouble.empty();
        return this;
    }

    @Override
    public Raster divide(Raster raster) {
        operators.add((i, d) -> d / raster.get(i));
        min = OptionalDouble.empty();
        max = OptionalDouble.empty();
        return this;
    }

    @Override
    public Raster mod(Raster raster) {
        operators.add((i, d) -> d % raster.get(i));
        min = OptionalDouble.empty();
        max = OptionalDouble.empty();
        return this;
    }

    @Override
    public Raster clamp(Raster min, Raster max) {
        operators.add((i, d) -> Math.min(Math.max(d, min.get(i)), max.get(i)));
        this.min = OptionalDouble.empty();
        this.max = OptionalDouble.empty();
        return this;
    }

    @Override
    public Raster min(Raster raster) {
        operators.add((i, d) -> Math.min(d, raster.get(i)));
        min = OptionalDouble.empty();
        max = OptionalDouble.empty();
        return this;
    }

    @Override
    public Raster max(Raster raster) {
        operators.add((i, d) -> Math.max(d, raster.get(i)));
        min = OptionalDouble.empty();
        max = OptionalDouble.empty();
        return this;
    }

    @Override
    public Raster normalize() {
        double max = max();
        operators.add((i, d) -> d / max);
        if (this.min.isPresent()) this.min = OptionalDouble.of(this.min.getAsDouble() / max);
        if (this.max.isPresent()) this.max = OptionalDouble.of(this.max.getAsDouble() / max);
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
            min = OptionalDouble.empty();
            max = OptionalDouble.empty();
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
            min = OptionalDouble.empty();
            max = OptionalDouble.empty();
            return this;
        } else {
            return eval().edges(thresholdSquared);
        }
    }

    @Override
    public Raster invert() {
        double max = max();
        operators.add((i, d) -> max - d);
        OptionalDouble temp = min;
        min = this.max;
        this.max = temp;
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
        throw new UnsupportedOperationException();
    }

    @Override
    public double min() {
        if (min.isPresent()) return min.getAsDouble();
        double min = Double.MAX_VALUE;
        for (int i = 0; i < width() * height(); i++) {
            min = Math.min(min, get(i));
        }
        this.min = OptionalDouble.of(min);
        return min;
    }

    @Override
    public double max() {
        if (max.isPresent()) return max.getAsDouble();
        double max = Double.MIN_VALUE;
        for (int i = 0; i < width() * height(); i++) {
            max = Math.max(max, get(i));
        }
        this.max = OptionalDouble.of(max);
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
