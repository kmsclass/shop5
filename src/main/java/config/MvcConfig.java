package config;

import java.util.Properties;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@ComponentScan(basePackages= {"controller","logic","dao","aop","websocket","util"})
@EnableAspectJAutoProxy //AOP 설정
@EnableWebMvc  //유효성 검증.
public class MvcConfig implements WebMvcConfigurer {
	@Bean   //객체화
	public HandlerMapping handlerMapping() {
		RequestMappingHandlerMapping hm =new RequestMappingHandlerMapping();
		hm.setOrder(0);
		return hm;
	}
	@Bean
	public ViewResolver viewResolver() { //뷰결정자
		InternalResourceViewResolver vr =new InternalResourceViewResolver();
		vr.setPrefix("/WEB-INF/view/");
		vr.setSuffix(".jsp");
		return vr;
	}
	@Bean
	public MessageSource messageSource() {
		ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
		ms.setBasename("messages");
		return ms;
	}	
	@Bean
	public MultipartResolver multipartResolver() { //파일 업로드 기능 처리
		//요청정보 중 enctype="multipart/form-data 인경우 동작
		CommonsMultipartResolver mr = new CommonsMultipartResolver();
		mr.setMaxInMemorySize(10485760); //메모리저장 최대 바이트 수
		mr.setMaxUploadSize(104857600);  //최대 업로드 가능 바이트 수
		return mr;
	}
	@Bean    //예외 처리
	public SimpleMappingExceptionResolver exceptionHandler() {
		SimpleMappingExceptionResolver ser =new SimpleMappingExceptionResolver();
		Properties pr = new Properties();
		pr.put("exception.CartEmptyException", "exception");
		pr.put("exception.LoginException", "exception");
		pr.put("exception.BoardException", "exception");
		ser.setExceptionMappings(pr);
		return ser;
	}	
}