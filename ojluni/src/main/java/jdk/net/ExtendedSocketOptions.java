/*
 * Copyright (c) 2014, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.net;

import java.io.FileDescriptor;
import java.net.SocketException;
import java.net.SocketOption;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import jdk.internal.access.JavaIOFileDescriptorAccess;
import jdk.internal.access.SharedSecrets;

/**
 * Defines extended socket options, beyond those defined in
 * {@link java.net.StandardSocketOptions}. These options may be platform
 * specific.
 *
 * @since 1.8
 */
// Android-removed: @jdk.Exported, not present on Android.
// @jdk.Exported
public final class ExtendedSocketOptions {

    private static class ExtSocketOption<T> implements SocketOption<T> {
        private final String name;
        private final Class<T> type;
        ExtSocketOption(String name, Class<T> type) {
            this.name = name;
            this.type = type;
        }
        @Override public String name() { return name; }
        @Override public Class<T> type() { return type; }
        @Override public String toString() { return name; }
    }

    private ExtendedSocketOptions() { }

    /**
     * Service level properties. When a security manager is installed,
     * setting or getting this option requires a {@link NetworkPermission}
     * {@code ("setOption.SO_FLOW_SLA")} or {@code "getOption.SO_FLOW_SLA"}
     * respectively.
     */
    public static final SocketOption<SocketFlow> SO_FLOW_SLA = new
        ExtSocketOption<SocketFlow>("SO_FLOW_SLA", SocketFlow.class);

    /**
     * Disable Delayed Acknowledgements.
     *
     * <p>
     * This socket option can be used to reduce or disable delayed
     * acknowledgments (ACKs). When {@code TCP_QUICKACK} is enabled, ACKs are
     * sent immediately, rather than delayed if needed in accordance to normal
     * TCP operation. This option is not permanent, it only enables a switch to
     * or from {@code TCP_QUICKACK} mode. Subsequent operations of the TCP
     * protocol will once again disable/enable {@code TCP_QUICKACK} mode
     * depending on internal protocol processing and factors such as delayed ACK
     * timeouts occurring and data transfer, therefore this option needs to be
     * set with {@code setOption} after each operation of TCP on a given socket.
     *
     * <p>
     * The value of this socket option is a {@code Boolean} that represents
     * whether the option is enabled or disabled. The socket option is specific
     * to stream-oriented sockets using the TCP/IP protocol. The exact semantics
     * of this socket option are socket type and system dependent.
     *
     * @since 10
     */
    public static final SocketOption<Boolean> TCP_QUICKACK =
            new ExtSocketOption<Boolean>("TCP_QUICKACK", Boolean.class);

    /**
     * Keep-Alive idle time.
     *
     * <p>
     * The value of this socket option is an {@code Integer} that is the number
     * of seconds of idle time before keep-alive initiates a probe. The socket
     * option is specific to stream-oriented sockets using the TCP/IP protocol.
     * The exact semantics of this socket option are system dependent.
     *
     * <p>
     * When the {@link java.net.StandardSocketOptions#SO_KEEPALIVE
     * SO_KEEPALIVE} option is enabled, TCP probes a connection that has been
     * idle for some amount of time. The default value for this idle period is
     * system dependent, but is typically 2 hours. The {@code TCP_KEEPIDLE}
     * option can be used to affect this value for a given socket.
     *
     * @since 11
     */
    public static final SocketOption<Integer> TCP_KEEPIDLE
            = new ExtSocketOption<Integer>("TCP_KEEPIDLE", Integer.class);

    /**
     * Keep-Alive retransmission interval time.
     *
     * <p>
     * The value of this socket option is an {@code Integer} that is the number
     * of seconds to wait before retransmitting a keep-alive probe. The socket
     * option is specific to stream-oriented sockets using the TCP/IP protocol.
     * The exact semantics of this socket option are system dependent.
     *
     * <p>
     * When the {@link java.net.StandardSocketOptions#SO_KEEPALIVE
     * SO_KEEPALIVE} option is enabled, TCP probes a connection that has been
     * idle for some amount of time. If the remote system does not respond to a
     * keep-alive probe, TCP retransmits the probe after some amount of time.
     * The default value for this retransmission interval is system dependent,
     * but is typically 75 seconds. The {@code TCP_KEEPINTERVAL} option can be
     * used to affect this value for a given socket.
     *
     * @since 11
     */
    public static final SocketOption<Integer> TCP_KEEPINTERVAL
            = new ExtSocketOption<Integer>("TCP_KEEPINTERVAL", Integer.class);

