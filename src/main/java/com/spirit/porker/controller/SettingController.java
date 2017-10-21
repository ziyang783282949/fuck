package com.spirit.porker.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.spirit.porker.enums.ResultType;
import com.spirit.porker.model.UserModel;
import com.spirit.porker.service.ServiceHandler;
import com.spirit.porker.service.SettingService;
import com.spirit.porker.util.LoggerUtil;
import com.spirit.porker.vo.request.AddCreditRequest;
import com.spirit.porker.vo.request.GetCreditRequest;
import com.spirit.porker.vo.response.BaseResponse;
import com.spirit.porker.vo.response.LoginResponse;

@Controller
public class SettingController {
	
	@Resource
	SettingService settingService;
	
	@RequestMapping(value = "/addCredit", method = RequestMethod.POST)
	@ResponseBody
	public String addCredit(AddCreditRequest pojo,HttpServletRequest servletRequest, HttpServletResponse servletResponse) {

		BaseResponse<Object> result = null;

		try {
			result = settingService.addCredit(pojo,servletRequest,servletResponse);
		} catch (Exception e) {
			LoggerUtil.error("增加积分失败", e);
			result = new BaseResponse<Object>(ResultType.fail);

		}
		return JSON.toJSONString(result);

	}
	
	
}
