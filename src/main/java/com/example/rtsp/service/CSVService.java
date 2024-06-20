package com.example.rtsp.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.rtsp.model.CSVTable;
import com.example.rtsp.repository.CSVTableRepository;

@Service
public class CSVService {

    @Autowired
    private CSVTableRepository csvTableRepository;

    @Value("${server.static.path}")
    private String staticPath;

    public Iterable<CSVTable> getCSVs() {
        return csvTableRepository.findAll();
    }

    public ResponseEntity<?> saveCSVTable(MultipartFile csvFile) {
        if (csvFile.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        if (!csvFile.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity.badRequest().body("File is not CSV");
        }

        if (!Files.exists(Paths.get(staticPath +"/csv"))) {
            try {
                Files.createDirectory(Paths.get(staticPath + "csv"));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error creating directory");
            }
        }

        String fileName = csvFile.getOriginalFilename();
        String baseName = getBaseName(fileName);
        String fileExtension = getExtension(fileName);
        int count = 0;
        while (fileExists(fileName)) {
            count++;
            fileName = baseName + "_" + count + fileExtension;
        }

        try {
            Path path = Paths.get(staticPath + "csv/" + fileName);
            Files.copy(csvFile.getInputStream(), path);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error saving file: " + e);
        }

        CSVTable csvTable = new CSVTable();
        csvTable.setName(fileName);
        csvTableRepository.save(csvTable);

        return ResponseEntity.ok().body("File saved: " + fileName);
    }

    public ResponseEntity<?> deleteCSVTable(String csvName) {
        CSVTable csvTable = csvTableRepository.findByName(csvName);
        if (csvTable != null) {
            Path path = Paths.get(staticPath + "csv/" + csvName);
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

    public String getCSVPath(String csvName) {
        File file = new File(staticPath + "csv/" + csvName);
        if (file.exists()) {
            return "/csv/" + csvName;
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

    private boolean fileExists(String fileName) {
        File file = new File(staticPath + "csv/" + fileName);
        return file.exists();
    }
}
