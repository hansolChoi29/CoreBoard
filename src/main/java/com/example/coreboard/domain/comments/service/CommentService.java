package com.example.coreboard.domain.comments.service;

import com.example.coreboard.domain.comments.repository.CommentRepository;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    private final CommentRepository CommentRepository;

    public CommentService(CommentRepository commentRepository) {
        CommentRepository = commentRepository;
    }
}
