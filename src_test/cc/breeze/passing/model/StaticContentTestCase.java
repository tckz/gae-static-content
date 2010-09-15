/**
 * 
 */
package cc.breeze.passing.model;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cc.breeze.passing.model.StaticContent;

/**
 *
 */
public class StaticContentTestCase {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link cc.breeze.passing.model.StaticContent#StaticContent(java.lang.String, java.lang.Long, byte[])}.
	 */
	@Test
	public void testStaticContentStringLongByteArray() {

		byte[] bin = {0x00, 0x01, 0x03};
		StaticContent sc = new StaticContent("/path/a.txt", (long)1283938902, bin);

		assertEquals("/path/a.txt", sc.getPath());
		assertEquals(new Long(1283938902), sc.getMtime());
		assertArrayEquals(bin, sc.getContent());
	}

	/**
	 * Test method for {@link cc.breeze.passing.model.StaticContent#fromMap(java.util.HashMap)}.
	 */
	@Test
	public void testFromHash() {
		byte[] bin = {0x00, 0x01, 0x03};
		Map<String, Object> h = new HashMap<String, Object>();
		h.put("path", "/path/to/some");
		h.put("mtime", new Long(1234));
		h.put("content", bin);
		
		StaticContent sc = StaticContent.fromMap(h);
		assertEquals("/path/to/some", sc.getPath());
		assertEquals(new Long(1234), sc.getMtime());
		assertArrayEquals(bin, sc.getContent());
	}

	/**
	 * Test method for {@link cc.breeze.passing.model.StaticContent#toMap()}.
	 */
	@Test
	public void testToHash() {
		byte[] bin = {0x00, 0x01, 0x03};
		StaticContent sc = new StaticContent("/path/a.txt", (long)1283938902, bin);

		Map<String, Object> h = sc.toMap();
		assertEquals("/path/a.txt", h.get("path"));
		assertEquals(new Long(1283938902), h.get("mtime"));
		assertArrayEquals(bin, (byte[])h.get("content"));
	}

	@Test
	public void testGetHexStringDigest() {

		assertEquals("9495632a9b539d24719a8ad0ba1db9527541f174", StaticContent.getHexStringDigest("aiu"));
		assertEquals("4c24a2b289cf45bd41410a08fd21312209ad8361", StaticContent.getHexStringDigest("日本語UTF-8"));
		assertEquals("4c24a2b289cf45bd41410a08fd21312209ad8361", StaticContent.getHexStringDigest("日本語UTF-8", "UTF-8"));
		
	}

	@Test
	public void testFromResourceZip() throws IOException {
		System.err.println(String.format("CurrentDir: %s", new File(".").getAbsoluteFile().getParent()));

		String	zipFilenameFormat = "test/static-content/zips/static-content-test-%s.zip";

		StaticContent	sc;
		
		// redirect to index.html on top dir.
		sc = StaticContent.fromResourceZip("", zipFilenameFormat);
		assertNotNull(sc);
		assertEquals("index.html", sc.getPath());
		assertArrayEquals(this.getFileContent("test/static-content/public_html/index.html"), sc.getContent());

		// redirect to index.html
		sc = StaticContent.fromResourceZip("mt/", zipFilenameFormat);
		assertNotNull(sc);
		assertEquals("mt/index.html", sc.getPath());
		assertArrayEquals(this.getFileContent("test/static-content/public_html/mt/index.html"), sc.getContent());

		// not found
		sc = StaticContent.fromResourceZip("notfound", zipFilenameFormat);
		assertNull(sc);

		// exist file.
		sc = StaticContent.fromResourceZip("cgi/cgi.html", zipFilenameFormat);
		assertNotNull(sc);
		assertEquals("cgi/cgi.html", sc.getPath());
		assertArrayEquals(this.getFileContent("test/static-content/public_html/cgi/cgi.html"), sc.getContent());
		
		// redirect to index.html, but not exist.
		sc = StaticContent.fromResourceZip("cgi/", zipFilenameFormat);
		assertNull(sc);
	}

	private byte[] getFileContent(String path) throws IOException	{
		FileInputStream in = new FileInputStream(path);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		try {
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			buf = out.toByteArray();
		} finally {
			in.close();
			out.close();
		}
		
		return	buf;
	}
}
