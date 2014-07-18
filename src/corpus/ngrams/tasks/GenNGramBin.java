package corpus.ngrams.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.regex.Pattern;

import corpus.ngrams.bean.TermBean;
import corpus.ngrams.bean.UGramBean;
import flib.util.TimeStr;
import flib.util.io.QSReader;
import flib.util.io.enums.EFileType;

public class GenNGramBin {
	public static Pattern WNPtn = Pattern.compile("[a-zA-Z0-9]+");
	public static Pattern FHeadPtn = Pattern.compile("^['\"#$%^&+-~!]");
	
	public static boolean Filter(String t1)
	{
		if(t1.startsWith("<") && t1.endsWith(">")) return false;
		if(WNPtn.matcher(t1).find()) return false;
		if(FHeadPtn.matcher(t1).find()) return false;
		return true;
	}
	
	public static boolean Filter(String t1, String t2)
	{
		if(t1.startsWith("<") && t1.endsWith(">")) return false;
		if(WNPtn.matcher(t1).find() && WNPtn.matcher(t2).find()) return false;
		if(FHeadPtn.matcher(t1).find()) return false;
		return true;
	}
	
	public static boolean Filter(String t1, String t2, String t3)
	{
		if(t1.startsWith("<") && t1.endsWith(">")) return false;
		if(WNPtn.matcher(t1).find() && 
		   WNPtn.matcher(t2).find() &&
		   WNPtn.matcher(t3).find()) return false;
		if(FHeadPtn.matcher(t1).find()) return false;
		return true;
	}
	
	public static boolean Filter(String t1, String t2, String t3, String t4)
	{
		if(t1.startsWith("<") && t1.endsWith(">")) return false;
		if(WNPtn.matcher(t1).find() && 
		   WNPtn.matcher(t2).find() &&
		   WNPtn.matcher(t3).find() &&
		   WNPtn.matcher(t4).find()) return false;
		if(FHeadPtn.matcher(t1).find()) return false;
		return true;
	}
	
	public static boolean Filter(String t1, String t2, String t3, String t4, String t5)
	{
		if(t1.startsWith("<") && t1.endsWith(">")) return false;
		if(WNPtn.matcher(t1).find() && 
		   WNPtn.matcher(t2).find() &&
		   WNPtn.matcher(t3).find() &&
		   WNPtn.matcher(t4).find() &&
		   WNPtn.matcher(t5).find()) return false;
		if(FHeadPtn.matcher(t1).find()) return false;
		return true;
	}