    /**
     * Keep-Alive retransmission maximum limit.
     *
     * <p>
     * The value of this socket option is an {@code Integer} that is the maximum
     * number of keep-alive probes to be sent. The socket option is specific to
     * stream-oriented sockets using the TCP/IP protocol. The exact semantics of
     * this socket option are system dependent.
     *
     * <p>
     * When the {@link java.net.StandardSocketOptions#SO_KEEPALIVE
     * SO_KEEPALIVE} option is enabled, TCP probes a connection that has been
     * idle for some amount of time. If the remote system does not respond to a
     * keep-alive probe, TCP retransmits the probe a certain number of times
     * before a connection is considered to be broken. The default value for
     * this keep-alive probe retransmit limit is system dependent, but is
     * typically 8. The {@code TCP_KEEPCOUNT} option can be used to affect this
     * value for a given socket.
     *
     * @since 11
     */
    public static final SocketOption<Integer> TCP_KEEPCOUNT
            = new ExtSocketOption<Integer>("TCP_KEEPCOUNT", Integer.class);

    private static final PlatformSocketOptions platformSocketOptions =
            PlatformSocketOptions.get();

    private static final boolean flowSupported =
            platformSocketOptions.flowSupported();
    private static final boolean quickAckSupported =
            platformSocketOptions.quickAckSupported();
    private static final boolean keepAliveOptSupported =
            platformSocketOptions.keepAliveOptionsSupported();
    private static final Set<SocketOption<?>> extendedOptions = options();

    static Set<SocketOption<?>> options() {
        Set<SocketOption<?>> options = new HashSet<>();
        if (flowSupported) {
            options.add(SO_FLOW_SLA);
        }
        if (quickAckSupported) {
            options.add(TCP_QUICKACK);
        }
        if (keepAliveOptSupported) {
            options.addAll(Set.of(TCP_KEEPCOUNT, TCP_KEEPIDLE, TCP_KEEPINTERVAL));
        }
        return Collections.unmodifiableSet(options);
    }

