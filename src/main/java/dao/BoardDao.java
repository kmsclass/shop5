package dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import javax.validation.Valid;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import dao.mapper.BoardMapper;
import logic.Board;

@Repository 
public class BoardDao {
	@Autowired
	private SqlSessionTemplate template;
	private Map<String,Object> param = new HashMap<>();
	public int count(String searchtype, String searchcontent) {
		if(searchtype != null && searchcontent != null) { //검색 요청
			param.put("searchtype",searchtype);
			param.put("searchcontent",searchcontent);
		}
		return template.getMapper(BoardMapper.class).count(param);
	}
	public List<Board> list(Integer pageNum, int limit, 
			                       String searchtype, String searchcontent) {
		param.clear();
		if(searchtype != null && searchcontent != null) { //검색 조건 존재
			param.put("searchtype",searchtype);
			param.put("searchcontent",searchcontent);
		}
		param.put("startrow", (pageNum - 1) * limit);
		param.put("limit",  limit);		
		return template.getMapper(BoardMapper.class).select(param);
	}
	public void readcntadd(Integer num) {
		template.getMapper(BoardMapper.class).readcntadd(num);
	}
	public Board selectOne(Integer num) {
		param.clear();
		param.put("num", num);
		return template.getMapper(BoardMapper.class).select(param).get(0);
	}
	public int maxNum() {
		return template.getMapper(BoardMapper.class).maxnum();
	}
	public void write(Board board) {
		template.getMapper(BoardMapper.class).insert(board);
		
	}
	public void updateGrpStep(Board board) {
		template.getMapper(BoardMapper.class).updateGrpStep(board);
	}
	public void update(Board board) {
		template.getMapper(BoardMapper.class).update(board);		
	}
	public void delete(int num) {
		template.getMapper(BoardMapper.class).delete(num);
	}
	public List<Map<String, Object>> graph1() {
		return template.getMapper(BoardMapper.class).graph1();
	}
	public List<Map<String, Object>> graph2() {
		return template.getMapper(BoardMapper.class).graph2();
	}
}
