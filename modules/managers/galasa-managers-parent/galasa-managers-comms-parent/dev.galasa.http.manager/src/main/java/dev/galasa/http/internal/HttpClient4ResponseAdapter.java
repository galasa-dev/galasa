/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http.internal;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpParams;

/**
 * Adapter that wraps an httpclient5 CloseableHttpResponse to provide
 * an httpclient4 CloseableHttpResponse interface for backward compatibility.
 * 
 * This is a temporary solution to maintain backward compatibility during
 * the migration from httpclient4 to httpclient5.
 */
public class HttpClient4ResponseAdapter implements CloseableHttpResponse {

    private final org.apache.hc.client5.http.impl.classic.CloseableHttpResponse httpclient5Response;
    private StatusLine statusLine;
    private Locale locale;

    public HttpClient4ResponseAdapter(org.apache.hc.client5.http.impl.classic.CloseableHttpResponse httpclient5Response) {
        this.httpclient5Response = httpclient5Response;
        this.locale = httpclient5Response.getLocale();
    }

    @Override
    public void close() throws IOException {
        httpclient5Response.close();
    }

    @Override
    public StatusLine getStatusLine() {
        if (statusLine != null) {
            return statusLine;
        }
        return new StatusLine() {
            @Override
            public ProtocolVersion getProtocolVersion() {
                return new ProtocolVersion(
                    httpclient5Response.getVersion().getProtocol(),
                    httpclient5Response.getVersion().getMajor(),
                    httpclient5Response.getVersion().getMinor()
                );
            }

            @Override
            public int getStatusCode() {
                return httpclient5Response.getCode();
            }

            @Override
            public String getReasonPhrase() {
                return httpclient5Response.getReasonPhrase();
            }
        };
    }

    @Override
    public void setStatusLine(StatusLine statusline) {
        this.statusLine = statusline;
        if (statusline != null) {
            httpclient5Response.setCode(statusline.getStatusCode());
            httpclient5Response.setReasonPhrase(statusline.getReasonPhrase());
        }
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code) {
        httpclient5Response.setCode(code);
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
        httpclient5Response.setCode(code);
        httpclient5Response.setReasonPhrase(reason);
    }

    @Override
    public void setStatusCode(int code) throws IllegalStateException {
        httpclient5Response.setCode(code);
    }

    @Override
    public void setReasonPhrase(String reason) throws IllegalStateException {
        httpclient5Response.setReasonPhrase(reason);
    }

    @Override
    public HttpEntity getEntity() {
        org.apache.hc.core5.http.HttpEntity entity5 = httpclient5Response.getEntity();
        if (entity5 == null) {
            return null;
        }
        
        return new HttpEntity() {
            @Override
            public boolean isRepeatable() {
                return entity5.isRepeatable();
            }

            @Override
            public boolean isChunked() {
                return entity5.isChunked();
            }

            @Override
            public long getContentLength() {
                return entity5.getContentLength();
            }

            @Override
            public org.apache.http.Header getContentType() {
                String contentType = entity5.getContentType();
                if (contentType == null) {
                    return null;
                }
                return new org.apache.http.message.BasicHeader("Content-Type", contentType);
            }

            @Override
            public org.apache.http.Header getContentEncoding() {
                String encoding = entity5.getContentEncoding();
                if (encoding == null) {
                    return null;
                }
                return new org.apache.http.message.BasicHeader("Content-Encoding", encoding);
            }

            @Override
            public java.io.InputStream getContent() throws IOException, UnsupportedOperationException {
                return entity5.getContent();
            }

            @Override
            public void writeTo(java.io.OutputStream outstream) throws IOException {
                entity5.writeTo(outstream);
            }

            @Override
            public boolean isStreaming() {
                return entity5.isStreaming();
            }

            @Override
            @Deprecated
            public void consumeContent() throws IOException {
                // No-op for compatibility
            }
        };
    }

    @Override
    public void setEntity(HttpEntity entity) {
        // For responses, setting entity is typically not needed
        // but we'll support it for completeness
        if (entity != null) {
            try {
                org.apache.hc.core5.http.io.entity.ByteArrayEntity entity5 =
                    new org.apache.hc.core5.http.io.entity.ByteArrayEntity(
                        org.apache.commons.io.IOUtils.toByteArray(entity.getContent()),
                        org.apache.hc.core5.http.ContentType.parse(entity.getContentType().getValue())
                    );
                httpclient5Response.setEntity(entity5);
            } catch (IOException e) {
                throw new RuntimeException("Failed to set entity", e);
            }
        } else {
            httpclient5Response.setEntity(null);
        }
    }

    @Override
    public Locale getLocale() {
        return locale != null ? locale : httpclient5Response.getLocale();
    }

    @Override
    public void setLocale(Locale loc) {
        this.locale = loc;
        httpclient5Response.setLocale(loc);
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        org.apache.hc.core5.http.ProtocolVersion ver5 = httpclient5Response.getVersion();
        return new ProtocolVersion(ver5.getProtocol(), ver5.getMajor(), ver5.getMinor());
    }

    @Override
    public boolean containsHeader(String name) {
        return httpclient5Response.containsHeader(name);
    }