    static {
        // Registers the extended socket options with the base module.
        sun.net.ext.ExtendedSocketOptions.register(
                new sun.net.ext.ExtendedSocketOptions(extendedOptions) {

            @Override
            public void setOption(FileDescriptor fd,
                                  SocketOption<?> option,
                                  Object value)
                throws SocketException
            {
                SecurityManager sm = System.getSecurityManager();
                if (sm != null)
                    sm.checkPermission(new NetworkPermission("setOption." + option.name()));

                if (fd == null || !fd.valid())
                    throw new SocketException("socket closed");

                if (option == SO_FLOW_SLA) {
                    assert flowSupported;
                    SocketFlow flow = checkValueType(value, option.type());
                    setFlowOption(fd, flow);
                } else if (option == TCP_QUICKACK) {
                    setQuickAckOption(fd, (boolean) value);
                } else if (option == TCP_KEEPCOUNT) {
                    setTcpkeepAliveProbes(fd, (Integer) value);
                } else if (option == TCP_KEEPIDLE) {
                    setTcpKeepAliveTime(fd, (Integer) value);
                } else if (option == TCP_KEEPINTERVAL) {
                    setTcpKeepAliveIntvl(fd, (Integer) value);
                } else {
                    throw new InternalError("Unexpected option " + option);
                }
            }

            @Override
            public Object getOption(FileDescriptor fd,
                                    SocketOption<?> option)
                throws SocketException
            {
                SecurityManager sm = System.getSecurityManager();
                if (sm != null)
                    sm.checkPermission(new NetworkPermission("getOption." + option.name()));

                if (fd == null || !fd.valid())
                    throw new SocketException("socket closed");

                if (option == SO_FLOW_SLA) {
                    assert flowSupported;
                    SocketFlow flow = SocketFlow.create();
                    getFlowOption(fd, flow);
                    return flow;
                } else if (option == TCP_QUICKACK) {
                    return getQuickAckOption(fd);
                } else if (option == TCP_KEEPCOUNT) {
                    return getTcpkeepAliveProbes(fd);
                } else if (option == TCP_KEEPIDLE) {
                    return getTcpKeepAliveTime(fd);
                } else if (option == TCP_KEEPINTERVAL) {
                    return getTcpKeepAliveIntvl(fd);
                } else {
                    throw new InternalError("Unexpected option " + option);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T checkValueType(Object value, Class<?> type) {
        if (!type.isAssignableFrom(value.getClass())) {
            String s = "Found: " + value.getClass() + ", Expected: " + type;
            throw new IllegalArgumentException(s);
        }
        return (T) value;
    }

    private static final JavaIOFileDescriptorAccess fdAccess =
            SharedSecrets.getJavaIOFileDescriptorAccess();

    private static void setFlowOption(FileDescriptor fd, SocketFlow f)
        throws SocketException
    {
        int status = platformSocketOptions.setFlowOption(fdAccess.get(fd),
                                                         f.priority(),
                                                         f.bandwidth());
        f.status(status);  // augment the given flow with the status
    }

    private static void getFlowOption(FileDescriptor fd, SocketFlow f)
            throws SocketException {
        int status = platformSocketOptions.getFlowOption(fdAccess.get(fd), f);
        f.status(status);  // augment the given flow with the status
    }

    private static void setQuickAckOption(FileDescriptor fd, boolean enable)
            throws SocketException {
        platformSocketOptions.setQuickAck(fdAccess.get(fd), enable);
    }

    private static Object getQuickAckOption(FileDescriptor fd)
            throws SocketException {
        return platformSocketOptions.getQuickAck(fdAccess.get(fd));
    }

    private static void setTcpkeepAliveProbes(FileDescriptor fd, int value)
            throws SocketException {
        platformSocketOptions.setTcpkeepAliveProbes(fdAccess.get(fd), value);
    }

    private static void setTcpKeepAliveTime(FileDescriptor fd, int value)
            throws SocketException {
        platformSocketOptions.setTcpKeepAliveTime(fdAccess.get(fd), value);
    }

    private static void setTcpKeepAliveIntvl(FileDescriptor fd, int value)
            throws SocketException {
        platformSocketOptions.setTcpKeepAliveIntvl(fdAccess.get(fd), value);
    }

    private static int getTcpkeepAliveProbes(FileDescriptor fd) throws SocketException {
        return platformSocketOptions.getTcpkeepAliveProbes(fdAccess.get(fd));
    }

    private static int getTcpKeepAliveTime(FileDescriptor fd) throws SocketException {
        return platformSocketOptions.getTcpKeepAliveTime(fdAccess.get(fd));
    }

    private static int getTcpKeepAliveIntvl(FileDescriptor fd) throws SocketException {
        return platformSocketOptions.getTcpKeepAliveIntvl(fdAccess.get(fd));
    }

    static class PlatformSocketOptions {

        protected PlatformSocketOptions() {}

        @SuppressWarnings("unchecked")
        private static PlatformSocketOptions newInstance(String cn) {
            Class<PlatformSocketOptions> c;
            try {
                c = (Class<PlatformSocketOptions>)Class.forName(cn);
                return c.getConstructor(new Class<?>[] { }).newInstance();
            } catch (ReflectiveOperationException x) {
                throw new AssertionError(x);
            }
        }

        private static PlatformSocketOptions create() {
            // Android-removed: other platforms are unsupported.
//            String osname = AccessController.doPrivileged(
//                    new PrivilegedAction<String>() {
//                        public String run() {
//                            return System.getProperty("os.name");
//                        }
//                    });
//            if ("SunOS".equals(osname)) {
//                return newInstance("jdk.net.SolarisSocketOptions");
//            } else if ("Linux".equals(osname)) {
//                return newInstance("jdk.net.LinuxSocketOptions");
//            } else if (osname.startsWith("Mac")) {
//                return newInstance("jdk.net.MacOSXSocketOptions");
//            }
            return new PlatformSocketOptions();
        }

        private static final PlatformSocketOptions instance = create();

        static PlatformSocketOptions get() {
            return instance;
        }

        int setFlowOption(int fd, int priority, long bandwidth)
            throws SocketException
        {
            throw new UnsupportedOperationException("unsupported socket option");
        }

        int getFlowOption(int fd, SocketFlow f) throws SocketException {
            throw new UnsupportedOperationException("unsupported socket option");
        }

        boolean flowSupported() {
            return false;
        }

        void setQuickAck(int fd, boolean on) throws SocketException {
            throw new UnsupportedOperationException("unsupported TCP_QUICKACK option");
        }

        boolean getQuickAck(int fd) throws SocketException {
            throw new UnsupportedOperationException("unsupported TCP_QUICKACK option");
        }

        boolean quickAckSupported() {
            return false;
        }

        boolean keepAliveOptionsSupported() {
            return false;
        }

        void setTcpkeepAliveProbes(int fd, final int value) throws SocketException {
            throw new UnsupportedOperationException("unsupported TCP_KEEPCNT option");
        }

        void setTcpKeepAliveTime(int fd, final int value) throws SocketException {
            throw new UnsupportedOperationException("unsupported TCP_KEEPIDLE option");
        }

        void setTcpKeepAliveIntvl(int fd, final int value) throws SocketException {
            throw new UnsupportedOperationException("unsupported TCP_KEEPINTVL option");
        }

        int getTcpkeepAliveProbes(int fd) throws SocketException {
            throw new UnsupportedOperationException("unsupported TCP_KEEPCNT option");
        }

        int getTcpKeepAliveTime(int fd) throws SocketException {
            throw new UnsupportedOperationException("unsupported TCP_KEEPIDLE option");
        }

        int getTcpKeepAliveIntvl(int fd) throws SocketException {
            throw new UnsupportedOperationException("unsupported TCP_KEEPINTVL option");
        }
    }
}
