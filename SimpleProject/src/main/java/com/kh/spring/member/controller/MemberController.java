package com.kh.spring.member.controller;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.kh.spring.member.model.dto.MemberDto;
import com.kh.spring.member.model.service.MemberServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MemberController {
	
	// public MemberController() {
	//	 System.out.println("안녕한가 난 빈이다");
	// }
	
	// 1. 값 뽑기
	// 2. 가공
	/*
	@RequestMapping("login")
	public void login() {
		System.out.println("난가?");
	}
	*/
	/*
	@RequestMapping("login")
	public String login(HttpServletRequest request) {
		String userId = request.getParameter("id");
		String userPwd = request.getParameter("pwd");
		
		System.out.printf("id : %s, pwd : %s", userId, userPwd);
		
		return "main";
		
	}
	*/
	/*
	@RequestMapping("login")
	public String login(@RequestParam(value="id")String userId,
						@RequestParam(value="pwd")String userPwd) {
		System.out.printf("id : %s, pwd : %s", userId, userPwd);
		
		return "main";
	}
	*/
	/*
	@RequestMapping("login")
	public String login(String id, String pwd) {
		System.out.printf("id : %s, pwd : %s", id, pwd);
		return "main";
	}
	*/
	/*
	 * HandlerAdapter의 판단 :
	 * 
	 * 1. 매개변수 자리에 기본타입(int, boolean, String, Date...)이 있거나
	 * 	  @RequestParam에노테이션이 적혀있는 경우 == @RequestParam으로 인식
	 * 
	 * 2. 매개변수 자리에 사용자 정의 클래스(MemberDto, Board, Reply ...)이 있거나
	 *    @ModelAttribute애노테이션이 존재하는 경우 == 커맨드객체 방식으로 인식
	 *    
	 *    커맨ㄷ 객체 방식
	 *    
	 *    스프링에서 해당 객체를 기본생성자를 이용해서 생성한 후 내부적으로 setter매서드를 찾아서
	 *    요청 시 전달값을 해당 필드에 대입해줌
	 *    
	 *    1. 매개변수 자료형에 반드시 기본생성자가 존재할 것
	 *    2. setter매서드가 반드시 존재할 것
	 *    3. 전달되는 키값과 객체의 필드명이 동일할 것
	 * 
	 */
	
	private final MemberServiceImpl memberService;
	
	
	// @Autowired
	// public void setMemberService(MemberService memberService) {
	// 	this.memberService = memberService;
	// }
	
//	@Autowired
//	public MemberController(MemberService memberService) {
//		this.memberService = memberService;
//	}
	
	/*
	@RequestMapping("login")
	public String login(MemberDto member,
						HttpSession session,
						Model model) {
		// System.out.println(member);
		// new MemberServiceImpl().login(member);
		
		MemberDto userInfo = memberService.login(member);
		
		log.info("조회된 사용자의 정보 : {}", userInfo);
		
		// /WEB-INF/views/main.jsp
		// /WEB-INF/views            .jsp
		
		// 로그인에 성공 / 실패
		// 응답하면 지정
		
		if(userInfo != null) {
			// sesseionScope에 로그인된 사용자의 정보를 set
			
			// 포워딩보다는 => sendRedirect => localhost:8088/kh/
			session.setAttribute("userInfo", userInfo);
			return "redirect:/";
		} else {
			// requestScope에 실패메시지를 set
			// /WEB-INF/views/common/error-page.jsp => 포워딩
			model.addAttribute("message", "로그인 실패");
			// /WEB-INF/views/
			// .jsp
			
			// /WEB-INF/views/     include/error_page        .jsp
			return "include/error_page";
		}
		// return "main";
	}
	*/
	
	// 두번쨰 방법 : 반환타입을 ModelAndView타입으로 반환
	@PostMapping("/login")
	public ModelAndView login(MemberDto member,
							  HttpSession session,
							  ModelAndView mv) {
		
		MemberDto userInfo = memberService.login(member);
		
		if(userInfo != null) {
			session.setAttribute("userInfo", userInfo);
			mv.setViewName("redirect:/");
		} else {
			mv.addObject("meaage", "로그인 실패")
			  .setViewName("include/error_page");
		}
		return mv;
	}
	
	// CRUD
	// 행위 -> .do
	
	// INSERT => POST
	// SELECT => GET
	// UPDATE => PUT, PATH
	// DELETE => DELETE
	
	// 내가 지금 요청하는 자원
	// member://8088/spring/members(기본적으로 복수형을 권장)
	
	// POST
	// /members => MEMBER테이블에 한 행 INSERT
	// GET
	// /members => MEMBER테이블에서 여러 행 조회
	// PUT, PATH
	// /members => MEMBER테이블에서 한 행 UPDATE
	// DELETE
	// /members => MEMBER체이블에서 한 행 삭제
	
	// GET
	// /members/1 => MEMBER테이블에서 회원번호가 1번인 회원 조회 
	
	@GetMapping("logout")
	public String logout(HttpSession session) {
		session.removeAttribute("userInfo");
		return "redirect:/";
	}
	
	@GetMapping("signup")
	public String signupForm() {
		// 포워딩할 jsp파일의 논리적인 경로
		// WEB-INF/views/          member/signup          .jsp
		return "member/signup";
	}
	
	@PostMapping("members")
	public String signup(MemberDto member
						//,HttpServletRequest request
						) {
		
		/*
		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		*/
		
		// 아이디, 이름, 이메일, 비밀번호
		log.info("회원가입 정보 : {}", member);
		memberService.signup(member);
		
		return "main";
	}
	
	@GetMapping("mypage")
	public String myPage() {
		
		return "member/mypage";
	}
	
	@PostMapping("members/{userId}")
	public String update(MemberDto member, @PathVariable(name="userId") String userId, HttpSession session) {
		
		//log.info("URL에 포함된 id값 : {} / 실제 넘어온 DTO : {}", userId, member);
		/*
		 * Base Practice
		 * 
		 * 컨트롤러에서 세션관리를 담담
		 * 서비스에서 HttpSession이 필요하다면 메소드 보충 시 인자로 전달
		 */
		memberService.update(member, session);
		
		return "redirect:/mypage";
	}
	
	@PostMapping("members/{userId}/delete")
	public String delete(@RequestParam(value="userPwd") String userPwd,
						 @PathVariable(value="userId") String userId,
						 HttpSession session) {
		
		memberService.delete(userId, userPwd, session);
		
		return "redirect:/";
	}
	
	@ResponseBody
	@GetMapping(value="checkId", produces="application/json; charset=UTF-8")
	public Map<String, String> checkId(@RequestParam(value="id") String id){
		return Map.of("result", memberService.checkId(id));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
