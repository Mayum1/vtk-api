package com.example.rtsp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.rtsp.model.RtspLink;
import com.example.rtsp.model.User;

public interface RtspLinkRepository extends JpaRepository<RtspLink, Long> {
    RtspLink findByUrl(String url);

    @Query("SELECT rl FROM RtspLink rl JOIN rl.names un WHERE KEY(un) = :user")
    List<RtspLink> findAllByUser(@Param("user") User user);

    @Query("SELECT rl FROM RtspLink rl JOIN rl.names un WHERE KEY(un) = :user AND VALUE(un) = :name")
    RtspLink findByUserAndName(@Param("user") User user, @Param("name") String name);

    @Modifying
    @Query("DELETE FROM RtspLink rtspLink WHERE rtspLink.id = :id")
    void deleteById(@Param("id") Long id);
}
