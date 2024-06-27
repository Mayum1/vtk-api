package com.example.rtsp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.rtsp.model.CSVTable;
import com.example.rtsp.service.CSVService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/csv")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CSV')")
public class CSVController {
    @Autowired
    private CSVService csvService;

    @GetMapping("/")
    public Iterable<CSVTable> getCSVTables() {
        return csvService.getCSVs();
    }
    
    @PostMapping("/save")
    public ResponseEntity<?> saveCSVTable(@RequestParam MultipartFile csvFile, HttpSession session) {
        return csvService.saveCSVTable(csvFile, session);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteCSVTable(@RequestParam String csvName, HttpSession session) {
        return csvService.deleteCSVTable(csvName, session);
    }

    @PostMapping("/path")
    public String getCSVPath(@RequestParam String csvName, HttpSession session) {
        return csvService.getCSVPath(csvName, session);
    }

    @PostMapping("/")
    public Iterable<CSVTable> getCSVTablesByUser(HttpSession session) {
        return csvService.getCSVsByUser(session);
    }
}
