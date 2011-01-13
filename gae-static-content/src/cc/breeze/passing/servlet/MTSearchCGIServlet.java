package cc.breeze.passing.servlet;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class MTSearchCGIServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

		req.setCharacterEncoding("utf-8");
		String	p_search = req.getParameter("search");
		if (p_search == null) {
			p_search = "";
		}
		String	q = "site:passing.breeze.cc/mt/ " + p_search;
		String	encoded_q = URLEncoder.encode(q, "UTF-8");
		URL	url2redirect = new URL("http://www.google.co.jp/search?ie=UTF-8&q=" + encoded_q);

		res.sendRedirect(url2redirect.toString());
		
	}
}
