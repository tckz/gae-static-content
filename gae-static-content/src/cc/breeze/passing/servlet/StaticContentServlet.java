package cc.breeze.passing.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cc.breeze.passing.model.StaticContent;

@SuppressWarnings("serial")
public class StaticContentServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(StaticContentServlet.class.getName());

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

		req.setCharacterEncoding("UTF-8");

		String requested = req.getRequestURL().toString();
		URL	cleansedURL = null;
		try {
			cleansedURL = this.cleanseURL(requested);
		} catch (Exception e) {
			log.warning(String.format("Failed to cleanse: %s", requested));
			res.sendError(400);
			return;
		}

		// Requested path without leading '/'.
		// It is assumed that leading '/' of the path in the archive is stripped.
		String requestedPath = cleansedURL.getPath().substring(1);

		StaticContent sc = this.getStaticContent(requestedPath);
		if (sc == null) {
			// Contents not found. but there is a chance to match directory.
			// If requested path likes "/some/dirname"(variable value is "some/dirname"),
			// Try to check for existence of "/some/dirname/".
			// If the directory exists(means the directory has index.html), redirect to there.
			if (!requestedPath.endsWith("/")) {
				String slashedPath = requestedPath + "/";
				sc = this.getStaticContent(slashedPath);
				if (sc != null)	{
					try {
						URI	uri2redirect = new URI(cleansedURL.getProtocol(), cleansedURL.getUserInfo(), cleansedURL.getHost(), cleansedURL.getPort(), "/" + slashedPath, null, null);
						res.sendRedirect(uri2redirect.toURL().toString());
						return;
					} catch (URISyntaxException e) {
						log.warning(e.toString());
					}
				}
			}
			res.sendError(404);
			return;
		}

		long mtime = sc.getMtime();
		long ifModifiedSince = req.getDateHeader("If-Modified-Since");
		if (ifModifiedSince > 0 && mtime <= ifModifiedSince) {
			// Not Modified
			res.setStatus(304);
			return;
		}

		//
		// return its contents actually.
		//

		// Last-Modified
		if (mtime > 0){
			res.setDateHeader("Last-Modified", mtime);
		}

		// Content-Type
		String mime_type = this.getServletContext().getMimeType(sc.getPath());
		if (mime_type != null) {
		    res.setContentType(mime_type);
		}

		res.getOutputStream().write(sc.getContent());
	}

	private	String getZipFileNameFormat() {
		return this.getInitParameter("zip-filename-format");
	}
	
	private URL	cleanseURL(String requested) throws Exception {
		URL	url = null;
		try {
			URI uri = new URI(requested);
			URI nomalized = uri.normalize();
			URI reduced = new URI(nomalized.getScheme(), nomalized.getUserInfo(), nomalized.getHost(), nomalized.getPort(), nomalized.getPath(), null, null);
			url = reduced.toURL();
		} catch (URISyntaxException e) {
			log.warning(e.toString());
			throw new Exception(e);
		} catch (MalformedURLException e) {
			log.warning(e.toString());
			throw new Exception(e);
		}
		
		return	url;
	}
	  
	private StaticContent getStaticContent(String key) throws IOException {
		StaticContent sc = null;
		Cache cache = null;

		try {
			cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
			Map<String, Object> entry = (Map<String, Object>) cache.get(key);
			if (entry != null) 	{
				sc = StaticContent.fromMap(entry);
				// from cache
				return sc;
			}

			log.warning(String.format("Key:%s not found in cache", key));
		} catch (CacheException e) {
			// Ignore. try to retrieve from zip.
			log.severe(String.format("Key=%s :%s", key, e.toString()));
		}

		sc = StaticContent.fromResourceZip(key, this.getZipFileNameFormat());
		if (sc != null && cache != null) {
			try {
				cache.put(key, sc.toMap());
			} catch (RuntimeException  e) {
				// Ignore.
				// Possible reasons:
				//   o The object is too large to cache.
				//   o Some trouble around cache environment.
				log.severe(String.format("Key=%s :%s", key, e.toString()));
			}
		}

		return sc;
	}

}
