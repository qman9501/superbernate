package supernate.core;
/**
 * 
 */


import java.io.Console;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Administrator
 *
 */
@SuppressWarnings("unchecked")
public class Entity<T> {
	private static final Logger logger = LogManager.getLogger("supernate");
	@Autowired
	private transient SJpa jpaconf;
	private transient TableInfo baseTableInfo = null;
	
	private transient boolean initclass = false;
	private transient TableInfo tableInfo = null;
	private transient Connection conn = null;
	
	/**
	 * @return the conn
	 */
	public Connection getConn() {
		return conn;
	}
	/**
	 * @param conn the conn to set
	 */
	public void setConn(Connection conn) {
		this.conn = conn;
	}
	
	public Entity(){
		init();
		Class z = this.getClass();
		if(z==Entity.class) {
			return;
		}
		this.tableInfo = this.baseTableInfo.initWithClass(z);
		if(tableInfo.getConnectionInfo()!=null) {
			try {
				this.conn = DriverManager.getConnection(tableInfo.getConnectionInfo().getUrl(), tableInfo.getConnectionInfo().getUsername(), tableInfo.getConnectionInfo().getPassword());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.log(Level.ERROR,e.getMessage());
			}
		}
	}
	public Entity(Connection conn)  {
		init();
		Class z = this.getClass();
		this.conn = conn;
		if(z==Entity.class) {
			return;
		}
		this.tableInfo = this.baseTableInfo.initWithClass(z);
	}
	public Entity(String driver,String url,String username,String password) throws Exception {

		init();
		Class z = this.getClass();
		this.conn = DriverManager.getConnection(url, username, password);
		if(z==Entity.class) {
			return;
		}
		this.tableInfo = this.baseTableInfo.initWithClass(z);
	}
	
	private void init() {
		try {
			Class cla = Class.forName(this.jpaconf.dialect);
			this.baseTableInfo = (TableInfo)cla.newInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.log(Level.ERROR, e.getMessage());
		}
		
	}
	/*
	 * @SuppressWarnings("unchecked") public DefaultTargetType() { }
	 */
	
	public TableInfo getTableInfo() {
		return this.tableInfo;
	}
	public TableInfo getTableInfo(Class z){
		return baseTableInfo.initWithClass(z);
	}
	
	public void InitTable(boolean createhis) {
		Class z = this.getClass();
		List<String> sqls = this.tableInfo.getInitSql(createhis);
		try {
		this.conn.setAutoCommit(false);
		String sqltemp = "";
			for(int i = 0;i<sqls.size();i++) {
				sqltemp = sqls.get(i);
				this.Excutesql(sqltemp);
			}
			this.conn.commit();
		}catch(Exception ex) {
			try {
				this.conn.rollback();
				logger.log(Level.ERROR, ex.getMessage()+"\r\n"+ex.getStackTrace());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.log(Level.ERROR, e.getMessage()+"\r\n"+ex.getStackTrace());
			}
		}
	}
	public void InitTable(TableInfo tableInfo,boolean createhis) {
		List<String> sqls = tableInfo.getInitSql(createhis);
		try {
		this.conn.setAutoCommit(false);
		String sqltemp = "";
			for(int i = 0;i<sqls.size();i++) {
				sqltemp = sqls.get(i);
				this.Excutesql(sqltemp);
			}
			this.conn.commit();
		}catch(Exception ex) {
			try {
				this.conn.rollback();
				logger.log(Level.ERROR, ex.getMessage()+"\r\n"+ex.getStackTrace());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.log(Level.ERROR, e.getMessage()+"\r\n"+ex.getStackTrace());
			}
		}
	}

	public void InitTable(Class z,boolean createhis) {
		TableInfo tableInfo = baseTableInfo.initWithClass(z);
		List<String> sqls = tableInfo.getInitSql(createhis);
		try {
		this.conn.setAutoCommit(false);
		String sqltemp = "";
			for(int i = 0;i<sqls.size();i++) {
				sqltemp = sqls.get(i);
				this.Excutesql(sqltemp);
			}
			this.conn.commit();
		}catch(Exception ex) {
			try {
				this.conn.rollback();
				logger.log(Level.ERROR, ex.getMessage()+"\r\n"+ex.getStackTrace());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.log(Level.ERROR, e.getMessage()+"\r\n"+ex.getStackTrace());
			}
		}
	}
	
