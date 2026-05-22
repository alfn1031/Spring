package com.kh.spring.board.model.service;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.kh.spring.board.model.dao.BoardMapper;
import com.kh.spring.board.model.dto.BoardDto;
import com.kh.spring.exception.AuthorizationException;
import com.kh.spring.member.model.dto.MemberDto;
import com.kh.spring.util.PageInfo;
import com.kh.spring.util.Pagination;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequestMapping("boards")
@RequiredArgsConstructor
public class BoardService {
	
	private final BoardMapper boardMapper;
	private final Pagination pagenation;
	
	/*
	 * 1. @Transactional은 public메서드만 동작
	 * 
	 * 2. 같은 클래스 안에서 메서드끼리 호출하면 트랜잭션 안걸림
	 * 
	 * 3. CheckedException은 기본적으로 롤백 안됨
	 * 
	 * 4. 의존성 이슈
	 * 
	 */
	
	
	// 안정장치 X => 성능최적화 + 의도표시
	@Transactional(readOnly = true) // DB작업할 떄 이 트래잭션은 데이터 변경합니다 => 힌트
	public Map findAll(int page) {
		// 유효성 검증
		if(page < 1) {
			throw new InvalidParameterException("잘못된 접근입니다");
		}
		// 실질적인 비즈니스 로직 => 페이징 처리를 위한 PageInfo객체 생성 및 페이징 처리후 게시글 조회
		
		 int totalCount = boardMapper.selectTotalCount();
		 
		 // log.info("총 게시글 개수 : {}", totalCount);
		 PageInfo pi = pagenation.getPageInfo(totalCount,  page,  5,  3);
		 
		 RowBounds rb = new RowBounds((page - 1) * 5, 5);
		 List<BoardDto> boards = boardMapper.findAll(rb); // 실제로 페이징 처리시에는 SQL문에서 해결(OFFSET문법)하는것을 권장
		 // log.info("게시글 목록 : {}", boards);
		 
		 return Map.of("boards", boards, "pi", pi);
	}
	
	@Transactional // <tx:annotion-driven이 커지면 스프링이 @Transactional이 붙은 메서드를 발견해서 프록시로 감쌈
				   // 프록시 객체 내부에서 save()메서드를 호출할 때 connection.setAutoCommit(false)로 돌리고 호출
				   // 그 후 메서드 종료시 commit()을 호출 예외 발생시 rollback()
	public void save(BoardDto board, MultipartFile upfile, HttpSession session) {
		// 1. 권한 검증
		validateUser(board, session);
		
		// 유효성 검증
		validateContent(board);
		
		// 3. 파일이 있을 경우 이름을 바꿔서 서버에 업로드 => 파일의 정보를 board의 필드에 대입
		fileUpload(upfile, board, session);
		
		// 4. 
		int result = boardMapper.save(board);
		
		if(result != 1) {
			throw new RuntimeException("관리자에게 문의하세요");
		}
		
	}
	
	private void fileUpload(MultipartFile upfile, BoardDto board, HttpSession session) {
		
		if(!upfile.getOriginalFilename().isEmpty()) {
					
			StringBuilder sb = new StringBuilder();
			sb.append("KH");
			sb.append(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
			sb.append("_");
			sb.append((int)(Math.random() * 900) + 100);
			sb.append(upfile.getOriginalFilename().substring(upfile.getOriginalFilename().lastIndexOf(".")));
			
			//파일 업로드
			ServletContext application = session.getServletContext();
			String savePath = application.getRealPath("/resources/files/");
			
			try {
				upfile.transferTo(new File(savePath + sb.toString()));
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}
			
			// board setting
			board.setOriginName(upfile.getOriginalFilename());
			board.setChangeName("/spring/resources/files/" + sb.toString());
			
		}
	}
	
	private void validateUser(BoardDto board, HttpSession session) {
		String boardWriter = board.getBoardWriter();
		MemberDto userInfo = (MemberDto)session.getAttribute("userInfo");
		
		if(userInfo == null || !userInfo.getUserId().equals(boardWriter)) {
			throw new AuthorizationException("권한없는 요청입니다");
		}
	}
	
	private void validateContent(BoardDto board) {
		if(board.getBoardTitle().trim().isEmpty() || board.getBoardContent().trim().isEmpty()) {
			throw new InvalidParameterException("권한없는 요청입니다");
		}
		
		String boardTitle = board.getBoardTitle().replaceAll("<", "&lt;").replaceAll("\n", "<br>");
		String boardContent = board.getBoardContent().replaceAll("<", "&lt;").replaceAll("\n", "<br>");
		if(board.getBoardTitle().contains("바보")) {
			boardTitle = board.getBoardTitle().replace("바보", "글쓴이바보");
		}
		board.setBoardTitle(boardTitle);
		board.setBoardContent(boardContent);
	}
	
	public BoardDto findByBoardNo(Long boardNo) {
		
		if(boardNo < 1 || boardNo == null) {
			throw new InvalidParameterException("유효하지 않은 값입니다");
		}
		
		int result = boardMapper.increaseCount(boardNo);
		
		if(result!= 1){
			throw new InvalidParameterException("잘못된 요청입니다");
		}
		
		BoardDto board = boardMapper.findByBoardNo(boardNo);
		
		if(board == null) {
			throw new InvalidParameterException("존재하지않는 게시글입니다");
		}
		
		return board;
	}
	
	public Map findByKeyword(String condition, String keyword, int page) {
		
		// 사용자가 선택한 condition과 사용자가 입력한 keyword를 가지고
		// DB상에서 조건을 검색해서 나온조회 결과물을
		// 페이징 처리까지 끝내고난 뒤 페이징객체와 함께 응답할 것
		
		// if(!condition.equals("title) || !condition.equals("content)
		
		int searchedCount = boardMapper.findByKeywordCount(condition, keyword);
		
		// log.info("검색결과 개수 : {}", searchedCount);
		
		PageInfo pi = pagenation.getPageInfo(searchedCount, page, 3, 3);
		RowBounds rb = new RowBounds((page - 1) * 3, 3);
		
		List<BoardDto> boards = boardMapper.findByKeyword(condition, keyword, rb);
		// log.info("검색결과 : {}", boards);
		
		return Map.of("boards", boards, "pi", pi);
	}

	public int count() {
		return boardMapper.selectTotalCount();
	}
	
	
	
	
}


