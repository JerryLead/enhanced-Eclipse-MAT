package cn.ac.iscas.oomr.main;

import java.text.DecimalFormat;

import cn.ac.iscas.oomr.dominatortree.Row;
import cn.ac.iscas.oomr.thread.path.Path;

public class UserObj {

	private Row rawObj;
	private int length = 1;
	private String thread = "";
	private String code = "";
	
	// If UserObj is List, innerObj will be set
	private Row innerObj;
	// If UserObj is Map, key and value will be set
	private Row key;
	private Row value;
	
	private Path path;
	

	
	public UserObj(Row rawObj) {
		this.rawObj = rawObj;
	}
	
	public Row getUserObj() {
		return rawObj;
	}
	
	public void setLength(int length) {
		this.length = length;
	}
	
	public void setThread(String thread) {
		if(this.thread.isEmpty())
			this.thread = thread;
		else
			this.thread += " + " + thread;
	}
	
//	public void setCode(String code) {
//		if(this.code.isEmpty())
//			this.code = code;
//		else
//			this.code += " + " + code;
//	}
	
	public void setInnerObj(Row firstObj) {
		this.innerObj = firstObj;
	}
	
	public void setKey(Row key) {
		this.key = key;
	}
	
	public void setValue(Row value) {
		this.value = value;
	}

	public void setPath(Path path) {
		this.path = path;
		code = path.getCode();
	}

	public void display() {
		DecimalFormat format = new DecimalFormat(",###");
		//System.out.println("| User object | shallow heap | retained heap | length | inner object | inner size | threads | code() |");
		StringBuilder sb = new StringBuilder("| " + rawObj.getClassName() + " | " + format.format(rawObj.getShallowHeap())
				+ " | " + format.format(rawObj.getRetainedHeap()) + " | " + format.format(length) + " | ");
		if(innerObj != null)
			sb.append(innerObj.getClassName() + " | " + format.format(innerObj.getRetainedHeap()) + " | ");
		else if(key == null) {
			sb.append(" | | ");
		}
		else {
			sb.append("Key: " + key.getClassName() + " + Value: " + value.getClassName() + " | "
					+ format.format(key.getRetainedHeap()) + " + " + format.format(value.getRetainedHeap()) + " | ");
		}
		
		sb.append(thread + " | " + code + " |");
		
		System.out.println(sb.toString());
		
	}
}
	
	// dominator => map()-main, combine()-SpillThread, reduce()-main
//	private Path map; 
//	private Path combine;
//	private Path reduce;

//	dominator => thread1
//					at stackframe1
//						local object1
//							dominator
//						local object2
//							object
//								dominator
//					at stackframe2
//						local object1
//							dominator
//						local object2
//							object
//								dominator
//			  => thread2
//					at stackframe1
//						local object1
//							dominator
//						local object2
//							object
//								dominator
//					at stackframe2
//						local object1
//							dominator
//						local object2
//							object
//								dominator
			  		

