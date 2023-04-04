package jpabook.jpashop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

/**
 * 
	ㆍ스프링 부트 타임리프 viewName 매핑
		resources:templates/ +{ViewName}+ .html
		resources:templates/home.html
	ㆍ반환한 문자( home )과 스프링부트 설정 prefix , suffix 정보를 사용해서 렌더링할 뷰( html )를 찾는다.
 */
@Controller
@Slf4j
public class HomeController {

	@RequestMapping("/")
	public String home() {
		log.info("home controller");
		return "home";
	}
}
