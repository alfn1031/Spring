package com.kh.spring.board.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import com.kh.spring.board.model.dto.BoardDto;

@Mapper
public interface BoardMapper {
	
	int selectTotalCount();
	
	List<BoardDto> findAll(RowBounds rb);
	
	int save(BoardDto board);
	
	int increaseCount(Long boardNo);
	
	BoardDto findByBoardNo(Long boardNo);
	
						  // @Param에노테이션을 사용해서 여러개의 인자를 전달할 경우 여러개의 인자값을 하나의 Map믜 요소로 담아서 전달함!
	int findByKeywordCount(@Param(value="condition") String condition,@Param(value="keyword") String ketword);
	
	
	List<BoardDto> findByKeyword(@Param(value="condition") String condition, @Param(value="keyword") String ketword, RowBounds rb);
	
	
	
	
	
	
	
}
