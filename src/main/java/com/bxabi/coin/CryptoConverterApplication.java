package com.bxabi.coin;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.AbstractRequestLoggingFilter;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@SpringBootApplication
public class CryptoConverterApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoConverterApplication.class, args);
	}

	@Bean
	public AbstractRequestLoggingFilter requestLoggingFilter() {
		AbstractRequestLoggingFilter filter = new RequestLogginFilter();

		filter.setIncludeClientInfo(true);
		filter.setIncludeQueryString(true);
		filter.setIncludePayload(true);
		filter.setIncludeHeaders(true);

		return filter;
	}

	private final class RequestLogginFilter extends CommonsRequestLoggingFilter {
		@Override
		protected void beforeRequest(HttpServletRequest request, String message) {
			if (request.getRequestURI().equals("/") && request.getQueryString() == null)
				logger.info(message);
		}

		@Override
		protected void afterRequest(HttpServletRequest request, String message) {
		}

		@Override
		protected boolean shouldLog(HttpServletRequest request) {
			return true; // logger.isDebugEnabled();
		}
	}
}
