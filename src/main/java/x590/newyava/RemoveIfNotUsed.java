package x590.newyava;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Аннотация, которой можно помечать всё, что надо удалить в будущем, если оно не будет использоваться */
@Retention(RetentionPolicy.SOURCE)
@RemoveIfNotUsed // It's recursion!
public @interface RemoveIfNotUsed {}