	public void insert() throws Exception {
		String sql = this.tableInfo.getInsertSql();
		PreparedStatement st = null;
		Class<?> z = this.getClass();
		if(this.tableInfo.getKeyColumn()!=null) {
				Object tempkey = ModelRef.getFieldValue(this,this.tableInfo.getKeyColumn().fieldName);
				if(tempkey==null) {
				    Statement stkey = conn.createStatement();
				    ResultSet rs=stkey.executeQuery("select "+this.tableInfo.getTableName()+"_SEQUENCE.nextval from dual");
				    if(rs.next()){
				    	tempkey = rs.getObject(1);
			        }
				    Object key = null;
				    if(tableInfo.getKeyColumn().getType()==0) {
				    	key = Integer.parseInt(tempkey.toString());
				    }else {
				    	key = tempkey;
				    }
				    ModelRef.setFieldValue(this,this.tableInfo.getKeyColumn().fieldName,key);
				    //field.set(this, key);
				    stkey.close();
				}
		}
		st = conn.prepareStatement(sql);
		List<ColumnInfo> columns = this.tableInfo.getColumns();
		int count = 0;
		for(int i = 0;i<columns.size();i++) {
				st.setObject(count+1, ModelRef.getFieldValue(this,columns.get(i).fieldName));
				count++;
		}
		st.executeUpdate();
		st.close();
	}	
	
	public void insert(Object t) throws Exception {
		Class z = t.getClass();
		TableInfo tableinfo= baseTableInfo.initWithClass(z);
		String sql = tableinfo.getInsertSql();
		PreparedStatement st = null;
		if(tableinfo.getKeyColumn()!=null){
			Object tempkey = ModelRef.getFieldValue(t,tableinfo.getKeyColumn().fieldName);
			if(tempkey==null) {
			    Statement stkey = conn.createStatement();
			    ResultSet rs=stkey.executeQuery("select "+tableinfo.getTableName()+"_SEQUENCE.nextval from dual");
			    if(rs.next()){
			    	tempkey = rs.getObject(1);
		        }
			    Object key = null;
			    if(tableinfo.getKeyColumn().getType()==0) {
			    	key = Integer.parseInt(tempkey.toString());
			    }else {
			    	key = tempkey;
			    }
			    ModelRef.setFieldValue(t,tableinfo.getKeyColumn().fieldName,key);
			    //field.set(t, key);
			    stkey.close();
			}
		}
		st = conn.prepareStatement(sql);
		List<ColumnInfo> columns = tableinfo.getColumns();
		int count = 0;
		for(int i = 0;i<columns.size();i++) {
			Field field = ModelRef.getDeclaredField(t, columns.get(i).getFieldName());
				//Field field = z.getDeclaredField(columns.get(i).getFieldName());
				//field.setAccessible(true);
			Object tt = ModelRef.getFieldValue(t,columns.get(i).fieldName);
			if(t.getClass().getSimpleName().toLowerCase().contains("date")&&tt!=null) {
				Date d = (Date)tt;
				Timestamp ts = new Timestamp(d.getTime());
				st.setObject(count+1, ts);
			}else {
				st.setObject(count+1, columns.get(i).getValueWithObj(5));
			}
				count++;
		}
		st.executeUpdate();
		st.close();
	}	
	
	public void update() throws Exception {
		String sql = this.tableInfo.getUpdateSql(null);
		PreparedStatement st = null;
		st = conn.prepareStatement(sql);
		List<ColumnInfo> columns = this.tableInfo.getColumns();
		int count = 1;
		Class<?> z = this.getClass();
		Object keyvalue = null;
		for(int i = 0;i<columns.size();i++) {
			//Field field = ModelRef.getDeclaredField(this, columns.get(i).getFieldName());
			//field.setAccessible(true);
			if(!columns.get(i).isAi()) {
				st.setObject(count, columns.get(i).getValueWithObj(this));
				count++;
			}else {
				keyvalue =columns.get(i).getValueWithObj(this);
			}
		}
		if(tableInfo.getKeyColumn()!=null) {
			st.setObject(count, keyvalue);
		}
		st.executeUpdate();
		st.close();
	}
	
