package cc.breeze.passing.model;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class StaticContent {
	private static final Logger log = Logger.getLogger(StaticContent.class.getName());

	private String	path;
	private	Long	mtime;
	private	byte[]	content;
	
	public	StaticContent()	{
	}

	public	StaticContent(String path, Long mtime, byte[] content)	{
		this.setPath(path);
		this.setMtime(mtime);
		this.setContent(content);
	}
	
	static public StaticContent fromMap(Map<String, Object> h)	{
		return new StaticContent((String)h.get("path"), (Long)h.get("mtime"), (byte[])h.get("content"));
	}

	static public StaticContent fromResourceZip(String key, String zipFilenameFormat) throws IOException {
		if (key.equals(""))	{
			return fromResourceZip("index.html", zipFilenameFormat);
		}

		String	digest = getHexStringDigest(key);
		String	head = digest.substring(0, 1);
		String	zipFilename = String.format(zipFilenameFormat, head);

		ZipInputStream in = null;
		try {
			in = new ZipInputStream(new FileInputStream(zipFilename));
		} catch (FileNotFoundException e) {
			log.warning(e.toString());
			return	null;
		}
		
		try {
			ZipEntry entry;
			while ((entry = in.getNextEntry()) != null) {
	
				String entryName = entry.getName();

				if (key.equals(entryName)) {
					if (entry.isDirectory()) {
						return	fromResourceZip(key + "index.html", zipFilenameFormat);
					}

					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte[] buf = new byte[8192];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					out.close();
	
					log.info(String.format("Key:%s found in %s", key, zipFilename));
					StaticContent sc = new StaticContent(key, entry.getTime(), out.toByteArray());
					return sc;
				}
			}
		} finally {
			in.close();
		}

		log.warning(String.format("Key:%s not found in %s", key, zipFilename));
		return	null;
	}
	
	public	Map<String, Object>	toMap() {
		Map<String, Object> h = new HashMap<String, Object>();
		h.put("path", this.path);
		h.put("mtime", this.mtime);
		h.put("content", this.content);
		
		return h;
	}

	public void setPath(String path) {
		this.path = path;
	}
	public String getPath() {
		return path;
	}
	public void setMtime(Long mtime) {
		this.mtime = mtime;
	}
	public Long getMtime() {
		return mtime;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	public byte[] getContent() {
		return content;
	}
	
	protected static String getHexStringDigest(String data) {
		return getHexStringDigest(data, "utf-8");
	}

	protected static String getHexStringDigest(String data, String charset) {
		MessageDigest md ;
		byte[] dat ;
		try {
			md = MessageDigest.getInstance("SHA1");
			dat = data.getBytes(charset);
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		md.update(dat);
		StringBuffer sb = new StringBuffer();
		for (int b : md.digest()) {
			if (b < 0)	{
				b += 256;
			}
			sb.append(String.format("%02x", b));
		}
		
		return sb.toString();
	}
	
}
