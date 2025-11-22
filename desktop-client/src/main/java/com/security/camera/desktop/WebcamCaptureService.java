package com.security.camera.desktop;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.IplImage;
import static org.bytedeco.ffmpeg.global.avcodec.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import org.bytedeco.javacv.OpenCVFrameGrabber;

/**
 * Servicio para capturar video desde la webcam usando JavaCV
 */
public class WebcamCaptureService {
    
    private FrameGrabber grabber;
    private FrameRecorder recorder;
    private volatile boolean isCapturing = false;
    private volatile boolean isRecording = false;
    private Thread captureThread;
    private Consumer<BufferedImage> frameCallback;
    
    private final int width = 640;
    private final int height = 480;
    private final int frameRate = 30;
    
    /**
     * Inicia la captura de video desde la webcam
     */
    public void startCapture(Consumer<BufferedImage> frameCallback) throws Exception {
        if (isCapturing) {
            throw new IllegalStateException("Capture already started");
        }
        
        this.frameCallback = frameCallback;
        
        // Configurar grabber para captura desde webcam usando OpenCV directamente
        // Esto evita intentar backends como FlyCapture2 o Kinect que requieren hardware especial
        grabber = new OpenCVFrameGrabber(0); // 0 = primera webcam
        grabber.setImageWidth(width);
        grabber.setImageHeight(height);
        grabber.setFrameRate(frameRate);
        grabber.start();
        
        isCapturing = true;
        
        // Thread para capturar frames y mostrarlos
        captureThread = new Thread(() -> {
            Java2DFrameConverter converter = new Java2DFrameConverter();
            try {
                while (isCapturing) {
                    Frame frame = grabber.grab();
                    if (frame != null && frame.image != null) {
                        BufferedImage image = converter.convert(frame);
                        if (image != null && frameCallback != null) {
                            frameCallback.accept(image);
                        }
                    }
                    Thread.sleep(33); // ~30 fps
                }
            } catch (Exception e) {
                System.err.println("Error in capture thread: " + e.getMessage());
            } finally {
                converter.close();
            }
        });
        captureThread.setDaemon(true);
        captureThread.start();
    }
    
    /**
     * Detiene la captura de video
     */
    public void stopCapture() {
        isCapturing = false;
        if (captureThread != null) {
            try {
                captureThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
            } catch (Exception e) {
                System.err.println("Error stopping grabber: " + e.getMessage());
            }
        }
    }
    
    /**
     * Graba un video de duración especificada y lo guarda en un archivo temporal
     * @param durationSeconds Duración del video en segundos
     * @return File con el video grabado
     */
    public File recordVideo(int durationSeconds) throws Exception {
        if (!isCapturing) {
            throw new IllegalStateException("Capture not started. Call startCapture() first.");
        }
        
        if (isRecording) {
            throw new IllegalStateException("Already recording");
        }
        
        // Crear archivo temporal
        Path tempFile = Files.createTempFile("webcam_recording_", ".mp4");
        File outputFile = tempFile.toFile();
        outputFile.deleteOnExit();
        
        // Configurar recorder para grabar video
        recorder = new FFmpegFrameRecorder(outputFile, width, height);
        recorder.setVideoCodec(AV_CODEC_ID_H264);
        recorder.setFormat("mp4");
        recorder.setFrameRate(frameRate);
        recorder.setVideoBitrate(2000000); // 2 Mbps
        recorder.start();
        
        isRecording = true;
        
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationSeconds * 1000L);
        
        try {
            while (System.currentTimeMillis() < endTime && isCapturing && isRecording) {
                Frame frame = grabber.grab();
                if (frame != null && frame.image != null) {
                    recorder.record(frame);
                }
            }
        } finally {
            isRecording = false;
            if (recorder != null) {
                recorder.stop();
                recorder.release();
            }
        }
        
        return outputFile;
    }
    
    /**
     * Detiene la grabación actual
     */
    public void stopRecording() {
        isRecording = false;
    }
    
    /**
     * Captura un frame actual como imagen
     */
    public BufferedImage captureSnapshot() throws Exception {
        if (!isCapturing || grabber == null) {
            throw new IllegalStateException("Capture not started");
        }
        
        Frame frame = grabber.grab();
        if (frame != null && frame.image != null) {
            Java2DFrameConverter converter = new Java2DFrameConverter();
            BufferedImage image = converter.convert(frame);
            converter.close();
            return image;
        }
        
        return null;
    }
    
    /**
     * Guarda un snapshot en un archivo
     */
    public File saveSnapshot() throws Exception {
        BufferedImage image = captureSnapshot();
        if (image == null) {
            throw new IOException("Failed to capture image");
        }
        
        Path tempFile = Files.createTempFile("webcam_snapshot_", ".jpg");
        File outputFile = tempFile.toFile();
        outputFile.deleteOnExit();
        
        ImageIO.write(image, "jpg", outputFile);
        return outputFile;
    }
    
    public boolean isCapturing() {
        return isCapturing;
    }
    
    public boolean isRecording() {
        return isRecording;
    }
}
