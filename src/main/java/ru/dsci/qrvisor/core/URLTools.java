package ru.dsci.qrvisor.core;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class URLTools {

    private final static int BUFFER_SIZE = 1024;
    private final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final static int PAUSE = 1000;

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject(readDataFromUrl(url));
        return jsonObject.getJSONObject("result");
    }

    public static String readDataFromUrl(String url) throws IOException, JSONException {
        try (ReadableByteChannel channel = Channels.newChannel(new URL(url).openStream());) {
            ByteBuffer buff = ByteBuffer.allocate(BUFFER_SIZE);
            channel.read(buff);
            String data = new String(buff.array(), DEFAULT_CHARSET);
            return data;
        } catch (RuntimeException e) {
            throw new IOException(String.format("Error reading resource '%s': %s", url, e.getMessage()));
        }
    }

    public static String getUrlContent(String url) {
        SessionFactory factory = new Launcher().launch();
        String content = null;
        try (Session session = factory.create()) {
            session.navigate(url);
            session.waitDocumentReady();
            session.wait(PAUSE);
            content = session.getContent();
        } catch (RuntimeException e) {
            log.error(String.format("Error retrieving content from URL: %s", url));
        }
        factory.close();
        return content;
    }
}
