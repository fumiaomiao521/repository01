package cn.itcast.lucene.first;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class LuceneFirst {

	@Test
	public void createIndex() throws Exception {
	
		// 1）创建一个java工程
		// 2）把jar包添加到工程中
		// 3）创建一个Directory对象，可以保存到内存中，也可以保存到磁盘
		//把索引库保存到内存中
		//Directory directory = new RAMDirectory();
		Directory directory = FSDirectory.open(new File("D:/temp/term268/index"));
		// 4）创建一个IndexWriter对象，两个参数一个IndexWriterConfig对象（需要分析器对象），Directory对象
		//创建一个标准分析器对象
		Analyzer analyzer = new StandardAnalyzer();
		//参数1：lucene的版本号,参数2：分析器对象
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, config);
		// 5）读取磁盘上的文档，为每个文件创建一个Document对象。
		File srcPath = new File("D:/传智播客/01.课程/04.lucene/01.参考资料-上课使用/searchsource");
		File[] listFiles = srcPath.listFiles();
		for (File file : listFiles) {
			//创建一个文档对象
			Document document = new Document();
			// 6）向Document对象中添加域
			//取文件名
			String fileName = file.getName();
			//取文件路径
			String filePath = file.getPath();
			//文件的内容
			String fileContent = FileUtils.readFileToString(file);
			//文件的大小
			long fileSize = FileUtils.sizeOf(file);
			//创建对应的域
			//参数1：域的名称 参数2：域的值 参数3：是否存储，如果不存储就不能展示给用户。不影响分词
			Field fieldName = new TextField("name", fileName, Store.YES);
			Field fieldPath = new TextField("path", filePath, Store.YES);
			Field fieldContent = new TextField("content", fileContent, Store.YES);
			Field fieldSize = new TextField("size", fileSize + "", Store.YES);
			//向文档中添加域
			document.add(fieldName);
			document.add(fieldPath);
			document.add(fieldContent);
			document.add(fieldSize);
			// 7）把文档对象写入索引库。
			indexWriter.addDocument(document);
		}
		// 8）关闭IndexWriter对象。
		indexWriter.commit();
		indexWriter.close();
	}
	
	@Test
	public void searchIndex() throws Exception {
		// 1）创建一个IndexReader对象，需要制定索引库的位置
		Directory directory = FSDirectory.open(new File("D:/temp/term268/index"));
		IndexReader indexReader = DirectoryReader.open(directory);
		// 2）创建一个IndexSearcher对象，需要IndexReader对象
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		// 3）创建一个Query对象，TermQuery，需要指定要查询的域及关键词。
		Query query = new TermQuery(new Term("name", "是什么"));
		// 4）执行查询
		//参数1：查询对象 参数2：查询结果返回的最大记录数
		TopDocs topDocs = indexSearcher.search(query, 10);
		//取查询结果的总记录数
		System.out.println("查询结果总记录数：" + topDocs.totalHits);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		// 5）取查询结果ScoreDocs对象。
		for (ScoreDoc scoreDoc : scoreDocs) {
			//取文档id
			int doc = scoreDoc.doc;
			// 6）根据id取文档对象
			Document document = indexSearcher.doc(doc);
			// 7）从Document对象中取field
			System.out.println(document.get("name"));
			System.out.println(document.get("path"));
			System.out.println(document.get("size"));
			System.out.println(document.get("content"));
		}
		// 8）关闭资源
		indexReader.close();
	}
	
	@Test
	public void testTokenStream() throws Exception {
		// 1）创建一个分析器对象
//		Analyzer analyzer = new StandardAnalyzer();
//		Analyzer analyzer = new CJKAnalyzer();
//		Analyzer analyzer = new SmartChineseAnalyzer();
		Analyzer analyzer = new IKAnalyzer();
		// 2）调用分析器的tokenStream()方法，指定要分析的字符串，得到TokenStream对象。
		TokenStream tokenStream = analyzer.tokenStream("", "Lucene是传智播客apache法轮功软件基金会4 jakarta项目组的一个子项目，是一个开放源代码的全文检索引擎工具包，但它不是一个完整的全文检索引擎");
		// 3）调用TokenStream的reset方法。
		tokenStream.reset();
		// 4）设置一个引用，作为指针指向关键词。
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		// 5）遍历TokenStream对象
		while(tokenStream.incrementToken()) {
			// 6）打印结果
			System.out.println(charTermAttribute);
		}
		// 7）关闭TokenStream
		tokenStream.close();
	}
}
