package com.spirit.porker.service;

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
import com.spirit.porker.vo.request.GetCreditRequest;
import com.spirit.porker.vo.request.ProfileRequest;
import com.spirit.porker.vo.request.RegistRequest;
import com.spirit.porker.vo.response.BaseResponse;
import com.spirit.porker.vo.response.GetCreditList;
import com.spirit.porker.vo.response.GetCreditResponse;
import com.spirit.porker.vo.response.LoginResponse;
import com.spirit.porker.vo.response.RegistResponse;

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
		userDao.updateEntity(updataUser);

		// 返回客户端cookie及用户数据
		data.setUsername(user.getUsername());
		data.setPassword(user.getPassword());
		data.setCookie(sessionId.getValue().toString());
		return result;
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
			user.setSex("");
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
				String prefix = "E:\\upload\\";
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