    @Override
    public Header[] getHeaders(String name) {
        org.apache.hc.core5.http.Header[] headers5 = httpclient5Response.getHeaders(name);
        Header[] headers4 = new Header[headers5.length];
        for (int i = 0; i < headers5.length; i++) {
            final org.apache.hc.core5.http.Header h5 = headers5[i];
            headers4[i] = new org.apache.http.message.BasicHeader(h5.getName(), h5.getValue());
        }
        return headers4;
    }

    @Override
    public Header getFirstHeader(String name) {
        org.apache.hc.core5.http.Header header5 = httpclient5Response.getFirstHeader(name);
        if (header5 == null) {
            return null;
        }
        return new org.apache.http.message.BasicHeader(header5.getName(), header5.getValue());
    }

    @Override
    public Header getLastHeader(String name) {
        org.apache.hc.core5.http.Header header5 = httpclient5Response.getLastHeader(name);
        if (header5 == null) {
            return null;
        }
        return new org.apache.http.message.BasicHeader(header5.getName(), header5.getValue());
    }

    @Override
    public Header[] getAllHeaders() {
        org.apache.hc.core5.http.Header[] headers5 = httpclient5Response.getHeaders();
        Header[] headers4 = new Header[headers5.length];
        for (int i = 0; i < headers5.length; i++) {
            final org.apache.hc.core5.http.Header h5 = headers5[i];
            headers4[i] = new org.apache.http.message.BasicHeader(h5.getName(), h5.getValue());
        }
        return headers4;
    }

    @Override
    public void addHeader(Header header) {
        if (header != null) {
            httpclient5Response.addHeader(header.getName(), header.getValue());
        }
    }

    @Override
    public void addHeader(String name, String value) {
        httpclient5Response.addHeader(name, value);
    }

    @Override
    public void setHeader(Header header) {
        if (header != null) {
            httpclient5Response.setHeader(header.getName(), header.getValue());
        }
    }

    @Override
    public void setHeader(String name, String value) {
        httpclient5Response.setHeader(name, value);
    }

    @Override
    public void setHeaders(Header[] headers) {
        if (headers != null) {
            for (Header header : headers) {
                if (header != null) {
                    httpclient5Response.setHeader(header.getName(), header.getValue());
                }
            }
        }
    }

    @Override
    public void removeHeader(Header header) {
        if (header != null) {
            httpclient5Response.removeHeader(
                new org.apache.hc.core5.http.message.BasicHeader(header.getName(), header.getValue())
            );
        }
    }

    @Override
    public void removeHeaders(String name) {
        httpclient5Response.removeHeaders(name);
    }

    @Override
    public HeaderIterator headerIterator() {
        return new HeaderIteratorAdapter(httpclient5Response.headerIterator());
    }

    @Override
    public HeaderIterator headerIterator(String name) {
        return new HeaderIteratorAdapter(httpclient5Response.headerIterator(name));
    }

    @Override
    @Deprecated
    public HttpParams getParams() {
        // HttpParams is deprecated in httpclient4 and doesn't exist in httpclient5
        // Return a no-op implementation for compatibility
        return new HttpParams() {
            @Override
            public Object getParameter(String name) {
                return null;
            }

            @Override
            public HttpParams setParameter(String name, Object value) {
                return this;
            }

            @Override
            public HttpParams copy() {
                return this;
            }

            @Override
            public boolean removeParameter(String name) {
                return false;
            }

            @Override
            public long getLongParameter(String name, long defaultValue) {
                return defaultValue;
            }

            @Override
            public HttpParams setLongParameter(String name, long value) {
                return this;
            }

            @Override
            public int getIntParameter(String name, int defaultValue) {
                return defaultValue;
            }

            @Override
            public HttpParams setIntParameter(String name, int value) {
                return this;
            }

            @Override
            public double getDoubleParameter(String name, double defaultValue) {
                return defaultValue;
            }

            @Override
            public HttpParams setDoubleParameter(String name, double value) {
                return this;
            }

            @Override
            public boolean getBooleanParameter(String name, boolean defaultValue) {
                return defaultValue;
            }

            @Override
            public HttpParams setBooleanParameter(String name, boolean value) {
                return this;
            }

            @Override
            public boolean isParameterTrue(String name) {
                return false;
            }

            @Override
            public boolean isParameterFalse(String name) {
                return false;
            }
        };
    }

    @Override
    @Deprecated
    public void setParams(HttpParams params) {
        // HttpParams is deprecated in httpclient4 and doesn't exist in httpclient5
        // No-op for compatibility
    }

    /**
     * Internal adapter to convert httpclient5 header iterator to httpclient4 header iterator
     */
    private static class HeaderIteratorAdapter implements HeaderIterator {
        private final Iterator<org.apache.hc.core5.http.Header> iterator5;
        private org.apache.hc.core5.http.Header current;

        public HeaderIteratorAdapter(Iterator<org.apache.hc.core5.http.Header> iterator5) {
            this.iterator5 = iterator5;
        }

        @Override
        public boolean hasNext() {
            return iterator5.hasNext();
        }

        @Override
        public Header next() {
            current = iterator5.next();
            return new BasicHeader(current.getName(), current.getValue());
        }

        @Override
        public Header nextHeader() {
            return next();
        }

        @Override
        public void remove() {
            iterator5.remove();
        }
    }
}
