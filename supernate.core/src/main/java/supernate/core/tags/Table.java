/**
 * 
 */
package supernate.core.tags;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Administrator
 *
 */
@Target({TYPE}) 
@Retention(RUNTIME)
public @interface Table {

    /**
     * (Optional) The name of the column. Defaults to 
     * the property or field name.
     */
    String name() default "";

    String comment() default "";
}