package com.spirit.porker.controller;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.spirit.porker.enums.ResultType;
import com.spirit.porker.service.ServiceHandler;
import com.spirit.porker.service.SettingService;
import com.spirit.porker.util.LoggerUtil;
import com.spirit.porker.vo.request.ProfileRequest;
import com.spirit.porker.vo.response.BaseResponse;



@Controller
public class ProfileController extends BaseController{
	@Resource
	SettingService settingService;
	
	
	/*@RequestMapping(value = "/profile", method = RequestMethod.POST)
	@ResponseBody
	public String profile(ProfileRequest pojo,MultipartFile uploadFile,HttpServletRequest servletRequest, HttpServletResponse servletResponse) {

		BaseResponse<Object> result = null;

		try {
			result = settingService.profile(pojo,uploadFile,servletRequest,servletResponse);
		} catch (Exception e) {
			LoggerUtil.error("修改信息失败", e);
			result = new BaseResponse<Object>(ResultType.fail);

		}
		return JSON.toJSONString(result);

	}*/
	
	@RequestMapping(value = "/profile",method = RequestMethod.POST)
	@ResponseBody
	public String profile(ProfileRequest pojo,HttpServletRequest servletRequest,HttpServletResponse servletResponse){
		return super.doMain(pojo, servletRequest, servletResponse, new ServiceHandler<ProfileRequest>() {

			@SuppressWarnings("unchecked")
			@Override
			public <T extends BaseResponse> T doService(ProfileRequest pojo, HttpServletRequest servletRequest,
					HttpServletResponse servletResponse) throws Exception {
				return (T) settingService.profile(pojo, servletRequest, servletResponse);
			}
		});
	}
}