	public static TermBean LoadBean(File dir, String head)
	{
		File cDir = new File(dir, String.valueOf(head.toCharArray()[0]));
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
	
	public static UGramBean LoadUBean(File dir, String head)
	{
		File cDir = new File(dir, String.valueOf(head.toCharArray()[0]));
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
					System.err.printf("\t[Error] Fail to deserialize TermBean:'%s'!\n", head);
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public static void DumpUBean(File dir, UGramBean tb)
	{
		File cDir = new File(dir, String.valueOf(tb.getHead().toCharArray()[0]));
		cDir.mkdirs();
		File tbFile = new File(cDir, tb.getHead());
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tbFile));
			oos.writeObject(tb);
			oos.close();
		}
		catch(Exception e)
		{
			System.err.printf("\t[Error] Fail to serialize UGramBean:'%s'!\n", tb.getHead());
			e.printStackTrace();
		}
	}
	
	public static void DumpBean(File dir, TermBean tb)
	{
		File cDir = new File(dir, String.valueOf(tb.getHead().toCharArray()[0]));
		cDir.mkdirs();
		File tbFile = new File(cDir, tb.getHead());
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tbFile));
			oos.writeObject(tb);
			oos.close();
		}
		catch(Exception e)
		{
			System.err.printf("\t[Error] Fail to serialize TermBean:'%s'!\n", tb.getHead());
			e.printStackTrace();
		}
	}
	
	public static String MergeTail(String ...ts)
	{
		StringBuffer tailBuf = new StringBuffer();
		tailBuf.append(ts[0]);
		for(int i=1; i<ts.length; i++) tailBuf.append(String.format("\t%s", ts[i]));
		return tailBuf.toString();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		long st = System.currentTimeMillis();
		File tmpDir = new File("C:/WorkingHouse/Google/GNGWdir/1g");
		
		QSReader qsr = null;
		File googleNGramSrc = new File("C:/WorkingHouse/Google/data/1-unigram/");
		int fc=0;
		int gc=0;
		int pmt=-1;
		int sp=-1;
		for(File gz:googleNGramSrc.listFiles())
		{
			System.out.printf("\t[Info] Handle GZ File=%s...(%d)\n", gz.getName(), ++fc);
			if(sp>0 && fc<sp) continue;
			if(qsr==null)
			{
				 qsr = new QSReader(gz,EFileType.GZ);
				 qsr.skipCommentLine = qsr.skipEmptyLine;
				 qsr.open();
			}
			else qsr.reopen(gz);
			
			TermBean bean=null;
			UGramBean ubean = null;
			for(String line:qsr)
			{
				String items[] = line.split("[\t ]");		/*Term1<Space>Term2<Tag>Freq*/
				if(items.length==2)
				{
					if(Filter(items[0]))
					{
						ubean = new UGramBean(items[0], Long.valueOf(items[1]));
						DumpUBean(tmpDir, ubean);
					}
				}
				else if(items.length==3)
				{
					if(Filter(items[0], items[1]))
					{
						//System.out.printf("%s -> %s (%d)\n", items[0], items[1], Integer.valueOf(items[2]));
						if(bean==null || !bean.getHead().equals(items[0]))
						{
							if(bean!=null) DumpBean(tmpDir, bean);
							bean = LoadBean(tmpDir, items[0]);
							if(bean==null)
							{
								bean = new TermBean();
								bean.setHead(items[0]);
							}
						}
						bean.getTails().put(items[1], Integer.valueOf(items[2]));
						gc++;
					}
				}
				else if(items.length==4)
				{
					// 3gram
					if(Filter(items[0], items[1], items[2]))
					{
						//System.out.printf("%s -> %s (%d)\n", items[0], items[1], Integer.valueOf(items[2]));
						if(bean==null || !bean.getHead().equals(items[0]))
						{
							if(bean!=null) DumpBean(tmpDir, bean);
							bean = LoadBean(tmpDir, items[0]);
							if(bean==null)
							{
								bean = new TermBean();
								bean.setHead(items[0]);
							}
						}
						bean.getTails().put(MergeTail(items[1], items[2]), Integer.valueOf(items[3]));
						gc++;
					}
				}
				else if(items.length==5)
				{
					// 4gram
					if(Filter(items[0], items[1], items[2], items[3]))
					{
						//System.out.printf("%s -> %s (%d)\n", items[0], items[1], Integer.valueOf(items[2]));
						if(bean==null || !bean.getHead().equals(items[0]))
						{
							if(bean!=null) DumpBean(tmpDir, bean);
							bean = LoadBean(tmpDir, items[0]);
							if(bean==null)
							{
								bean = new TermBean();
								bean.setHead(items[0]);
							}
						}
						bean.getTails().put(MergeTail(items[1], items[2], items[3]), Integer.valueOf(items[4]));
						gc++;
					}
				}
				else if(items.length==6)
				{
					// 5gram
					if(Filter(items[0], items[1], items[2], items[3], items[4]))
					{
						//System.out.printf("%s -> %s (%d)\n", items[0], items[1], Integer.valueOf(items[2]));
						if(bean==null || !bean.getHead().equals(items[0]))
						{
							if(bean!=null) DumpBean(tmpDir, bean);
							bean = LoadBean(tmpDir, items[0]);
							if(bean==null)
							{
								bean = new TermBean();
								bean.setHead(items[0]);
							}
						}
						bean.getTails().put(MergeTail(items[1], items[2], items[3], items[4]), Integer.valueOf(items[5]));
						gc++;
					}
				}
				else System.err.printf("\t[Error] Illegal format: %s (%d)\n", line, items.length);
			}
			if(pmt>0 && fc>pmt) break;
		}
		qsr.close();
		System.out.printf("\t[Info] Total %,d GZ file(s)...\n", fc-1);
		System.out.printf("\t[Info] Total %,d NGrams...\n", gc);
		System.out.printf("\t[Info] Done! %s\n ", TimeStr.ToStringFrom(st));
	}
}
