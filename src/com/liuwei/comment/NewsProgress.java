package com.liuwei.comment;

import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DefaultSubscriber;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

import okhttp3.ResponseBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.liuwei.main.Entrance;
import com.liuwei.pojo.BasePOJO;
import com.liuwei.pojo.DetailPOJO;
import com.liuwei.pojo.NewsPOJO;
import com.liuwei.util.CompressUtil;

/**
 * @Project: news-crawling
 * @Class: NewsProgress.java
 * @Description: 线程爬取实现类
 * @Date: 2017年12月5日
 * @author liuwei5
 */
public class NewsProgress implements Runnable {

	public static final Logger logger = LoggerFactory.getLogger(NewsProgress.class);

	private BasePOJO mBasePOJO;
	private ArrayBlockingQueue<Vector<NewsPOJO>> blockQueue;
	private String[] newsId;
	public NewsProgress(ArrayBlockingQueue<Vector<NewsPOJO>> queue,String[] newsID) {
			this.blockQueue = queue;
			this.newsId = newsID;
	}
	
	@Override
	public void run() {
		int size = newsId.length;
		for (int j = 0; j < size; j++) {
			String id = newsId[j];
			for (int i = 0; i <= 420;) {
				parseNewsList(id, i);
				i += 20;
			}
		}	
	}
	/**
	 * 获取新闻列表数据
	 */
	private void parseNewsList(final String newsID, final int page) {
		HttpManager.getClient().getNews(newsID, page)
				 .subscribeOn(Schedulers.trampoline())
				.subscribe(new DefaultSubscriber<String>() {
					@Override
					public void onComplete() {
						logger.info("======onComplete=======");
					}

					@Override
					public void onError(Throwable arg0) {
						logger.info("======parseNewsList onError=======");
						logger.error("{}", arg0.toString());
					}

					@Override
					public void onNext(String arg0) {
						logger.info("======onNext Parse NewsList Start =======");
						if (null != arg0 && !"".equals(arg0)) {
							int temp = page;
							//logger.debug("第temp:"+temp +"/page:"+page+"页:{}",arg0);
							JSONObject jsonObject = JSON.parseObject(arg0);
							JSONArray jsonArray = jsonObject.getJSONArray(newsID);
							List<BasePOJO> objDTO = JSON.parseArray(jsonArray.toString(), BasePOJO.class);
							int size = objDTO.size();
							logger.debug(Constant.vnewsMap.get(newsID)+":第temp:"+temp +"/page:"+page+"页--"+newsID+"--数量:{}",size);
							if (objDTO != null && size > 0) {
								validateFirst(newsID,objDTO,temp);
								Vector<NewsPOJO> vector = new Vector<NewsPOJO>(26);
								for (int i = 0; i < size; i++) {
									BasePOJO basePOJO = objDTO.get(i);
									NewsPOJO newsPOJO = new NewsPOJO();
									newsPOJO.setNewsId(newsID);
									if (basePOJO.getImgextra() != null 
											&& basePOJO.getImgextra().size() > 0) {
										newsPOJO.setImgExtra(JSON.toJSONString(basePOJO.getImgextra()));
									}
									newsPOJO.setTitle(basePOJO.getTitle());
									newsPOJO.setDocId(basePOJO.getDocid());
									newsPOJO.setShortCentent(basePOJO.getDigest());
									newsPOJO.setNewsResouce(basePOJO.getSource());
									newsPOJO.setImgUrl(basePOJO.getImgsrc());
									newsPOJO.setNewsType(Constant.vnewsMap.get(newsID));
									newsPOJO.setProductTime(Timestamp.valueOf(basePOJO.getPtime()));
									newsPOJO.setCreateTime(new Timestamp(System.currentTimeMillis()));
									parseDetail(basePOJO.getDocid(), newsPOJO);
									//saveNewsImg(basePOJO.getImgsrc(), newsPOJO);
									vector.add(newsPOJO);
								}
								try {
									blockQueue.put(vector);
								} catch (InterruptedException e) {
									logger.error(e.toString());
									logger.info("======队列添加对象出现错误!!!=======");
								}
							}
							
						} else {
							logger.debug(Constant.NONE_CONTENT);
						}
						logger.info("======onNext Parse NewsList End=======");
					}
				});
	}

