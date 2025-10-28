package com.socialblog.dto;

import com.socialblog.model.enums.Visibility;
import lombok.Data;

@Data
public class PostRequest {
    private String content;
    private String imageUrl;
    private Visibility visibility = Visibility.PUBLIC;
}