package supernate.core;
/**
 * 
 */


import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import supernate.core.tags.Column;
import supernate.core.tags.Key;
import supernate.core.tags.Table;


/**
 * @author Administrator
 *
 */
@Table
public abstract class TableInfo{
	private static final Logger logger = LogManager.getLogger("supernate");
	
	/**
	 * @throws Exception
	 */
	public TableInfo() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	protected abstract TableInfo initWithClass(Class z);
	
	protected abstract TableInfo initWithDatabase(Entity et,int id);
	
	protected abstract Object getKeyValue(Entity et);
	
	protected abstract List<String> getInitSql(boolean createhis);
	
	public List<Map<String,Object>> QueryByKey(Entity et,Object...objects){
		return et.QueryToMap("select * from "+this.getTableName()+" where "+this.keyColumn.getName()+" =?", objects);
	}
	public boolean Save(Entity et,Map<String,Object> columnvalues) {
		try {
			CaseInsensitiveMap<String,Object> map = new CaseInsensitiveMap();
			map.putAll(columnvalues);
			String cc = "";
			Object[] obs = new Object[columnvalues.size()];
			int i = 0;
			boolean flag = false;
			Object t = map.get(this.keyColumn.name);
			if(t==null) {
				flag = true;
			}
			String cc1 = "";
			for(ColumnInfo co:this.columns) {
				if(!co.isKey()) {
					obs[i]=map.get(co.name);
					i++;
				}else {
					if(flag) {
						Statement stkey = et.getConn().createStatement();
					    ResultSet rs=stkey.executeQuery("select "+this.getTableName()+"_SEQUENCE.nextval from dual");
					    if(rs.next()){
					    	obs[i]=rs.getObject(1);
				        }
					    rs.close();
					    stkey.close();
						i++;
					}else {
						obs[obs.length-1]=map.get(co.name);
					}
				}
			}
			if(flag) {
				et.Excutesql(this.getInsertSql(), obs);
			}else {
				et.Excutesql(this.getUpdateSql(null), obs);
			}
			return true;
		}catch(Exception e) {
			logger.log(Level.ERROR, e.getMessage());
			return false;
		}
	}
	public boolean Save(Entity et) {
		try {
			String cc = "";
			Object[] obs = new Object[this.columns.size()];
			int i = 0;
			boolean flag = false;
			String cc1 = "";
			if(this.getKeyColumn()==null)
				throw new Exception("请设定表主键！");
			else {
				Object temp = this.getKeyColumn().getValue();
				Object v = ModelRef.paseType("int", temp);
				if(v==null) {
					Statement stkey = et.getConn().createStatement();
				    ResultSet rs=stkey.executeQuery("select "+this.getTableName()+"_SEQUENCE.nextval from dual");
				    if(rs.next()){
				    	this.getKeyColumn().setValue(rs.getObject(1));
			        }
				    rs.close();
				    stkey.close();
				    flag=true;
				}
			}
			int aiindex = 0;
			for(ColumnInfo co:this.columns) {
				if(!co.isAi()) {
					obs[i]=co.getValue();
					i++;
				}else {
					if(flag) {
						obs[i]=co.getValue();
						i++;
					}
				}
			}
			
			if(flag) {
				et.Excutesql(this.getInsertSql(), obs);
			}else {
				obs[obs.length-1]=this.getKeyColumn().getValue();
				et.Excutesql(this.getUpdateSql(null), obs);
			}
			return true;
		}catch(Exception e) {
			logger.log(Level.ERROR, e.getMessage());
			return false;
		}
	}
	
	protected abstract ColumnInfo getColumnInfo(Field field);

