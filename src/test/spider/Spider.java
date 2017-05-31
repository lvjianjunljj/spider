package test.spider;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class Spider {
	public static void main(String[] args) {
		// System.out.println(getMeaningFromZemingwang("Jay"));
		// System.out.println(getMeaningFromEname("Jay"));
		// for (int i = 548; i < 704; i++) {
		// getStarListFromYoka(i);
		// }
		for (char c = 'M'; c <= 'Z'; c++) {
			boolean notEnd = true;
			int index = 1;
			while (notEnd) {
				notEnd = getStarListFromManmankan(c, index++);
			}
		}
		// getStarListFromManmankan('A', 2);
	}

	/**
	 * 根据网络地址保存图片
	 * 
	 * @param destUrl
	 *            网络地址
	 * @param filePath
	 *            图片存储路径
	 */
	private static void saveImg(String destUrl, String filePath) {
		filePath = filePath.replaceAll(" ", "_");
		FileOutputStream fos = null;
		BufferedInputStream bis = null;
		HttpURLConnection httpUrl = null;
		URL url = null;
		int BUFFER_SIZE = 1024;
		byte[] buf = new byte[BUFFER_SIZE];
		int size = 0;
		try {
			url = new URL(destUrl);
			httpUrl = (HttpURLConnection) url.openConnection();
			httpUrl.connect();
			bis = new BufferedInputStream(httpUrl.getInputStream());
			// HttpResponse resp = HttpClientTool.getInstance().doGet(destUrl);
			// bis = new BufferedInputStream(resp.getEntity().getContent());
			fos = new FileOutputStream(filePath);

			while ((size = bis.read(buf)) != -1) {
				fos.write(buf, 0, size);
			}
			fos.flush();
		} catch (IOException e) {
		} catch (ClassCastException e) {
		} finally {
			try {
				fos.close();
				bis.close();
				httpUrl.disconnect();
			} catch (IOException e) {
			} catch (NullPointerException e) {
			}
		}
	}

	public static void getStarListFromYoka(int index) {
		String successInfo = "autocont clearfix";
		String endStr = "</div>";
		String startInfo = "<a href=\"";
		String endInfo = "html";
		String info = getInfo("http://www.yoka.com/dna/star/item-------------"
				+ index + ".html", "utf-8");
		// System.out.println(info);
		if (info.contains(successInfo)) {
			int indexStart = info.indexOf(successInfo);
			int indexEnd = info.indexOf(endStr, indexStart);
			if (indexStart < 0) {
				return;
			}
			while (indexStart > 0) {
				indexStart = info.indexOf(startInfo,
						indexStart + startInfo.length());
				if (indexStart < 0 || indexStart > indexEnd) {
					break;
				}
				indexStart = info.indexOf(startInfo,
						indexStart + startInfo.length());
				if (indexStart < 0 || indexStart > indexEnd) {
					break;
				}
				int end = info.indexOf(endInfo,
						indexStart + successInfo.length());
				String url = "http://www.yoka.com"
						+ info.substring(indexStart + startInfo.length(), end)
						+ endInfo;
				String[] infos = getStarInfoFromYoka(url);
				if (infos[4] != null && !infos[4].equals("")) {
					System.out.println(index + ": " + infos[1]);
					try {
						saveImg(infos[4],
								"D:\\nametest\\data\\img\\"
										+ ((infos[0] == null
												|| infos[0].equals("") ? infos[1]
												: infos[0])
												+ "_" + index + ".jpg")
												.replaceAll("\\\\", "&"));
						saveTxt("D:\\nametest\\data\\data\\"
								+ ((infos[0] == null || infos[0].equals("") ? infos[1]
										: infos[0])
										+ "_" + index + ".txt").replaceAll(
										"\\\\", "&"), infos[0] + "\r\n"
								+ infos[1] + "\r\n" + infos[2] + "\r\n"
								+ infos[3]);
					} catch (Exception e) {
						System.out.println("error: " + index + "_" + infos[1]);
					}
				}
			}

		}
	}

	public static String[] getStarInfoFromYoka(String url) {
		// 五项内容分别是英文名、中文名、国籍、职业和头像链接
		String[] res = new String[5];
		String[] startMark = new String[] { "<strong  class=\"en\">",
				"<strong  class=\"n\">", "籍：</span><span class=\"info\">",
				"<strong  class=\"job\">", "<dt><img src=\"" };
		String[] endMark = new String[] { "</strong>", "</strong>", "</span>",
				"</strong>", "\" alt=\"" };
		String info = getInfo(url, "utf-8");
		// System.out.println(info);
		int start = 1, end = 1;
		for (int i = 0; i < res.length; i++) {
			start = info.indexOf(startMark[i]);
			if (start < 0) {
				res[i] = "";
			} else {
				end = info.indexOf(endMark[i], start + startMark[i].length());
				if (end < 0) {
					res[i] = "";
				} else {
					res[i] = info.substring(start + startMark[i].length(), end);
				}
			}
		}
		if (!res[3].equals("")) {
			String[] jobs = res[3].split("｜");
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (String job : jobs) {
				if (first) {
					sb.append(getJob(job));
					first = false;
				} else {
					sb.append("|" + getJob(job).trim());
				}
			}
			res[3] = sb.toString();
		}
		return res;
	}

	public static boolean getStarListFromManmankan(char mark, int index) {
		String url = "";
		if (index == 1) {
			url = "http://www.manmankan.com/dy2013/mingxing/" + mark
					+ "/index.shtml";
		} else {
			url = "http://www.manmankan.com/dy2013/mingxing/" + mark
					+ "/index_" + index + ".shtml";
		}
		String successInfo = "<div class=\"i_cont\">";
		String endStr = "</div>";
		String startInfo = "<a href=\"";
		String endInfo = ".shtml";
		String info = getInfo(url, "gbk");
		// System.out.println(info);
		if (info.contains(successInfo)) {
			int indexStart = info.indexOf(successInfo);
			int indexEnd = info.indexOf(endStr,
					indexStart + successInfo.length());
			if (indexStart < 0) {
				return false;
			}
			while (indexStart > 0) {
				indexStart = info.indexOf(startInfo,
						indexStart + startInfo.length());
				if (indexStart < 0 || indexStart > indexEnd) {
					break;
				}
				int end = info.indexOf(endInfo,
						indexStart + successInfo.length());
				String starUrl = "http://www.manmankan.com"
						+ info.substring(indexStart + startInfo.length(), end)
						+ endInfo;
				String[] infos = getStarInfoFromManmankan(starUrl);
				System.out.println(mark + " " + index + ": " + infos[0]);
				try {
					saveImg(infos[4],
							"D:\\nametest\\star\\img\\"
									+ (infos[0] + "_" + mark + ".jpg")
											.replaceAll("\\\\", "&"));
					saveTxt("D:\\nametest\\star\\data\\"
							+ (infos[0] + "_" + mark + ".txt").replaceAll(
									"\\\\", "&"), infos[0] + "\r\n" + infos[1]
							+ "\r\n" + infos[2] + "\r\n" + infos[3]);
				} catch (Exception e) {
					System.out.println("error: " + index + "_" + infos[1]);
				}
			}
			return true;
		} else {
			return false;
		}
	}

	private static String[] getStarInfoFromManmankan(String starUrl) {
		// 五项内容分别是中文名、国籍、出生地、职业和头像链接
		String[] res = new String[5];
		String[] startMark = new String[] { "<li><span>中文名：</span>",
				"<li><span>国籍：</span>", "<li><span>出生地：</span>",
				"<li><span>职业：</span>",
				"class=\"img_mx\" target=\"_blank\"><img src=\"" };
		String[] endMark = new String[] { "</li>", "</li>", "</li>", "</li>",
				"\" alt=\"" };
		String info = getInfo(starUrl, "gbk");
		// System.out.println(info);
		int start = 1, end = 1;
		for (int i = 0; i < res.length; i++) {
			start = info.indexOf(startMark[i]);
			if (start < 0) {
				res[i] = "";
			} else {
				end = info.indexOf(endMark[i], start + startMark[i].length());
				if (end < 0) {
					res[i] = "";
				} else {
					res[i] = info.substring(start + startMark[i].length(), end);
				}
			}
			res[i] = res[i].replaceAll("/t", "");
		}
		return res;
	}

	private static String getJob(String str) {
		String startStr = "html\" >";
		String endStr = "</a>";
		int start = str.indexOf(startStr);
		if (start < 0) {
			return str;
		} else {
			int end = str.indexOf(endStr);
			return str.substring(start + startStr.length(), end);
		}
	}

	private static void saveTxt(String path, String content) {
		path = path.replaceAll(" ", "_");
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		pw.print(content);
		pw.flush();
		pw.close();
	}

	private static String getInfo(String url, String code) {
		HttpResponse resp = null;
		try {
			resp = doGet(url);
			HttpEntity entity = resp.getEntity();
			return EntityUtils.toString(entity, code);
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
			return "";
		} finally {
			closeQuiet(resp);
		}
	}

	public static HttpResponse doGet(String composeQueryUrl) {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(composeQueryUrl);
		get.addHeader("Accept", "text/html");
		get.addHeader("Accept-Charset", "utf-8");
		get.addHeader("Accept-Encoding", "utf-8");
		get.addHeader("Accept-Language", "en-US,en");
		get.addHeader(
				"User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.160 Safari/537.22");
		try {
			HttpResponse resp = client.execute(get);
			return resp;
		} catch (Exception e) {
			return null;
		}
	}

	public static void closeQuiet(HttpResponse response) {

		if (response != null) {
			EntityUtils.consumeQuietly(response.getEntity());
		}
	}
}
