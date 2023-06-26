package com.worksap.nlp.tools

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.Transformer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test

import javax.inject.Inject
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.function.BiFunction
import java.util.function.Predicate
import java.util.zip.ZipFile

class EsTestEnvExtension {
    Path bundlePath = null
    Path systemDic = null
    Path configFile = null
    List<Path> additionalJars = new ArrayList<>()
    List<PluginDescriptor> additionalPlugins = new ArrayList<>()

    void addPlugin(String name, Object value) {
        additionalPlugins.add(new PluginDescriptor(name: name, value: value))
    }
}

class PluginDescriptor {
    String name
    Object value

    Task task() {
        if (value instanceof Task) {
            return value
        }
        if (value instanceof TaskProvider<Task>) {
            return value.get()
        }
        throw new IllegalStateException("$value must be a Task or TaskProvider")
    }
}

class StringProvider implements Provider<String>, Serializable {
    private static final long serialVersionUID = 42L
    String value

    @Override
    String get() {
        return value
    }

    @Override
    String getOrNull() {
        return value
    }

    @Override
    String getOrElse(String defaultValue) {
        if (value == null) return defaultValue else return value
    }

    @Override
    def <S> Provider<S> map(Transformer<? extends S, ? super String> transformer) {
        throw new IllegalStateException("not implemented")
    }

    @Override
    def <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super String> transformer) {
        throw new IllegalStateException("not implemented")
    }

    @Override
    boolean isPresent() {
        return value != null
    }

    @Override
    Provider<String> orElse(String value) {
        return this
    }

    @Override
    Provider<String> orElse(Provider<? extends String> provider) {
        if (value == null) return provider else return this
    }

    @Override
    Provider<String> forUseAtConfigurationTime() {
        return this
    }

    @Override
    def <U, R> Provider<R> zip(Provider<U> right, BiFunction<? super String, ? super U, ? extends R> combiner) {
        throw new IllegalStateException("not implemented")
    }


    @Override
    String toString() {
        return value;
    }
}

class EsTestEnvPlugin implements Plugin<Project> {

    private final ObjectFactory objectFactory

    @Inject
    EsTestEnvPlugin(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory
    }

    @Override
    void apply(Project target) {
        EsTestEnvExtension ext = new EsTestEnvExtension()
        target.extensions.add(EsTestEnvExtension.class, "esTestEnv", ext)
        target.tasks.named("test").configure { Test task ->
            Provider<String> envRoot = new StringProvider()
            Path esHomePath = target.buildDir.toPath().resolve("es-env")
            envRoot.setValue(esHomePath.toString())
            task.systemProperty("sudachi.es.root", envRoot)
            task.doFirst {
                envRoot.setValue(prepareEnvironment(target, task, esHomePath, ext).toString())
            }
            task.doLast { cleanupEnvironment(esHomePath) }
            ext.additionalPlugins.forEach {
                if (it.value instanceof TaskProvider || it.value instanceof Task) {
                    dependsOn(it.value)
                }
            }

            def gradle = target.getGradle()
            def userHomeDir = target.getGradle().getGradleUserHomeDir().toPath()
            def gradleCacheDir = userHomeDir.resolve("caches").resolve(gradle.gradleVersion)
            def gradleRtDir = gradle.getGradleHomeDir().toPath()
            // configuration for ES test framework
            task.systemProperty("tests.gradle", true)
            task.systemProperty("tests.task", task.getPath())
            task.systemProperty("gradle.dist.lib", gradleRtDir.resolve("lib").toString())
            task.systemProperty("gradle.worker.jar", gradleCacheDir.resolve("workerMain/gradle-worker.jar").toString())
            task.systemProperty("java.io.tmpdir", envRoot)
        }

        target.gradle.taskGraph.whenReady {
            boolean shouldRun = false
            if (target.plugins.findPlugin(EsSudachiPlugin.class) != null) {
                shouldRun = shouldTestsRun(target.extensions.getByType(EsExtension).kind.get())
            }
            target.tasks.findAll().forEach { Task task ->
                task.onlyIf { shouldRun }
            }
        }
    }

