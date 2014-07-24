package corpus.ngrams;

import java.io.File;
import java.util.HashSet;

import org.apache.jcs.access.behavior.ICacheAccess;
import org.apache.jcs.access.exception.CacheException;

import corpus.ngrams.bean.TermBean;
import corpus.ngrams.bean.UGramBean;
import corpus.ngrams.inst.IGGA;

public class JCSAgent extends BasicGGA{	
	File 						workDir;
	ICacheAccess 				cacheMap = null;
	ICacheAccess 				uCacheMap = null;
	HashSet<String>				missCache = new HashSet<String>();
	int 						roundCheck=1000;							/*Every 1000 times calling in pairFreq(), will call optimizeCache().*/
	int							piCnt=0;

	public JCSAgent(File wDir) {
		super(wDir);		
	}

	@Override
	public void freeCache() {		
		try
		{
			cacheMap.remove(); uCacheMap.remove();
		}
		catch(Exception e){e.printStackTrace();}
	}

	@Override
	public int pairFreq(String t1, String t2) {
		piCnt++;
		if(piCnt>roundCheck)
		{
			optimizeCache(t1, t2);
		}
		if(missCache.contains(String.format("%s\t%s", t1, t2))) return -1;
		TermBean bean = (TermBean)cacheMap.get(t1);
		if(bean==null)
		{
			bean = loadBean(t1);
			if(bean==null) {
				missCache.add(String.format("%s\t%s", t1, t2));
				updatePI(t1, t2, false);
				return -1;
			}
			else 
			{
				try {
					cacheMap.put(t1, bean);
				} catch (CacheException e) {
					e.printStackTrace();
					return -2;
				}
			}
		}		
		//else return -1;
		Integer f =  bean.getTails().get(t2);
		if(f==null) {
			missCache.add(String.format("%s\t%s", t1, t2));
			updatePI(t1, t2, false);
			return -1;
		}		
		else 
		{
			updatePI(t1, t2, true);
			return f;		
		}
	}

	@Override
	public long count(String t1) {
		UGramBean bean = (UGramBean)uCacheMap.get(t1);
		if(bean==null)
		{
			bean = loadUBean(t1);
			if(bean==null) {
				missCache.add(String.format("%s", t1));
				updatePI(t1, false);
				return -1;
			}
			else 
			{
				try {
					uCacheMap.put(t1, bean);
				} catch (CacheException e) {					
					e.printStackTrace();
					return -2;
				}
			}
		}		
		return bean.getCount();
	}
		
	
}
