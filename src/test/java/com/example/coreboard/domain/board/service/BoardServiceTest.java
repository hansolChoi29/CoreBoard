package com.example.coreboard.domain.board.service;

import com.example.coreboard.domain.board.dto.BoardCreateRequest;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;


@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    BoardRepository boardRepository;

    @Mock
    UsersRepository usersRepository;

    @InjectMocks
    BoardService boardService;

    MockMvc mockMvc;

    @Test
    @DisplayName("게시글 생성")
    void create() throws Exception{
        // given
        String username="tester";
        long userId=10;
        BoardCreateRequest request = new BoardCreateRequest("제목", "내용");

//        Users userMock = mock(Users.class);




        // when

        //then

    }

    @Test
    void findOne() {
    }

    @Test
    void findAll() {
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }
}