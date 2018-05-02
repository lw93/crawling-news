package com.liuwei.comment;

import java.util.HashMap;
import java.util.Map;

public class Constant {
	
	private Constant() {
	}
	
	// baseURL
	public static final String BASE_URL = "http://c.m.163.com/";
	
	public static final String[] PARSER_IDS_TEST = {
		
//		"T1348647909107",//头条:
		
//		"T1348648037603",//社会:
		
		"T1368497029546"//历史:
	};
	public static final String[] PARSER_IDS = {
		
		"T1348647909107",//头条:
		
		"T1348648037603",//社会:
		
		"T1368497029546",//历史:
		
		"T1379038288239",//电台:
		
		"T1348648141035",//军事:
		
		"T1474271789612",//航空:
		
		"T1467284926140",//要文:
		
		"T1348648517839",//娱乐:
		
		"T1348648650048",//影视歌:
		
		"T1348648756099",//财经:
		
		"T1473054348939",//股票:
		
		"T1356600029035",//彩票:
		
		"T1348649079062",//体育:
		
		"T1348649580692",//科技:
		
		"T1348649654285",//手机:
		
		"T1348649776727",//数码:
		
		"T1351233117091",//智能:
		
		"T1350383429665",//轻松一刻:
		
		"T1370583240249",//独家:
		
		"T1348654060988",//汽车:
		
		"T1348654085632",//房产:
		
		"T1348654105308",//家居:
		
		"T1348654151579",//游戏:
		
		"T1348654204705",//旅游:
		
		"T1414389941036",//健康:
		
		"T1348654225495",//教育:
		
		"T1348650593803",//时尚:
		
		"T1348650839000",//女人:
		
		"T1414142214384",//政务:
		
		"T1441074311424",//艺术:
	};
	
	public static final String NONE_TAG = "没有找到要查找的标签";

	public static final String NONE_CONTENT = "没有获取到任何内容";

	public static final Map<String,String> vnewsMap = new HashMap<String, String>(30);
	static {
		vnewsMap.put(PARSER_IDS[0], "头条");
		vnewsMap.put(PARSER_IDS[1], "社会");
		vnewsMap.put(PARSER_IDS[2], "历史");
		vnewsMap.put(PARSER_IDS[3], "电台");
		vnewsMap.put(PARSER_IDS[4], "军事");
		vnewsMap.put(PARSER_IDS[5], "航空");
		vnewsMap.put(PARSER_IDS[6], "要文");
		vnewsMap.put(PARSER_IDS[7], "娱乐");
		vnewsMap.put(PARSER_IDS[8], "影视歌");
		vnewsMap.put(PARSER_IDS[9], "财经");
		vnewsMap.put(PARSER_IDS[10], "股票");
		vnewsMap.put(PARSER_IDS[11], "彩票");
		vnewsMap.put(PARSER_IDS[12], "体育");
		vnewsMap.put(PARSER_IDS[13], "科技");
		vnewsMap.put(PARSER_IDS[14], "手机");
		vnewsMap.put(PARSER_IDS[15], "数码");
		vnewsMap.put(PARSER_IDS[16], "智能");
		vnewsMap.put(PARSER_IDS[17], "轻松一刻");
		vnewsMap.put(PARSER_IDS[18], "独家");
		vnewsMap.put(PARSER_IDS[19], "汽车");
		vnewsMap.put(PARSER_IDS[20], "房产");
		vnewsMap.put(PARSER_IDS[21], "家居");
		vnewsMap.put(PARSER_IDS[22], "游戏");
		vnewsMap.put(PARSER_IDS[23], "旅游");
		vnewsMap.put(PARSER_IDS[24], "健康");
		vnewsMap.put(PARSER_IDS[25], "教育");
		vnewsMap.put(PARSER_IDS[26], "时尚");
		vnewsMap.put(PARSER_IDS[27], "女人");
		vnewsMap.put(PARSER_IDS[28], "政务");
		vnewsMap.put(PARSER_IDS[29], "艺术");
	}
}
