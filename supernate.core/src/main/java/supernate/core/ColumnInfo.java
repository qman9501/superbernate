/**
 * 
 */
package supernate.core;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.ReflectionUtils;

import com.sxkj.jutils.TimestampHelper;

import supernate.core.tags.AutoGen;
import supernate.core.tags.Column;
import supernate.core.tags.Key;
import supernate.core.tags.Table;


/**
 * @author Administrator
 *
 */	@Table
	public abstract class ColumnInfo{
		private static final Logger logger = LogManager.getLogger("supernate");
		
		public ColumnInfo() {
			super();
		}
		
		public ColumnInfo(Column co) {
			super();
			this.setName(co.name().toUpperCase());
			this.setLength(co.length());
			this.setNullable(co.nullable()?1:0);
			this.setUnique(co.unique()?1:0);
			this.setType(co.type());
			this.setIndex(co.index()?1:0);
		}
		
		/**
		 * @return the name
		 */
		public String getName() {
			return name.toUpperCase();
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name.toUpperCase();
		}

		/**
		 * @return the ai
		 */
		public void setAi(int ai) {
			this.ai = ai;
		}
		
		public int getAi() {
			return ai;
		}

		/**
		 * @param ai the ai to set
		 */
		public boolean isAi() {
			return ai==1?true:false;
		}

		/**
		 * @return the unique
		 */
		public int getUnique() {
			return unique;
		}

		/**
		 * @param unique the unique to set
		 */
		public void setUnique(int unique) {
			this.unique = unique;
		}
		
		public boolean isUnique() {
			return unique==1?true:false;
		}

		/**
		 * @return the nullable
		 */
		public int getNullable() {
			return nullable;
		}

		/**
		 * @param nullable the nullable to set
		 */
		public void setNullable(int nullable) {
			this.nullable = nullable;
		}

		/**
		 * @return the length
		 */
		public String getLength() {
			return length;
		}

		/**
		 * @param length the length to set
		 */
		public void setLength(String length) {
			this.length = length;
		}

		/**
		 * @return the type
		 */
		public int getType() {
			return type;
		}

		/**
		 * @param type the type to set
		 */
		public void setType(int type) {
			this.type = type;
		}

		/**
		 * @return the index
		 */
		public int getIndex() {
			return index;
		}
		
		public boolean isIndex() {
			return index==1?true:false;
		}

		/**
		 * @param index the index to set
		 */
		public void setIndex(int index) {
			this.index = index;
		}

		/**
		 * @return the fieldName
		 */
		public String getFieldName() {
			return fieldName;
		}

		/**
		 * @param fieldName the fieldName to set
		 */
		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		/**
		 * @return the value
		 */
		public Object getValue() {
			return value;
		}

		/**
		 * @return the id
		 */
		public Integer getId() {
			return id;
		}

		/**
		 * @param id the id to set
		 */
		public void setId(Integer id) {
			this.id = id;
		}

		@Column(name="COLUMNNAME")
		protected String name;
		@Column(name="ISAI")
		protected int ai;
		@Column(name="ISUNIQUE")
		protected int unique;
		@Column(name="ISNULLABLE")
		protected int nullable;
		@Column(name="COLUMNLENGTH")
		protected String length;
		@Column(name="COLUMNTYPE")
		protected int type;
		@Column(name="ISINDEX")
		protected int index;
		@Column(name="FIELDNAME")
		protected String fieldName;
		
		protected Object value;
		@Column(name="ID")
		@Key()
		@AutoGen()
		protected Integer id;
		@Column
		protected String tableName;
		
		@Column
		protected int key;
		
		public boolean isKey() {
			return key==1?true:false;
		}

		public void setIsKey(int key) {
			this.key = key;
		}
		@Column
		protected String sqlType;
		
		public String getSqlType() {
			return sqlType;
		}

		public void setSqlType(String sqlType) {
			this.sqlType = sqlType;
		}
		
		public String getTableName() {
			return tableName.toUpperCase();
		}

		public void setTableName(String tableName) {
			this.tableName = tableName.toUpperCase();
		}

		public void setValue(Object value) {
			if(value==null) {
				this.value = null;
				return;
			}
			if(value.getClass().getSimpleName().toLowerCase().contains("string")) {
				if(value.equals("")) {
					if(this.nullable==0&&!this.isAi())
						this.value = "";
					else
						this.value = null;
					return;
				}
				String v = value.toString();
				switch(type) {
					case 0:
						this.value = Integer.parseInt(v);
						break;
					case 1:
						BigDecimal bd = new BigDecimal(v);
						String[] length = this.length.split(",");
						int a = 2;
					    if(length!=null&&length.length>1) {
					    	a = Integer.valueOf(length[1]);
					    }
					    this.value = bd.setScale(a, BigDecimal.ROUND_HALF_UP).doubleValue();
						break;
					case 3:
					case 4:
						this.value = v;
						break;
					case 5:
						this.value = TimestampHelper.getTimestamp(v);
						break;
					case 6:
						this.value = Long.parseLong(v);
						break;
					case 7:
						if(v.length()>1) {
							v = v.substring(v.length()-2);
						}
						this.value = Integer.parseInt(v);
						break;
				}
			}else {
				this.value = value;
			}
		}
		
		public void setObjectValue(Object obj,Object value) {
			Class z = obj.getClass();
			try {
				Field field = ReflectionUtils.findField(z, fieldName);
				field.setAccessible(true);
				field.set(obj, value);
			} catch (Exception e) {
				logger.log(Level.ERROR,e.getMessage());
			}
		}
		public void setValueWithObj(Object obj) {
			Class z = obj.getClass();
			try {
				Field field = ReflectionUtils.findField(z, fieldName);
				this.setValue(field.get(obj));
			} catch (Exception e) {
				logger.log(Level.ERROR,e.getMessage());
			}
		}
		public Object getValueWithObj(Object obj) {

			Class z = obj.getClass();
			try {
				Field field = ReflectionUtils.findField(z, fieldName);
				return field.get(obj);
			} catch (Exception e) {
				logger.log(Level.ERROR,e.getMessage());
				return null;
			}
		}
		public abstract String getIndexSql(boolean ishistory);
		public abstract String getSqlString(boolean ishistory);
		
		@Override
		public boolean equals(Object columnInfo) {
			return columnInfo.toString().toLowerCase().contains(this.getName().toLowerCase());
		}
		
		@Override
		public String toString() {
			return this.getName();
		}
		
		public abstract Object getAutoValue(Entity et);
		
		public abstract String getAutoFunctioin();
	}
