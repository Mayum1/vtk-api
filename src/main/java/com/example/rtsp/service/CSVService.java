package com.example.rtsp.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.rtsp.model.CSVTable;
import com.example.rtsp.model.User;
import com.example.rtsp.repository.CSVTableRepository;
import com.example.rtsp.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class CSVService {

    @Autowired
    private CSVTableRepository csvTableRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${server.static.path}")
    private String staticPath;

    public Iterable<CSVTable> getCSVs() {
        return csvTableRepository.findAll();
    }

    public ResponseEntity<?> saveCSVTable(MultipartFile csvFile, HttpSession session) {
        if (csvFile.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        if (!csvFile.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity.badRequest().body("File is not CSV");
        }

        String username = (String) session.getAttribute("username");

        if (!Files.exists(Paths.get(staticPath +"/csv/" + username))) {
            try {
                Files.createDirectory(Paths.get(staticPath + "csv/" + username));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error creating directory");
            }
        }

        String fileName = csvFile.getOriginalFilename();
        String baseName = getBaseName(fileName);
        String fileExtension = getExtension(fileName);
        int count = 0;
        while (fileExists(fileName, username)) {
            count++;
            fileName = baseName + "_" + count + fileExtension;
        }

        try {
            Path path = Paths.get(staticPath + "csv/" + username + "/" + fileName);
            Files.copy(csvFile.getInputStream(), path);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error saving file: " + e);
        }

        CSVTable csvTable = new CSVTable();
        csvTable.setName(fileName);
        csvTable.setUser(userRepository.findByUsername(username).get());
        csvTableRepository.save(csvTable);

        return ResponseEntity.ok().body("File saved: " + fileName);
    }

    public ResponseEntity<?> deleteCSVTable(String csvName, HttpSession session) {
        String username = (String) session.getAttribute("username");
        CSVTable csvTable = csvTableRepository.findByNameAndUser(csvName, userRepository.findByUsername(username).get());
        if (csvTable != null) {
            Path path = Paths.get(staticPath + "csv/" + username + "/" + csvName);
            try {
                Files.delete(path);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error deleting file: " + e);
            }
            csvTableRepository.deleteById(csvTable.getId());
            return ResponseEntity.ok().body("CSV table deleted");
        } else {
            return ResponseEntity.badRequest().body("CSV table not found");
        }
    }

    public List<CSVTable> getCSVsByUser(HttpSession session) {
        String username = (String) session.getAttribute("username");
        User user = userRepository.findByUsername(username).get();
        return csvTableRepository.findAllByUser(user);
    }

    public String getCSVPath(String csvName, HttpSession session) {
        String username = (String) session.getAttribute("username");
        File file = new File(staticPath + "csv/" + username + "/" + csvName);
        if (file.exists()) {
            return "/csv/" + username + "/" + csvName;
        } else {
            return "File not exist";
        }
    }

    private String getBaseName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return fileName.substring(0, dotIndex);
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return fileName.substring(dotIndex);
    }

    private boolean fileExists(String fileName, String username) {
        File file = new File(staticPath + "csv/" + username + "/" + fileName);
        return file.exists();
    }
}
