package fr.vidal.oss.jax_rs_linker;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import fr.vidal.oss.jax_rs_linker.api.Self;
import fr.vidal.oss.jax_rs_linker.api.SubResource;
import fr.vidal.oss.jax_rs_linker.functions.OptionalFunctions;
import fr.vidal.oss.jax_rs_linker.model.ClassName;
import fr.vidal.oss.jax_rs_linker.model.Mapping;
import fr.vidal.oss.jax_rs_linker.parser.ElementParser;
import fr.vidal.oss.jax_rs_linker.parser.ResourceGraphValidator;
import fr.vidal.oss.jax_rs_linker.writer.ContextPathHolderWriter;
import fr.vidal.oss.jax_rs_linker.writer.DotFileWriter;
import fr.vidal.oss.jax_rs_linker.writer.LinkerWriter;
import fr.vidal.oss.jax_rs_linker.writer.PathParamsEnumWriter;
import fr.vidal.oss.jax_rs_linker.writer.QueryParamsEnumWriter;
import fr.vidal.oss.jax_rs_linker.writer.ResourceFileWriters;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static fr.vidal.oss.jax_rs_linker.functions.MappingToPathParameters.TO_PATH_PARAMETERS;
import static fr.vidal.oss.jax_rs_linker.functions.MappingToQueryParameters.TO_QUERY_PARAMETERS;
import static fr.vidal.oss.jax_rs_linker.predicates.ElementHasKind.byKind;
import static javax.lang.model.SourceVersion.latest;
import static javax.lang.model.element.ElementKind.METHOD;


@AutoService(Processor.class)
public class LinkerAnnotationProcessor extends AbstractProcessor {

    private static final String GENERATED_CLASSNAME_SUFFIX = "Linker";
    private static final ClassName CONTEXT_PATH_HOLDER = ClassName.valueOf("fr.vidal.oss.jax_rs_linker.ContextPathHolder");
    private static final String GRAPH_OPTION = "graph";

    private final Multimap<ClassName, Mapping> elements = LinkedHashMultimap.create();
    private ResourceFileWriters resourceFiles;
    private ElementParser elementParser;
    private ResourceGraphValidator validator;

    @Override
    public Set<String> getSupportedOptions() {
        return ImmutableSet.of(GRAPH_OPTION);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return newHashSet(
            Self.class.getName(),
            SubResource.class.getName()
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return latest();
    }

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Messager messager = processingEnv.getMessager();
        resourceFiles = new ResourceFileWriters(processingEnv.getFiler());
        validator = new ResourceGraphValidator(messager);
        elementParser = new ElementParser(
            messager,
            processingEnv.getTypeUtils()
        );
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {

        ImmutableListMultimap.Builder<ClassName, Mapping> buildingRoundElements = ImmutableListMultimap.builder();
        annotations.stream()
            .flatMap(annotation -> roundEnv.getElementsAnnotatedWith(annotation).stream())
            .filter(byKind(METHOD))
            .map(e -> elementParser.parse(e))
            .filter(Optional::isPresent)
            .map(OptionalFunctions.intoUnwrapped())
            .filter(Objects::nonNull)
            .forEach(e -> buildingRoundElements.put(e.getJavaLocation().getClassName(), e));

        Multimap<ClassName, Mapping> roundElements = buildingRoundElements.build();
        if (validator.validateMappings(roundElements)) {
            tryGenerateSources(roundElements);
            tryExportGraph(roundEnv);
        }

        return false;
    }

    private void tryGenerateSources(Multimap<ClassName, Mapping> roundElements) {
        try {
            generateSources(roundElements);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    private void tryExportGraph(RoundEnvironment roundEnv) {
        if (roundEnv.processingOver() && processingEnv.getOptions().get(GRAPH_OPTION) != null) {
            try (DotFileWriter writer = new DotFileWriter(resourceFiles.writer("resources.dot"))) {
                writer.write(elements);
            }
        }
    }

    private void generateSources(Multimap<ClassName, Mapping> roundElements) throws IOException {
        tryGenerateContextPathHolder();
        elements.putAll(roundElements);
        generateLinkerSources(roundElements);
    }

    private void tryGenerateContextPathHolder() throws IOException {
        try {
            new ContextPathHolderWriter(processingEnv.getFiler()).write(CONTEXT_PATH_HOLDER);
        } catch (FilerException ignored) {
        }
    }

    private void generateLinkerSources(Multimap<ClassName, Mapping> elements) throws IOException {
        for (ClassName className : elements.keySet()) {
            generateLinkerClasses(className, elements.get(className));
            generatePathParamEnums(className, elements.get(className));
            generateQueryParamEnums(className, elements.get(className));
        }
    }

    private void generateLinkerClasses(ClassName className, Collection<Mapping> mappings) throws IOException {
        ClassName generatedClass = className.append(GENERATED_CLASSNAME_SUFFIX);
        new LinkerWriter(processingEnv.getFiler()).write(generatedClass, mappings, CONTEXT_PATH_HOLDER);
    }

    private void generatePathParamEnums(ClassName className, Collection<Mapping> mappings) throws IOException {
        if (!mappings.stream().flatMap(TO_PATH_PARAMETERS).findAny().isPresent()) {
            return;
        }
        ClassName generatedEnum = className.append("PathParameters");
        new PathParamsEnumWriter(processingEnv.getFiler()).write(generatedEnum, mappings);
    }

    private void generateQueryParamEnums(ClassName className, Collection<Mapping> mappings) throws IOException {
        if (!mappings.stream().flatMap(TO_QUERY_PARAMETERS).findAny().isPresent()) {
            return;
        }
        ClassName generatedEnum = className.append("QueryParameters");
        new QueryParamsEnumWriter(processingEnv.getFiler()).write(generatedEnum, mappings);
    }
}