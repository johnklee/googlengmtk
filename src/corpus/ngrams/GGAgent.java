package corpus.ngrams;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import corpus.ngrams.bean.TermBean;
import corpus.ngrams.bean.UGramBean;
import corpus.ngrams.inst.IGGA;
import flib.util.TimeStr;
import flib.util.Tuple;
import flib.util.os.MonitorInfoBean;
import flib.util.os.MonitorServiceImpl;

public class GGAgent implements IGGA{
	File 						workDir;
	HashMap<String,TermBean> 	cacheMap = new HashMap<String,TermBean>();
	HashSet<String>				missCache = new HashSet<String>();
	int 						roundCheck=1000;							/*Every 1000 times calling in pairFreq(), will call optimizeCache().*/
	int							piCnt=0;
	
	public GGAgent(File wd)
	{
		this.workDir = wd;
	}
	
	public TermBean loadBean(String head)
	{
		File cDir = new File(workDir, String.valueOf(head.toCharArray()[0]));
		if(cDir.exists())
		{
			File tbFile = new File(cDir, head);
			if(tbFile.exists())
			{
				try
				{
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tbFile));
					TermBean bean = (TermBean)ois.readObject();
					ois.close();
					return bean;
				}
				catch(Exception e)
				{
					System.err.printf("\t[Error] Fail to deserialize TermBean:'%s'!\n", head);
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public UGramBean loadUBean(String head)
	{
		File cDir = new File(workDir, String.valueOf(head.toCharArray()[0]));
		if(cDir.exists())
		{
			File tbFile = new File(cDir, head);
			if(tbFile.exists())
			{
				try
				{
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tbFile));
					UGramBean bean = (UGramBean)ois.readObject();
					ois.close();
					return bean;
				}
				catch(Exception e)
				{
					System.err.printf("\t[Error] Fail to deserialize UGramBean:'%s'!\n", head);
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public void optimizeCache(String t1, String t2){}
	
	public void updatePI(String t1, String t2, boolean bHit){}
	
	public int pairFreq(String t1, String t2)
	{
		piCnt++;
		if(piCnt>roundCheck)
		{
			optimizeCache(t1, t2);
		}
		if(missCache.contains(String.format("%s\t%s", t1, t2))) return -1;
		TermBean bean = cacheMap.get(t1);
		if(bean==null)
		{
			bean = loadBean(t1);
			if(bean==null) {
				missCache.add(String.format("%s\t%s", t1, t2));
				updatePI(t1, t2, false);
				return -1;
			}
			else cacheMap.put(t1, bean);			
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
		
	public void showBean(String head)
	{
		TermBean bean = loadBean(head);
		if(bean!=null)
		{
			System.out.printf("\t[Info] Head='%s' with tail(%d):\n", head, bean.getTails().size());
			Comparator<Tuple> cmp = new Comparator<Tuple>(){
				@Override
				public int compare(Tuple o1, Tuple o2) {
					return Integer.valueOf(o2.getInt(1)).compareTo(o1.getInt(1));
				}
				
			};
			PriorityQueue<Tuple> pq = new PriorityQueue<Tuple>(10, cmp);
			Iterator<Entry<String,Integer>> iter = bean.getTails().entrySet().iterator();
			while(iter.hasNext())
			{
				Entry<String,Integer> e = iter.next();
				//System.out.printf("\t%s (%d)\n", e.getKey(), e.getValue());
				pq.add(new Tuple(e.getKey(), e.getValue()));
			}
			while(!pq.isEmpty())
			{
				Tuple t = pq.poll();
				System.out.printf("\t%s (%d)\n", t.getStr(0), t.getInt(1));
			}
		}
		else
		{
			System.out.printf("\t[Warn] Head='%s' doesn't exist!\n", head);
		}
	}

	public void freeCache(){cacheMap.clear();missCache.clear();}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{		
		String cases[][] = {{"语","心系"}, {"语","心系"}, {"语","意"}, {"语","不存在"}, 
				            {"交回", "组团社"}, {"交回", "讓"}, {"交回", "赔款"}, {"交回", "不存在"},
				            {"完", "蛋"}, {"完", "成"}, {"完", "備"}, {"完", "不存在"},
				            {"完成", "了"}, {"完成", "嗎"}, {"完成", "工作"}, {"完成", "不存在"},
				            {"局", "中局"}, {"局", "外局"}, {"局", "房"},
				            {"步", "步"}, {"步", "行"}, {"步", "出"}};
		MonitorServiceImpl monitorSrvImp = new MonitorServiceImpl();
		IGGA agent = new GGAgent(new File("tmpdir"));
		//agent.showBean("语");
		System.out.printf("\t[Test] Total JVM Memory=%,d KB...\n", monitorSrvImp.getMonitorInfoBean().getTotalJVMMemory()/1024);
		long st = System.currentTimeMillis();
		long gst = System.currentTimeMillis();
		for(String[] pairs:cases)
		{
			System.out.printf("\t[Test] %s->%s...%d (%s)\n", pairs[0], pairs[1], agent.pairFreq(pairs[0], pairs[1]), TimeStr.ToStringFrom(st));
			MonitorInfoBean mb = monitorSrvImp.getMonitorInfoBean();
			System.out.printf("\t\tFree JVM Memory=%,d KB...\n", mb.getFreeJVMMemory()/1024);
			st = System.currentTimeMillis();
		}
		System.out.printf("\t[Test] Done! %s\n", TimeStr.ToStringFrom(gst));
	}
}
