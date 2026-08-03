package com.movieday.backend.domain;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDto {
    private String userId;
    private String videoStyle;
    private Long price;
    private boolean bgmYn;
    private boolean narrationYn;
    private String textStory;
    private List<String> fileNames; // 📸 프론트에서 ["pic1.jpg", "pic2.png"] 형태로 받아올 리스트
}