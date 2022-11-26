package de.yoyosource;

import de.yoyosource.data.Image;
import de.yoyosource.data.Raster;
import de.yoyosource.data.impl.ImageImpl;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        BufferedImage bufferedImage = ImageIO.read(Main.class.getResourceAsStream("/NewYear2023-1.png"));

        Image image = new ImageImpl(bufferedImage);
        Raster lightRaster = image.lightness()
                .min(200, 0)
                .gaussianBlur(20)
                .normalize()
                .max(0.0, 1)
                .gaussianBlur(20)
                .add(1.0)
                .eval();

        image.map(raster -> raster.multiply(lightRaster).clamp(0, 255))
                .write(new File("output.png"));
        Raster lightRaster2 = lightRaster.eval().multiply(255);
        image.map(raster -> lightRaster2)
                .write(new File("output-1.png"));

        Raster edges = image.lightness()
                .edges(100)
                .invert()
                .eval();
        image.map(raster -> raster.multiply(edges).multiply(lightRaster).clamp(0, 255))
                .write(new File("output-2.png"));

        Raster edgesOfEdges = image.lightness().edges(100).multiply(255).edges(100).invert().eval();
        Raster copyOfEdgesOfEdges = edgesOfEdges.eval();
        image.map(raster -> copyOfEdgesOfEdges.multiply(255))
                .write(new File("output-3.png"));

        image.map(raster -> raster.multiply(edgesOfEdges).multiply(lightRaster).clamp(0, 255))
                .write(new File("output-4.png"));
    }

    /*
    process {
        lightRaster = input -> lightness -> minCutOff 200 0 -> gaussianBlur 20 -> normalize -> maxCutOff 0 1 -> gaussianBlur 20 -> add 1
        return input -> multiply lightRaster -> clamp 0 255
    }

    process {
        l1 = input -> lightness -> edges 100 -> invert
        l2 = input -> lightness -> minCutOff 200 0 -> gaussianBlur 20 -> normalize -> maxCutOff 0 1 -> gaussianBlur 20 -> add 1
        return input -> multiply l1 -> multiply l2 -> clamp 0 255
    }
     */
}