	public void update(Object t) throws Exception {
		Class z = t.getClass();
		TableInfo tableinfo= baseTableInfo.initWithClass(z);
		String sql = tableinfo.getUpdateSql(null);
		PreparedStatement st = null;
		st = conn.prepareStatement(sql);
		List<ColumnInfo> columns = tableinfo.getColumns();
		int count = 1;
		Object keyvalue = null;
		for(int i = 0;i<columns.size();i++) {
			//Field field = ModelRef.getDeclaredField(t, columns.get(i).getFieldName());
			//field.setAccessible(true);
			if(!columns.get(i).isKey()) {
				st.setObject(count, columns.get(i).getValueWithObj(t));
				count++;
			}else {
				keyvalue =columns.get(i).getValueWithObj(this);
			}
		}
		if(tableinfo.getKeyColumn()!=null) {
			st.setObject(count, keyvalue);
		}
		st.executeUpdate();
		st.close();
	}

	public void update(String where,Object...objects) throws Exception {
		String sql = this.tableInfo.getUpdateSql(where);
		PreparedStatement st = null;
		st = conn.prepareStatement(sql);
		List<ColumnInfo> columns = this.tableInfo.getColumns();
		int count = 1;
		Class<?> z = this.getClass();
		for(int i = 0;i<columns.size();i++) {
			//Field field = ModelRef.getDeclaredField(this, columns.get(i).getFieldName());
			//field.setAccessible(true);
			if(!columns.get(i).isAi()) {
				st.setObject(count,columns.get(i).getValueWithObj(this));
				count++;
			}
		}
        if(objects!=null&&objects.length>0) {
        	for(int i = 0;i<objects.length;i++) {
				st.setObject(count, objects[i]);
				count++;
        	}
        }
		st.executeUpdate();
		st.close();
	}
	
//	public void updateByTableInfo(TableInfo tableinfo) throws Exception {
//		String sql = tableinfo.getUpdateSql(null);
//		PreparedStatement st = null;
//		st = conn.prepareStatement(sql);
//		List<ColumnInfo> columns = tableinfo.getColumns();
//		Object[] o = new Object[columns.size()];
//		int i = 0;
//		for(ColumnInfo co:columns) {
//			if(!co.isAi()) {
//				Object ot = co.getValue();
//				o[i]=ot;
//				i++;
//			}
//		}
//		o[o.length-1]=tableinfo.getAiColumn().getValue();
//		st.executeUpdate();
//		st.close();
//	}

	public void delete(String where,Object...objects) throws Exception {
		String sql = this.tableInfo.getDeleteSql(where);
		try {
			this.Excutesql(sql, objects);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.log(Level.ERROR, e.getMessage());
		}
	}
	public void delete(Object t) {
		String sql;
		try {
			Class z = t.getClass();
			TableInfo tableinfo= baseTableInfo.initWithClass(z);
			sql = tableinfo.getDeleteSql(null);
			this.Excutesql(sql, tableinfo.getKeyColumn().getValueWithObj(t));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.log(Level.ERROR, e.getMessage());
		}
	}
	
	public T getByKey(Object id){
		T ret = null;
		TableInfo ti = this.tableInfo;
		List<T> ls = this.Query("select * from "+ti.getTableName()+" where "+ti.getKeyColumn().getName()+"=?", id);
		if(ls.size()>0)
			ret = ls.get(0);
		return ret;
	}
	
	public T getByClassKey(Class z,Object id){
        T ret = null;
        this.tableInfo = baseTableInfo.initWithClass(z);
		List<Object> ls = this.Query(z,"select * from "+this.tableInfo.getTableName()+" where "+this.tableInfo.getKeyColumn().getName()+"=?", id);
		if(ls.size()>0)
			ret = (T)ls.get(0);
		return ret;
	}
	
	public Object getByKey(Class z,Object id) {
		this.tableInfo = baseTableInfo.initWithClass(z);
        Object ret;
		try {
			ret = z.newInstance();
			List<Object> ls = this.Query(z,"select * from "+tableInfo.getTableName()+" where "+tableInfo.getKeyColumn().getName()+"=?", id);
			if(ls.size()>0)
				ret = ls.get(0);
			return ret;
		} catch (Exception e) {
			logger.log(Level.ERROR, e.getMessage());
			return null;
		}
	}

