package cn.ac.iscas.oomr.thread.path;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;

public class Path {
	private int dominatorId;
	
	private List<SinglePath> map;
	private List<SinglePath> combine;
	private List<SinglePath> reduce;
	private List<SinglePath> others;
	
	public Path(int dominatorId) {
		this.dominatorId = dominatorId;
		map = new ArrayList<SinglePath>();
		combine = new ArrayList<SinglePath>();
		reduce = new ArrayList<SinglePath>();
		others = new ArrayList<SinglePath>();
	}
	
	public void addMapPath(SinglePath sp) {
		map.add(sp);
	}
	
	public void addCombinePath(SinglePath sp) {
		combine.add(sp);
	}
	
	public void addReducePath(SinglePath sp) {
		reduce.add(sp);
	}
	
	public void addOthersPath(SinglePath sp) {
		others.add(sp);
	}

	public void display(ISnapshot snapshot) {
		try {
		 
			if(!map.isEmpty()) {
				System.out.println("\t--------------------------map()--------------------------");
				for(SinglePath sp : map) {
					System.out.println("\t\t" + sp.getStackframe().getText());
					
					for(Integer id : sp.getPath())
						
							System.out.println("\t\t  " + snapshot.getObject(id).getDisplayName()+ "[" + id + "]");
						
					
					System.out.println("\t\t  " + "--------------------------------------------------------");	
				}
			}
			
			if(!combine.isEmpty()) {
				System.out.println("\t------------------------combine()-------------------------");
				for(SinglePath sp : combine) {
					System.out.println("\t\t" + sp.getStackframe().getText());
					
					for(Integer id : sp.getPath()) 
						System.out.println("\t\t  " + snapshot.getObject(id).getDisplayName()+ "[" + id + "]");
					
					System.out.println("\t\t  " + "--------------------------------------------------------");	
				}
			}
			
			
			if(!reduce.isEmpty()) {
				System.out.println("\t--------------------------reduce()-------------------------");
				for(SinglePath sp : reduce) {
					System.out.println("\t\t" + sp.getStackframe().getText());
					
					for(Integer id : sp.getPath()) 
						System.out.println("\t\t  " + snapshot.getObject(id).getDisplayName()+ "[" + id + "]");
					
					System.out.println("\t\t  " + "--------------------------------------------------------");	
				}
			}
			
			if(!others.isEmpty()) {
				System.out.println("\t--------------------------others---------------------------");
				for(SinglePath sp : others) {
					System.out.println("\t\t" + sp.getStackframe().getText());
					
					for(Integer id : sp.getPath()) 
						System.out.println("\t\t  " + snapshot.getObject(id).getDisplayName()+ "[" + id + "]");
					
					System.out.println("\t\t  " + "--------------------------------------------------------");	
				}
			}
			
		} catch (SnapshotException e) {
			e.printStackTrace();
		}
	}
}
