package net.rakugakibox.spring.boot.logback.access;

import java.io.Serializable;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.ServerAdapter;
import lombok.Getter;
import lombok.Setter;

/**
 * The base class of Logback-access events.
 */
public abstract class AbstractLogbackAccessEvent extends AccessEvent {

    /**
     * Whether to use the server port instead of the local port.
     */
    @Getter
    @Setter
    private boolean useServerPortInsteadOfLocalPort;

    /**
     * The local port.
     */
    private final LocalPort localPort = new LocalPort();

    /**
     * The remote user.
     */
    private final RemoteUser remoteUser = new RemoteUser();

    /**
     * Constructs an instance.
     *
     * @param request the HTTP request.
     * @param response the HTTP response.
     * @param adapter the server adapter.
     */
    public AbstractLogbackAccessEvent(HttpServletRequest request, HttpServletResponse response, ServerAdapter adapter) {
        super(request, response, adapter);
        setThreadName(Thread.currentThread().getName());
    }

    /** {@inheritDoc} */
    @Override
    public int getLocalPort() {
        return localPort.get();
    }

    /** {@inheritDoc} */
    @Override
    public String getRemoteUser() {
        return remoteUser.get();
    }

    /**
     * The base class that indicates the overridden attribute.
     * The value is optional and it is lazily evaluated.
     * If the evaluated value is present, caches it. Otherwise, returns the original value.
     *
     * @param <T> the type of value.
     */
    protected static abstract class AbstractOverridenAttribute<T extends Serializable> implements Serializable {

        /**
         * Whether was evaluated.
         */
        private boolean evaluated;

        /**
         * The evaluated value.
         */
        private T value;

        /**
         * Returns the value.
         *
         * @return the value.
         */
        public T get() {
            if (!evaluated) {
                value = evaluateValueToOverride().orElse(null);
                evaluated = true;
            }
            return Optional.ofNullable(value).orElseGet(this::getOriginalValue);
        }

        /**
         * Evaluates the value to override.
         *
         * @return the value to override.
         */
        protected abstract Optional<T> evaluateValueToOverride();

        /**
         * Returns the original value.
         *
         * @return the original value.
         */
        protected abstract T getOriginalValue();

    }

    /**
     * The local port.
     * Takes the server port if necessary.
     */
    private class LocalPort extends AbstractOverridenAttribute<Integer> {

        /** {@inheritDoc} */
        @Override
        protected Optional<Integer> evaluateValueToOverride() {
            return Optional.of(AbstractLogbackAccessEvent.this)
                    .filter(AbstractLogbackAccessEvent::isUseServerPortInsteadOfLocalPort)
                    .map(AccessEvent::getRequest)
                    .map(HttpServletRequest::getServerPort);
        }

        /** {@inheritDoc} */
        @Override
        protected Integer getOriginalValue() {
            return AbstractLogbackAccessEvent.super.getLocalPort();
        }

    }

    /**
     * The remote user.
     * Takes the remote user provided by Spring Security if necessary.
     */
    private class RemoteUser extends AbstractOverridenAttribute<String> {

        /** {@inheritDoc} */
        @Override
        protected Optional<String> evaluateValueToOverride() {
            return Optional.of(AbstractLogbackAccessEvent.this)
                    .map(AccessEvent::getRequest)
                    .map(request -> (String) request.getAttribute(
                            LogbackAccessSecurityAttributesSaveFilter.REMOTE_USER_ATTRIBUTE_NAME));
        }

        /** {@inheritDoc} */
        @Override
        protected String getOriginalValue() {
            return AbstractLogbackAccessEvent.super.getRemoteUser();
        }

    }

}
