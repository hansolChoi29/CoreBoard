package com.example.coreboard.domain.comments.controller;

import com.example.coreboard.domain.comments.service.CommentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comment")
public class CommentController {
    private final CommentService CommentService;

    public CommentController(CommentService commentService) {
        CommentService = commentService;
    }
}