	public PageRecords<T> getObjectsByPage(Class z,String where,int pageNum,int pageSize,Object...objects) {
		this.tableInfo = baseTableInfo.initWithClass(z);
		String sql = "";
		int startNumber = (pageNum-1)*pageSize+1;
		int endNumber = pageNum*pageSize;
		PreparedStatement st = null;
		try {

			if(where!=null) {
				if(startNumber>0) {
					sql = "SELECT * FROM ( SELECT A.*, ROWNUM RN  FROM (SELECT * FROM "+this.tableInfo.getTableName()+" where "+where+") A  WHERE ROWNUM <= ?)WHERE RN >= ?";
					st = conn.prepareStatement(sql);
					for(int i = 0;i<objects.length;i++) {
						st.setObject(i+1,objects[i]);
					}
					st.setObject(objects.length+1,endNumber);
					st.setObject(objects.length+2,startNumber);
				}else {
					sql = "SELECT * FROM "+this.tableInfo.getTableName()+" where "+where;
					st = conn.prepareStatement(sql);
				}
			}else {
				if(startNumber>0) {
					sql = "SELECT * FROM ( SELECT A.*, ROWNUM RN  FROM (SELECT * FROM "+this.tableInfo.getTableName()+") A  WHERE ROWNUM <= ?)WHERE RN >= ?";
					st = conn.prepareStatement(sql);
					st.setObject(1,endNumber);
					st.setObject(2,startNumber);
				}else {
					sql = "SELECT * FROM "+this.tableInfo.getTableName();
					st = conn.prepareStatement(sql);
				}
			}
			PageRecords<T> ret = new PageRecords<T>();
			ResultSet result = st.executeQuery();
			long size = GetRecordCound(where,objects);
			int maxpage=(int)size/pageSize+(size%pageSize>0?1:0);
			ret.setMaxpage(maxpage);
			ret.setCurrentpage(pageNum);
			ret.setTotal(size);
			while(result.next()) {
				T temp = (T)z.newInstance();
				List<ColumnInfo> columns = tableInfo.getColumns();
				for(int i = 0;i<columns.size();i++) {
					ColumnInfo co = columns.get(i);
					co.setObjectValue(temp, result.getObject(co.getName()));
				}
				ret.getObjs().add(temp);
			}
			return ret;
		}catch(Exception e) {
			logger.log(Level.ERROR, e.getMessage());
			return null;
		}
	}
	public PageRecords<T> getObjectsByPage(String where,int pageNum,int pageSize,Object...objects) throws Exception {
		String sql = "";
		int startNumber = (pageNum-1)*pageSize+1;
		int endNumber = pageNum*pageSize;
		PreparedStatement st = null;
		if(where!=null) {
			if(startNumber>0) {
				sql = "SELECT * FROM ( SELECT A.*, ROWNUM RN  FROM (SELECT * FROM "+this.tableInfo.getTableName()+" where "+where+") A  WHERE ROWNUM <= ?)WHERE RN >= ?";
				st = conn.prepareStatement(sql);
				for(int i = 0;i<objects.length;i++) {
					st.setObject(i+1,objects[i]);
				}
				st.setObject(objects.length+1,endNumber);
				st.setObject(objects.length+2,startNumber);
			}else {
				sql = "SELECT * FROM "+this.tableInfo.getTableName()+" where "+where;
				st = conn.prepareStatement(sql);
			}
		}else {
			if(startNumber>0) {
				sql = "SELECT * FROM ( SELECT A.*, ROWNUM RN  FROM (SELECT * FROM "+this.tableInfo.getTableName()+") A  WHERE ROWNUM <= ?)WHERE RN >= ?";
				st = conn.prepareStatement(sql);
				st.setObject(1,endNumber);
				st.setObject(2,startNumber);
			}else {
				sql = "SELECT * FROM "+this.tableInfo.getTableName();
				st = conn.prepareStatement(sql);
			}
		}
		PageRecords<T> ret = new PageRecords<T>();
		ResultSet result = st.executeQuery();
		Class<?> z = this.getClass();
		long size = GetRecordCound(where,objects);
		int maxpage=(int)size/pageSize+(size%pageSize>0?1:0);
		ret.setMaxpage(maxpage);
		ret.setCurrentpage(pageNum);
		ret.setTotal(size);
		while(result.next()) {
			T temp = (T)z.newInstance();
			List<ColumnInfo> columns = tableInfo.getColumns();
			for(int i = 0;i<columns.size();i++) {
				ColumnInfo co = columns.get(i);
				co.setObjectValue(temp, result.getObject(co.getName()));
			}
			ret.getObjs().add(temp);
		}
		return ret;
	}
	
