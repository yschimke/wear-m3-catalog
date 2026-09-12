package androidx.annotation

/**
 * A local stand-in for AndroidX's `@Sampled`, which **no published artifact provides**.
 *
 * Every vendored sample carries `@Sampled` and imports `androidx.annotation.Sampled`, so without
 * this the module does not compile — 19 of the 27 errors on the first build were this one
 * annotation. It is not an oversight upstream: `Sampled` is an AOSP-internal marker that the docs
 * tooling reads to pair a `@sample` tag with the function it names, and it is deliberately not
 * shipped. `androidx.annotation:annotation` does not contain it (checked, both the KMP and `-jvm`
 * jars), and `androidx.annotation:annotation-sampled` does not exist on Google Maven.
 *
 * Declaring it here rather than stripping it from the sources is the choice that keeps the vendored
 * tree **byte-identical to upstream**. The alternative — rewriting 48 files on every import to
 * remove an annotation — would make every sample a modified file, which is exactly the position
 * `samples/patches/` exists to avoid, and would cost the property that makes an upstream bump a
 * readable diff.
 *
 * It carries no behaviour and nothing reads it at runtime. `scripts/samples-spec.mjs` reads the
 * annotation from **source text**, not from the classpath, so this declaration is invisible to it.
 *
 * If AndroidX ever publishes the real annotation, delete this file: two declarations of one FQN on
 * a classpath is a problem worth avoiding, and the import will keep working because the sources
 * already name the real one.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
annotation class Sampled
