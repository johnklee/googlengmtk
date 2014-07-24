package corpus.ngrams;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Map.Entry;

import corpus.ngrams.bean.TermBean;
import corpus.ngrams.bean.UGramBean;
import corpus.ngrams.inst.IGGA;
import flib.util.Tuple;

public abstract class BasicGGA implements IGGA{
	File 						workDir;
	
	public BasicGGA(File wDir){this.workDir = wDir;}
	
	@Override
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
	
	@Override
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
	
	@Override
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
	
	@Override
	public void optimizeCache(String t1, String t2){}
	
	public void updatePI(String t1, String t2, boolean bHit){}
	public void updatePI(String t1, boolean bHit){}
}