	public List<T> getObjects(Class z,String where,Object...objects) throws Exception{
		String sql = "";
		this.tableInfo = baseTableInfo.initWithClass(z);
		
		PreparedStatement st = null;
		if(where!=null) {
			sql = "SELECT * FROM "+this.tableInfo.getTableName()+" where "+where;
			st = conn.prepareStatement(sql);
			for(int i = 0;i<objects.length;i++) {
				st.setObject(i+1,objects[i]);
			}
		}else {
			sql = "SELECT * FROM "+this.tableInfo.getTableName();
			st = conn.prepareStatement(sql);
		}
		List<T> ret = new ArrayList<T>();
		ResultSet result = st.executeQuery();
		while(result.next()) {
			T temp = (T)z.newInstance();
			List<ColumnInfo> columns = tableInfo.getColumns();
			for(int i = 0;i<columns.size();i++) {
				ColumnInfo co = columns.get(i);
				co.setObjectValue(temp, result.getObject(co.getName()));
			}
			ret.add(temp);
		}
		return ret;
	}

	public List<T> getObjects(String where,Object...objects) throws Exception {
		String sql = "";
		PreparedStatement st = null;
		if(where!=null) {
			sql = "SELECT * FROM "+this.tableInfo.getTableName()+" where "+where;
			st = conn.prepareStatement(sql);
			for(int i = 0;i<objects.length;i++) {
				st.setObject(i+1,objects[i]);
			}
		}else {
			sql = "SELECT * FROM "+this.tableInfo.getTableName();
			st = conn.prepareStatement(sql);
		}
		List<T> ret = new ArrayList<T>();
		ResultSet result = st.executeQuery();
		Class<?> z = this.getClass();
		while(result.next()) {
			T temp = (T)z.newInstance();
			List<ColumnInfo> columns = tableInfo.getColumns();
			for(int i = 0;i<columns.size();i++) {
				ColumnInfo co = columns.get(i);
				co.setObjectValue(temp, result.getObject(co.getName()));
			}
			ret.add(temp);
		}
		return ret;
	}
	
	public void delete() {
		this.delete(this);
	}
	