	/**
	 * 获取新闻详情数据
	 */
	private void parseDetail(final String docId, final NewsPOJO newsPOJO) {
		HttpManager.getClient().getDetailNews(docId)
				 .subscribeOn(Schedulers.trampoline())
				.subscribe(new DefaultSubscriber<String>() {

					@Override
					public void onComplete() {
						logger.info("======parseDetail onComplete=======");
					}

					@Override
					public void onError(Throwable arg0) {
						logger.info("======parseDetail onError=======");
						logger.error(docId+":{}", arg0.toString());
					}

					@Override
					public void onNext(String arg0) {
						logger.info("======onNext ParseDetail Start =======");
						if (null != arg0 && !"".equals(arg0)) {
							logger.debug("===ParseDetail==={}",arg0);
							JSONObject jsonObject = JSON.parseObject(arg0);
							JSONObject detailDTO = jsonObject.getJSONObject(docId);
							DetailPOJO detailPojo = JSON.parseObject(detailDTO.toString(), DetailPOJO.class);
							newsPOJO.setBody(detailPojo.getBody());
							newsPOJO.setRelativeKey(detailPojo.getDkeys());
							newsPOJO.setEditor(detailPojo.getEc());
						} else {
							logger.info(Constant.NONE_CONTENT);
						}
						logger.info("======onNext ParseDetail End=======");
					}
				});
	}

	/**
	 * 下载新闻对应的图片
	 */
	private void saveNewsImg(final String url, final NewsPOJO newsPOJO) {
		Map<String, Object> headerMap = new HashMap<String, Object>(1);
		headerMap.put("If-Modified-Since", new Date().toGMTString());
		HttpManager.getClient().getNewsImg(url,headerMap)
				.subscribeOn(Schedulers.trampoline())
				.map(new Function<ResponseBody, byte[]>() {

					@Override
					public byte[] apply(ResponseBody arg0) throws Exception {
						// TODO Auto-generated method stub
						return arg0.bytes();
					}
				})
				.subscribe(new DefaultSubscriber<byte[]>() {

					@Override
					public void onComplete() {
						logger.info("======saveNewsImg onComplete=======");
					}

					@Override
					public void onError(Throwable arg0) {
						logger.info("======saveNewsImg onError=======");
						logger.error(url+":{}", arg0.toString());
					}

					@Override
					public void onNext(byte[] arg0) {
						logger.info("======onNext saveNewsImg  Start =======");
						newsPOJO.setLocalImg(CompressUtil.compress(arg0));
						logger.info("======onNext saveNewsImg End=======");

					}
				});
	}
	
