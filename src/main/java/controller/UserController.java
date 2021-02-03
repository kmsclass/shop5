
package controller;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import exception.LoginException;
import logic.Item;
import logic.Sale;
import logic.SaleItem;
import logic.ShopService;
import logic.User;
import util.CipherUtil;

@Controller
@RequestMapping("user")
public class UserController {
	@Autowired
	private ShopService service;
	@Autowired
	private CipherUtil cipher;
	
	@GetMapping("*")
//	public ModelAndView user() {
//		ModelAndView mav = new ModelAndView();
//		mav.addObject(new User());
//		return mav;
//	}
	public String user(Model model) {
		model.addAttribute(new User());
		return null;
	}
	@RequestMapping({"main","password"}) //login되어야 하는 메서드이름을 loginCheckXXX로 지정
	public String loginCheckmain(HttpSession session) {
		return null;
	}
	
	@PostMapping("userEntry")
	public ModelAndView userAdd(@Valid User user, BindingResult bindingResult) {
		ModelAndView mav = new ModelAndView();
		if(bindingResult.hasErrors()) {
			mav.getModel().putAll(bindingResult.getModel());
			bindingResult.reject("error.input.user");
			return mav;
		}
		//db에 user 정보 useraccount 테이블에 저장.
		try {
			String hashpass = cipher.makehash(user.getPassword());
			user.setPassword(hashpass);
			service.insert(user);
			mav.addObject("user",user);
		}catch(DataIntegrityViolationException e) {
			e.printStackTrace();
			bindingResult.reject("error.duplicate.user");
			mav.getModel().putAll(bindingResult.getModel());
			return mav;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		mav.setViewName("redirect:login.shop");
		return mav;
	}
	@PostMapping("login")
	public ModelAndView login(@Valid User user, BindingResult bresult, HttpSession session) {
		ModelAndView mav = new ModelAndView();
		if(bresult.hasErrors()) {
			mav.getModel().putAll(bresult.getModel());
			bresult.reject("error.input.user");
			return mav;
		}
		//userid 맞는 User 객체 조회.
		//아이디가 없는 경우 아이디없음 화면에 출력
		//비밀번호 틀린경우 비밀번호 오류 화면에 출력
		//정상 : session.setAttribute("loginUser",User객체)
		//      main.shop으로 페이지 이동.
		try {
			User dbuser = service.selectUserOne(user.getUserid());
			if(dbuser == null) {
				bresult.reject("error.login.id");
				mav.getModel().putAll(bresult.getModel());
				return mav;
			}
			//user.getPassword() : 입력비밀번호
			//dbuser.getPassword() : 등록된 비밀번호. 해쉬암호 저장
			//hashpass : 입력된 비밀번호 해쉬암호값 저장
			String hashpass = cipher.makehash(user.getPassword());
			if(hashpass.equals(dbuser.getPassword())) { //정상 로그인
				session.setAttribute("loginUser", dbuser);
				mav.setViewName("redirect:main.shop");
			}else {   //비밀번호 오류
				bresult.reject("error.login.password");
				mav.getModel().putAll(bresult.getModel());
			}
		} catch(Exception e) { //해당 아이디 없는 경우 예외발생
			bresult.reject("error.login.id");
			mav.getModel().putAll(bresult.getModel());
		}
		return mav;
	}
	@RequestMapping("logout")
	public String loginChecklogout(HttpSession session) {
		session.invalidate(); //loginUser속성, CART 속성 제거. 새로운 session 객체변경
		return "redirect:login.shop";
	}
	/*
	 * AOP 설정하기
	 * 1. UserController의 idCheck로 시작하는 메서드 +
	 *    매개변수가 id, session 인 경우를 pointcut으로 설정
	 * 2. 로그인 안된경우 : 로그인하세요 메세지 출력.  login.shop 페이지 이동
	 *    admin이 아니고, 다른 아이디 정보 조회시 :
	 *                  본인만 조회가능합니다. main.shop페이지 이동    
	 */
	@RequestMapping("mypage")
	public ModelAndView idCheckmypage(String id, HttpSession session) {
		ModelAndView mav = new ModelAndView();
		//db에서 userid에 맞는 User 정보 한개리턴 
		User user = service.selectUserOne(id); 
		//userid가 주문한 주문 정보 목록 리턴
		List<Sale> salelist = service.salelist(id); 
		for(Sale sa : salelist) {
			//주문번호에 주문한 주문상품 목록 리턴
			List<SaleItem> saleitemlist = service.saleItemList(sa.getSaleid());
			for(SaleItem si : saleitemlist) {
				//주문상품id에 해당하는 Item 객체를 리턴
				Item item = service.getItem(Integer.parseInt(si.getItemid()));
				si.setItem(item);
			}
			sa.setItemList(saleitemlist); //한개의 주문에 해당하는 주문상품 목록 추가
		}
		mav.addObject("user",user); //회원정보
		mav.addObject("salelist", salelist); //회원의 주문 정보
		return mav;
	}
	
	@GetMapping({"update","delete"})
	public ModelAndView idCheckupdate(String id, HttpSession session) {
		ModelAndView mav = new ModelAndView();
		User user = service.selectUserOne(id);
		mav.addObject("user",user);
		return mav;
	}
	//1. 사용자정보 수정시 비밀번호 암호화 실행.
	//2. 사용자 비밀번호 수정시 비밀번호 암호화 실행.
	//3. 사용자 탈퇴시 비밀번호 암호화 실행
	@PostMapping("update")
	public ModelAndView update(@Valid User user, BindingResult bindResult, HttpSession session) {
		ModelAndView mav = new ModelAndView();
		if(bindResult.hasErrors()) {
			mav.getModel().putAll(bindResult.getModel());
			bindResult.reject("error.input.user");
			return mav;
		}
		//비밀번호 검증. 비밀번호가 일치하는 경우 useraccount 테이블 수정
		//로그인한 사용자의 비밀번호와, 입력된 비밀번호가 일치 검증
		//비밀번호가 일치하면 user 정보로 db 수정.
		// 일치하지 않으면 : error.login.password 코드를 입력하여 update.jsp 페이지에
		//                글로벌 오류 메시지 출력. 
		try {
			User loginUser=(User)session.getAttribute("loginUser");
			String hashpass = cipher.makehash(user.getPassword());
			user.setPassword(hashpass);
			if(user.getPassword().equals(loginUser.getPassword())) {
				service.updateUser(user);
				mav.setViewName("redirect:/user/mypage.shop?id="+user.getUserid());
				if(user.getUserid().equals(loginUser.getUserid())) {
					//session 정보 수정
					session.setAttribute("loginUser", user);
				}
			}else {
				bindResult.reject("error.login.password");
				mav.getModel().putAll(bindResult.getModel());
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return mav;
	}
	@PostMapping("delete")
	public ModelAndView idCheckdelete
	      (String userid, HttpSession session,String password ) {
		ModelAndView mav = new ModelAndView();
		User loginUser = (User)session.getAttribute("loginUser");
		if(userid.equals("admin")) {
			throw new LoginException("관리자 탈퇴는 불가합니다.","main.shop");
		}
		String hashpass=null;
		try {
			hashpass = cipher.makehash(password);
		} catch (NoSuchAlgorithmException e1) {
			throw new LoginException
			("비밀번호 암호화 오류입니다.","delete.shop?id="+ userid);
		}
		if(!hashpass.equals(loginUser.getPassword())) {
			throw new LoginException
			("탈퇴시 비밀번호가 틀립니다.","delete.shop?id="+ userid);
		}
		try {
			service.userDelete(userid);
		} catch(Exception e) {
			e.printStackTrace();
			throw new LoginException
			("탈퇴시 오류가 발생했습니다.","delete.shop?id="+ userid);
		}
		//탈퇴 이후
		if(loginUser.getUserid().equals("admin")) {
			mav.setViewName("redirect:/admin/list.shop");
		} else {
//			mav.setViewName("redirect:logout.shop");
			session.invalidate();
			throw new LoginException
			(userid+"회원님의 탈퇴 처리가 되었습니다.","login.shop");
		}
		return mav;
	}
	//@RequestMapping과 PostMapping이 같이 설정된 경우 Post방식 요청인 경우 이 메서드 호출
	//@RequestParam : 요청 파라미터 값을 저장하기 위한 객체 설정
	// 요청 파라미터 : 1. 파라미터 이름과 매개변수 이름이 같은 경우
	//              2. Bean 클래스의 프로퍼티와 파라미터가 같은 경우 Bean클래스의 객체에 저장
	//              3. Map객체를 이용하여 파라미터 저장
	@PostMapping("password")
	public ModelAndView loginCheckpassword(@RequestParam Map<String,String> param,HttpSession session) {
		ModelAndView mav = new ModelAndView();
		System.out.println(param);
		User loginUser= (User)session.getAttribute("loginUser");
		String hashpass = null;
		try {
			hashpass = cipher.makehash(param.get("password"));
		} catch (NoSuchAlgorithmException e1) {
			throw new LoginException("비밀번호 암호화 오류입니다.","password.shop");
		}
		if(hashpass.equals(loginUser.getPassword())) {
		  try {
		     service.userPasswordUpdate(loginUser.getUserid(),cipher.makehash(param.get("chgpass")));
		     loginUser.setPassword(cipher.makehash(param.get("chgpass")));
		     mav.addObject("message",loginUser.getUsername()+"님 비밀번호 변경완료");
		     mav.addObject("url", "main.shop");
		     mav.setViewName("alert");
		  } catch(Exception e) {
			  e.printStackTrace();
			  throw new LoginException("비밀번호 변경시 오류가 있습니다.","password.shop");
		  }		  
		} else {
		  throw new LoginException("현재 비밀번호가 틀립니다.","password.shop");
		}
		return mav;		
	}
	//{url}search : {url} : 지정되지 않음. *search인 요청 url인 경우 호출되는 메서드
	//@PathVariable : {url}에 해당되는 문자열 매개변수 전달.
	/* 비밀번호 찾기 => 비밀번호 초기화
	 *   1. 조건에 맞는 사용자의 비밀번호를 랜덤값으로 생성
	 *      COJCBGRD => 대문자 8자를 랜덤하게 선택
	 *   2. 임시비밀번호의 hash값을 생성 db의 비밀번호 변경
	 *      생성된 임시비밀번호로 로그인되는지 확인.
	 *   3. 등록된 이메일로 임시비밀번호 전송   
	 */
//	@PostMapping("{url}search")
	public ModelAndView search (User user, BindingResult bresult,@PathVariable String url,
			String naverid,String naverpw) {
		ModelAndView mav = new ModelAndView();
		String code = "error.userid.search"; //messages.properties 설정해야함.
		String title = "아이디찾기";
		if(url.equals("pw")) {
			code = "error.password.search";
			title = "비밀번호초기화";
			if(user.getUserid()==null || user.getUserid().equals("")) {
				bresult.rejectValue("userid", "error.required");
			}
		}
		if(user.getEmail()==null || user.getEmail().equals("")) {
			bresult.rejectValue("email", "error.required");
		}
		if(user.getPhoneno()==null || user.getPhoneno().equals("")) {
			bresult.rejectValue("phoneno", "error.required");
		}
		if(bresult.hasErrors()) {
			mav.getModel().putAll(bresult.getModel());
			return mav;
		}
		
		if(user.getUserid() != null && user.getUserid().equals(""))	
           user.setUserid(null);
		
		String result = null;
		String key;
		String email = user.getEmail();
		try {
			key = cipher.makehash(user.getUserid()).substring(0,16);
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		    throw new LoginException("이메일 암호화 오류", url+"search.shop");	
		}
		
		String cipherEmail = cipher.encrypt(email,key);
		user.setEmail(cipherEmail);
		result = service.getSearch(user);
		if(result==null) { //조건에 맞는 사용자가 없는 경우
			bresult.reject(code);
			mav.getModel().putAll(bresult.getModel());
			return mav;
		}
		if(url.equals("pw")) {
		   result = randomPass(); //임의의 임시번호 생성
		   System.out.println(result);
//		   mailSend(user,result,naverid,naverpw);	//이메일 전송	  
		   try {
			 service.userPasswordUpdate(user.getUserid(), cipher.makehash(result)); //임시번호 암호화 db저장
			 result = user.getEmail()+"로 임시비밀번호 전송";
		   } catch (NoSuchAlgorithmException e) {
			  throw new LoginException("비밀번호 암호화 오류", url+"search.shop");	
		   }
		}		
		mav.addObject("result",result);
		mav.addObject("title",title);
		mav.setViewName("search");
		return mav;
	}
	@PostMapping("idsearch")
	public ModelAndView idsearch (User user, BindingResult bresult) {
		ModelAndView mav = new ModelAndView();
		String code = "error.userid.search"; //messages.properties 설정해야함.
		String title = "아이디찾기";
		String result = null;
		if(user.getEmail()==null || user.getEmail().equals("")) {
			bresult.rejectValue("email", "error.required");
		}
		if(user.getPhoneno()==null || user.getPhoneno().equals("")) {
			bresult.rejectValue("phoneno", "error.required");
		}
		if(bresult.hasErrors()) {
			mav.getModel().putAll(bresult.getModel());
			return mav;
		}
		String email = user.getEmail();
		List<User> list = service.getUserList();//전체 사용자정보 조회
		for(User u : list) {
			String key;
			try {
				key = cipher.makehash(u.getUserid()).substring(0,16);
			} catch (NoSuchAlgorithmException e1) {
				e1.printStackTrace();
			    throw new LoginException("암호화 오류", "idsearch.shop");	
			}
			String cipherEmail = cipher.encrypt(email,key);
			user.setEmail(cipherEmail);
			result = service.getSearch(user);
			if(result != null) break;
		}
		if(result==null) { //조건에 맞는 사용자가 없는 경우
			bresult.reject(code);
			mav.getModel().putAll(bresult.getModel());
			return mav;
		}
		mav.addObject("result",result);
		mav.addObject("title",title);
		mav.setViewName("search");
		return mav;
	}
	@PostMapping("pwsearch")
	public ModelAndView pwsearch (User user, BindingResult bresult,String naverid,String naverpw) {
		ModelAndView mav = new ModelAndView();
		String code = "error.password.search";
		String title = "비밀번호초기화";
		if(user.getUserid()==null || user.getUserid().equals("")) {
			bresult.rejectValue("userid", "error.required");
		}
		if(user.getEmail()==null || user.getEmail().equals("")) {
			bresult.rejectValue("email", "error.required");
		}
		if(user.getPhoneno()==null || user.getPhoneno().equals("")) {
			bresult.rejectValue("phoneno", "error.required");
		}
		if(bresult.hasErrors()) {
			mav.getModel().putAll(bresult.getModel());
			return mav;
		}
		String key;
		try {
			//사용자아이디를 이용하여 키로 설정
			key = cipher.makehash(user.getUserid()).substring(0,16);
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		    throw new LoginException("암호화 오류", "idsearch.shop");	
		}
		String email = user.getEmail();
		String cipherEmail = cipher.encrypt(email,key);
		user.setEmail(cipherEmail);
		String result = service.getSearch(user);
		if(result==null) {
			bresult.reject(code);
			mav.getModel().putAll(bresult.getModel());
			return mav;
		}
		//검색된 비밀번호 존재하는 경우
		result = randomPass(); //임의의 임시번호 생성
 	    System.out.println(result);
//	    mailSend(user,result,naverid,naverpw);	//이메일 전송	  
		try {
			 service.userPasswordUpdate(user.getUserid(), cipher.makehash(result)); //임시번호 암호화 db저장
			 result = email+"로 임시비밀번호 전송";
	    } catch (NoSuchAlgorithmException e) {
		     throw new LoginException("비밀번호 암호화 오류","pwsearch.shop");	
		}
		mav.addObject("result",result);
		mav.addObject("title",title);
		mav.setViewName("search");
		return mav;
	}

	private String randomPass() {
		String pass = "";
		for(int i=0;i<8;i++) {
			pass += (char)((int)(Math.random()*26) + 'A'); //A ~ Z 임의 문자
		}
		return pass;
	}
	private void mailSend(User user,String pass,String naverid,String naverpw) {
		try {
		String sendid = naverid;
		String sendpw = naverpw;
		Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.naver.com");
		prop.put("mail.smtp.port", "465");
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.debug", "false");
		prop.put("mail.smtp.user", sendid);
		prop.put("mail.smtp.socketFactory.port", "465");
		prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		prop.put("mail.smtp.socketFactory.fallback", "false");
		MyAuthenticator auth = new MyAuthenticator(sendid, sendpw);
		Session session = Session.getInstance(prop, auth);
		MimeMessage mail = new MimeMessage(session);
		mail.setFrom(new InternetAddress(sendid)); 
		String contents = user.getUserid() +"님의 임시비밀번호:" + pass;		
//		InternetAddress address = new InternetAddress(user.getEmail());		
		InternetAddress address = new InternetAddress(sendid);
		mail.setRecipient(Message.RecipientType.TO, address);
		mail.setSubject("구디 쇼핑몰 임시비밀번호 전송");
		mail.setSentDate(new Date());
		mail.setText(contents);
		MimeMultipart multipart = new MimeMultipart();
		MimeBodyPart body = new MimeBodyPart();
		body.setContent(contents, "text/html; charset=EUC-KR");
		multipart.addBodyPart(body);
		mail.setContent(multipart); 
		Transport.send(mail); 
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}	
	private final class MyAuthenticator extends Authenticator {
		private String id;
		private String pw;
		public MyAuthenticator(String id, String pw) {
			this.id = id;
			this.pw = pw;
		}
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(id, pw);
		}
	}	
}