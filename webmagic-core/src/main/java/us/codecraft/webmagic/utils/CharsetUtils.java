package us.codecraft.webmagic.utils;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author code4crafter@gmail.com
 *         Date: 17/3/11
 *         Time: 10:36
 * @since 0.6.2
 */
public abstract class CharsetUtils {

    private static Logger logger = LoggerFactory.getLogger(CharsetUtils.class);

    private CharsetUtils() {
        throw new AssertionError("No us.codecraft.webmagic.utils.CharsetUtils instances for you!");
    }

    public static String detectCharset(String contentType, byte[] contentBytes) throws IOException {
        String charset;
        // charset
        // 1、encoding in http header Content-Type
        charset = getCharsetFromContentType(contentType);
        if (StringUtils.isEmpty(charset)) {
            // use default charset to decode first time
            charset = Charset.defaultCharset().name();
            String content = new String(contentBytes, charset);
            // 2、charset in meta
            charset = getCharsetFromMeta(content);
            // 3、todo use tools as cpdetector for content decode
        }
        logger.debug("Auto get charset: {}", charset);
        return charset;
    }

    private static String getCharsetFromContentType(String contentType) {
        String charset = UrlUtils.getCharset(contentType);
        if (StringUtils.isNotBlank(contentType) && StringUtils.isNotBlank(charset)) {
            return charset;
        }
        return null;
    }

    private static String getCharsetFromMeta(String content) {
        if (StringUtils.isNotEmpty(content)) {
            Document document = Jsoup.parse(content);
            Elements links = document.select("meta");
            for (Element link : links) {
                // 2.1、html4.01 <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                String metaContent = link.attr("content");
                String metaCharset = link.attr("charset");
                if (metaContent.contains("charset")) {
                    metaContent = metaContent.substring(metaContent.indexOf("charset"), metaContent.length());
                    return metaContent.split("=")[1];
                }
                // 2.2、html5 <meta charset="UTF-8" />
                else if (StringUtils.isNotEmpty(metaCharset)) {
                    return metaCharset;
                }
            }
        }
        return null;
    }
    
}
