package de.yoyosource.data2;

import de.yoyosource.data.Color;

import java.io.File;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public interface Image {
    void write(File file);

    default Image map(UnaryOperator<Raster> operator) {
        return mapRed(operator).mapGreen(operator).mapBlue(operator);
    }
    default Image mapWithAlpha(UnaryOperator<Raster> operator) {
        return map(operator).mapAlpha(operator);
    }
    Image mapRed(UnaryOperator<Raster> operator);
    Image mapGreen(UnaryOperator<Raster> operator);
    Image mapBlue(UnaryOperator<Raster> operator);
    Image mapAlpha(UnaryOperator<Raster> operator);
    Image filter(Predicate<Color> filter);
    Raster lightness();
}
