package supernate.core;
/**
 * 
 */


import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 *
 */
public class PageRecords<T> {
	private List<T> objs=new ArrayList<T>();
	private int maxpage;
	private int currentpage;
	private long total;
	/**
	 * @return the total
	 */
	public long getTotal() {
		return total;
	}
	/**
	 * @param total the total to set
	 */
	public void setTotal(long total) {
		this.total = total;
	}
	/**
	 * @return the objs
	 */
	public List<T> getObjs() {
		return objs;
	}
	/**
	 * @param objs the objs to set
	 */
	public void setObjs(List<T> objs) {
		this.objs = objs;
	}
	/**
	 * @return the maxpage
	 */
	public int getMaxpage() {
		return maxpage;
	}
	/**
	 * @param maxpage the maxpage to set
	 */
	public void setMaxpage(int maxpage) {
		this.maxpage = maxpage;
	}
	/**
	 * @return the currentpage
	 */
	public int getCurrentpage() {
		return currentpage;
	}
	/**
	 * @param currentpage the currentpage to set
	 */
	public void setCurrentpage(int currentpage) {
		this.currentpage = currentpage;
	}
}
