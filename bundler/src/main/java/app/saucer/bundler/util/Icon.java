package app.saucer.bundler.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.formats.icns.IcnsImageParser;
import org.apache.commons.imaging.formats.ico.IcoImageParser;
import org.apache.commons.imaging.formats.png.PngImageParser;

public class Icon {
    private final BufferedImage image;
    private final int size;

    private Icon(BufferedImage image) {
        if (image.getWidth() != image.getHeight()) {
            throw new IllegalArgumentException("Icon must be perfectly square.");
        }

        // Smush it down if needed.
        final int MAX_SIZE = 256;
        if (image.getWidth() > MAX_SIZE) {
            image = resize(image, MAX_SIZE);
        }

        this.image = image;
        this.size = this.image.getWidth();
    }

    public byte[] toPng() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            new PngImageParser().writeImage(this.image, baos, null);
            return baos.toByteArray();
        } catch (ImagingException e) {
            throw new IOException(e);
        }
    }

    public byte[] toIco() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            new IcoImageParser().writeImage(this.image, baos, null);
            return baos.toByteArray();
        } catch (ImagingException e) {
            throw new IOException(e);
        }
    }

    public byte[] toIcns() throws IOException {
        // Lower the size to something that's supported by the format.
        int newSize;
        if (this.size >= 128) {
            newSize = 128;
        } else if (this.size >= 48) {
            newSize = 48;
        } else if (this.size >= 32) {
            newSize = 32;
        } else /*if (this.size >= 16)*/ {
            newSize = 16;
        }

        BufferedImage resized = resize(this.image, newSize);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            new IcnsImageParser().writeImage(resized, baos, null);
            return baos.toByteArray();
        } catch (ImagingException e) {
            throw new IOException(e);
        }
    }

    public static Icon from(File imageFile) throws IOException {
        try {
            return new Icon(
                Imaging.getBufferedImage(imageFile)
            );
        } catch (ImagingException e) {
            throw new IOException(e);
        }
    }

    private static BufferedImage resize(BufferedImage img, int newSize) {
        Image tmp = img.getScaledInstance(newSize, newSize, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newSize, newSize, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

}
