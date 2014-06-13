package corpus.ngrams.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.regex.Pattern;

import corpus.ngrams.bean.TermBean;
import flib.util.TimeStr;
import flib.util.io.QSReader;
import flib.util.io.enums.EFileType;

public class GenNGramBin {
	public static Pattern WNPtn = Pattern.compile("[a-zA-Z0-9]+");
	public static Pattern FHeadPtn = Pattern.compile("^['\"#$%^&+-~!]");
	public static boolean Filter(String t1, String t2)
	{
		if(t1.startsWith("<") && t1.endsWith(">")) return false;
		if(WNPtn.matcher(t1).find() && WNPtn.matcher(t2).find()) return false;
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
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		long st = System.currentTimeMillis();
		File tmpDir = new File("tmpdir");
		
		QSReader qsr = null;
		File googleNGramSrc = new File("C:/WorkingHouse/Google/data/2-bigrams/");
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
			for(String line:qsr)
			{
				String items[] = line.split("[\t ]");		/*Term1|Term2|Freq*/
				if(items.length==3)
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
