package com.liuwei.main;

import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liuwei.comment.Constant;
import com.liuwei.comment.NewsProgress;
import com.liuwei.comment.ThreadManager;
import com.liuwei.db.DBHelper;
import com.liuwei.pojo.NewsPOJO;

public class Entrance {
	public static final Logger logger = LoggerFactory.getLogger(Entrance.class);
	private static ThreadPoolExecutor threadPool = ThreadManager.getThreadManager(1);
	private static ArrayBlockingQueue<Vector<NewsPOJO>> blockQueue;
	private static DBHelper dbHelper;
	public volatile static String TOUTIAO_NEWS_DOCID = "";//头条
	public volatile static String SHEHUI_NEWS_DOCID = "";//社会
	public volatile static String HISTROY_NEWS_DOCID = "";//历史
	public volatile static String DIANTAI_NEWS_DOCID = "";//电台
	public volatile static String JUNSHI_NEWS_DOCID = "";//军事
	public volatile static String HANKONHG_NEWS_DOCID = "";//航空
	public volatile static String YAOWEN_NEWS_DOCID = "";//要文
	public volatile static String YULE_NEWS_DOCID = "";//娱乐
	public volatile static String YINGSHIGE_NEWS_DOCID = "";//影视歌
	public volatile static String CAIJING_NEWS_DOCID = "";//财经
	public volatile static String GUPIAO_NEWS_DOCID = "";//股票
	public volatile static String CAIPIAO_NEWS_DOCID = "";//彩票
	public volatile static String TIYU_NEWS_DOCID = "";//体育
	public volatile static String KEJI_NEWS_DOCID = "";//科技
	public volatile static String PHONE_NEWS_DOCID = "";//手机
	public volatile static String SHUMA_NEWS_DOCID = "";//数码
	public volatile static String ZHINENG_NEWS_DOCID = "";//智能
	public volatile static String HAPPYTIME_NEWS_DOCID = "";//轻松一刻
	public volatile static String DUJIA_NEWS_DOCID = "";//独家
	public volatile static String CAR_NEWS_DOCID = "";//汽车
	public volatile static String HOUSE_NEWS_DOCID = "";//房产
	public volatile static String JIAJU_NEWS_DOCID = "";//家居
	public volatile static String GAME_NEWS_DOCID = "";//游戏
	public volatile static String TRAVEL_NEWS_DOCID = "";//旅游
	public volatile static String HEALTHY_NEWS_DOCID = "";//健康
	public volatile static String EDUCATION_NEWS_DOCID = "";//教育
	public volatile static String FANSHION_NEWS_DOCID = "";//时尚
	public volatile static String WOMEN_NEWS_DOCID = "";//女人
	public volatile static String ZHENWU_NEWS_DOCID = "";//政务
	public volatile static String ART_NEWS_DOCID = "";//艺术
	private static boolean isEmpty = true;

	public static void main(String[] args) {
		try {
			start();
		} catch (Exception e) {
			logger.error(e.toString());
			System.exit(0);
		}
	}

	// 开启获取数据
	private static void start() {
		blockQueue = new ArrayBlockingQueue<Vector<NewsPOJO>>(20);
		threadPool.execute(new NewsProgress(blockQueue, Constant.PARSER_IDS));
		try {
			dbHelper = new DBHelper();
			while (isEmpty) {
				if (!blockQueue.isEmpty()) {
					Vector<NewsPOJO> vector = blockQueue.take();
					dbHelper.InsertToDB(vector);
				}
			}
//			logger.info("###################");
//			logger.info("程序即将完成并退出。");
//			if (!isEmpty) {
//				blockQueue = null;
//				dbHelper = null;
//				Entrance.TOUTIAO_NEWS_DOCID = null;
//				Entrance.SHEHUI_NEWS_DOCID = null;// 社会
//				Entrance.HISTROY_NEWS_DOCID = null;// 历史
//				Entrance.DIANTAI_NEWS_DOCID = null;// 电台
//				Entrance.JUNSHI_NEWS_DOCID = null;// 军事
//				Entrance.HANKONHG_NEWS_DOCID = null;// 航空
//				Entrance.YAOWEN_NEWS_DOCID = null;// 要文
//				Entrance.YULE_NEWS_DOCID = null;// 娱乐
//				Entrance.YINGSHIGE_NEWS_DOCID = null;// 影视歌
//				Entrance.CAIJING_NEWS_DOCID = null;// 财经
//				Entrance.GUPIAO_NEWS_DOCID = null;// 股票
//				Entrance.CAIPIAO_NEWS_DOCID = null;// 彩票
//				Entrance.TIYU_NEWS_DOCID = null;// 体育
//				Entrance.KEJI_NEWS_DOCID = null;// 科技
//				Entrance.PHONE_NEWS_DOCID = null;// 手机
//				Entrance.SHUMA_NEWS_DOCID = null;// 数码
//				Entrance.ZHINENG_NEWS_DOCID = null;// 智能
//				Entrance.HAPPYTIME_NEWS_DOCID = null;// 轻松一刻
//				Entrance.DUJIA_NEWS_DOCID = null;// 独家
//				Entrance.CAR_NEWS_DOCID = null;// 汽车
//				Entrance.HOUSE_NEWS_DOCID = null;// 房产
//				Entrance.JIAJU_NEWS_DOCID = null;// 家居
//				Entrance.GAME_NEWS_DOCID = null;// 游戏
//				Entrance.TRAVEL_NEWS_DOCID = null;// 旅游
//				Entrance.HEALTHY_NEWS_DOCID = null;// 健康
//				Entrance.EDUCATION_NEWS_DOCID = null;// 教育
//				Entrance.FANSHION_NEWS_DOCID = null;// 时尚
//				Entrance.WOMEN_NEWS_DOCID = null;// 女人
//				Entrance.ZHENWU_NEWS_DOCID = null;// 政务
//				Entrance.ART_NEWS_DOCID = null;// 艺术
//				System.gc();
//				System.exit(0);
//			}
		} catch (Exception exception) {
			logger.info("======队列获取对象出现错误!!!=======");
			logger.error(exception.toString());
			throw new RuntimeException(exception);
		}
	}

}
