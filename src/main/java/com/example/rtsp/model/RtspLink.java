package com.example.rtsp.model;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyJoinColumn;
import lombok.Data;

@Data
@Entity
public class RtspLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String url;

    @ElementCollection
    @CollectionTable(name = "user_rtsp_link_names", joinColumns = @JoinColumn(name = "rtsp_link_id"))
    @MapKeyJoinColumn(name = "user_id")
    @Column(name = "name")
    @Cascade(value = {CascadeType.ALL})
    private Map<User, String> names = new HashMap<>();
}
