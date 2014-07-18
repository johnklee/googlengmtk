package corpus.ngrams.bean;

import java.io.Serializable;

public class UGramBean implements Serializable{
	private static final long 	serialVersionUID = 1L;
	String 						head;
	long						count=0;

	public UGramBean(String head, long cnt){this.head=head; this.count=cnt;}
	
	public String getHead() {
		return head;
	}
	public void setHead(String head, long cnt) {
		this.head = head;
		this.count=cnt;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public void setHead(String head) {
		this.head = head;
	}
}
