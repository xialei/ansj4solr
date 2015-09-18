package org.ansj.solr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ansj.library.DATDictionary;
import org.ansj.library.UserDefineLibrary;
import org.ansj.util.MyStaticValue;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.StringUtil;

public class AnsjTokenizerFactory extends TokenizerFactory implements ResourceLoaderAware, UpdateKeeper.UpdateJob {

	// analysisType="1" 表示分词类型，1代表标准切分，默认是0。
	private int analysisType = 0;
	// rmPunc="false" 表示不去除标点符号
	private boolean rmPunc = true;
	private ResourceLoader loader;

	private long lastUpdateTime = -1;
	private String conf = null;

	public AnsjTokenizerFactory(Map<String, String> args) {
		super(args);
		analysisType = getInt(args, "analysisType", 0);
		rmPunc = getBoolean(args, "rmPunc", true);
		conf = get(args, "conf");
		System.out.println(":::ansj:construction::::::::::::::::::::::::::" + conf);
	}

	public void update() throws IOException {
		Properties p = needUpdate();
		if (p != null) {
			String storageType = p.getProperty("dict.storage");
			if ("mongo".equalsIgnoreCase(storageType)) {
				String hoststr = p.getProperty("dict.mongo");
				String[] hostParts = hoststr.split("/");
				String hostport = hostParts[0];
				String db = hostParts[1];
				String[] parts = hostport.split(":");
				String host = parts[0];
				int port = 27017;
				if (parts.length > 1) {
					port = Integer.parseInt(parts[1]);
				}
				String table = p.getProperty("dict.table");
				String fields = p.getProperty("dict.fields");
				String[] farray = fields.split(",");
				Map<String, String> fm = new HashMap<String, String>();
				for (int i = 0; i < farray.length; i++) {
					String[] fv = farray[i].split(":");
					fm.put(fv[0], fv[1]);
				}
				new MongoAdaptor(host, port).getDictWords(db, table, fm.get("word"), fm.get("weight"));

			} else if ("file".equalsIgnoreCase(storageType)) {
				List<String> dicPaths = getFileNames(p.getProperty("files"));
				for (String path : dicPaths) {
					if ((path != null && !path.isEmpty())) {
						InputStream is = loader.openResource(path);

						if (is != null) {
							addUserDefinedWords(is);
						}
					}
				}
			}

		}
	}

	public void inform(ResourceLoader loader) throws IOException {
		System.out.println(":::ansj:::inform::::::::::::::::::::::::" + conf);
		this.loader = loader;
		this.update();
		if (conf != null && !conf.trim().isEmpty()) {
			UpdateKeeper.getInstance().register(this);
		}
	}

	@Override
	public Tokenizer create(AttributeFactory factory) {
		return new AnsjTokenizer(factory, analysisType, rmPunc);
	}

	private void addUserDefinedWords(InputStream file) {

		String temp = null;
		BufferedReader br = null;
		String[] parts = null;
		try {
			br = IOUtil.getReader(file, "UTF-8");
			while ((temp = br.readLine()) != null) {
				if (StringUtil.isBlank(temp)) {
					continue;
				} else {
					parts = temp.split("\t");

					parts[0] = parts[0].toLowerCase();

					// 如何核心辞典存在那么就放弃
					if (MyStaticValue.isSkipUserDefine && DATDictionary.getId(parts[0]) > 0) {
						continue;
					}

					int len = parts.length;

					UserDefineLibrary.insertWord(parts[0], len > 1 ? parts[1] : UserDefineLibrary.DEFAULT_NATURE,
							Integer.parseInt(len > 2 ? parts[2] : UserDefineLibrary.DEFAULT_FREQ_STR));
				}
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtil.close(br);
			br = null;
		}
	}

	private List<String> getFileNames(String fileNames) {
		if (fileNames == null)
			return Collections.<String> emptyList();
		List<String> result = new ArrayList<String>();
		for (String file : fileNames.split("[,\\s]+")) {
			result.add(file);
		}
		return result;
	}

	private Properties needUpdate() {

		try {
			if (conf == null)
				return null;
			Properties p = new Properties();
			InputStream confStream = loader.openResource(conf);
			p.load(confStream);
			confStream.close();
			String lastupdate = p.getProperty("lastupdate", "0");
			Long t = new Long(lastupdate);

			if (t > this.lastUpdateTime) {
				this.lastUpdateTime = t.longValue();
				String paths = p.getProperty("files");
				if (paths == null || paths.trim().isEmpty())
					return null;
				System.out.println("loading conf");
				return p;
			} else {
				this.lastUpdateTime = t.longValue();
				return null;
			}
		} catch (Exception e) {
			System.err.println("ansj parsing conf NullPointerException~~~~~" + e.getMessage());
			return null;
		}
	}

}
