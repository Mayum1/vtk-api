package com.example.rtsp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.rtsp.model.CSVTable;
import com.example.rtsp.service.CSVService;

@RestController
@RequestMapping("/api/csv")
public class CSVController {
    @Autowired
    private CSVService csvService;

    @GetMapping("/")
    public Iterable<CSVTable> getCSVTables() {
        return csvService.getCSVs();
    }
    
    @PostMapping("/save")
    public ResponseEntity<?> saveCSVTable(@RequestParam MultipartFile csvFile) {
        return csvService.saveCSVTable(csvFile);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteCSVTable(@RequestParam String csvName) {
        return csvService.deleteCSVTable(csvName);
    }

    @PostMapping("/path")
    public String getCSVPath(@RequestParam String csvName) {
        return csvService.getCSVPath(csvName);
    }
}
