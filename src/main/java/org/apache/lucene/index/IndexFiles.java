package org.apache.lucene.index;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

/** Index all text files under a directory. */
public class IndexFiles {
  
  private IndexFiles() {}

  static final File INDEX_DIR = new File("indexDoc");
  static String docDirPath = null;
  private static final Logger logger = LoggerFactory.getLogger(IndexFiles.class);
  static{
      docDirPath =  IndexFiles.class.getClassLoader().getResource("org").getPath() ;
  }
  
  /** Index all text files under a directory. */
  public static void main(String[] args) {
    String usage = "java org.apache.lucene.{}.IndexFiles <root_directory>";
//    if (args.length == 0) {
//      System.err.println("Usage: " + usage);
//      System.exit(1);
//    }
    if (INDEX_DIR.exists()) {
        File[] files = INDEX_DIR.listFiles();
        for(int i=0; i < files.length; i++){
            if(files[i].delete()){
                logger.info(" deleted file "+ files[i]);
            }
        }
        INDEX_DIR.delete();
    }
    
    final File docDir = new File(docDirPath);

    if (!docDir.exists() || !docDir.canRead()) {
      logger.info("Document directory '" +docDir.getAbsolutePath()+
                "' does not exist or is not readable, please check the path");
      System.exit(1);
    }
    
    Date start = new Date();
    try {
      IndexWriter writer = new IndexWriter(FSDirectory.open(INDEX_DIR),
                                 new StandardAnalyzer(Version.LUCENE_30),
                                 true,
                                 IndexWriter.MaxFieldLength.LIMITED);
      logger.info("Indexing to directory '" +INDEX_DIR+ "'...");
      indexDocs(writer, docDir);
      logger.info("Optimizing...");
      writer.optimize();
      writer.close();

      Date end = new Date();
      logger.info(end.getTime() - start.getTime() + " total milliseconds");

    } catch (IOException e) {
      logger.info(" caught a " + e.getClass() +
       "\n with message: " + e.getMessage());
    }
  }

  static void indexDocs(IndexWriter writer, File file)
    throws IOException {
    // do not try to index files that cannot be read
    if (file.canRead()) {
      if (file.isDirectory()) {
        String[] files = file.list();
        // an IO error could occur
        if (files != null) {
          for (int i = 0; i < files.length; i++) {
            indexDocs(writer, new File(file, files[i]));
          }
        }
      } else {

        try {
            if(file.getName().endsWith(".class"))   {
                logger.info("adding " + file);
                writer.addDocument(FileDocument.Document(file));
            }
        }
        // at least on windows, some temporary files raise this exception with an "access denied" message
        // checking if the file can be read doesn't help
        catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
      }
    }
  }
  
}
