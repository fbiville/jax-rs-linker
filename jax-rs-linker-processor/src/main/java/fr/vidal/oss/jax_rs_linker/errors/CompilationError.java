package fr.vidal.oss.jax_rs_linker.errors;

public enum CompilationError {

    NO_PATH_FOUND(
        "%n\tNo path could be found. " +
        "%n\tPlease make sure JAX-RS @Path is set on the method, its class or superclasses." +
        "%n\tGiven method: <%s>"
    ),
    ANNOTATION_MISUSE(
        "%n\tMethod should be annotated with exactly one annotation: @Self or @SubResource." +
        "%n\tGiven method: <%s>"
    ),
    NOT_A_METHOD(
        "%n\tElement should be a method, directly wrapped in a class." +
        "%n\tGiven method: <%s>"
    ),
    NO_HTTP_ANNOTATION_FOUND(
        "%n\tNo JAX-RS HTTP verb annotation found (e.g. @GET, @POST...)." +
        "%n\tGiven method: <%s>"
    ),
    MISSING_ANNOTATIONS(
        "%n\tThe following method links to an unreachable resource class via @SubResource." +
        "%n\tGiven method: <%s>"
    ),
    TOO_MANY_SELF(
        "%n\tThe enclosing class already defined one @Self-annotated method. Only one method should be annotated so." +
        "%n\tGiven method: <%s>"
    ),
    ONE_APPLICATION_ONLY(
        "%n\tThere should be exactly one @ExposedApplication-annotated Jersey Application."
    ),
    INCONSISTENT_APPLICATION_MAPPING(
        "%n\tEither annotate your configuration class with @ApplicationPath or provide a servletName to @ExposedApplication (not both)." +
        "%n\tGiven class: <%s>"
    ),
    NO_APPLICATION_SERVLET_NAME(
        "%n\t@ExposedApplication servletName must not be empty when used on a package." +
        "%n\tGiven package: <%s>"
    ),
    MISSING_SELF(
        "%n\tThe following classes need exactly 1 method annotated with @Self annotation:" +
        "%n\t - %s"
    );

    private final String errorMessage;

    CompilationError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String text() {
        return errorMessage;
    }

    public String format(Object... args) {
        return String.format(errorMessage, args);
    }
}
