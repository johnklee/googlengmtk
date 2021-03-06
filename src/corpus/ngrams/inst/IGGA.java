package corpus.ngrams.inst;

import corpus.ngrams.bean.TermBean;
import corpus.ngrams.bean.UGramBean;

public interface IGGA {
	public TermBean loadBean(String head);
	public void showBean(String head);
	public UGramBean loadUBean(String head);
	public void freeCache();
	public int pairFreq(String t1, String t2);
	public long count(String t1);
	public void updatePI(String t1, String t2, boolean bHit);
	public void optimizeCache(String t1, String t2);
}
