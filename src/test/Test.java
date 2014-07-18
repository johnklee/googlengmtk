package test;

import java.io.File;

import corpus.ngrams.GGAgent;
import corpus.ngrams.inst.IGGA;
import flib.util.TimeStr;
import flib.util.io.QSReader;
import flib.util.io.enums.EFileType;
import flib.util.os.MonitorInfoBean;
import flib.util.os.MonitorServiceImpl;

public class Test {

	public static void FiveGram() throws Exception
	{
		File workDir = new File("C:/WorkingHouse/Google/GNGWdir/5g/"); // Working directory
		IGGA agent = new GGAgent(workDir);
		//agent.showBean("大");
		String cases[][] = {{"三峡", "银行", "原", "计划", "于"},
				            {"三峡", "第一", "人造", "美女", "应征者"},
				            {"三峡", "银行", "原", "计划", "不存在"},
				            {"大", "股东", "资产", "注入", "原水"},
				            {"大", "它", "不仅",	"展现",	"了"}};
		MonitorServiceImpl monitorSrvImp = new MonitorServiceImpl();
		// agent.showBean("语");
		System.out.printf("\t[Test] Total JVM Memory=%,d KB...\n",
				monitorSrvImp.getMonitorInfoBean().getTotalJVMMemory() / 1024);
		long st = System.currentTimeMillis();
		long gst = System.currentTimeMillis();
		for (String[] pairs : cases) {
			StringBuffer tailBuf = new StringBuffer(pairs[1]);
			for(int i=2; i<pairs.length; i++) tailBuf.append(String.format("\t%s", pairs[i]));
			System.out.printf("\t[Test] %s->%s...%d (%s)\n", pairs[0], tailBuf.toString().replaceAll("\t", "|"), agent.pairFreq(pairs[0], tailBuf.toString()),
					TimeStr.ToStringFrom(st));
			MonitorInfoBean mb = monitorSrvImp.getMonitorInfoBean();
			System.out.printf("\t\tFree JVM Memory=%,d KB...\n",
					mb.getFreeJVMMemory() / 1024);
			st = System.currentTimeMillis();
		}
		System.out.printf("\t[Test] Done! %s\n", TimeStr.ToStringFrom(gst));
	}
	
	public static void TwoGram() throws Exception{
		File workDir = new File("C:/WorkingHouse/Google/tmpdir/"); // Working
		// directory
		IGGA agent = new GGAgent(workDir);
		String cases[][] = { { "语", "心系" }, { "语", "心系" }, { "语", "意" },
				{ "语", "不存在" }, { "交回", "组团社" }, { "交回", "讓" }, { "交回", "赔款" },
				{ "交回", "不存在" }, { "完", "蛋" }, { "完", "成" }, { "完", "備" },
				{ "完", "不存在" }, { "完成", "了" }, { "完成", "嗎" }, { "完成", "工作" },
				{ "完成", "不存在" }, { "局", "中局" }, { "局", "外局" }, { "局", "房" },
				{ "步", "步" }, { "步", "行" }, { "步", "出" } };
		MonitorServiceImpl monitorSrvImp = new MonitorServiceImpl();
		// agent.showBean("语");
		System.out.printf("\t[Test] Total JVM Memory=%,d KB...\n",
				monitorSrvImp.getMonitorInfoBean().getTotalJVMMemory() / 1024);
		long st = System.currentTimeMillis();
		long gst = System.currentTimeMillis();
		for (String[] pairs : cases) {
			System.out.printf("\t[Test] %s->%s...%d (%s)\n", pairs[0],
					pairs[1], agent.pairFreq(pairs[0], pairs[1]),
					TimeStr.ToStringFrom(st));
			MonitorInfoBean mb = monitorSrvImp.getMonitorInfoBean();
			System.out.printf("\t\tFree JVM Memory=%,d KB...\n",
					mb.getFreeJVMMemory() / 1024);
			st = System.currentTimeMillis();
		}
		System.out.printf("\t[Test] Done! %s\n", TimeStr.ToStringFrom(gst));
	}

	public static void ReadGZ() throws Exception 
	{
		File gzFile = new File("C:/WorkingHouse/Google/data/3-trigrams/ngrams-00030-of-00394.gz");
        QSReader qsr = new QSReader(gzFile, EFileType.GZ);
        qsr.open();
        for(String line:qsr)
        {
        	System.out.printf("\t[Info] Line='%s'\n", line);
            String items[] = line.split("[ \t]");  // /*Term1<Space>Term2<Space>Term3<Tag>Frequency*/
            System.out.printf("%s->%s (%,d)\n", items[0], items[1], Integer.valueOf(items[2]));
        }
        qsr.close();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception 
	{
		FiveGram();	
	}
}
