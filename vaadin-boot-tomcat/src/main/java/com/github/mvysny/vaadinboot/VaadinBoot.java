package com.github.mvysny.vaadinboot;

import com.github.mvysny.vaadinboot.common.TomcatWebServer;
import com.github.mvysny.vaadinboot.common.VaadinBootBase;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;

/**
 * Bootstraps your Vaadin application from your main() function. Simply call
 * <pre>
 * new VaadinBoot().run();
 * </pre>
 * from your <code>main()</code> method.
 * <br/>
 * By default, listens on all interfaces; call {@link #localhostOnly()} to only
 * listen on localhost.
 */
public class VaadinBoot extends VaadinBootBase<VaadinBoot> {
    @NotNull
    public final String mainJarNameRegex;

    /**
     * Creates new boot instance.
     * @param mainJarNameRegex the regex of the main app jar file name, e.g. <code>testapp-.*\\.jar</code>
     */
    public VaadinBoot(@NotNull @RegExp String mainJarNameRegex) {
        super(new TomcatWebServer());
        this.mainJarNameRegex = mainJarNameRegex;
    }
}
