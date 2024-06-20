package com.example.rtsp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.rtsp.model.CSVTable;

public interface CSVTableRepository extends JpaRepository<CSVTable, Long> {
    CSVTable findByName(String name);
}
