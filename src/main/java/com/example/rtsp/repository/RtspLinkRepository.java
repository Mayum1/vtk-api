package com.example.rtsp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.rtsp.model.RtspLink;

public interface RtspLinkRepository extends JpaRepository<RtspLink, Long> {
    RtspLink findByUrl(String url);
    RtspLink findByName(String name);
}
