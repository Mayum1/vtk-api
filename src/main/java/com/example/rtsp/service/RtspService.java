package com.example.rtsp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.rtsp.model.RtspLink;
import com.example.rtsp.repository.RtspLinkRepository;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class RtspService {

    @Autowired
    private RtspLinkRepository rtspLinkRepository;

    private Map<Long, Process> ffmpegProcesses = new HashMap<>();

    @Value("${server.static.path}")
    private String staticPath;

    public Iterable<RtspLink> getRtspLinks() {
        return rtspLinkRepository.findAll();
    }
    
    public ResponseEntity<?> saveRtspLink(String rtspName, String rtspUrl) {
        if (rtspName.isEmpty() || rtspUrl.isEmpty()) {
            return ResponseEntity.badRequest().body("Rtsp name or url cannot be null");
        }
        if (rtspLinkRepository.findByName(rtspName) != null) {
            return ResponseEntity.badRequest().body("Rtsp link with this name already exists");
        }
        if (rtspLinkRepository.findByUrl(rtspUrl) != null) {
            return ResponseEntity.badRequest().body("This rtsp link already exists by name: " + rtspLinkRepository.findByUrl(rtspUrl).getName());
        }

        RtspLink rtspLink = new RtspLink();
        rtspLink.setName(rtspName);
        rtspLink.setUrl(rtspUrl);
        rtspLinkRepository.save(rtspLink);

        return ResponseEntity.ok().body("Rtsp link saved");
    }

    public ResponseEntity<?> deleteRtspLink(String rtspName) {
        RtspLink rtspLink = rtspLinkRepository.findByName(rtspName);
        if (rtspLink != null) {
            rtspLinkRepository.deleteById(rtspLink.getId());
            return ResponseEntity.ok().body("Rtsp link deleted");
        } else {
            return ResponseEntity.badRequest().body("Rtsp link not found");
        }
    }

    public String startStream(String rtspName) {
        RtspLink rtspLink = rtspLinkRepository.findByName(rtspName);
        String rtspUrl = rtspLink.getUrl();

        String outputPath = staticPath + "rtsp/" + rtspLink.getId() + "_playlist.m3u8";
        ProcessBuilder processBuilder = new ProcessBuilder(
            "ffmpeg",
            "-rtsp_transport", "tcp",
            "-i", rtspUrl,
            "-analyzeduration", "5000000",
            "-probesize", "100000000",
            "-f", "hls",
            "-hls_time", "2",
            "-hls_list_size", "5",
            "-hls_flags", "delete_segments",
            "-hls_allow_cache", "0",
            "-vf", "yadif",
            "-c:v", "libx264",
            "-preset", "veryfast",
            "-tune", "zerolatency",
            "-x264-params", "keyint=45",
            "-c:a", "aac",
            "-b:a", "128k",
            "-max_muxing_queue_size", "1024",
            outputPath
        );

        processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);

        // Path without m3u8 file
        Path pathWithoutM3u8 = Paths.get(staticPath + "rtsp");
        if (!Files.exists(pathWithoutM3u8)) {
            try {
                Files.createDirectories(pathWithoutM3u8);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            if (ffmpegProcesses.containsKey(rtspLink.getId())) {
                return "/rtsp/" + rtspLink.getId() + "_playlist.m3u8";
            }
            Process process = processBuilder.start();
            ffmpegProcesses.put(rtspLink.getId(), process);

            while (!Files.exists(Paths.get(outputPath))) {
                try {
                    Thread.sleep(100); // Ожидание 100мс перед следующей проверкой
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
            Thread.sleep(1000);
            return "/rtsp/" + rtspLink.getId() + "_playlist.m3u8";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public void stopStream(Long id) {
        Process process = ffmpegProcesses.get(id);
        if (process != null) {
            process.destroy();
            ffmpegProcesses.remove(id);

            String outputPath = staticPath + "rtsp/" + id + "_playlist.m3u8";
            File outputFile = new File(outputPath);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            // Удаление всех файлов сегментов .ts
            File dir = new File(staticPath +"rtsp/");
            for (File file : dir.listFiles()) {
                if (file.getName().matches(id + "_(.*).ts")) {
                    file.delete();
                }
            }
        }
    }
}
