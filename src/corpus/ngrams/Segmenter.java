package corpus.ngrams;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import flib.util.io.QSReader;
import flib.util.io.QSWriter;

public class Segmenter {
	HashMap<String,Float> vocabMap = new HashMap<String,Float>();
	int max_word_len=0;
	
	public Segmenter(File vocFile)
	{
		try
		{
			QSReader qsr = new QSReader(vocFile);
			qsr.skipCommentLine = qsr.skipEmptyLine = true;
			qsr.open();
			for(String line:qsr)
			{
				//System.out.printf("\t[Test] %s\n", line);
				String items[] = line.split("\t");
				vocabMap.put(items[0], Float.valueOf(items[1]));
				if(items[0].length()>max_word_len) max_word_len=items[0].length();
			}
			qsr.close();
			System.out.printf("\t[Info] Loading VocabMap Done! (%,d)\n", vocabMap.size());
		}
		catch(Exception e)
		{
			System.err.printf("\t[Error] Fail to load VocabMap!\n");
			e.printStackTrace();
		}
	}
	
	/**
	 * Substring function (Perl version)
	 * @param txt: Input string
	 * @param offset: Offset in input string
	 * @param len: Length from offset to cut.
	 * @return substring
	 */
	public String substr(String txt, int offset, int len)
	{
		return txt.substring(offset, offset+len);
	}
	
	/**
	 * Segment the text on the text as parameter, return the segmented text separated by space.
	 */
	public String do_segment(String text)
	{
		int default_weight = 40;
		Float tf=null;
		if(text.isEmpty()) return "";
		int text_len = text.length();
		Map<Integer,Float> best_prob = new HashMap<Integer,Float>();
		Map<Integer,Integer> prev = new HashMap<Integer,Integer>();
		best_prob.put(0, (float)0);
		for(int i=0; i<text_len; i++)
		{
			for(int j=i+1; j<=text_len; j++)
			{
				// exceeds the maximal length
				if(j-i>max_word_len) break;
				String current_word = substr(text, i, j-i);
				Float f = vocabMap.get(current_word);
				if(f==null) continue;
				
				// get the previous found path, if not exists, use the default value,
			    // which means we may take the previous token as the path.
				float prev_weight=0;
				if(i>0)
				{
					if((tf=best_prob.get(i))!=null) prev_weight = tf; 
					else prev_weight = default_weight;
				}
				Float w = vocabMap.get(current_word);
				if(w==null) w = (float)0;
				float current_weight = prev_weight+w;
				
				System.out.printf("\t%s->%.02f...", current_word, current_weight);
				// update the path
				/*if (!exists($prev[$j]) || $best_prob[$j] > $current_weight) {
			        $prev[$j] = $i;
			        $best_prob[$j] = $current_weight;
			    }*/			    
				if(!prev.containsKey(j) || ((w=best_prob.get(j))!=null && w>current_weight))
				{
					prev.put(j, i);
					best_prob.put(j, current_weight);
					System.out.printf("Yes!");
				}
				System.out.println();
			} // for(int j=i+1; j<text_len; j++)					
		} // for(int i=0; i<text_len; i++)
		
		// get boundaries
		Map<Integer,Integer> boundaries = new HashMap<Integer,Integer>();
		for(int k=text_len; k>0;)
		{
			boundaries.put(k, 1);
			if(prev.containsKey(k))
			{
				k = prev.get(k);
			}
			else
			{
				k--;
			}
		}
		System.out.printf("\t[Test] Boundaries size=%d...\n", boundaries.size());
		
		// fill the result string
		StringBuffer result = new StringBuffer();
		int previ=0;		
		for(int i=1; i<=text_len; i++)
		{
			if(boundaries.containsKey(i))
			{
				String current_word = substr(text, previ, i-previ);
				result.append(current_word);
				result.append(" ");
				previ=i;
			}
		}
		return result.toString().trim();
	}
	
	/**
	 * Segment the input file and output result to given output file.
	 * @param input: Input file
	 * @param output: Output file
	 * @return Number of line being segmented.
	 */
	public int do_segment(File input, File output) throws Exception
	{		
		int lc=0;
		QSReader qsr = new QSReader(input);
		QSWriter qsw = new QSWriter(output);
		//qsr.skipCommentLine = qsr.skipEmptyLine;
		for(String line:qsr)
		{
			qsw.line(do_segment(line));
			lc++;
		}
		qsr.close();
		qsw.close();
		return lc;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Segmenter segmenter = new Segmenter(new File("vocab.txt"));
		String text = "今天天气不错";
		String segm = segmenter.do_segment(text);
		System.out.printf("%s: '%s'\n", text, segm);
	}
}
