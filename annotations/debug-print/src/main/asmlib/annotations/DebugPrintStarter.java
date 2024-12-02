package asmlib.annotations;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import java.io.File;
import java.net.URI;

public class DebugPrintStarter extends LombokPluginStarter {

    public static final Context.Key<File> outputFolderKey = new Context.Key<>();
    private static File outputFolder;

    static {
        initSelf(asmlib.annotations.DebugPrintStarter.class);
    }

    @NotNull
    public static File outputFile(@Nullable Context context, String fileName) {
        return new File(context == null ? outputFolder : context.get(outputFolderKey), fileName);
    }

    @SneakyThrows
    @Override
    public void init(ProcessingEnvironment procEnv) {
        super.init(procEnv);

        FileObject resource = procEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "test", "test1");
        JavacProcessingEnvironment environment = super.getJavacProcessingEnvironment(procEnv);
        URI uri = resource.toUri();
        File parentFile = new File(uri).getParentFile().getParentFile();
        outputFolder = parentFile;
        environment.getContext().put(DebugPrintStarter.outputFolderKey, parentFile);
    }

    @Override
    public @NotNull String dontForgetToInitSelfInStatic() {
        return "ok";
    }
}
