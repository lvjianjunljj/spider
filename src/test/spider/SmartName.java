package test.spider;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jxl.*;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class SmartName {
	public static void main(String[] args) throws ParseException, IOException {
		// saveImg("http://avatar.csdn.net/2/E/2/1_zhihui1017.jpg",
		// "D:\\ocrtest\\test1\\17.jpg");

		// System.out.println(getInfo("http://www.baidu.com/"));

		// String[] txt = read("D:\\nametest\\data\\rank\\1992").split("\n");
		// for (int i = 0; i < txt.length; i++)
		// System.out.println(i + " " + txt[i].split("\t")[1]);

		// List<String> filePathList =
		// getFilePathList("D:\\nametest\\data\\rank");
		// for (String f : filePathList) {
		// System.out.println(f);
		// }

		readExcel("D:\\nametest\\data\\tttt.xls",
				"D:\\nametest\\data\\", 0, 1);

		// createExcel();

	}

	private static List<String> getFilePathList(String path) {
		File file = new File(path);
		List<String> res = new ArrayList<String>();
		if (file.isDirectory()) {
			for (String f : file.list()) {
				res.addAll(getFilePathList(path + "\\" + f));
			}
		} else if (file.isFile()) {
			res.add(path);
		}
		return res;
	}

	private static String read(String path) {
		File file = new File(path);
		InputStream input = null;
		try {
			input = new FileInputStream(file);
			StringBuilder sb = new StringBuilder();
			BufferedInputStream buf = new BufferedInputStream(input);
			byte[] buffer = new byte[1024];
			int iRead;
			while ((iRead = buf.read(buffer)) != -1) {
				sb.append(new String(buffer, 0, iRead, "UTF-8"));
			}
			if (input != null) {
				input.close();
			}
			return sb.toString();
		} catch (Exception e) {
			System.out.println("read txt error");
			return "";
		}

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

	/**
	 * 读取excel中的内容
	 * 
	 * @param path
	 */
	private static void readExcel(String readPath, String writePath,
			int nameCol, int infoCol) {
		File file = new File(readPath);
		try {
			// 创建输入流，读取Excel
			InputStream is = new FileInputStream(file.getAbsolutePath());
			// jxl提供的Workbook类
			Workbook wb = Workbook.getWorkbook(is);
			// Excel的页签数量
			int sheet_size = wb.getNumberOfSheets();
			String[] sheetNames = new String[sheet_size];
			List<String>[] names = new List[sheet_size];
			List<String>[] infos = new List[sheet_size];
			List<String>[] means = new List[sheet_size];
			for (int index = 0; index < sheet_size; index++) {
				// 每个页签创建一个Sheet对象
				Sheet sheet = wb.getSheet(index);
				sheetNames[index] = sheet.getName();
				names[index] = new ArrayList<String>();
				infos[index] = new ArrayList<String>();
				means[index] = new ArrayList<String>();
				// sheet.getRows()返回该页的总行数
				for (int i = 0; i < sheet.getRows(); i++) {
					String name = sheet.getCell(nameCol, i).getContents();
					String info = sheet.getCell(infoCol, i).getContents();
					names[index].add(name);
					infos[index].add(info);
					// sheet.getColumns()返回该页的总列数
					if (i == 0) {// 第一行不是
						means[index].add("");
					} else if (name.contains("，") || name.contains(",")) {
						means[index].add("");
					} else {
						String mean = getMeaningFromZemingwang(name);
						if (mean == null) {
							mean = getMeaningFromEname(name);
						}
						if (mean != null) {
							means[index].add(mean);
						} else {
							means[index].add("");
						}
					}
					System.out.println(name + "\t"
							+ means[index].get(means[index].size() - 1));
					// writeExcel(path, index, i, col + 2, name);
					createTxt(writePath, sheetNames, names, infos, means,
							nameCol, infoCol, infoCol + 1);
//					createExcel(writePath, sheetNames, names, infos, means,
//							nameCol, infoCol, infoCol + 1);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void createExcel(String path, String[] sheetNames,
			List[] names, List[] infos, List[] means, int nameCol, int infoCol,
			int meanCol) {
		try {
			// 打开文件
			WritableWorkbook book = Workbook.createWorkbook(new File(path));
			for (int i = 0; i < sheetNames.length; i++) {
				// 生成名为“sheet1”的工作表，参数0表示这是第一页
				WritableSheet sheet = book.createSheet(sheetNames[i], i);
				// 在Label对象的构造子中指名单元格位置是第一列第一行(0,0),单元格内容为string
				for (int j = 0; j < means[i].size(); j++) {
					Label nameLabel = new Label(nameCol, j, names[i].get(j)
							.toString());
					Label infoLabel = new Label(infoCol, j, infos[i].get(j)
							.toString());
					Label meanLabel = new Label(meanCol, j, means[i].get(j)
							.toString());
					// 将定义好的单元格添加到工作表中
					sheet.addCell(nameLabel);
					sheet.addCell(infoLabel);
					sheet.addCell(meanLabel);
				}
				// 生成一个保存数字的单元格,单元格位置是第二列，第一行，单元格的内容为1234.5
				// Number number = new Number(1, 0, 1234.5);
				// sheet.addCell(number);
				// 生成一个保存日期的单元格，单元格位置是第三列，第一行，单元格的内容为当前日期
				// DateTime dtime = new DateTime(2, 0, new Date());
				// sheet.addCell(dtime);
			}
			// 写入数据并关闭文件
			book.write();
			book.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void createTxt(String path, String[] sheetNames,
			List[] names, List[] infos, List[] means, int nameCol, int infoCol,
			int meanCol) {
		try {
			// 打开文件
			for (int i = 0; i < sheetNames.length; i++) {
				String sheetName = sheetNames[i];
				PrintWriter pw = null;
			       try {
			           pw = new PrintWriter(path + sheetName + ".txt");
//			    	   pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + sheetName + ".txt"),"utf-8")));
			       }
			       catch (FileNotFoundException e) {
			           e.printStackTrace();
			       }
			       StringBuilder sb = new StringBuilder();
			       for (int j = 0; j < means[i].size(); j++) {
			    	   sb.append(means[i].get(j) + "\r\n");
			       }
			       pw.print(sb);
			       pw.flush();
			       pw.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void writeExcel(String path, int index, int i, int j,
			String name) {
		Workbook wb = null; // 创建一个workbook对象
		try {
			InputStream is = new FileInputStream(new File(path)); // 创建一个文件流，读入Excel文件
			wb = Workbook.getWorkbook(is); // 将文件流写入到workbook对象
			// jxl.Workbook 对象是只读的，所以如果要修改Excel，需要创建一个可读的副本，副本指向原Excel文件（即下面的new
			// File(excelpath)）
			WritableWorkbook book = Workbook.createWorkbook(new File(path), wb);// 创建workbook的副本
			WritableSheet sheet = book.getSheet(index); // 获取第index个sheet
			Cell l = sheet.getCell(i, j);
			if (l.getContents() == null || l.getContents().equals("")) {
				String mean = getMeaningFromZemingwang(name);
				if (mean == null) {
					mean = getMeaningFromEname(name);
				}
				if (mean != null) {
					System.out.println("write");
					Label lbl = new Label(j, i, mean);// 将第一个单元格的值改为“修改後的值”
					sheet.addCell(lbl);// 将改过的单元格保存到sheet
					book.write();
				}
			}
			book.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getMeaningFromZemingwang(String name) {
		String errorInfo = "并不是一个正规英文名字";
		String successInfo = "<p>意义：";
		String endInfo = "</p>";
		String info = getInfo("http://zemingwang.cn/name-search/" + name);
		if (!info.contains(errorInfo)) {
			int index = info.indexOf(successInfo);
			if (index < 0) {
				return null;
			}
			int index2 = info.indexOf(successInfo, index + 3);
			if (index2 < 0) {
				return null;
			}
			int endIndex = info.indexOf(endInfo, index);
			return info.substring(index + successInfo.length(), endIndex);
		}
		return null;
	}
	
	public static String getMeaningFromEname(String name) {
		String successInfo = "名字含义：</label><span>";
		String endInfo = "</span>";
		String info = getInfo("http://ename.dict.cn/" + name);
		if (info.contains(successInfo)) {
			int index = info.indexOf(successInfo);
			if (index < 0) {
				return null;
			}
			int endIndex = info.indexOf(endInfo, index);
			return info.substring(index + successInfo.length(), endIndex);
		}
		return null;
	}
	
	
	private static String getInfo(String url) {
		HttpResponse resp = null;
		try {
			resp = doGet(url);
			HttpEntity entity = resp.getEntity();
			return EntityUtils.toString(entity, "utf-8");
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
        try {
            HttpResponse resp = client.execute(get);
            return resp;
        } catch (Exception e) {
        	return null;
        }
    }
	
public static void closeQuiet(HttpResponse response){
        
        if (response != null) {
            EntityUtils.consumeQuietly(response.getEntity());
        }
    }
}
