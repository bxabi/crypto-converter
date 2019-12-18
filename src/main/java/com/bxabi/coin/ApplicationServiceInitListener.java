package com.bxabi.coin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;

//@Component
@Deprecated // probably not needed with the new vaadin
public class ApplicationServiceInitListener implements VaadinServiceInitListener {
	private static final long serialVersionUID = -3387989951492682246L;

	protected final Log logger = LogFactory.getLog(getClass());

	@Override
	public void serviceInit(ServiceInitEvent event) {
		event.addRequestHandler(new RequestHandler() {
			private static final long serialVersionUID = -2724029905321763492L;

			@Override
			public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response)
					throws IOException {
				Enumeration<String> headers = request.getHeaders("user-agent");
				String browser = headers.nextElement();
				if (browser.contains("Chrome/41.0")) {
					Cookie[] cookies = request.getCookies();
					if (cookies == null) {
						response.addCookie(new Cookie("vaadinforceload", "1"));
						PrintWriter writer = response.getWriter();
						writer.append("<html><head>" + "<meta http-equiv=\"refresh\" content=\"0; url=/\">" + "</head>"
								+ "<body><a href='/''>Google Bot using Chrome 41, please follow this refresh link. "
								+ "We are setting a cookie, reloading the page will allow access to the page for an outdated browser.</a></body>"
								+ "</html>");
						return true;
					}
				}
				return false;
			}
		});
	}

}