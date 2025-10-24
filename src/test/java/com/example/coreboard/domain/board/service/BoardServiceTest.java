package com.example.coreboard.domain.board.service;

import com.example.coreboard.domain.board.dto.*;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import com.example.coreboard.domain.common.response.PageResponse;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class) // 모키토 준비 완료!
class BoardServiceTest {
    // 목표 : 서비스 내부에서 레포지토리에게 무엇을 요청햇는지, 그 결과를 받아 어떻게 처리했는지를 확인

    // 왜 레포지토리를 가짜로 할까? : DB에 의존하면 느려지고 실패원인이 DB/네트워크/데이터 상태로 섞여 버림
    // 스텁 : 레포지토리 반환값을 내가 스크립트함
    // 검증 : 서비스가 정확한 인자로 레포를 불렀는지, 딱 그만큼만 불렀는지 확인

    @Mock
    BoardRepository boardRepository;

    @Mock
    UsersRepository usersRepository;

    @InjectMocks // 테스트할 대상
    BoardService boardService;

    // 어노테이션 없는 이유 : 내부 필드가 초기화 안됨 그래서 진짜 값(new)이 필요하기 때문
    BoardCreateRequest boardCreateRequest;
    BoardCreateCommand boardCreateCommand;

    // 실행 전마다 초기화
    @BeforeEach
    void setUpCreate() {
        boardCreateRequest = new BoardCreateRequest("제목", "내용");
        //  커맨드 초기화 추가
        boardCreateCommand = new BoardCreateCommand("제목", "내용");
    }

    // AAA 패턴
    // given : 상황이 이렇다(조건 주입) (가짜데이터, mock 설정)
    // when : 이걸 실행했을 때(행동주입) (실제로 메서드를 호출)
    // then : 결과는 이렇게 나와야 한다(기대결과) (검증, verify, assert)

    @Test
    @DisplayName("게시글_생성")
    void create() {
        // given
        Users users = mock(Users.class); // 빈껍데기 - 생성이니깐 가짜 객체 써야 함
        // Users users = new Users("tester", "encodePassword", "email@naver.com", "phoneNumber"); // 진짜 객체면 안됨
        // 모키토가 진짜 객체라고 에러 던짐

        // findByUsername에 tester가 오면 서비스가 먼저 유저를 찾으니까, 유저가 있다고 응답해 줘야 다음 단계 진행 가능
        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(users));

        // mock(Users.class) 쓸 거면, 같이 써야 하는 이유: 빈 껍데기기 때문에 id가 null 이기 때문에 10L 스텁해야 함
        given(users.getUserId()).willReturn(10L);

        // 제목이 중복이 아니어야 save 단계로 간다
        given(boardRepository.existsByTitle("제목")).willReturn(false);

        // save를 호출되면 DB저장말고 내가 정해준 Board 객체에 넣어라
        given(boardRepository.save(any(Board.class))).willReturn( // 레포의 save 호출될 때 빈 껍데기 Board가 들어오면 반환해라
                new Board(1L, 10L, "제목", "내용", LocalDateTime.now(), LocalDateTime.now()) // 인스턴스한 Board를, 필드에 데이터 채워서
        );

        //when
        BoardCreateDto result = boardService.create(boardCreateCommand, "tester");

        // then 1. assert는 리턴값을 검증, 2. verify는 메서드 호출자체를 검증
        assertNotNull(result);
        assertEquals("제목", result.getTitle());
        assertEquals("내용", result.getContent());

