package de.yoyosource.data.impl;

import de.yoyosource.data.Color;
import de.yoyosource.data.Image;
import de.yoyosource.data.Raster;
import de.yoyosource.data.RasterSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class ImageImpl implements Image {

    private int width;
    private int height;
    private Raster red;
    private Raster green;
    private Raster blue;
    private Raster alpha;

    public ImageImpl(BufferedImage image) {
        if (image == null) return;
        this.width = image.getWidth();
        this.height = image.getHeight();
        double[] redData = new double[width * height];
        double[] greenData = new double[width * height];
        double[] blueData = new double[width * height];
        double[] alphaData = new double[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                redData[y * width + x] = (rgb >> 16) & 0xFF;
                greenData[y * width + x] = (rgb >> 8) & 0xFF;
                blueData[y * width + x] = rgb & 0xFF;
                alphaData[y * width + x] = (rgb >> 24) & 0xFF;
            }
        }
        this.red = new RasterImpl(new ImageDataSource(width, height, redData));
        this.green = new RasterImpl(new ImageDataSource(width, height, greenData));
        this.blue = new RasterImpl(new ImageDataSource(width, height, blueData));
        this.alpha = new RasterImpl(new ImageDataSource(width, height, alphaData));
    }

    private class ImageDataSource implements RasterSource {
        private int width;
        private int height;
        private double[] data;

        public ImageDataSource(int width, int height, double[] data) {
            this.width = width;
            this.height = height;
            this.data = data;
        }

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
            return data[index];
        }
    }

    @Override
    public void write(File file) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = ((int) alpha.get(x, y) << 24) | ((int) red.get(x, y) << 16) | ((int) green.get(x, y) << 8) | (int) blue.get(x, y);
                image.setRGB(x, y, rgb);
            }
        }
        try {
            ImageIO.write(image, "png", file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Image mapRed(UnaryOperator<Raster> operator) {
        ImageImpl image = new ImageImpl(null);
        image.width = width;
        image.height = height;
        image.red = operator.apply(red.eval());
        image.green = green;
        image.blue = blue;
        image.alpha = alpha;
        return image;
    }

    @Override
    public Image mapGreen(UnaryOperator<Raster> operator) {
        ImageImpl image = new ImageImpl(null);
        image.width = width;
        image.height = height;
        image.red = red;
        image.green = operator.apply(green.eval());
        image.blue = blue;
        image.alpha = alpha;
        return image;
    }

    @Override
    public Image mapBlue(UnaryOperator<Raster> operator) {
        ImageImpl image = new ImageImpl(null);
        image.width = width;
        image.height = height;
        image.red = red;
        image.green = green;
        image.blue = operator.apply(blue.eval());
        image.alpha = alpha;
        return image;
    }

    @Override
    public Image mapAlpha(UnaryOperator<Raster> operator) {
        ImageImpl image = new ImageImpl(null);
        image.width = width;
        image.height = height;
        image.red = red;
        image.green = green;
        image.blue = blue;
        image.alpha = operator.apply(alpha);
        return image;
    }

    @Override
    public Image filter(Predicate<Color> filter) {
        return null;
    }

    @Override
    public Raster lightness() {
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
                return red.get(index) * 0.2126 + green.get(index) * 0.7152 + blue.get(index) * 0.0722;
            }
        });
    }
}
