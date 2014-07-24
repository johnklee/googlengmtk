package corpus.ngrams;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Properties;

import org.apache.jcs.JCS;
import org.apache.jcs.access.behavior.ICacheAccess;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.control.CompositeCacheManager;

import corpus.ngrams.bean.TermBean;
import corpus.ngrams.bean.UGramBean;
import corpus.ngrams.inst.IGGA;

public class JCSAgent extends BasicGGA{	
	File 						workDir;
	File						jcsConfig = new File("cache.ccf");
	ICacheAccess 				cacheMap = null;
	ICacheAccess 				uCacheMap = null;
	HashSet<String>				missCache = new HashSet<String>();
	int 						roundCheck=1000;							/*Every 1000 times calling in pairFreq(), will call optimizeCache().*/
	int							piCnt=0;
	
	public JCSAgent(File wDir, Properties props)
	{
		super(wDir);
		CompositeCacheManager ccm = CompositeCacheManager.getUnconfiguredInstance();
		ccm.configure(props);
		try
		{
			cacheMap = JCS.getInstance("NGram");
			uCacheMap = JCS.getInstance("UGram");
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	public JCSAgent(File wDir) {
		super(wDir);
		CompositeCacheManager ccm = CompositeCacheManager.getUnconfiguredInstance();
		Properties props = new Properties();
		if(jcsConfig.exists())
		{			
			try
			{								
				props.load(new FileInputStream(new File("cache.ccf")));							
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			props.setProperty("jcs.default", "DC");
			props.setProperty("jcs.default.cacheattributes", "org.apache.jcs.engine.CompositeCacheAttributes");
			props.setProperty("jcs.default.cacheattributes.MaxObjects", "1000");
			props.setProperty("jcs.default.cacheattributes.MemoryCacheName", "org.apache.jcs.engine.memory.lru.LRUMemoryCache");
			
			props.setProperty("jcs.auxiliary.DC", "org.apache.jcs.auxiliary.disk.indexed.IndexedDiskCacheFactory");
			props.setProperty("jcs.auxiliary.DC.attributes", "org.apache.jcs.auxiliary.disk.indexed.IndexedDiskCacheAttributes");						
			props.setProperty("jcs.auxiliary.DC.attributes.DiskPath", "jcs_swap");
			props.setProperty("jcs.auxiliary.DC.attributes.MaxPurgatorySize", "10000000");
			props.setProperty("jcs.auxiliary.DC.attributes.MaxKeySize", "1000000");
			props.setProperty("jcs.auxiliary.DC.attributes.MaxRecycleBinSize", "5000");
			props.setProperty("jcs.auxiliary.DC.attributes.OptimizeAtRemoveCount", "300000");
			props.setProperty("jcs.auxiliary.DC.attributes.ShutdownSpoolTimeLimit", "60");
		}
		
		ccm.configure(props);
		try
		{
			cacheMap = JCS.getInstance("NGram");
			uCacheMap = JCS.getInstance("UGram");
		}
		catch(Exception e){e.printStackTrace();}
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
