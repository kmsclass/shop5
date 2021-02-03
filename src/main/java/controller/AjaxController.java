package controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import logic.ShopService;

//@Controller + @ResponseBody 기능
//@ResponseBody : View 없이 Controller에서 직접 데이터를 클라이언트로 전송
@RestController
@RequestMapping("ajax")
public class AjaxController {
	@Autowired
	private ShopService service;

	/*
	 * 일본엔화, 중국CNH,유로화 추가하기 value="exchange" : 요청 url 설정. /ajax/exchange.shop
	 * produces="text/html;charset=UTF-8" : 클라이언트에 데이터로 전송 될때 문서종류,인코딩 방식 설정
	 */
	@RequestMapping(value = "exchange", produces="text/html; charset=UTF-8")
	public String exchange() {
		Document doc = null;
		List<String> list = new ArrayList<>();
		List<String> list2 = new ArrayList<>();
		String url = "https://www.koreaexim.go.kr/site/program/financial/exchange?menuid=001001004002001";
		try {
			doc = Jsoup.connect(url).get();
			// class속성이 tc인 태그들 : 통화코드, 환율값
			Elements e1 = doc.select(".tc");
			// class속성이 tl2 bdl인 태그들 : 국가명
			Elements e2 = doc.select(".tl2.bdl");

			for (int i = 0; i < e1.size(); i++) {
				if (e1.get(i).html().equals("USD") || 
					e1.get(i).html().equals("CNH") || 
					e1.get(i).html().equals("JPY(100)") || 
					e1.get(i).html().equals("EUR")) {
					list.add(e1.get(i).html());
					for (int j = 1; j <= 6; j++) {
						list.add(e1.get(i + j).html());
					}
				}
			}
			for (Element ele : e2) {
				if (ele.html().contains("미국") || 
					ele.html().contains("위안화") || 
					ele.html().contains("일본")	|| 
					ele.html().contains("유로")) {
					list2.add(ele.html());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		StringBuilder html = new StringBuilder();
		html.append("<table class='nopadding'>");
		html.append("<caption>수출입은행:" + today + "</caption>");
		html.append("<tr><th>통화</th><th>기준율</th><th>받을때</th><th>파실때</th></tr>");
		for (int i = 0; i < list.size(); i += 7) {
			html.append("<tr><td>" + list2.get(i / 7) + "("
					+ list.get(i) + ")</td>");
			html.append("<td>" + list.get(i + 3) + "</td>");
			html.append("<td>" + list.get(i + 1) + "</td>");
			html.append("<td>" + list.get(i + 2) + "</td></tr>");
		}
		html.append("</table>");
		return html.toString();
	}
	@RequestMapping(value = "exchange2", produces = "text/html; charset=UTF8")
	public String exchange2() {
		Map<String, List<String>> map = new HashMap<>();
		StringBuilder html = null;
		try {
			String kebhana=Jsoup.connect
					("http://fx.kebhana.com/FER1101M.web").get().text();
			String strJson = kebhana.substring(kebhana.indexOf("{"));
			//json 문서 분석 객체
			JSONParser jsonParser = new JSONParser();
			//JSONObject 형태: {....}
			JSONObject json = (JSONObject) jsonParser.parse(strJson.trim());
			//JSONArray 형태 : [...]
			JSONArray array = (JSONArray) json.get("리스트");
			for (int i = 0; i < array.size(); i++) {
				JSONObject obj = (JSONObject) array.get(i);
				if (obj.get("통화명").toString().contains("미국") || 
					obj.get("통화명").toString().contains("일본")	|| 
					obj.get("통화명").toString().contains("유로") ||
					obj.get("통화명").toString().contains("중국")) {
					String str = obj.get("통화명").toString();
					String[] sarr = str.split(" ");
					String key = sarr[0]; //국가명
					String code = sarr[1];//통화코드
					List<String> list = new ArrayList<String>();
					list.add(code);
					list.add(obj.get("매매기준율").toString());
					list.add(obj.get("현찰파실때").toString());
					list.add(obj.get("현찰사실때").toString());
					//{"국가명",["통화코드","매매기준율","현찰파실때","현찰사실때"]}
					map.put(key, list);
				}
			}
			html = new StringBuilder();
			html.append("<table class='nopadding'>");
			html.append
		  ("<caption>KEB하나은행(" + json.get("날짜").toString() + ")</caption>");
			html.append	("<tr><th rowspan='2'>코드</th>");
			html.append("<th rowspan='2'>기준율</th>");
			html.append("<th colspan='2'>현찰</th></tr>");
			html.append("<tr><th>파실때</th><th>사실때</th></tr>");
			for (Map.Entry<String, List<String>> m : map.entrySet()) {
				html.append
				("<tr><td class='w3-center'>" + m.getKey() 
				+ "(" + m.getValue().get(0) + ")</td>");
				html.append("<td>" + m.getValue().get(1) + "</td><td>" 
				+ m.getValue().get(2) + "</td><td>"
				+ m.getValue().get(3) + "</td></tr>");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return html.toString();
	}
	@RequestMapping(value="graph",produces="text/plain; charset=UTF8")
	public String graph1() {
		//map : {"홍길동",7}...
		Map<String, Object> map = service.graph1();	
		StringBuilder json = new StringBuilder("[");
		int i = 0;
		for (Map.Entry<String, Object> me : map.entrySet()) {
			json.append("{\"name\":\""+me.getKey() + "\"," 
		               + "\"cnt\":\""+me.getValue() +"\"}");
			i++;
			if (i < map.size())	json.append(",");
		}
		json.append("]");
		return json.toString();
	}	
	@RequestMapping(value="graph2",produces="text/plain; charset=UTF8")
	public String graph2() {
		Map<String, Object> map = service.graph2();		
		StringBuilder json = new StringBuilder("[");
		int i = 0;
		for (Map.Entry<String, Object> me : map.entrySet()) {
			json.append("{\"regdate\":\""+me.getKey() + "\"," 
		               + "\"cnt\":\""+me.getValue() +"\"}");
			i++;
			if (i < map.size())
				json.append(",");
		}
		json.append("]");
		return json.toString();
	}	
}