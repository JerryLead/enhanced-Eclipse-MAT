package cn.ac.iscas.oomr.thread.path;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;

public class Path {
	private int dominatorId;
	
	private List<SinglePath> premap;
	private List<SinglePath> map;
	private List<SinglePath> combine;
	private List<SinglePath> prereduce;
	private List<SinglePath> reduce;
	private List<SinglePath> others;
	
	public Path(int dominatorId) {
		this.dominatorId = dominatorId;
		premap = new ArrayList<SinglePath>();
		map = new ArrayList<SinglePath>();
		combine = new ArrayList<SinglePath>();
		prereduce = new ArrayList<SinglePath>();
		reduce = new ArrayList<SinglePath>();
		others = new ArrayList<SinglePath>();
	}
	
	public void addPreMapPath(SinglePath sp) {
		premap.add(sp);	
	}
	
	public void addMapPath(SinglePath sp) {
		map.add(sp);
	}
	
	public void addCombinePath(SinglePath sp) {
		combine.add(sp);
	}
	
	public void addPreReducePath(SinglePath sp) {
		prereduce.add(sp);
	}
	
	public void addReducePath(SinglePath sp) {
		reduce.add(sp);
	}
	
	public void addOthersPath(SinglePath sp) {
		others.add(sp);
	}

	public String getCode() {
		StringBuilder sb = new StringBuilder();
		
		if(!premap.isEmpty())
			sb.append("premap");
		if(!map.isEmpty()) {
			if(sb.length() != 0)
				sb.append(" + ");
			sb.append("map");
		}
			
		if(!combine.isEmpty()) {
			if(sb.length() != 0)
				sb.append(" + ");
			sb.append("combine");
		}
			
		if(!prereduce.isEmpty()) {
			if(sb.length() != 0)
				sb.append(" + ");
			sb.append("prereduce");
		}
			
		if(!reduce.isEmpty()) {
			if(sb.length() != 0)
				sb.append(" + ");
			sb.append("reduce");
		}
			
		if(!others.isEmpty() && sb.length() == 0)
			sb.append("others");
		
		return sb.toString();
	}
	
	public void display(ISnapshot snapshot) {
		try {
		 
			int i = 0;
			if(!map.isEmpty()) {
				System.out.println("\t|---------------------------- in map() ----------------------------|");
				
				for(SinglePath sp : map) {
					if(i++ == 10)
						break;
					System.out.println("\t" + sp.getStackframe().getText());
					// System.out.println("\t  " + "--------------------------------------------------------");	
					for(Integer id : sp.getPath())
							System.out.println("\t\t" + snapshot.getObject(id).getDisplayName()+ " [" + id + "]");			
					System.out.println();
				}
			}
			
			i = 0;
			if(!combine.isEmpty()) {
				System.out.println("\t|----------------------------  in combine() ----------------------------|");
				for(SinglePath sp : combine) {
					if(i++ == 10)
						break;
					
					System.out.println("\t" + sp.getStackframe().getText());
					// System.out.println("\t  " + "--------------------------------------------------------");	
					
					for(Integer id : sp.getPath()) 
						System.out.println("\t\t" + snapshot.getObject(id).getDisplayName()+ " [" + id + "]");
					System.out.println();			
				}
			}
			
			i = 0;
			if(!reduce.isEmpty()) {
				System.out.println("\t|----------------------------  in reduce() ----------------------------|");
				for(SinglePath sp : reduce) {
					if(i++ == 10)
						break;
					
					System.out.println("\t" + sp.getStackframe().getText());
					// System.out.println("\t  " + "--------------------------------------------------------");
					
					for(Integer id : sp.getPath()) 
						System.out.println("\t\t" + snapshot.getObject(id).getDisplayName()+ " [" + id + "]");			
					System.out.println();
				}
			}
			
			i = 0;
			if(!premap.isEmpty()) {
				System.out.println("\t|---------------------------- in runmapper() before map() ----------------------------|");
				for(SinglePath sp : premap) {
					if(i++ == 10)
						break;
					
					System.out.println("\t" + sp.getStackframe().getText());
					// System.out.println("\t  " + "--------------------------------------------------------");	
					for(Integer id : sp.getPath())
							System.out.println("\t\t" + snapshot.getObject(id).getDisplayName()+ " [" + id + "]");			
					System.out.println();
				}
			}
			
			i = 0;
			if(!prereduce.isEmpty()) {
				System.out.println("\t|---------------------------- in runreducer() before reduce() ----------------------------|");
				for(SinglePath sp : prereduce) {
					if(i++ == 10)
						break;
					
					System.out.println("\t" + sp.getStackframe().getText());
					// System.out.println("\t  " + "--------------------------------------------------------");	
					for(Integer id : sp.getPath())
							System.out.println("\t\t" + snapshot.getObject(id).getDisplayName()+ " [" + id + "]");			
					System.out.println();
				}
			}
			/*
			if(!others.isEmpty()) {
				System.out.println("\t|---------------------------- in other func() ----------------------------|");
				for(SinglePath sp : others) {
					
					System.out.println("\t" + sp.getStackframe().getText());
					// System.out.println("\t  " + "--------------------------------------------------------");	
					
					for(Integer id : sp.getPath()) 
						System.out.println("\t\t" + snapshot.getObject(id).getDisplayName()+ " [" + id + "]");
					System.out.println();
				}
			}
			*/
		} catch (SnapshotException e) {
			e.printStackTrace();
		}
	}

	
}
