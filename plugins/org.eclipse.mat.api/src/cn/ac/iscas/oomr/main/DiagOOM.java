package cn.ac.iscas.oomr.main;

import java.util.List;
import java.util.Map;

import org.eclipse.mat.query.IResultTree;
import org.eclipse.mat.snapshot.ISnapshot;

import cn.ac.iscas.oomr.dominatortree.Row;
import cn.ac.iscas.oomr.dominatortree.TreeAnalyzer;
import cn.ac.iscas.oomr.map.MapPhaseAnalyzer;
import cn.ac.iscas.oomr.thread.ThreadAnalyzer;
import cn.ac.iscas.oomr.thread.path.Path;

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
	private List<FrameworkObj> fObjs;
	private List<UserObj> uObjs;
	
	// hooks of heap dump and dominator objects
	private ISnapshot snapshot;
	private IResultTree dominatorRootTree;
	
	// occurring phase of OOM
	private String phase;
	
	public DiagOOM(ISnapshot snapshot, IResultTree dominatorRootTree, String phase) {
		this.snapshot = snapshot;
		this.dominatorRootTree = dominatorRootTree;
		this.phase = phase;
	}
	
	// classify the memory-consuming objects into framework and user objects 
	public void classifyObjects() {
		List<Row> largeDominators = getLargeDominators(5.0f);
		List<Row> userObjs = filterFrameworkObjs(largeDominators);
		
		if(!userObjs.isEmpty())
			findReferencedThreads(userObjs);
	}
	
	
	
	/**
	 * @param mb Object which is larger than mb will be selected
	 * @return large dominators from the dominator tree, List\<Row\> is empty if none objects are larger than mb
	 */
	private List<Row> getLargeDominators(float mb) {
		TreeAnalyzer treeAnalyzer = new TreeAnalyzer(snapshot, dominatorRootTree);
		return treeAnalyzer.getLargeDominators(mb);
	}
	
	/**
	 * @param largeDominators classify large object into framework objects and user objects, leave framework objects, return user objects.
	 * @return user objects
	 */
	private List<Row> filterFrameworkObjs(List<Row> largeDominators) {
		if(phase.equals("map")) {
			MapPhaseAnalyzer mapAnaly = new MapPhaseAnalyzer(snapshot, largeDominators);
			return mapAnaly.filterFrameworkObjs();
		}
		else if(phase.equals("spill")) {
			
		}
		else if(phase.equals("merge")) {
			
		}
		
		else if(phase.equals("shuffle")) {
			
		}
		
		else if(phase.equals("sort")) {
			
		}
		
		else if(phase.equals("reduce")) {
			
		}
		
		return largeDominators;
	}
	
	private void findReferencedThreads(List<Row> userObjs) {
		ThreadAnalyzer threadAnaly = new ThreadAnalyzer(snapshot, phase);
		Map<Integer, Path> dominatorsToThreads = threadAnaly.findReferencedThreads(userObjs);
		threadAnaly.display(dominatorsToThreads);
	}
}
