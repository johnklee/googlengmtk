package corpus.ngrams.inst;

import corpus.ngrams.bean.TermBean;

public interface IGGA {
	public TermBean loadBean(String head);
	public void showBean(String head);
	public void freeCache();
	public int pairFreq(String t1, String t2);
	public void updatePI(String t1, String t2, boolean bHit);
	public void optimizeCache(String t1, String t2);
}
