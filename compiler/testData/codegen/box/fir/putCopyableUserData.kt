// TARGET_BACKEND: JVM
// WITH_STDLIB
// MODULE: m1
// FILE: UserDataHolderBase.java

import org.jetbrains.annotations.NotNull;

public class UserDataHolderBase {
    public <T> void putCopyableUserData(@NotNull Key<T> key, T value )
    {}
}

// FILE: Key.java

import org.jetbrains.annotations.NotNull;

public class Key<K> {
    public Key(@NotNull String name)
    {
    }

    @NotNull
    public static <T> Key<T> create(@NotNull String name) {
        return new Key<T>(name);
    }
}

// MODULE: m2(m1)
// FILE: RunConfigurationBase.java

public abstract class RunConfigurationBase<R> extends UserDataHolderBase {}

// FILE: test.kt

private val RUN_EXTENSIONS = Key.create<List<String>>("run.extension.elements")

open class RunConfigurationExtensionsManager<U : RunConfigurationBase<*>> {
    fun readExternal(configuration: U) {
        val copy = mutableListOf("Alpha", "Omega")
        copy += "Beta"
        configuration.putCopyableUserData(RUN_EXTENSIONS, copy)
    }
}

fun box(): String {
    class Configuration : RunConfigurationBase<String>() {}
    val configuration = Configuration()
    RunConfigurationExtensionsManager<Configuration>().readExternal(configuration)
    return "OK"
}
