package de.yoyosource.data;

import de.yoyosource.data.impl.RasterImpl;

public interface RasterSource {
    int width();
    int height();
    double get(int index);

    default Raster subRaster(int x, int y, int width, int height) {
        int x2 = Math.max(0, x);
        int y2 = Math.max(0, y);
        int width2 = Math.min(width(), x + width) - x;
        int height2 = Math.min(height(), y + height) - y;
        return new RasterImpl(new RasterSource() {
            @Override
            public int width() {
                return width2;
            }

            @Override
            public int height() {
                return height2;
            }

            @Override
            public double get(int index) {
                int x = index % width;
                int y = index / width;
                return RasterSource.this.get(x + x2 + (y + y2) * RasterSource.this.width());
            }
        });
    }
}
