package com.spirit.porker.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.cert.CertPathValidatorException.BasicReason;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.DateUtils;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.spirit.porker.dao.OrderIdGenerator;
import com.spirit.porker.dao.UserDao;
import com.spirit.porker.dao.pagination.PaginationList;
import com.spirit.porker.enums.ResultType;
import com.spirit.porker.model.UserModel;
import com.spirit.porker.vo.request.AddCreditRequest;
import com.spirit.porker.vo.request.GetCreditRequest;
import com.spirit.porker.vo.request.ProfileRequest;
import com.spirit.porker.vo.request.RegistRequest;
import com.spirit.porker.vo.response.BaseResponse;
import com.spirit.porker.vo.response.GetCreditList;
import com.spirit.porker.vo.response.GetCreditResponse;
import com.spirit.porker.vo.response.LoginResponse;
import com.spirit.porker.vo.response.RegistResponse;
import com.swetake.util.Qrcode;

@Service
public class SettingService {

	@Resource
	UserDao userDao;

	@Resource
	OrderIdGenerator orderIdGenerator;

	public BaseResponse<LoginResponse> login(@RequestBody UserModel pojo, HttpServletRequest request,
			HttpServletResponse response) {
		BaseResponse<LoginResponse> result = new BaseResponse<>(ResultType.succes);
		LoginResponse data = new LoginResponse();
		result.setData(data);

		Map<String, Object> cond = new HashMap<>();
		cond.put("username", pojo.getUsername());
		cond.put("password", pojo.getPassword());
		List<UserModel> users = userDao.login(cond);

		if (users == null || users.size() == 0) {
			result.setCode(ResultType.unKnowUser.getCode());
			result.setDesc("用户名或密码错误");
			return result;
		}
		Cookie sessionId = new Cookie("sessionId", UUID.randomUUID().toString());
		response.addCookie(sessionId);

		// 在数据库写入cookie数据
		UserModel user = users.get(0);
		UserModel updataUser = new UserModel();
		updataUser.setId(user.getId());
		updataUser.setCookie(sessionId.getValue().toString());
		initQrcode(user.getId());
		userDao.updateEntity(updataUser);

		// 返回客户端cookie及用户数据
		data.setUsername(user.getUsername());
		data.setPassword(user.getPassword());
		data.setCookie(sessionId.getValue().toString());
		data.setId(user.getId());
		return result;
	}

