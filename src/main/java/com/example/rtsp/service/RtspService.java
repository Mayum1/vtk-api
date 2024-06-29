package com.example.rtsp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.rtsp.model.RtspLink;
import com.example.rtsp.model.User;
import com.example.rtsp.repository.RtspLinkRepository;
import com.example.rtsp.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RtspService {

    @Autowired
    private RtspLinkRepository rtspLinkRepository;

    @Autowired
    private UserRepository userRepository;

    private Map<Long, Process> ffmpegProcesses = new HashMap<>();
    
    @Value("${server.static.path}")
    private String staticPath;

    public Iterable<RtspLink> getRtspLinks() {
        return rtspLinkRepository.findAll();
    }

    public List<String> getRtspLinksByUser(HttpSession session) {
        String username = (String) session.getAttribute("username");
        User user = userRepository.findByUsername(username).get();

        List<String> rtspLinks = new ArrayList<>();
        List<RtspLink> userRtspLinks = rtspLinkRepository.findAllByUser(user);

        for (RtspLink rtspLink : userRtspLinks) {
            String linkName = rtspLink.getNames().get(user);
            if (linkName != null) {
                rtspLinks.add(linkName);
            }
        }

        return rtspLinks;
    }
    
    public ResponseEntity<?> saveRtspLink(String rtspName, String rtspUrl, HttpSession session) {
        if (rtspName.isEmpty() || rtspUrl.isEmpty()) {
            return ResponseEntity.badRequest().body("Rtsp name or url cannot be null");
        }
        
        String username = (String) session.getAttribute("username");
        User user = userRepository.findByUsername(username).get();

        if (rtspLinkRepository.findByUserAndName(user, rtspName) != null) {
            return ResponseEntity.badRequest().body("Rtsp link with this name already exists");
        }

        if (rtspLinkRepository.findAllByUser(user).contains(rtspLinkRepository.findByUrl(rtspUrl))) {
            return ResponseEntity.badRequest().body("Rtsp link with this url already exists. Name: " + rtspLinkRepository.findByUrl(rtspUrl).getNames().get(user));
        }

        RtspLink rtspLink = new RtspLink();

        if (rtspLinkRepository.findByUrl(rtspUrl) == null) {
            rtspLink.setUrl(rtspUrl);
            rtspLink.getNames().put(user, rtspName);
        } else {
            rtspLink = rtspLinkRepository.findByUrl(rtspUrl);
            rtspLink.getNames().put(user, rtspName);
        }

        rtspLinkRepository.save(rtspLink);

        return ResponseEntity.ok().body("Rtsp link saved");
    }

    public ResponseEntity<?> deleteRtspLink(String rtspName, HttpSession session) {
        String username = (String) session.getAttribute("username");
        User user = userRepository.findByUsername(username).get();

        RtspLink rtspLink = rtspLinkRepository.findByUserAndName(user, rtspName);

        if (rtspLink == null) {
            return ResponseEntity.badRequest().body("Rtsp link not found");
        }

        rtspLink.getNames().remove(user, rtspName);
        if (rtspLink.getNames().isEmpty()) {
            rtspLinkRepository.deleteById(rtspLink.getId());
        }

        rtspLinkRepository.save(rtspLink);
        
        return ResponseEntity.ok().body("Rtsp link deleted");
    }

    public ResponseEntity<?> startStream(String rtspName, HttpSession session) {
        String username = (String) session.getAttribute("username");
        User user = userRepository.findByUsername(username).get();
        RtspLink rtspLink = rtspLinkRepository.findByUserAndName(user, rtspName);
        String rtspUrl = rtspLink.getUrl();
        Path path = Paths.get(staticPath).resolve("rtsp").resolve(rtspLink.getId().toString());
        String outputPath = path.resolve("playlist.m3u8").toString();

        ProcessBuilder processBuilder = new ProcessBuilder(
            "ffmpeg",
            "-rtsp_transport", "tcp",
            "-i", rtspUrl,
            "-analyzeduration", "5000000",
            "-probesize", "100000000",
            "-f", "hls",
            "-hls_time", "5",
            "-hls_list_size", "1",
            "-hls_flags", "delete_segments",
            "-hls_allow_cache", "0",
            "-vf", "yadif",
            "-c:v", "libx264",
            "-preset", "veryfast",
            "-tune", "zerolatency",
            "-x264-params", "keyint=15",
            "-c:a", "aac",
            "-b:a", "128k",
            "-max_muxing_queue_size", "1024",
            outputPath
        );

        processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);

        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error creating directory");
            }
        }

        try {
            if (ffmpegProcesses.containsKey(rtspLink.getId())) {
                return ResponseEntity.ok().body("/rtsp/" + rtspLink.getId() + "/playlist.m3u8");
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
            return ResponseEntity.ok().body("/rtsp/" + rtspLink.getId() + "/playlist.m3u8");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    public void stopStream(Long id) {
        Process process = ffmpegProcesses.get(id);
        if (process != null) {
            process.destroy();
            ffmpegProcesses.remove(id);

            String outputPath = staticPath + "rtsp/" + id + "/playlist.m3u8";
            File outputFile = new File(outputPath);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            // Удаление всех файлов сегментов .ts
            File dir = new File(staticPath + "rtsp/" + id);
            for (File file : dir.listFiles()) {
                if (file.getName().matches(".*\\.ts$")) {
                    file.delete();
                }
            }
        }
    }

    public void stopAllStreams() {
        for (Long id : ffmpegProcesses.keySet()) {
            stopStream(id);
        }
    }
}