        // time(1) : usersRepository.findByUsername("tester")가 1번 호출되었는지
        // 2번 이상이면 TooManyActualInvocations 에러 던지게
        verify(usersRepository, times(1)).findByUsername("tester");
        verify(boardRepository, times(1)).existsByTitle("제목");
        verify(boardRepository, times(1)).save(any(Board.class));
    }

    @Test
    @DisplayName("게시글_생성_예외_유저없음_404")
    void createUserNotFound() {
        // given(mock, stub)
        // 없는 유저
        given(usersRepository.findByUsername("tester")).willReturn(Optional.empty());
        AuthErrorException notFoundUser = assertThrows(
                AuthErrorException.class,
                () -> boardService.create(
                        boardCreateCommand, // 인스턴스화 안 해도 통과되는 이유 : findByUsername(username).orElseThrow 없으면 걍 에러던짐
                        "tester"
                )
        );
        assertEquals(404, notFoundUser.getStatus());

        verify(boardRepository, never()).existsByTitle(anyString());
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글_생성_중복_제목_409")
    void createTitleDuplicated() {
        // 유저는 있어야 함 (Optional.empty()가 아님)
        Users users = mock(Users.class);
        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(users));
        // 제목 중복
        given(boardRepository.existsByTitle("제목")).willReturn(true);

        BoardErrorException duplicatedBoard = assertThrows(
                BoardErrorException.class,
                () -> boardService.create(
                        boardCreateCommand,
                        "tester"
                )
        );
        assertEquals(409, duplicatedBoard.getStatus());

        verify(usersRepository).findByUsername("tester");
        verify(boardRepository).existsByTitle("제목");
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글_단건_조회_성공")
    void findOne() {
        Long id = 1L;
        Board entity = new Board(
                1L,
                10L,
                "제목",
                "본문",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        given(boardRepository.findById(id)).willReturn(Optional.of(entity));

        BoardGetOneCommand boardGetOneCommand = new BoardGetOneCommand(id);

        BoardGetOneDto out = boardService.findOne(boardGetOneCommand);

        assertNotNull(out);
        assertEquals(id, out.getId());
        assertEquals("제목", out.getTitle());
        assertEquals("본문", out.getContent());
        assertNotNull(out.getCreatedDate());

        verify(boardRepository, times(1)).findById(id);
        verifyNoMoreInteractions(boardRepository);
    }

    // 예외처리 - 존재하지 않는 게시글
    @Test
    @DisplayName("게시글_단건_조회_미존재_404")
    void findOnNotFound() {
        Long id = 1L;
        given(boardRepository.findById(id)).willReturn(Optional.empty());
        BoardGetOneCommand command = new BoardGetOneCommand(id); // 인스턴스화 해줘야 하는 이유 : findById(boardGetOneCommand
        // .getId()).orElseThrow 에러 던지기 전에 NEP 발생
        BoardErrorException findOneNotFound = assertThrows(
                BoardErrorException.class,
                () -> boardService.findOne(command)
        );
        assertEquals(404, findOneNotFound.getStatus());
        verify(boardRepository, times(1)).findById(id);
        verifyNoMoreInteractions(boardRepository);
    }

    @Test
    @DisplayName("게시글_전체조회_성공")
    void findAll() {
        // 입력 파라미터 준비 : 서비스 메서드가 받는 것과 같은 모양
        int page = 0;
        int size = 10;
        String sort = "asc";

        // given : 입력값
        // Pageable pageable = mock(Pageable.class);

        // (page,size, sort)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "title"));

        // 가짜 리스트 만들기 - ArgumanetCaptor로 실제 서비스가 만든 Pagealbe 잡아서 페이지/크기/정렬 값이 맞는지 비교하려고 기준값 만듦
        List<Board> boards = List.of( // List한 이유는 PageImpl 만들기 위함
                new Board(1L, 10L, "제목1", "내용1", LocalDateTime.now(), LocalDateTime.now()),
                new Board(2L, 20L, "제목2", "내용2", LocalDateTime.now(), LocalDateTime.now()),
                new Board(3L, 10L, "제목3", "내용3", LocalDateTime.now(), LocalDateTime.now())
        );

        // PageImpl : 테스트에서 가짜 Page 만들려고 쓰는 구현체임 (서비스단에는 없음)
        Page<Board> pageResult = new PageImpl<>(boards, pageable, boards.size());
        // 서비스가 repo.findAll(any Pageable) 부르면 pageResult를 돌려줘!
        given(boardRepository.findAll(any(Pageable.class))).willReturn(pageResult);

        // when : 메서드
        PageResponse<BoardSummaryResponse> result = boardService.findAll(page, size, sort);

        // then : 1) 페이징 검즘 : Page -> PageResponse로 제대로 포장했는지 (API응답 형식)
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(boards.size(), result.getTotalElements());
        assertEquals(boards.size(), result.getContent().size());
        // then : 2) 매핑 검증 : 첫 번째 entity -> DTO로 잘 변환 됐는지
        Board firstEntity = boards.get(0);
        BoardSummaryResponse firstDto = result.getContent().get(0);
        assertEquals(firstEntity.getId(), firstDto.getId());
        assertEquals(firstEntity.getUserId(), firstDto.getUserId());
        assertEquals(firstEntity.getTitle(), firstDto.getTitle());

        // then : 3) 인자 검증 : 정말로 올바른 Pageable로 호출했는지
        // ArgumentCaptor : 전달된 값을 나중에 비교하기 위해 잠깐 저장
        // forClass : 어떤 타입을 담을지
        // capture() : 전달된 값을 상자에 저장
        // getValue() : 실제 전달된 값 확인
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(boardRepository, times(1)).findAll(captor.capture());
        Pageable used = captor.getValue();
        assertEquals(page, used.getPageNumber());
        assertEquals(size, used.getPageSize());
        assertEquals(Sort.by(Sort.Direction.ASC, "title"), used.getSort());

        verifyNoMoreInteractions(boardRepository);
    }

    @Test
    @DisplayName("게시글_전체_조회_desc_성공")
    void findAllDesc() {
        // given
        int page = 0;
        int size = 10;
        String sort = "desc";

        // 정렬방향 desc 기대
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "title"));

        List<Board> boards = List.of(
                new Board(1L, 10L, "제목1", "내용1", LocalDateTime.now(), LocalDateTime.now()),
                new Board(2L, 20L, "제목2", "내용2", LocalDateTime.now(), LocalDateTime.now()),
                new Board(3L, 30L, "제목3", "내용3", LocalDateTime.now(), LocalDateTime.now())
        );

        Page<Board> pageResult = new PageImpl<>(boards, pageable, boards.size());
        given(boardRepository.findAll(any(Pageable.class))).willReturn(pageResult);

        // when
        PageResponse<BoardSummaryResponse> result = boardService.findAll(page, size, sort);

        // then 1) 페이징 래핑 검증
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(boards.size(), result.getTotalElements());
        assertEquals(boards.size(), result.getContent().size());

        // then 2) 인자 검증
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(boardRepository, times(1)).findAll(captor.capture());
        Pageable used = captor.getValue();

        // then 3) Pageable 인자 검증
        assertEquals(page, used.getPageNumber());
        assertEquals(size, used.getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC, "title"), used.getSort());

        verifyNoMoreInteractions(boardRepository);
    }

    @Test
    @DisplayName("게시글_수정_성공")
    void update() {
        Long id = 1L;
        Users users = mock(Users.class);
        Board board = mock(Board.class);
        BoardUpdateCommand cmd = new BoardUpdateCommand( // 입력된 값과 시제 테스트 할 데이터 값이 일치해야 함
                "tester",
                id,
                "새제목",
                "새본문"
        );
        // given
        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(users)); // 유저 조회 성공하도록 
        given(boardRepository.findById(id)).willReturn(Optional.of(board)); // 게시글 조회 성공하도록 세팅
        // 권한 검사에 필요한 값들
        given(users.getUserId()).willReturn(10L); // 요청 보낸 사람의 userId
        given(board.getUserId()).willReturn(10L); // 게시글 주인 userId 같게
        //id
        given(board.getId()).willReturn(id);

        // when
        BoardUpdatedDto result = boardService.update(cmd);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(board).update("새제목", "새본문");
    }

    @Test
    void delete() {
    }
}