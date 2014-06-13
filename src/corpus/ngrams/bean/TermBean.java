package corpus.ngrams.bean;

import java.io.Serializable;
import java.util.HashMap;

public class TermBean implements Serializable{
	private static final long serialVersionUID = 1L;
	String 						head;
	HashMap<String,Integer> 	tails=new HashMap<String,Integer>();
	public String getHead() {
		return head;
	}
	public void setHead(String head) {
		this.head = head;
	}
	public HashMap<String, Integer> getTails() {
		return tails;
	}
	public void setTails(HashMap<String, Integer> tails) {
		this.tails = tails;
	}	
}
