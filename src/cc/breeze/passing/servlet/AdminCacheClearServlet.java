package cc.breeze.passing.servlet;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class AdminCacheClearServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

		req.setCharacterEncoding("utf-8");

		try {
			Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
			cache.clear();
	        
		} catch (CacheException e) {
			throw	new RuntimeException(e);
		}

		res.setContentType("text/plain");
		res.setCharacterEncoding("utf-8");

		DateFormat df = DateFormat.getDateTimeInstance();
		df.setTimeZone(TimeZone.getTimeZone("JST"));
        
        res.getWriter().println(String.format("Cache cleared at %s", df.format(new Date())));
	}
}
