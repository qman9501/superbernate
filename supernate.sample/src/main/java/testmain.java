import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;

import supernate.core.Entity;
import supernate.core.tags.AutoGen;
import supernate.core.tags.Column;
import supernate.core.tags.ConnectionInfo;
import supernate.core.tags.Key;
import supernate.core.tags.Table;

/**
 * 
 */

/**
 * @author Administrator
 *
 */
public class testmain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			A et = new A();
			Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost/vedio", "root","bq134524");
			et.setConn(conn);
			et.setDialectPackage("supernate.mariadbDialect");
			et.InitTable(true);
			ModelContent t = new ModelContent();
			t.setConn(conn);
			t.setDialectPackage("supernate.mariadbDialect");
			t.InitTable(false);
			//et.setConn(conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static class A extends IndexPageModel{

		/**
		 * @throws Exception
		 */
		public A() throws Exception {
			super();
			// TODO Auto-generated constructor stub
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}
	
	@Table
	public static class IndexPageModel extends Entity<IndexPageModel> implements Serializable{

		/**
		 * @throws Exception
		 */
		public IndexPageModel() throws Exception {
			super();
			// TODO Auto-generated constructor stub
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		@Column()
		@Key()
		@AutoGen()
		private Integer id;
		@Column
		private String name;
		@Column(type=7)
		private Integer hadTitle;
		@Column
		private Integer x;
		@Column
		private Integer y;
		@Column
		private Integer width;
		@Column
		private Integer height;
		@Column(type=7)
		private Integer modelType;
		@Column(type=4)
		private Integer modelContent;
		@Column
		private String backgroundColor;
		@Column
		private String backgroundImg;
	}
	@Table
	public static class ModelContent extends Entity<ModelContent> implements Serializable{

		/**
		 * @throws Exception
		 */
		public ModelContent() throws Exception {
			super();
			// TODO Auto-generated constructor stub
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Column()
		@Key()
		@AutoGen()
		private Integer id;
		@Column
		private String name;
		@Column
		private String backgroundColor;
		@Column
		private String backgroundImg;
		private Integer idx;
		@Column
		private String width;
		@Column
		private String height;
		@Column(type=4)
		private String url;
		@Column(type=7)
		private Integer isViewName;
		@Column
		private Integer modelId;
		
	}

}
