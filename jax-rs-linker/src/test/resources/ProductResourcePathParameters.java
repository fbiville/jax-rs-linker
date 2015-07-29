
package fr.vidal.oss.jax_rs_linker.parser;

import fr.vidal.oss.jax_rs_linker.api.PathParameters;
import java.lang.Override;
import java.lang.String;
import java.util.regex.Pattern;
import javax.annotation.Generated;

@Generated("fr.vidal.oss.jax_rs_linker.LinkerAnnotationProcessor")
public enum ProductResourcePathParameters implements PathParameters {
    ID("id", null);

    private final String placeholder;

    private final Pattern regex;

    ProductResourcePathParameters(String placeholder, Pattern regex) {
        this.placeholder = placeholder;
        this.regex = regex;
    }

    @Override
    public final String placeholder() {
        return this.placeholder;
    }

    @Override
    public final Pattern regex() {
        return this.regex;
    }
}
