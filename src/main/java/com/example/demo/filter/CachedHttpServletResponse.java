package com.example.demo.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Wrapper for HttpServletResponse to capture response body
 * Used by IdempotencyFilter to cache responses for replay
 */
public class CachedHttpServletResponse extends HttpServletResponseWrapper {
    
    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final ServletOutputStream outputStream = new CachedServletOutputStream(byteArrayOutputStream);
    private PrintWriter writer;

    public CachedHttpServletResponse(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(byteArrayOutputStream, true);
        }
        return writer;
    }

    public byte[] getCapturedData() {
        if (writer != null) {
            writer.flush();
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Wrapper for ServletOutputStream to capture data
     */
    private static class CachedServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream byteArrayOutputStream;

        public CachedServletOutputStream(ByteArrayOutputStream byteArrayOutputStream) {
            this.byteArrayOutputStream = byteArrayOutputStream;
        }

        @Override
        public void write(int b) throws IOException {
            byteArrayOutputStream.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            byteArrayOutputStream.write(b, off, len);
        }

        @Override
        public void write(byte[] b) throws IOException {
            byteArrayOutputStream.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener listener) {
            // Not used in this context
        }
    }
}
