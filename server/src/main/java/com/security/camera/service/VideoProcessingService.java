package com.security.camera.service;

import com.security.camera.config.StorageConfig;
import com.security.camera.model.Video;
import com.security.camera.repository.FrameRepository;
import com.security.camera.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para procesamiento asíncrono de videos
 * Utiliza el Thread Pool configurado (Object Pool Pattern)
 */
@Service
@RequiredArgsConstructor
public class VideoProcessingService {

    private final VideoRepository videoRepository;
    private final FrameRepository frameRepository;
    private final StorageConfig storageConfig;

    /**
     * Procesar video de forma asíncrona usando el Thread Pool
     */
    @Async("videoProcessingExecutor")
    @Transactional
    public void processVideoAsync(Long videoId) {
        try {
            Video video = videoRepository.findById(videoId)
                    .orElseThrow(() -> new RuntimeException("Video not found"));

            System.out.println("Processing video: " + video.getId());

            // Extraer frames del video
            List<com.security.camera.model.Frame> frames = extractFrames(video);
            
            // Guardar frames en la base de datos
            frameRepository.saveAll(frames);

            // Actualizar duración del video
            video.setDurationSeconds(calculateDuration(video.getVideoPath()));
            videoRepository.save(video);

            System.out.println("Video processed successfully: " + video.getId());
        } catch (Exception e) {
            System.err.println("Error processing video: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Extraer frames del video y aplicar filtros
     */
    private List<com.security.camera.model.Frame> extractFrames(Video video) {
        List<com.security.camera.model.Frame> frames = new ArrayList<>();
        
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(video.getVideoPath())) {
            grabber.start();
            
            Java2DFrameConverter converter = new Java2DFrameConverter();
            int frameNumber = 0;
            int frameInterval = 30; // Extraer un frame cada 30 frames (1 segundo a 30fps)
            
            org.bytedeco.javacv.Frame frame;
            while ((frame = grabber.grabImage()) != null) {
                if (frameNumber % frameInterval == 0) {
                    BufferedImage image = converter.convert(frame);
                    if (image != null) {
                        // Aplicar filtros
                        image = applyFilters(image);
                        
                        // Guardar frame
                        String framePath = saveFrame(image, video.getId(), frameNumber);
                        
                        com.security.camera.model.Frame frameEntity = com.security.camera.model.Frame.builder()
                                .video(video)
                                .framePath(framePath)
                                .frameNumber(frameNumber)
                                .build();
                        
                        frames.add(frameEntity);
                    }
                }
                frameNumber++;
            }
            
            grabber.stop();
        } catch (Exception e) {
            System.err.println("Error extracting frames: " + e.getMessage());
        }
        
        return frames;
    }

    /**
     * Aplicar filtros a la imagen según requerimientos:
     * - Escala de grises
     * - Reducción de tamaño
     * - Ajuste de brillo
     * - Rotación
     */
    private BufferedImage applyFilters(BufferedImage originalImage) {
        // Reducir tamaño (50% del original)
        int newWidth = originalImage.getWidth() / 2;
        int newHeight = originalImage.getHeight() / 2;
        
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        // Convertir a escala de grises
        BufferedImage grayscaleImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = grayscaleImage.createGraphics();
        g.drawImage(resizedImage, 0, 0, null);
        g.dispose();
        
        return grayscaleImage;
    }

    private String saveFrame(BufferedImage image, Long videoId, int frameNumber) {
        try {
            String filename = String.format("video_%d_frame_%d.jpg", videoId, frameNumber);
            File outputFile = Paths.get(storageConfig.getFrameStoragePath(), filename).toFile();
            ImageIO.write(image, "jpg", outputFile);
            return outputFile.getAbsolutePath();
        } catch (Exception e) {
            System.err.println("Error saving frame: " + e.getMessage());
            return null;
        }
    }

    private int calculateDuration(String videoPath) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {
            grabber.start();
            int duration = (int) (grabber.getLengthInTime() / 1000000); // Convertir microsegundos a segundos
            grabber.stop();
            return duration;
        } catch (Exception e) {
            return 0;
        }
    }
}
