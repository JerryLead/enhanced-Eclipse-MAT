package cn.ac.iscas.oomr.main;

import java.util.List;

import cn.ac.iscas.oomr.thread.path.Path;

public class UserObj {

	private int objectId;
	private String className;
	private long shallowHeap;
	private long retainedHeap;
	
	// dominator => map()-main, combine()-SpillThread, reduce()-main
	private Path map; 
	private Path combine;
	private Path reduce;

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
			  		
}
