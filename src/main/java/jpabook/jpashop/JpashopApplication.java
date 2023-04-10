package jpabook.jpashop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule.Feature;

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
	
	@Bean
	Hibernate5JakartaModule hibernate5JakartaModule() {
		Hibernate5JakartaModule jakartaModule = new Hibernate5JakartaModule();
		jakartaModule.configure(Feature.FORCE_LAZY_LOADING, false);
		return jakartaModule;
	}

}
