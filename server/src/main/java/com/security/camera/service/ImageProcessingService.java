package com.security.camera.service;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Servicio para procesamiento de imágenes
 * Aplica filtros: escala de grises, reducción de tamaño, brillo, rotación
 */
@Service
public class ImageProcessingService {

    private final Java2DFrameConverter converter = new Java2DFrameConverter();

    /**
     * Procesa una imagen aplicando todos los filtros configurados
     */
    public byte[] processImage(byte[] imageBytes, ImageFilters filters) throws IOException {
        // Convertir bytes a BufferedImage
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (originalImage == null) {
            throw new IOException("No se pudo leer la imagen");
        }

        // Convertir a Mat de OpenCV
        Mat matImage = bufferedImageToMat(originalImage);

        // Aplicar filtros
        if (filters.isGrayscale()) {
            matImage = applyGrayscale(matImage);
        }

        if (filters.getScaleFactor() != 1.0) {
            matImage = applyResize(matImage, filters.getScaleFactor());
        }

        if (filters.getBrightness() != 0) {
            matImage = applyBrightness(matImage, filters.getBrightness());
        }

        if (filters.getRotationAngle() != 0) {
            matImage = applyRotation(matImage, filters.getRotationAngle());
        }

        // Convertir de vuelta a BufferedImage y luego a bytes
        BufferedImage processedImage = matToBufferedImage(matImage);
        return bufferedImageToBytes(processedImage);
    }

    /**
     * Convierte a escala de grises
     */
    private Mat applyGrayscale(Mat src) {
        Mat gray = new Mat();
        opencv_imgproc.cvtColor(src, gray, opencv_imgproc.COLOR_BGR2GRAY);
        return gray;
    }

    /**
     * Reduce el tamaño de la imagen
     */
    private Mat applyResize(Mat src, double scaleFactor) {
        Mat resized = new Mat();
        Size newSize = new Size(
            (int)(src.cols() * scaleFactor),
            (int)(src.rows() * scaleFactor)
        );
        opencv_imgproc.resize(src, resized, newSize);
        return resized;
    }

    /**
     * Ajusta el brillo de la imagen
     */
    private Mat applyBrightness(Mat src, int brightness) {
        Mat result = new Mat();
        src.convertTo(result, -1, 1.0, brightness);
        return result;
    }

    /**
     * Rota la imagen (45°, 90°, 180°, 270°)
     */
    private Mat applyRotation(Mat src, int angle) {
        Mat rotated = new Mat();
        Point2f center = new Point2f(src.cols() / 2.0f, src.rows() / 2.0f);
        Mat rotationMatrix = opencv_imgproc.getRotationMatrix2D(center, angle, 1.0);
        
        opencv_imgproc.warpAffine(src, rotated, rotationMatrix, src.size());
        
        rotationMatrix.release();
        return rotated;
    }

    /**
     * Convierte BufferedImage a Mat de OpenCV
     */
    private Mat bufferedImageToMat(BufferedImage image) {
        org.bytedeco.javacv.Frame frame = converter.convert(image);
        org.bytedeco.opencv.opencv_core.Mat mat = new org.bytedeco.javacv.OpenCVFrameConverter.ToMat().convert(frame);
        return mat;
    }

    /**
     * Convierte Mat de OpenCV a BufferedImage
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        org.bytedeco.javacv.OpenCVFrameConverter.ToMat converter2 = new org.bytedeco.javacv.OpenCVFrameConverter.ToMat();
        org.bytedeco.javacv.Frame frame = converter2.convert(mat);
        return converter.convert(frame);
    }

    /**
     * Convierte BufferedImage a array de bytes
     */
    private byte[] bufferedImageToBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }

    /**
     * Clase para configurar los filtros a aplicar
     */
    public static class ImageFilters {
        private boolean grayscale = false;
        private double scaleFactor = 1.0; // 1.0 = sin cambio, 0.5 = 50% del tamaño
        private int brightness = 0; // -100 a +100
        private int rotationAngle = 0; // 0, 45, 90, 180, 270

        public ImageFilters() {}

        public ImageFilters(boolean grayscale, double scaleFactor, int brightness, int rotationAngle) {
            this.grayscale = grayscale;
            this.scaleFactor = scaleFactor;
            this.brightness = brightness;
            this.rotationAngle = rotationAngle;
        }

        // Getters y Setters
        public boolean isGrayscale() { return grayscale; }
        public void setGrayscale(boolean grayscale) { this.grayscale = grayscale; }

        public double getScaleFactor() { return scaleFactor; }
        public void setScaleFactor(double scaleFactor) { this.scaleFactor = scaleFactor; }

        public int getBrightness() { return brightness; }
        public void setBrightness(int brightness) { this.brightness = brightness; }

        public int getRotationAngle() { return rotationAngle; }
        public void setRotationAngle(int rotationAngle) { this.rotationAngle = rotationAngle; }
    }
}