	private void validateFirst(String newsId, List<BasePOJO> list, int tempPage) {
		if (tempPage == 0) {
			mBasePOJO = list.get(0);
			if (Constant.PARSER_IDS[0].equals(newsId)) {// 头条
				if (Entrance.TOUTIAO_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("TOUTIAO_NEWS_DOCID cache:{}",
							Entrance.TOUTIAO_NEWS_DOCID);
					return;
				}
				Entrance.TOUTIAO_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("TOUTIAO_NEWS_DOCID:{}", Entrance.TOUTIAO_NEWS_DOCID);
			} else if (Constant.PARSER_IDS[1].equals(newsId)) {// 社会
				if (Entrance.SHEHUI_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("SHEHUI_NEWS_DOCID cache:{}",
							Entrance.SHEHUI_NEWS_DOCID);
					return;
				}
				Entrance.SHEHUI_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("SHEHUI_NEWS_DOCID:{}", Entrance.SHEHUI_NEWS_DOCID);
			} else if (Constant.PARSER_IDS[2].equals(newsId)) {// 历史
				if (Entrance.HISTROY_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("HISTROY_NEWS_DOCID cache:{}",
							Entrance.HISTROY_NEWS_DOCID);
					return;
				}
				Entrance.HISTROY_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("HISTROY_NEWS_DOCID:{}", Entrance.HISTROY_NEWS_DOCID);
			} else if (Constant.PARSER_IDS[3].equals(newsId)) {// 电台
				if (Entrance.DIANTAI_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("DIANTAI_NEWS_DOCID cache:{}",
							Entrance.DIANTAI_NEWS_DOCID);
					return;
				}
				Entrance.DIANTAI_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("DIANTAI_NEWS_DOCID:{}", Entrance.DIANTAI_NEWS_DOCID);
			} else if (Constant.PARSER_IDS[4].equals(newsId)) {// 军事
				if (Entrance.JUNSHI_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("JUNSHI_NEWS_DOCID cache:{}",
							Entrance.JUNSHI_NEWS_DOCID);
					return;
				}
				Entrance.JUNSHI_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("JUNSHI_NEWS_DOCID:{}", Entrance.JUNSHI_NEWS_DOCID);
			} else if (Constant.PARSER_IDS[5].equals(newsId)) {// 航空
				if (Entrance.HANKONHG_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("HANKONHG_NEWS_DOCID cache:{}",
							Entrance.HANKONHG_NEWS_DOCID);
					return;
				}
				Entrance.HANKONHG_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("HANKONHG_NEWS_DOCID:{}", Entrance.HANKONHG_NEWS_DOCID);
			} else if (Constant.PARSER_IDS[6].equals(newsId)) {// 要文
				if (Entrance.YAOWEN_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("YAOWEN_NEWS_DOCID cache:{}",
							Entrance.YAOWEN_NEWS_DOCID);
					return;
				}
				Entrance.YAOWEN_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("YAOWEN_NEWS_DOCID:{}", Entrance.YAOWEN_NEWS_DOCID);
			} else if (Constant.PARSER_IDS[7].equals(newsId)) {// 娱乐
				if (Entrance.YULE_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("YULE_NEWS_DOCID cache:{}", Entrance.YULE_NEWS_DOCID);
					return;
				}
				Entrance.YULE_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("YULE_NEWS_DOCID:{}", Entrance.YULE_NEWS_DOCID);
			} else if (Constant.PARSER_IDS[8].equals(newsId)) {// 影视歌
				if (Entrance.YINGSHIGE_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("YINGSHIGE_NEWS_DOCID cache:{}",
							Entrance.YINGSHIGE_NEWS_DOCID);
					return;
				}
				Entrance.YINGSHIGE_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("YINGSHIGE_NEWS_DOCID:{}", Entrance.YINGSHIGE_NEWS_DOCID);
			} else if (Constant.PARSER_IDS[9].equals(newsId)) {// 财经
				if (Entrance.CAIJING_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("CAIJING_NEWS_DOCID cache:{}",
							Entrance.CAIJING_NEWS_DOCID);
					return;
				}
				Entrance.CAIJING_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("CAIJING_NEWS_DOCID:{}", Entrance.CAIJING_NEWS_DOCID);
			} else if (Constant.PARSER_IDS[10].equals(newsId)) {// 股票
				if (Entrance.GUPIAO_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("GUPIAO_NEWS_DOCID cache:{}",
							Entrance.GUPIAO_NEWS_DOCID);
					return;
				}
				Entrance.GUPIAO_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("GUPIAO_NEWS_DOCID:{}", Entrance.GUPIAO_NEWS_DOCID);
			} else if (Constant.PARSER_IDS[11].equals(newsId)) {// 彩票
				if (Entrance.CAIPIAO_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("CAIPIAO_NEWS_DOCID cache:{}",
							Entrance.CAIPIAO_NEWS_DOCID);
					return;
				}
				Entrance.CAIPIAO_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("CAIPIAO_NEWS_DOCID:{}", Entrance.CAIPIAO_NEWS_DOCID);
			}
		} else if (Constant.PARSER_IDS[12].equals(newsId)) {// 体育
			if (Entrance.TIYU_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
				logger.debug("TIYU_NEWS_DOCID cache:{}", Entrance.TIYU_NEWS_DOCID);
				return;
			}
			Entrance.TIYU_NEWS_DOCID = mBasePOJO.getDocid();
			logger.debug("TIYU_NEWS_DOCID:{}", Entrance.TIYU_NEWS_DOCID);
		} else if (Constant.PARSER_IDS[13].equals(newsId)) {// 科技
			if (Entrance.KEJI_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
				logger.debug("KEJI_NEWS_DOCID cache:{}", Entrance.KEJI_NEWS_DOCID);
				return;
			}
			Entrance.SHEHUI_NEWS_DOCID = mBasePOJO.getDocid();
			logger.debug("KEJI_NEWS_DOCID:{}", Entrance.KEJI_NEWS_DOCID);
		} else if (Constant.PARSER_IDS[14].equals(newsId)) {// 手机
			if (Entrance.PHONE_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
				logger.debug("PHONE_NEWS_DOCID cache:{}", Entrance.PHONE_NEWS_DOCID);
				return;
			}
			Entrance.PHONE_NEWS_DOCID = mBasePOJO.getDocid();
			logger.debug("PHONE_NEWS_DOCID:{}", Entrance.PHONE_NEWS_DOCID);
		} else if (Constant.PARSER_IDS[15].equals(newsId)) {// 数码
			if (Entrance.SHUMA_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
				logger.debug("SHUMA_NEWS_DOCID cache:{}", Entrance.SHUMA_NEWS_DOCID);
				return;
			}
			Entrance.SHUMA_NEWS_DOCID = mBasePOJO.getDocid();
			logger.debug("SHUMA_NEWS_DOCID:{}", Entrance.SHUMA_NEWS_DOCID);
		} else if (Constant.PARSER_IDS[16].equals(newsId)) {// 智能
			if (Entrance.ZHINENG_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
				logger.debug("ZHINENG_NEWS_DOCID cache:{}", Entrance.ZHINENG_NEWS_DOCID);
				return;
			}
			Entrance.ZHINENG_NEWS_DOCID = mBasePOJO.getDocid();
			logger.debug("ZHINENG_NEWS_DOCID:{}", Entrance.ZHINENG_NEWS_DOCID);
		} else if (Constant.PARSER_IDS[17].equals(newsId)) {// 轻松一刻
			if (Entrance.HAPPYTIME_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
				logger.debug("HAPPYTIME_NEWS_DOCID cache:{}",
						Entrance.HAPPYTIME_NEWS_DOCID);
				return;
			}
			Entrance.HAPPYTIME_NEWS_DOCID = mBasePOJO.getDocid();
			logger.debug("HAPPYTIME_NEWS_DOCID:{}", Entrance.HAPPYTIME_NEWS_DOCID);
		} else if (Constant.PARSER_IDS[18].equals(newsId)) {// 独家
			if (Entrance.DUJIA_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
				logger.debug("DUJIA_NEWS_DOCID cache:{}", Entrance.DUJIA_NEWS_DOCID);
				return;
			}
			Entrance.DUJIA_NEWS_DOCID = mBasePOJO.getDocid();
			logger.debug("DUJIA_NEWS_DOCID:{}", Entrance.DUJIA_NEWS_DOCID);
		} else if (Constant.PARSER_IDS[19].equals(newsId)) {// 汽车
			if (Entrance.CAR_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
				logger.debug("CAR_NEWS_DOCID cache:{}", Entrance.CAR_NEWS_DOCID);
				return;
			}
			Entrance.CAR_NEWS_DOCID = mBasePOJO.getDocid();
			logger.debug("CAR_NEWS_DOCID:{}", Entrance.CAR_NEWS_DOCID);
		} else if (Constant.PARSER_IDS[20].equals(newsId)) {// 房产
			if (Entrance.HOUSE_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
				logger.debug("HOUSE_NEWS_DOCID cache:{}", Entrance.HOUSE_NEWS_DOCID);
				return;
			}
			Entrance.HOUSE_NEWS_DOCID = mBasePOJO.getDocid();
			logger.debug("HOUSE_NEWS_DOCID:{}",Entrance.HOUSE_NEWS_DOCID);
		} else if (Constant.PARSER_IDS[21].equals(newsId)) {// 家居
			if (Entrance.JIAJU_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
				logger.debug("JIAJU_NEWS_DOCID cache:{}", Entrance.JIAJU_NEWS_DOCID);
				return;
			}
			Entrance.JIAJU_NEWS_DOCID = mBasePOJO.getDocid();
			logger.debug("JIAJU_NEWS_DOCID:{}", Entrance.JIAJU_NEWS_DOCID);
		} else if (Constant.PARSER_IDS[22].equals(newsId)) {// 游戏
			if (Entrance.GAME_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
				logger.debug("GAME_NEWS_DOCID cache:{}", Entrance.GAME_NEWS_DOCID);
				return;
			}
			Entrance.GAME_NEWS_DOCID = mBasePOJO.getDocid();
			logger.debug("GAME_NEWS_DOCID:{}", Entrance.GAME_NEWS_DOCID);
		} else if (Constant.PARSER_IDS[23].equals(newsId)) {// 旅游
			if (tempPage == 0) {
				if (Entrance.TRAVEL_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("TRAVEL_NEWS_DOCID cache:{}",
							Entrance.TRAVEL_NEWS_DOCID);
					return;
				}
				Entrance.TRAVEL_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("TRAVEL_NEWS_DOCID:{}", Entrance.TRAVEL_NEWS_DOCID);
			} else if (Constant.PARSER_IDS[24].equals(newsId)) {// 健康
				if (Entrance.HEALTHY_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("HEALTHY_NEWS_DOCID cache:{}",
							Entrance.HEALTHY_NEWS_DOCID);
					return;
				}
				Entrance.HEALTHY_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("HEALTHY_NEWS_DOCID:{}", Entrance.HEALTHY_NEWS_DOCID);
			} else if (Constant.PARSER_IDS[25].equals(newsId)) {// 教育
				if (Entrance.EDUCATION_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("EDUCATION_NEWS_DOCID cache:{}",
							Entrance.EDUCATION_NEWS_DOCID);
					return;
				}
				Entrance.EDUCATION_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("EDUCATION_NEWS_DOCID:{}", Entrance.EDUCATION_NEWS_DOCID);
			} else if (Constant.PARSER_IDS[26].equals(newsId)) {// 时尚
				if (Entrance.FANSHION_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("FANSHION_NEWS_DOCID cache:{}",
							Entrance.FANSHION_NEWS_DOCID);
					return;
				}
				Entrance.FANSHION_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("FANSHION_NEWS_DOCID:{}", Entrance.FANSHION_NEWS_DOCID);
			} else if (Constant.PARSER_IDS[27].equals(newsId)) {// 女人
				if (Entrance.WOMEN_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("WOMEN_NEWS_DOCID cache:{}", Entrance.WOMEN_NEWS_DOCID);
					return;
				}
				Entrance.WOMEN_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("WOMEN_NEWS_DOCID:{}", Entrance.WOMEN_NEWS_DOCID);
			} else if (Constant.PARSER_IDS[28].equals(newsId)) {// 政务
				if (Entrance.ZHENWU_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("ZHENWU_NEWS_DOCID cache:{}",
							Entrance.ZHENWU_NEWS_DOCID);
					return;
				}
				Entrance.ZHENWU_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("ZHENWU_NEWS_DOCID:{}", Entrance.ZHENWU_NEWS_DOCID);
			} else if (Constant.PARSER_IDS[29].equals(newsId)) {// 艺术
				if (Entrance.ART_NEWS_DOCID.equals(mBasePOJO.getDocid())) {
					logger.debug("ART_NEWS_DOCID cache:{}", Entrance.ART_NEWS_DOCID);
					return;
				}
				Entrance.ART_NEWS_DOCID = mBasePOJO.getDocid();
				logger.debug("ART_NEWS_DOCID:{}", Entrance.ART_NEWS_DOCID);
			}
		}
	}
}
