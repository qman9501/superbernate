/**
 * 
 */
package supernate.core.tags;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Administrator
 *
 */
@Target({FIELD}) 
@Retention(RUNTIME)
public @interface Column {

    /**
     * (Optional) The name of the column. Defaults to 
     * the property or field name.
     */
    String name() default "";

    /**
     * (Optional) Whether the column is a unique key.  This is a 
     * shortcut for the <code>UniqueConstraint</code> annotation at the table 
     * level and is useful for when the unique key constraint 
     * corresponds to only a single column. This constraint applies 
     * in addition to any constraint entailed by primary key mapping and 
     * to constraints specified at the table level.
     */
    boolean unique() default false;

    /**
     * (Optional) Whether the database column is nullable.
     */
    boolean nullable() default true;

    /**
     * (Optional) The column length. (Applies only if a
     * string-valued column is used.)
     */
    String length() default "255";
    
    boolean index() default false;
    
    /**
     * 0 int
     * 1 decimal
     * 2 number
     * 3 varchar2
     * 4 text
     * 5 datetime
     * 6 long
     * 7 byte
     * @return
     */
    int type() default -1;

    /**
     * (Optional) The precision for a decimal (exact numeric) 
     * column. (Applies only if a decimal column is used.)
     * Value must be set by developer if used when generating 
     * the DDL for the column.
     */
}
