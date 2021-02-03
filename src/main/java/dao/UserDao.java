package dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import dao.mapper.UserMapper;
import logic.User;

@Repository
public class UserDao {
	@Autowired
	private SqlSessionTemplate template;
	private Map<String, Object> param = new HashMap<>();
	public void insert(User user) {
		template.getMapper(UserMapper.class).insert(user);
	}
	public User selectUserOne(String userid) {
		param.clear();
		param.put("userid", userid);
		List<User> list = template.getMapper(UserMapper.class).select(param);
		if(list == null || list.size() == 0) return null;
		else             return list.get(0);
	}
	public void update(User user) {
		template.getMapper(UserMapper.class).update(user);		
	}
	public void delete(String userid) {
		template.getMapper(UserMapper.class).delete(userid);
	}
	public List<User> list() {
		return template.getMapper(UserMapper.class).select(null);
	}
	public List<User> list(String[] idchks) {
		param.clear();
		param.put("userids", idchks);
		return template.getMapper(UserMapper.class).select(param);
	}
	public String search(User user) {
		param.clear();
		param.put("email", user.getEmail());
		param.put("phoneno", user.getPhoneno());
		if(user.getUserid() == null) { //아이디 찾기
		   param.put("col","concat(substr(userid,1,char_length(userid)-2),'**')");
		}
		else {  //비밀번호 찾기
			param.put("userid", user.getUserid());
			param.put("col","concat('**',substr(password,3,char_length(password)-2))");
		}
		return template.getMapper(UserMapper.class).search(param);
	}
	public void passwordUpdate(String userid, String pass) {
		param.clear();
		param.put("userid", userid);
		param.put("password", pass);
		template.getMapper(UserMapper.class).passwordupdate(param);
	}
}
