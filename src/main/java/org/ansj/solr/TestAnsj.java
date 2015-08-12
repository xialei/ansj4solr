package org.ansj.solr;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeFactory;

public class TestAnsj {

	public static void main(String[] args) throws IOException {
		String exampleStr = "阿里将投资约280亿元参与苏宁云商的非公开发行，占发行后总股本的19.99%，成为苏宁云商的第二大股东。";
		List<Term> parse = ToAnalysis.parse(exampleStr);
		System.out.println(parse);
		Tokenizer tokenizer = new AnsjTokenizer(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY, 0, true);
		tokenizer.setReader(new StringReader(exampleStr));
		tokenizer.reset();
		CharTermAttribute termAtt = tokenizer.addAttribute(CharTermAttribute.class);
		OffsetAttribute offsetAtt = tokenizer.addAttribute(OffsetAttribute.class);
		PositionIncrementAttribute positionIncrementAtt = tokenizer.addAttribute(PositionIncrementAttribute.class);

		while (tokenizer.incrementToken()) {

			System.out.print(new String(termAtt.toString()));
			System.out.print(offsetAtt.startOffset() + "-" + offsetAtt.endOffset() + "-");
			System.out.print(positionIncrementAtt.getPositionIncrement() + "/");

		}
		tokenizer.close();
	}
}
