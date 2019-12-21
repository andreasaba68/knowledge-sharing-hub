package it.asaba.ksh.loghttprequestresponse;

import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Configuration
public class LoggingFilter extends GenericFilterBean {

  /**
   * It's important that you actually register your filter this way rather then just annotating it as @Component
   * as you need to be able to set for which "DispatcherType"s to enable the filter (see point *1*)
   * 
   * @return
   */
  @Bean
  public FilterRegistrationBean<LoggingFilter> initFilter(){
      FilterRegistrationBean<LoggingFilter> registrationBean = new FilterRegistrationBean<>();
      registrationBean.setFilter(new LoggingFilter());

      // *1* make sure you sett all dispatcher types if you want the filter to log upon
      registrationBean.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));

      // *2* this should put your filter above any other filter
      registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);

      return registrationBean;    
  }

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		ContentCachingRequestWrapper wreq= new ContentCachingRequestWrapper((HttpServletRequest)request);
		ContentCachingResponseWrapper wres=new ContentCachingResponseWrapper((HttpServletResponse)response);

		try {

      // let it be ...
			chain.doFilter(wreq, wres);

			while (wreq.getInputStream().read()>=0);
      System.out.printf("=== REQUEST%n%s%n=== end request%n",new String(wreq.getContentAsByteArray()));

			// Do whatever logging you wish here, in this case I'm writing request and response to system out which is certanly what you do not want
	    System.out.printf("=== RESPONSE%n%s%n=== end response%n",new String(wres.getContentAsByteArray()));

	    // this is specific of the "ContentCachingResponseWrapper" we are relying on, make sure you call it after you read the content from the response
      wres.copyBodyToResponse();

	    // One more point, in case of redirect this will be called twice! beware to handle that somewhat

		} catch (Throwable t) {
      // Do whatever logging you whish here, too
		  // TODO: here you should also be logging the error!!!
			throw t;
		}

	}
}