	protected void initcolumns(Class z) {
		List<ColumnInfo> ret = new ArrayList<ColumnInfo>();
		Field[] fields = z.getDeclaredFields();
		Field[] fields2 = z.getFields();
		Class a = z.getSuperclass();
		String fi= "";
		for(int i = 0;i<fields.length;i++) {
			ColumnInfo temp = getColumnInfo(fields[i]);
			if(temp!=null) {
				ret.add(temp);
				if(temp.isKey())
					this.keyColumn = temp;
				fi+=","+temp.getFieldName()+",";
			}
		}
		for(int i = 0;i<fields2.length;i++) {
			ColumnInfo temp = getColumnInfo(fields2[i]);
			if(temp!=null) {
				ret.add(temp);
				fi+=","+temp.getFieldName()+",";
			}
		}
		fields = a.getDeclaredFields();
		fields2 = a.getFields();
		for(int i = 0;i<fields.length;i++) {
			String tempname =fields[i].getName();
			if(fi.contains(","+tempname+","))
				continue;
			ColumnInfo temp = getColumnInfo(fields[i]);
			if(temp!=null) {
				ret.add(temp);
				if(temp.isKey())
					this.keyColumn = temp;
			}
		}
		for(int i = 0;i<fields2.length;i++) {
			String tempname =fields2[i].getName();
			if(fi.contains(","+tempname+","))
				continue;
			ColumnInfo temp = getColumnInfo(fields2[i]);
			if(temp!=null)ret.add(temp);
		}
		this.columns = ret;
	}

	private ConnectionInfo connectionInfo = null;
	@Column()
	@Key(autogen=true)
	private Integer id;
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int Integer) {
		this.id = id;
	}

	@Column(length="50")
	protected String tableName = "";
	protected List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
	protected ColumnInfo keyColumn = null;
	/**
	 * @return the aiColumn
	 */
	public ColumnInfo getKeyColumn() {
		return keyColumn;
	}
	/**
	 * @param aiColumn the aiColumn to set
	 */
	public void setKeyColumn(ColumnInfo keyColumn) {
		this.keyColumn = keyColumn;
	}

	@Column(nullable=true)
	private String comment = "";
	/**
	 * @return the connectionInfo
	 */
	public ConnectionInfo getConnectionInfo() {
		return connectionInfo;
	}
	/**
	 * @param conn the connectionInfo to set
	 */
	public void setConnectionInfo(ConnectionInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
	}
	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName.toUpperCase();
	}
	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	/**
	 * @return the columns
	 */
	public List<ColumnInfo> getColumns() {
		return columns;
	}
	/**
	 * @param columns the columns to set
	 */
	public void setColumns(List<ColumnInfo> columns) {
		for(int i = 0;i<columns.size();i++) {
			if(columns.get(i).isKey())
			{
				this.keyColumn = columns.get(i);
				break;
			}
		}
		this.columns = columns;
	}
	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}
	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getInsertSql() {
		String ret = "";
		String co = "";
		List<ColumnInfo> columns = this.getColumns();
		for(int i=0;i<columns.size();i++) {
				if(!ret.equals("")) {
					ret+=",";
					co+=",";
				}
				ret+="\""+columns.get(i).getName().toUpperCase()+"\"";
				co+="?";
		}
		return "insert into "+this.getTableName()+" ("+ret+") values ("+co+")";
	}
	
	public String getUpdateSql(String where) throws Exception {
		String ret = "";
		List<ColumnInfo> columns = this.getColumns();
		for(int i=0;i<columns.size();i++) {
			if(!columns.get(i).isAi()) {
				if(!ret.equals("")) {
					ret+=",";
				}
				ret+="\""+columns.get(i).getName().toUpperCase()+"\"=?";
			}
		}
		if(this.getKeyColumn()==null&&(where==null||where.equals("")))
			throw new Exception("未定义主键，或定义过滤条件！");
		return "update "+this.getTableName().toUpperCase()+" set "+ret+" where "+(where==null||where.equals("")?(getKeyColumn().name.toUpperCase()+"=?"):where);
	}
	
	public String getDeleteSql(String where) throws Exception {
		String ret = "";
		if(this.getKeyColumn()==null&&(where==null||where.equals("")))
			throw new Exception("未定义主键，或定义过滤条件！");
		return "delete "+this.getTableName()+" where "+(where==null||where.equals("")?(getKeyColumn().name+"=?"):where);
	}
}

