package com.kh.spring.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class LoginInterceptor extends HandlerInterceptorAdapter {
	
	/*
	 * Interceptor
	 * 
	 * Handler호출 시 수행할 내용을 만들어 둘 수 있음
	 */

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		HttpSession session = request.getSession();
		
		if(session.getAttribute("userInfo") != null) {
			return true;
		} else {
			response.sendRedirect(request.getContextPath());
			return false;
		}
		
	}
	
	
}
