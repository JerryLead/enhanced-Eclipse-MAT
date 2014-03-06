package cn.ac.iscas.oomr.main;

import java.util.List;
import java.util.Map;

import org.eclipse.mat.query.IResultTree;
import org.eclipse.mat.snapshot.ISnapshot;

import cn.ac.iscas.oomr.classifier.MapPhaseAnalyzer;
import cn.ac.iscas.oomr.classifier.MergePhaseAnalyzer;
import cn.ac.iscas.oomr.classifier.ReducePhaseAnalyzer;
import cn.ac.iscas.oomr.classifier.ShuffleSortPhaseAnalyzer;
import cn.ac.iscas.oomr.dominatortree.Row;
import cn.ac.iscas.oomr.dominatortree.TreeAnalyzer;
import cn.ac.iscas.oomr.thread.ThreadAnalyzer;
import cn.ac.iscas.oomr.thread.path.Path;
import cn.ac.iscas.oomr.userobject.UserObjectExtender;

/**
 * Given the heap dump of a mapper/reducer, DiagOOM will figure out the valuable information 
 * of memory-consuming objects. The information includes:
 * | Dominator objects | Attributes	        | Threads            | code()                   |
 * |:------------------|:-------------------|:-------------------|:-------------------------|
 * | framework objects | number, size, type | referenced threads | map()/combine()/reduce() |
 * | user objects      | number, size, type | referenced threads | map()/combine()/reduce() |
 * 
 * @author Lijie Xu
 *
 */
public class DiagOOM {
	
	// results
	// private List<Row> frameworkObjs;
	private List<Row> userObjs;
	
	// hooks of heap dump and dominator objects
	private ISnapshot snapshot;
	private IResultTree dominatorRootTree;
	
	// occurring phase of OOM
	private String phase;
	
	// object that are smaller than sizeLimit will be neglected
	private float sizeLimitMB;
	
	public DiagOOM(ISnapshot snapshot, IResultTree dominatorRootTree, String phase, float sizeLimitMB) {
		this.snapshot = snapshot;
		this.dominatorRootTree = dominatorRootTree;
		this.phase = phase;
		this.sizeLimitMB = sizeLimitMB;
	}
	
	// classify the memory-consuming objects into framework and user objects 
	public void classifyObjects() {
		// get raw memory-consuming objects
		List<Row> largeDominators = getLargeDominators(sizeLimitMB);
		
		userObjs = filterFrameworkObjs(largeDominators);
		displayLargeDominators(userObjs, "User objects");
		
		refineUserObjects();
	}
	
	// find the referenced threads/code() of each user object
	public void findReferencedThreads() {
		if(!userObjs.isEmpty())
			findReferencedThreads(userObjs);
	}
	
	// explore the details of individual object (e.g., explore the elements in ArrayList)
	public void refineUserObjects() {
		UserObjectExtender uExtender = new UserObjectExtender(snapshot, userObjs);
		uExtender.extendUserObjs();
	}
	/**
	 * @param mb Object which is larger than mb will be selected
	 * @return large dominators from the dominator tree, List\<Row\> is empty if none objects are larger than mb
	 */
	public List<Row> getLargeDominators(float mb) {
		TreeAnalyzer treeAnalyzer = new TreeAnalyzer(snapshot, dominatorRootTree);
		return treeAnalyzer.getLargeDominators(mb);
	}
	
	/**
	 * @param largeDominators classify large object into framework objects and user objects, leave framework objects, return user objects.
	 * @return user objects
	 */
	private List<Row> filterFrameworkObjs(List<Row> largeDominators) {
		if(phase.equals("map") || phase.equals("spill")) {
			MapPhaseAnalyzer mapAnaly = new MapPhaseAnalyzer(snapshot, largeDominators);
			return mapAnaly.filterFrameworkObjs();
		}
		
		else if(phase.equals("merge")) {
			MergePhaseAnalyzer mergeAnaly = new MergePhaseAnalyzer(snapshot, largeDominators);
			return mergeAnaly.filterFrameworkObjs();
		}
		
		else if(phase.equals("shuffle") || phase.equals("sort")) {
			ShuffleSortPhaseAnalyzer ssAnaly = new ShuffleSortPhaseAnalyzer(snapshot, largeDominators);
			return ssAnaly.filterFrameworkObjs();
		}
		
		else if(phase.equals("reduce")) {
			ReducePhaseAnalyzer redAnaly = new ReducePhaseAnalyzer(snapshot, largeDominators);
			return redAnaly.filterFrameworkObjs();
		}
		
		return largeDominators;
	}
	
	public void findReferencedThreads(List<Row> userObjs) {
		ThreadAnalyzer threadAnaly = new ThreadAnalyzer(snapshot, phase);
		Map<Integer, Path> dominatorsToThreads = threadAnaly.findReferencedThreads(userObjs);
		threadAnaly.display(dominatorsToThreads);
	}
	
	public void displayLargeDominators(List<Row> rows, String name) {
		if(rows == null || rows.isEmpty())
			return;
		
		System.out.println("|------------------------- " + name + " -------------------------|");
		System.out.println("| Class name \t| shallowHeap \t| retainedHeap |");
		System.out.println("|:-------------- | --------------: | --------------:|");
		
		for (Row row : rows)
			System.out.println(row);
		System.out.println();
		System.out.println();
	}
	
}
