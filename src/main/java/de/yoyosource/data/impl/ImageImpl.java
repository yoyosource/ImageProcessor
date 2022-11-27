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
        double redMax = 0, redMin = 0;
        double[] greenData = new double[width * height];
        double greenMax = 0, greenMin = 0;
        double[] blueData = new double[width * height];
        double blueMax = 0, blueMin = 0;
        double[] alphaData = new double[width * height];
        double alphaMax = 0, alphaMin = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                double red = (rgb >> 16) & 0xFF;
                redMax = Math.max(redMax, red);
                redMin = Math.min(redMin, red);
                redData[y * width + x] = red;
                double green = (rgb >> 8) & 0xFF;
                greenMax = Math.max(greenMax, green);
                greenMin = Math.min(greenMin, green);
                greenData[y * width + x] = green;
                double blue = rgb & 0xFF;
                blueMax = Math.max(blueMax, blue);
                blueMin = Math.min(blueMin, blue);
                blueData[y * width + x] = blue;
                double alpha = (rgb >> 24) & 0xFF;
                alphaMax = Math.max(alphaMax, alpha);
                alphaMin = Math.min(alphaMin, alpha);
                alphaData[y * width + x] = alpha;
            }
        }
        this.red = new RasterImpl(new ImageDataSource(width, height, redData), redMin, redMax);
        this.green = new RasterImpl(new ImageDataSource(width, height, greenData), greenMin, greenMax);
        this.blue = new RasterImpl(new ImageDataSource(width, height, blueData), blueMin, blueMax);
        this.alpha = new RasterImpl(new ImageDataSource(width, height, alphaData), alphaMin, alphaMax);
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