    private static boolean shouldTestsRun(ProjectKind kind) {
        Version v = kind.parsedVersion()
        if (kind.engine == EngineType.OpenSearch) {
            return v.ge(2, 6)
        } else if (kind.engine == EngineType.ElasticSearch) {
            return (v.ge(7, 14) && v.lt(7, 99)) || v.ge(8, 5)
        } else {
            throw new IllegalArgumentException("not supported version ${kind}")
        }
    }

    private Path prepareEnvironment(Project project, Test testTask, Path basePath, EsTestEnvExtension ext) {
        def formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH-mm-ss", Locale.ROOT)
        def now = Instant.now().atZone(ZoneId.of("UTC"))
        def timepart = formatter.format(now)
        def rootPath = basePath.resolve(timepart)

        def pluginDir = rootPath.resolve("plugins")
        def configPath = rootPath.resolve("config")

        Files.createDirectories(pluginDir)
        Files.createDirectories(configPath)

        def sudachiPluginDir = pluginDir.resolve("analysis-sudachi")
        copyTree(ext.bundlePath, sudachiPluginDir) {
            !(it.getFileName().toString().startsWith("kotlin")
                    || it.getFileName().toString().startsWith("annotations-"))
        }
        for (jar in ext.additionalJars) {
            Path name = jar.getFileName()
            Files.copy(jar, sudachiPluginDir.resolve(name))
        }
        for (plugin in ext.additionalPlugins) {
            def task = plugin.task()
            def extractedPath = pluginDir.resolve(plugin.name)
            // unfortunately, we can't make this a Copy plugin because it outputs to a different directory each execution
            extractZipArchive(task.outputs.files.singleFile.toPath(), extractedPath)
        }

        def sudachiConfigDir = configPath.resolve("sudachi")
        Files.createDirectories(sudachiConfigDir)
        Files.copy(ext.systemDic, sudachiConfigDir.resolve("system_core.dic"))
        Files.copy(ext.configFile, sudachiConfigDir.resolve("sudachi.json"))

        return rootPath
    }

    private void cleanupEnvironment(Path envPath) {

    }


    static void copyTree(Path source, Path destination, Predicate<Path> filter) {
        Files.createDirectories(destination)
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relative = source.relativize(file)
                if (filter.test(file)) {
                    Files.copy(file, destination.resolve(relative))
                }
                return FileVisitResult.CONTINUE
            }

            @Override
            FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relative = source.relativize(dir)
                Path dest = destination.resolve(relative)
                if (Files.notExists(dest)) {
                    Files.createDirectory(dest)
                } else if (!Files.isDirectory(dest)) {
                    throw new IllegalArgumentException("$dest must be a directory")
                }
                return FileVisitResult.CONTINUE
            }
        })
    }

    static void extractZipArchive(Path zip, Path result) {
        Files.createDirectories(result)
        try (def descr = new ZipFile(zip.toFile())) {
            def entries = descr.entries()
            while (entries.hasMoreElements()) {
                def entry = entries.nextElement()
                def fsPath = result.resolve(entry.name)
                if (entry.isDirectory() && Files.notExists(fsPath)) {
                    Files.createDirectory(fsPath)
                } else {
                    Files.createDirectories(fsPath.parent)
                    try (def stream = descr.getInputStream(entry)) {
                        if (entry.name.endsWith("plugin-descriptor.properties")) {
                            try (def ostream = Files.newOutputStream(fsPath)) {
                                filterPluginDescriptor(stream, ostream)
                            }
                        } else {
                            Files.copy(stream, fsPath)
                        }
                    }


                    Files.setLastModifiedTime(fsPath, entry.lastModifiedTime)
                }
            }
        }

    }

    static void filterPluginDescriptor(InputStream inputStream, OutputStream outputStream) {
        inputStream.filterLine('utf-8') { !it.startsWith("modulename=") }
                .writeTo(outputStream.newWriter('utf-8'))
    }
}


