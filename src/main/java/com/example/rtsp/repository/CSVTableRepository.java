package com.example.rtsp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.rtsp.model.CSVTable;
import com.example.rtsp.model.User;

public interface CSVTableRepository extends JpaRepository<CSVTable, Long> {
    CSVTable findByNameAndUser(String name, User user);
    List<CSVTable> findAllByUser(User user);
}
