package cn.ac.iscas.oomr.thread.path;

import java.util.List;

public class StackFrame {
	private boolean inMethod;
	private String method;
	
	List<SinglePath> tuples;
}
