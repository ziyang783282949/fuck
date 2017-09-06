package com.spirit.porker.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spirit.porker.service.ServiceHandler;
import com.spirit.porker.service.SettingService;
import com.spirit.porker.vo.request.GetCreditRequest;
import com.spirit.porker.vo.request.ProfileRequest;
import com.spirit.porker.vo.request.RegistRequest;
import com.spirit.porker.vo.response.BaseResponse;


@Controller
public class UserController extends BaseController{

	/*@Resource
	MeService meService;*/
	@Resource
	SettingService settingService;
	
	/*@RequestMapping(value = "/shop")
	@ResponseBody
	public String shop(ShowShopRequest pojo,HttpServletRequest servletRequest,HttpServletResponse servletResponse){
		return super.doMain(pojo, servletRequest, servletResponse, new ServiceHandler<ShowShopRequest>() {

			@SuppressWarnings("unchecked")
			@Override
			public <T extends BaseResponse> T doService(ShowShopRequest pojo, HttpServletRequest servletRequest,
					HttpServletResponse servletResponse) throws Exception {
				return (T) meService.shop(pojo, servletRequest, servletResponse);
			}
		});
	}*/
	/*@RequestMapping(value = "/shop")
	@ResponseBody
	public String shop(RegistRequest pojo,HttpServletRequest servletRequest,HttpServletResponse servletResponse){
		return super.doMain(pojo, servletRequest, servletResponse, new ServiceHandler<RegistRequest>() {

			@SuppressWarnings("unchecked")
			@Override
			public <T extends BaseResponse> T doService(RegistRequest pojo, HttpServletRequest servletRequest,
					HttpServletResponse servletResponse) throws Exception {
				return (T) settingService.shop(pojo, servletRequest, servletResponse);
			}
		});
	}*/
	@RequestMapping(value = "/getGredit",method = RequestMethod.POST)
	@ResponseBody
	public String getGredit(GetCreditRequest pojo,HttpServletRequest servletRequest,HttpServletResponse servletResponse){
		return super.doMain(pojo, servletRequest, servletResponse, new ServiceHandler<GetCreditRequest>() {

			@SuppressWarnings("unchecked")
			@Override
			public <T extends BaseResponse> T doService(GetCreditRequest pojo, HttpServletRequest servletRequest,
					HttpServletResponse servletResponse) throws Exception {
				return (T) settingService.getGredit(pojo, servletRequest, servletResponse);
			}
		});
	}
}


