package org.ansj.solr;

import org.ansj.library.DATDictionary;
import org.ansj.library.UserDefineLibrary;
import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MongoAdaptor {

	/**
	 * avoid multi-time insert of user defined words by multi collections
	 * concurrently
	 */
	public static synchronized void getDictWords(String host, int port, String dbName, String table,
			String word, String weight) {

		MongoClient client = new MongoClient(host, port);

		MongoDatabase db = client.getDatabase(dbName);

		db.getCollection(table).find().forEach(new Block<Document>() {

			@Override
			public void apply(Document doc) {

				String kw = doc.getString(word);
				// int nat = dbObj.getInt("nat");
				int freq = doc.getInteger(weight);

				// 如何核心辞典存在那么就放弃
				// MyStaticValue.isSkipUserDefine &&
				if (DATDictionary.getId(kw) > 0) {

				} else {

					UserDefineLibrary.insertWord(kw, UserDefineLibrary.DEFAULT_NATURE,
							freq > 2 ? freq : Integer.parseInt(UserDefineLibrary.DEFAULT_FREQ_STR));
				}
			}

		});

		client.close();

	}

}
