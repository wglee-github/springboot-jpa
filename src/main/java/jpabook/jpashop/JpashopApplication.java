package jpabook.jpashop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

/**
 * 
 * @author wglee
 *	
 *	@SpringBootApplication
 	- @SpringBootApplication 어노테이션이 선언된 패키지와 하위 패키지에 @Component( @Controller, @Service, @Repository ) 어노테이션으로 선언된 모든 클래스를 검색해서 spring bean에 등록한다.
 */
@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

}
