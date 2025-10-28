package com.socialblog.dto;

import com.socialblog.model.enums.ReactionType;
import lombok.Data;

@Data
public class ReactionRequest {
    private Long postId;
    private ReactionType type;
}