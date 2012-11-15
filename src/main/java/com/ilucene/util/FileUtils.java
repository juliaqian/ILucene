package com.ilucene.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {

	/*---store the generated index data in the directory----*/
	private static String indexDir = null;
	/*---display the data directory path----*/
	private static String dataDir = null ;
	
	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
	
	static {
		if(indexDir == null || dataDir == null){
			indexDir = FileUtils.class.getClassLoader().getResource("index").getPath();
			dataDir = FileUtils.class.getClassLoader().getResource("index/data").getPath();
			if(!System.getProperty("file.separator").equals("/")){ // windows os
				indexDir = indexDir.substring(1).replaceAll("/", "\\\\\\\\");
				dataDir = dataDir.substring(1).replaceAll("/", "\\\\\\\\");
			}
			logger.debug("build the directory path:" + indexDir);
		}
	}
	
	public static String getDataDir() {
		return dataDir;
	}

	public static String getIndexDir() {
		logger.debug("indexDir:" + indexDir);
		return indexDir;
	}
}
