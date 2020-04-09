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

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @author Administrator
 *
 */
@SuppressWarnings("unchecked")
public class Entity<T> {
	private static final Logger logger = LogManager.getLogger("supernate");
	private transient String dialectPackage;
	public String getDialectPackage() {
		return dialectPackage;
	}
	public void setDialectPackage(String dialectPackage) {
		this.dialectPackage = dialectPackage;
		try {
			Class cla = Class.forName(this.dialectPackage+".TableInfo");
			this.baseTableInfo = (TableInfo)cla.newInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

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
	}
	public Entity(Connection conn,String packageStr) throws Exception  {
		this.setDialectPackage(packageStr);
		init();
		Class z = this.getClass();
		this.conn = conn;
		if(z==Entity.class) {
			return;
		}
		this.tableInfo = this.baseTableInfo.initWithClass(z);
	}
	private static ComboPooledDataSource ds = null;
	
	public Entity(String url,String username,String password,String packageStr) throws Exception {

		this.setDialectPackage(packageStr);
		init();
		initconnection(url,username,password);
		Class z = this.getClass();
		//this.conn = DriverManager.getConnection(url, username, password);
		if(z==Entity.class) {
			return;
		}
		this.tableInfo = this.baseTableInfo.initWithClass(z);
	}
	
	private synchronized void initconnection(String url,String username,String password) throws Exception {
		if(ds==null) {
			ds = new ComboPooledDataSource();
			ds.setJdbcUrl(url);
			ds.setUser(username);
			ds.setPassword(password);
			ds.setInitialPoolSize(10);
			ds.setMinPoolSize(10);
			ds.setMaxPoolSize(100);
			ds.setDriverClass(this.baseTableInfo.getClassDriver());
		}
		this.conn = ds.getConnection(username,password);
	}
	
	private void init() {
		try {
			if(this.dialectPackage!=null) {
				Class cla = Class.forName(this.dialectPackage+".TableInfo");
				this.baseTableInfo = (TableInfo)cla.newInstance();
				Class.forName(this.baseTableInfo.getClassDriver());
			}
			Class z=this.getClass();
			if(z.isAnnotationPresent(supernate.core.tags.ConnectionInfo.class)) {
				supernate.core.tags.ConnectionInfo connectionInfo = (supernate.core.tags.ConnectionInfo)z.getAnnotation(supernate.core.tags.ConnectionInfo.class);
				
				this.conn = DriverManager.getConnection(connectionInfo.url(), connectionInfo.username(), connectionInfo.password());
			}
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
	public TableInfo getTableInfo(Class z) throws Exception{
		return baseTableInfo.initWithClass(z);
	}
	
	public void InitTable(boolean createhis) throws Exception {
		if(this.baseTableInfo==null) {
			logger.log(Level.ERROR, "There are no Dialect!");
			return;
		}
		Class z = this.getClass();
		if(this.tableInfo==null) {
			this.tableInfo = this.baseTableInfo.initWithClass(z);
		}
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
				throw ex;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.log(Level.ERROR, e.getMessage()+"\r\n"+ex.getStackTrace());
				throw e;
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

	public void InitTable(Class z,boolean createhis) throws Exception {
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
	
	public void insert() throws Exception{
		TableInfo tableInfo = baseTableInfo.initWithObject(this);
		tableInfo.setEntity(this);
		tableInfo.Insert();
	}	
	
	public void insert(Object t) throws Exception{
		TableInfo tableInfo = baseTableInfo.initWithObject(t);
		tableInfo.setEntity(this);
		tableInfo.Insert();
	}	
	
	public void update() throws Exception{
		TableInfo tableInfo = baseTableInfo.initWithObject(this);
		tableInfo.setEntity(this);
		tableInfo.Update();
	}
	
	public void update(Object t) throws Exception{
		TableInfo tableInfo = baseTableInfo.initWithObject(t);
		tableInfo.setEntity(this);
		tableInfo.Update();
	}

	public void update(String where,Object...objects) throws Exception{
		TableInfo tableInfo = baseTableInfo.initWithObject(this);
		tableInfo.setEntity(this);
		tableInfo.Update(where,objects);
	}

	public void delete(String where,Object...objects) throws Exception{
		TableInfo tableInfo = baseTableInfo.initWithObject(this);
		tableInfo.setEntity(this);
		tableInfo.Delete(where,objects);
	}
	public void delete(Object t) throws Exception {
		TableInfo tableInfo = baseTableInfo.initWithObject(t);
		tableInfo.setEntity(this);
		tableInfo.Delete();
	}
	public void delete() throws Exception {
		TableInfo tableInfo = baseTableInfo.initWithObject(this);
		tableInfo.setEntity(this);
		tableInfo.Delete();
	}
	
	public T getByKey(Object id){
		T ret = null;
		TableInfo ti = this.tableInfo;
		List<T> ls = this.Query("select * from "+ti.getTableName()+" where "+ti.getKeyColumn().getName()+"=?", id);
		if(ls.size()>0)
			ret = ls.get(0);
		return ret;
	}
	
	public T getByClassKey(Class z,Object id) throws Exception{
        T ret = null;
        this.tableInfo = baseTableInfo.initWithClass(z);
		List<Object> ls = this.Query(z,"select * from "+this.tableInfo.getTableName()+" where "+this.tableInfo.getKeyColumn().getName()+"=?", id);
		if(ls.size()>0)
			ret = (T)ls.get(0);
		return ret;
	}
	
	public Object getByKey(Class z,Object id) throws Exception {
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

	public PageRecords<T> getObjectsByPage(Class z,String where,int pageNum,int pageSize,Object...objects) throws Exception {
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
		String pagesql = this.baseTableInfo.getPageSql();
		int startNumber = (pageNum-1)*pageSize+1;
		int endNumber = pageNum*pageSize;
		PreparedStatement st = null;
		if(where!=null) {
			if(startNumber>0) {
				sql = pagesql.replaceAll("{sql}", "SELECT * FROM "+this.tableInfo.getTableName()+" where "+where);
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
				sql = pagesql.replaceAll("{sql}", "SELECT * FROM "+this.tableInfo.getTableName());
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
	
	
	public void Excutesql(String sql,Object...objects) throws SQLException {
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
		}catch(SQLException ex) {
			System.out.println(sql);
			logger.error(ex);
			throw ex;
		}
	}

	public List<Object> Query(Class z,String sql,Object...objects) throws Exception {
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
		}catch(SQLException e) {
			logger.log(Level.ERROR, e.getMessage());
			throw e;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e);
			throw e;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e);
			throw e;
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
		}catch(SQLException e) {
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
		}catch(SQLException e) {
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
		}catch(SQLException e) {
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
	public boolean checkTable(Class z) throws Exception {
		TableInfo tableInfo = baseTableInfo.initWithClass(z);
		int record = GetRecordCoundBysql("select count(*) from user_tables where table_name=?",tableInfo.getTableName());
		return record==1?false:true;
	}
	
	public void close() {
		try {
			this.conn.commit();
			this.conn.close();
		} catch (SQLException e) {
			logger.log(Level.ERROR, e.getMessage());
		}
	}
}
