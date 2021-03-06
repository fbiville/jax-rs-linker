package fr.vidal.oss.jax_rs_linker.functions;

import com.google.common.base.Function;
import fr.vidal.oss.jax_rs_linker.model.QueryParameter;

import javax.lang.model.element.Element;
import javax.ws.rs.QueryParam;

public enum ElementToQueryParameter implements Function<Element, QueryParameter> {

    ELEMENT_INTO_QUERY_PARAMETER;

    @Override
    public QueryParameter apply(Element parameterElement) {
        return new QueryParameter(parameterElement.getAnnotation(QueryParam.class).value());
    }

}
