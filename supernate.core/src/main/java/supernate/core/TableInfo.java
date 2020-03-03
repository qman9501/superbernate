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
import java.util.function.Predicate;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import supernate.core.tags.AutoGen;
import supernate.core.tags.Column;
import supernate.core.tags.Key;
import supernate.core.tags.Table;


/**
 * @author Administrator
 *
 */
@Table
public abstract class TableInfo{
	protected static final Logger logger = LogManager.getLogger("supernate");
	
	/**
	 * @throws Exception
	 */
	public TableInfo() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	protected TableInfo initWithClass(Class z) {
		TableInfo ret=null;
		try {
			ret = this.getClass().newInstance();
			Class<?> clazz = z ; 
			for(; clazz != Object.class ; clazz = clazz.getSuperclass()) {  
	            try {  
	        		if(clazz.isAnnotationPresent(Table.class)) {
	        			Table tableinfo = (Table) clazz.getAnnotation(Table.class);
	        			if(!tableinfo.name().equals("")) {
	        				ret.setTableName(tableinfo.name());
	        			}else {
	        				ret.setTableName(clazz.getSimpleName());
	        			}
	        			ret.setComment(tableinfo.comment());
	        		}
	            } catch (Exception e) {  
	                //这里甚么都不能抛出去。  
	                //如果这里的异常打印或者往外抛，则就不会进入                  
	            }   
	        }  
			if(ret.getTableName().equals("")) {
				logger.error("there's no table tag,please add it to class tag");
			}
			ret.initcolumns(z);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e);
			e.printStackTrace();
		}
		return ret;
	}
	
	protected TableInfo initWithObject(Object obj) {
		TableInfo ret = null;
		Class<?> clazz = obj.getClass() ; 
		ret = initWithClass(clazz);
		for(int i = 0;i<ret.getColumns().size();i++) {
			ColumnInfo co = (ColumnInfo)ret.getColumns().get(i);
			co.setValueWithObj(obj);
		}
		return ret;
	}
	
	protected TableInfo initWithDatabase(int id) {
		TableInfo ret = null;
		try {
			ret = this.getClass().newInstance();
			Entity<TableInfo> ti = new Entity<TableInfo>(entity.getConn(),entity.getDialectPackage());
			ret = ti.getByClassKey(TableInfo.class,id);
			Entity<ColumnInfo> cos = new Entity<ColumnInfo>(entity.getConn(),entity.getDialectPackage());
			List<ColumnInfo> colist = cos.getObjects(ColumnInfo.class,"tablename=?", ret.getTableName());
			for(int i = 0;i<colist.size();i++) {
				ColumnInfo co = colist.get(i);
				columns.add(co);
				if(co.isAi()) {
					autos.add(co);
				}
				if(co.isKey()) {
					keyColumn = co;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return ret;
	}
	
	protected abstract Object getKeyValue(Entity et);
	
	protected abstract List<String> getInitSql(boolean createhis);
	
	public ColumnInfo getColumnInfo(String name) {
		int idx = columns.lastIndexOf(name);
		if(idx>=0)
			return columns.get(columns.lastIndexOf(name));
		else
			return null;
	}
	
	public void Delete() {
		this.Delete(null);
	}
	
	public void Delete(String where,Object... params) {
			try {
				if(where==null) {
					String sql = this.getDeleteSql(null);
					entity.Excutesql(sql, new Object[] {this.keyColumn.getValue()});
				}else {
					String sql = this.getDeleteSql(where);
					entity.Excutesql(sql, params);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void Update() {
		Update(null);
	}
	
	public void Update(String where,Object... params) {
		try {
			String sql = "";
			Object[] pas = null;
			if(where==null) {
				sql =  this.getUpdateSql(null);
				pas = new Object[this.getColumns().size()];
				int i = 0;
				for(ColumnInfo co:this.columns) {
					pas[i]=co.getValue();
					i++;
				}
			}else {
				sql = this.getUpdateSql(where);
				pas = new Object[this.getColumns().size()+params.length];
				int i = 0;
				for(ColumnInfo co:this.columns) {
					pas[i]=co.getValue();
					i++;
				}
				for(Object ob:params) {
					pas[i]=ob;
					i++;
				}
			}
			entity.Excutesql(sql, pas);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e);
		}
	}

	public void Insert() {
		String sql = this.getInsertSql();
		Object[] params = new Object[this.columns.size()];
		int i = 0;
		for(ColumnInfo co:this.columns) {
			Object temp = co.getValue();
			if(co.getValue()==null) {
				if(co.isAi()) {
					temp = co.getAutoValue(entity);
					co.setValue(temp);
				}
			}
			params[i] = temp;
			i++;
		}
		entity.Excutesql(sql, params);
	}
	
	protected abstract void initColumnInfo(Field field);

	protected void initcolumns(Class z) {
		Field[] fields = z.getDeclaredFields();
		Field[] fields2 = z.getFields();
		Class a = z.getSuperclass();
		String fi= "";
		for(int i = 0;i<fields.length;i++) {
			initColumnInfo(fields[i]);
		}
		for(int i = 0;i<fields2.length;i++) {
			initColumnInfo(fields[i]);
		}
		fields = a.getDeclaredFields();
		fields2 = a.getFields();
		for(int i = 0;i<fields.length;i++) {
			initColumnInfo(fields[i]);
		}
		for(int i = 0;i<fields2.length;i++) {
			initColumnInfo(fields[i]);
		}
	}

	@Column()
	@Key()
	@AutoGen()
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
	protected List<ColumnInfo> autos = new ArrayList<ColumnInfo>();
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
	
	private Entity entity;
	
	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
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
	
	protected abstract List<String> getHisFunction();
	
	protected abstract String getPageSql();
	
	protected abstract String getClassDriver();
	
}