	private String initQrcode(int id) {
		//计算二维码图片的高宽比
	    // API文档规定计算图片宽高的方式 ，v是本次测试的版本号
	    int v =1;
	    int width = 67 + 12 * (v - 1);
	    int height = 67 + 12 * (v - 1);


	    Qrcode x = new Qrcode();
	    /**
	     * 纠错等级分为
	     * level L : 最大 7% 的错误能够被纠正；
	     * level M : 最大 15% 的错误能够被纠正；
	     * level Q : 最大 25% 的错误能够被纠正；
	     * level H : 最大 30% 的错误能够被纠正；
	     */
	    x.setQrcodeErrorCorrect('L');
	    x.setQrcodeEncodeMode('B');//注意版本信息 N代表数字 、A代表 a-z,A-Z、B代表 其他)
	    x.setQrcodeVersion(v);//版本号  1-40
	    String qrData = String.valueOf(id);//内容信息
	    String url="";
	    byte[] d;
		try {
			d = qrData.getBytes("utf-8");
		//汉字转格式需要抛出异常

	    //缓冲区
	    BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);

	    //绘图
	    Graphics2D gs = bufferedImage.createGraphics();

	    gs.setBackground(Color.WHITE);
	    gs.setColor(Color.BLACK);
	    gs.clearRect(0, 0, width, height);

	    //偏移量
	    int pixoff = 2;


	    /**
	     * 容易踩坑的地方
	     * 1.注意for循环里面的i，j的顺序，
	     *   s[j][i]二维数组的j，i的顺序要与这个方法中的 gs.fillRect(j*3+pixoff,i*3+pixoff, 3, 3);
	     *   顺序匹配，否则会出现解析图片是一串数字
	     * 2.注意此判断if (d.length > 0 && d.length < 120)
	     *   是否会引起字符串长度大于120导致生成代码不执行，二维码空白
	     *   根据自己的字符串大小来设置此配置
	     */
	    if (d.length > 0 && d.length < 120) {
	        boolean[][] s = x.calQrcode(d);

	        for (int i = 0; i < s.length; i++) {
	            for (int j = 0; j < s.length; j++) {
	                if (s[j][i]) {
	                    gs.fillRect(j * 3 + pixoff, i * 3 + pixoff, 3, 3);
	                }
	            }
	        }
	    }
	    gs.dispose();
	    bufferedImage.flush();
	    //设置图片格式，与输出的路径
	    url="E:\\upload\\"+qrData+"\\"+"qrcode.png";
	    ImageIO.write(bufferedImage, "png", new File(url));
	    System.out.println("二维码生成完毕");
		}
	    catch (IOException e) {
	    	e.printStackTrace();
		}
		return "qrcode.png";
	}

	public BaseResponse<RegistResponse> regist(@RequestBody UserModel pojo, HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {
		RegistResponse data = new RegistResponse();
		BaseResponse<RegistResponse> result = new BaseResponse<>(ResultType.succes);
		result.setData(data);

		Map<String, Object> cond = new HashMap<>();
		cond.put("username", pojo.getUsername());
		PaginationList<UserModel> userExist = userDao.findEntityListByCond(cond, null);
		if (userExist == null || userExist.size() == 0) {
			data.setUsername(pojo.getUsername());
			data.setPassword(pojo.getPassword());

			UserModel user = new UserModel();
			user.setUsername(pojo.getUsername());
			user.setPassword(pojo.getPassword());
			user.setUrlUserIcon("");
			user.setSex(0);
			userDao.addEntity(user);
			return result;
		}

		result.setCode(ResultType.fail.getCode());
		result.setDesc("该用户已存在，请更换用户名");
		return result;
	}

	public BaseResponse<Object> profile(ProfileRequest pojo, HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {
		BaseResponse<Object> result = new BaseResponse<>(ResultType.succes);
		MultipartHttpServletRequest request = (MultipartHttpServletRequest) servletRequest;
		MultipartFile newFile = request.getFile("imgfile");
		String filename = newFile.getOriginalFilename();
		if (filename.endsWith("jpg") || filename.endsWith("png") || filename.endsWith("gif")) {

			try {
				// 存储图片
				String prefix = "E:\\upload\\"+pojo.getUserId()+"\\";
				String newFilename = prefix + pojo.getCookie() + filename.substring(filename.lastIndexOf("."));
				File file = new File(newFilename);
				File parentFile=file.getParentFile();
				if (!parentFile.exists()) {
					parentFile.mkdirs();
				}
				newFile.transferTo(file);
				// 存储字符串
				String userInfo = request.getParameter("data");
				userInfo = userInfo.replace("\\", "");
				userInfo = userInfo.substring(1, userInfo.length() - 1);
				UserModel user = JSON.parseObject(userInfo, UserModel.class);

				Map<String, Object> cond = new HashMap<>();
				cond.put("username", user.getUsername());
				PaginationList<UserModel> users = userDao.findEntityListByCond(cond, null);
				UserModel curUser = new UserModel();
				curUser.setId(users.get(0).getId());
				curUser.setUsername(users.get(0).getUsername());
				curUser.setUrlUserIcon(newFilename);
				curUser.setSex(user.getSex());

				userDao.updateEntity(curUser);

				return result;
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				result = new BaseResponse<Object>(ResultType.fail);
				e.printStackTrace();
			} catch (IOException e) {
				result = new BaseResponse<Object>(ResultType.fail);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;

	}

	public BaseResponse<GetCreditResponse> getGredit(GetCreditRequest pojo, HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {
		BaseResponse<GetCreditResponse> result = new BaseResponse<>(ResultType.succes);
		GetCreditResponse data = new GetCreditResponse();
		result.setData(data);

		List<UserModel> users = userDao.selectCreditPoint();
		if (users == null || users.size() == 0) {
			return result;
		}
		List<GetCreditList> list = new ArrayList<>();
		for (int i = 0; i < users.size(); i++) {
			UserModel user = users.get(i);
			GetCreditList credit = new GetCreditList();
			credit.setId(String.valueOf(i + 1));
			credit.setIcon(user.getUrlUserIcon());
			credit.setName(user.getUsername());
			credit.setPoint(String.valueOf(user.getCredits()));
			list.add(credit);
		}
		data.setRankGson(list);
		return result;
	}
	
	public BaseResponse<Object> addCredit(AddCreditRequest pojo,HttpServletRequest servletRequest, HttpServletResponse servletResponse){
		BaseResponse<Object> result=new BaseResponse<>(ResultType.succes);
		
		Map<String,Object> cond=new HashMap<>();
		cond.put("id", pojo.getId());
		PaginationList<UserModel> list=userDao.findEntityListByCond(cond, null);
		if (list == null || list.size() == 0) {
			result.setCode(ResultType.unKnowUser.getCode());
			result.setDesc(ResultType.unKnowUser.getDesc());
			return result;
		}
		
		UserModel user=new UserModel();
		user.setId(list.get(0).getId());
		int credit=list.get(0).getCredits();
		if(0<pojo.getWeight() &&pojo.getWeight()<500) {
			user.setCredits(credit+10);
		}
		else if(500 <= pojo.getWeight() && pojo.getWeight()<1000){
			user.setCredits(credit+20);
		}
		else if(1000 <= pojo.getWeight() && pojo.getWeight()<5000){
			user.setCredits(credit+50);
		}
		else if(5000 <= pojo.getWeight() && pojo.getWeight()<Integer.MAX_VALUE){
			user.setCredits(credit+100);
		}
		userDao.updateEntity(user);
		return result;
	}
	
	
	
	/*
	 * public BaseResponse<Object> verifyBuyCredits(VerifyBuyCreditsRequest pojo) {
	 * BaseResponse<Object> result = new BaseResponse<>(ResultType.succes); if
	 * (pojo.getVerifyMessage().equals("成功")) {
	 * 
	 * Map<String, Object> cond = new HashMap<>(); cond.put("id", pojo.getId());
	 * PaginationList<OrderModel> order = orderDao.findEntityListByCond(cond, null);
	 * if (order == null || order.size() == 0) {
	 * result.setCode(ResultType.fail.getCode()); result.setDesc("查询不到该订单，请重新输入");
	 * return result; }
	 * 
	 * // 在订单表中修改为交易成功 OrderModel orderModel = new OrderModel();
	 * orderModel.setId(pojo.getId());
	 * orderModel.setOrderStatus(OrderStatus.payed.getCode());
	 * orderDao.updateEntity(orderModel);
	 * 
	 * // 在用户表中更新积分{查询用户} cond.clear(); cond.put("cardNo", orderModel.getCardNo());
	 * PaginationList<UserModel> user = userDao.findEntityListByCond(cond, null); if
	 * (user == null || user.size() == 0) {
	 * result.setCode(ResultType.fail.getCode()); result.setDesc("查询不到该用户"); return
	 * result; }
	 * 
	 * 
	 * 
	 * // 在用户表中更新积分{更新用户积分} UserModel updataUser=new UserModel();
	 * updataUser.setCardNo(user.get(0).getCardNo()); CreditsMisc
	 * creditsMisc=JSON.parseObject(order.get(0).getEventMisc(), CreditsMisc.class);
	 * updataUser.setScore(user.get(0).getScore()+creditsMisc.getCredits().intValue(
	 * )); userDao.updateEntity(updataUser); return result; }
	 * result.setCode(ResultType.fail.getCode()); result.setDesc("微信回调确认信息错误");
	 * return result;
	 * 
	 * }
	 */

}
