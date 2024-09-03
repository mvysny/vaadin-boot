package com.github.mvysny.vaadinboot;

import com.github.mvysny.vaadinboot.common.TomcatWebServer;
import com.github.mvysny.vaadinboot.common.VaadinBootBase;

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
    /**
     * Creates new boot instance.
     */
    public VaadinBoot() {
        super(new TomcatWebServer());
    }
}
