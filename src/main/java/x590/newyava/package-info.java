@DefaultQualifier(
		value = NotNull.class,
		locations = { // Всё, кроме LOCAL_VARIABLE
				FIELD,
				RESOURCE_VARIABLE,
				EXCEPTION_PARAMETER,
				RECEIVER,
				PARAMETER,
				RETURN,
				CONSTRUCTOR_RESULT,
				LOWER_BOUND,
				EXPLICIT_LOWER_BOUND,
				IMPLICIT_LOWER_BOUND,
				UPPER_BOUND,
				EXPLICIT_UPPER_BOUND,
				IMPLICIT_UPPER_BOUND,
		}
)
package x590.newyava;

import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

import static org.checkerframework.framework.qual.TypeUseLocation.*;