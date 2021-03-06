package com.alibaba.otter.shared.common.utils.cmd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.commons.io.IOUtils;

public class StreamAppender {
    private Thread      errWriter;
    private Thread      outWriter;
    private PrintWriter output;

    public StreamAppender(OutputStream output) {
        this.output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(output)));
    }

    public void writeInput(final InputStream err, final InputStream out) {
        errWriter = new Thread() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(err));

            public void run() {
                try {
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        output.println(line);
                    }
                } catch (IOException e) {
                    //ignore
                } finally {
                    output.flush();// 关闭之前flush一下
                    IOUtils.closeQuietly(reader);
                }
            }
        };
        outWriter = new Thread() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(out));

            public void run() {
                try {
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        output.println(line);
                    }
                } catch (IOException e) {
                    //ignore
                } finally {
                    output.flush();// 关闭之前flush一下
                    IOUtils.closeQuietly(reader);
                }
            }
        };
        errWriter.setDaemon(true);
        outWriter.setDaemon(true);
        errWriter.start();
        outWriter.start();
    }

    public void finish() throws Exception {
        outWriter.join();
        errWriter.join();
    }
}