	public void Excutesql(String sql,Object...objects) {
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			if(objects!=null&&objects.length>0) {
				for(int i = 0;i<objects.length;i++) {
					if(objects[i]!=null&&objects[i].getClass().getSimpleName().toLowerCase().contains("date")) {
						Date t = (Date)objects[i];
						Timestamp ts = new Timestamp(t.getTime());
						st.setObject(i+1, ts);
					}else {
						st.setObject(i+1, objects[i]);
					}
				}
			}
			st.execute();
			st.close();
		}catch(Exception ex) {
			System.out.println("----------------------------");
			System.out.println(sql);
			logger.log(Level.ERROR, ex.getMessage());
			
		}
	}

	public List<Object> Query(Class z,String sql,Object...objects) {
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			if(objects!=null&&objects.length>0) {
				for(int i = 0;i<objects.length;i++) {
					st.setObject(i+1, objects[i]);
				}
			}
			this.tableInfo = baseTableInfo.initWithClass(z);
			List<Object> ret = new ArrayList<Object>();
			ResultSet result = st.executeQuery();
			while(result.next()) {
				Object temp = z.newInstance();
				List<ColumnInfo> columns = tableInfo.getColumns();
				for(int i = 0;i<columns.size();i++) {
					ColumnInfo co = columns.get(i);
					co.setObjectValue(temp, result.getObject(co.getName()));
				}
				ret.add(temp);
			}
			st.close();
			return ret;
		}catch(Exception e) {
			logger.log(Level.ERROR, e.getMessage());
			return null;
		}
	}
	
	public List<T> Query(String sql,Object...objects) {
			PreparedStatement st;
			try {
				st = conn.prepareStatement(sql);
				if(objects!=null&&objects.length>0) {
					for(int i = 0;i<objects.length;i++) {
						st.setObject(i+1, objects[i]);
					}
				}
				List<T> ret = new ArrayList<T>();
				ResultSet result = st.executeQuery();
				Class<?> z = this.getClass();
				while(result.next()) {
					T temp = (T)z.newInstance();
					List<ColumnInfo> columns = tableInfo.getColumns();
					for(int i = 0;i<columns.size();i++) {
						ColumnInfo co = columns.get(i);
						co.setObjectValue(temp, result.getObject(co.getName()));
					}
					ret.add(temp);
				}
				st.close();
				return ret;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.log(Level.ERROR, e.getMessage());
				return null;
			}catch(Exception e) {
				logger.log(Level.ERROR, e.getMessage());
				return null;
			}
			
	}
	
	public List<Map<String,Object>> QueryToMap(String sql,Object...objects) {
		List<Map<String,Object>> ret = new ArrayList<Map<String,Object>>();
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			if(objects!=null&&objects.length>0) {
				for(int i = 0;i<objects.length;i++) {
					st.setObject(i+1, objects[i]);
				}
			}
			ResultSet result = st.executeQuery();
			ResultSetMetaData rsmd = result.getMetaData();
			int count=rsmd.getColumnCount();
			String[] name=new String[count];
			for(int i=0;i<count;i++)
			name[i]=rsmd.getColumnName(i+1);
			while(result.next()) {
				Map<String,Object> row = new CaseInsensitiveMap<String,Object>();
				for(int i = 0;i<name.length;i++) {
					row.put(name[i], result.getObject(name[i]));
				}
				ret.add(row);
			}
			st.close();
			return ret;
		}catch(Exception e) {
			logger.log(Level.ERROR, e.getMessage());
			return null;
		}
	}
	
	public List<Map<String,Object>> QueryToMapPage(String sql,int pageNum,int pageSize,Object...objects) {

		List<Map<String,Object>> ret = new ArrayList<Map<String,Object>>();
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			if(objects!=null&&objects.length>0) {
				for(int i = 0;i<objects.length;i++) {
					st.setObject(i+1, objects[i]);
				}
			}
			ResultSet result = st.executeQuery();
			ResultSetMetaData rsmd = result.getMetaData();
			int count=rsmd.getColumnCount();
			String[] name=new String[count];
			for(int i=0;i<count;i++)
			name[i]=rsmd.getColumnName(i+1);
			while(result.next()) {
				Map<String,Object> row = new HashMap<String,Object>();
				for(int i = 0;i<name.length;i++) {
					row.put(name[i], result.getObject(name[i]));
				}
				ret.add(row);
			}
			st.close();
			return ret;
		}catch(Exception e) {
			logger.log(Level.ERROR, e.getMessage());
			return null;
		}
	}
	
	public int GetRecordCound(){
		return GetRecordCound(null);
	}
	
	public int GetRecordCoundBysql(String sql,Object...objects){
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			if(objects!=null&&objects.length>0) {
				for(int i = 0;i<objects.length;i++) {
					st.setObject(i+1, objects[i]);
				}
			}
			int ret = 0;
			ResultSet result = st.executeQuery();
			while(result.next()) {
				ret = result.getInt(1);
			}
			st.close();
			return ret;
		}catch(Exception e) {
			logger.log(Level.ERROR, e.getMessage());
			return 0;
		}
	}
	
	public int GetRecordCound(String where,Object...objects){
		String sql = "select count(*) from "+this.tableInfo.getTableName();
		if(where!=null) {
			sql+=" where "+where;
		}
		
		return GetRecordCoundBysql(sql,objects);
	}
	
	public boolean checkTable() {
		int record = GetRecordCoundBysql("select count(*) from user_tables where table_name=?",this.tableInfo.getTableName());
		return record==1?false:true;
	}
	public boolean checkTable(Class z) {
		TableInfo tableInfo = baseTableInfo.initWithClass(z);
		int record = GetRecordCoundBysql("select count(*) from user_tables where table_name=?",tableInfo.getTableName());
		return record==1?false:true;
	}
	
	public void close() {
		try {
			this.conn.close();
		} catch (SQLException e) {
			logger.log(Level.ERROR, e.getMessage());
		}
	}
}
