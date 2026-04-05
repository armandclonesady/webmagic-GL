package us.codecraft.webmagic.thread;

import org.junit.Test;
import us.codecraft.webmagic.*;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

public class SpiderRunnableTest {

    private static final String testRequest = "http://www.oschina.net/";

    private Spider buildSpider(PageProcessor processor) {
        return Spider.create(processor).setDownloader(new Downloader() {
            @Override
            public Page download(Request request, Task task) {
                return new Page().setRawText("");
            }

            @Override
            public void setThread(int threadNum) {
            }
        });
    }

    @Test
    public void testCallsOnSuccessNoError() {
        AtomicBoolean successCalled = new AtomicBoolean(false); // obligé d'utiliser AtomicBoolean pour pouvoir changer une variable locoale dans une classe anonyme
        Spider spider = buildSpider(new PageProcessor() {
            @Override
            public void process(Page page) {
                page.setSkip(true);
            }
            @Override
            public Site getSite() {
                return Site.me();
            }
        });
        spider.setSpiderListeners(List.of(new SpiderListener() {
            @Override
            public void onSuccess(Request request) {
                successCalled.set(true);
            }

            @Override
            public void onError(Request request, Exception e) {
                // ne devrait pas être appelé dans ce test
                throw new RuntimeException("onError should not be called");
            }
        }));

        new SpiderRunnable(spider, new Request(testRequest)).run();

        assertThat(successCalled.get()).isTrue();
    }

    @Test
    public void testCallsOnError() {
        AtomicBoolean errorCalled = new AtomicBoolean(false);
        Spider spider = buildSpider(new PageProcessor() {
            @Override
            public void process(Page page) {
                throw new RuntimeException("test exception");
            }
            @Override
            public Site getSite() {
                return Site.me();
            }
        });
        spider.setSpiderListeners(List.of(new SpiderListener() {
            @Override
            public void onSuccess(Request request) {
                // ne devrait pas être appelé dans ce test
                throw new RuntimeException("onSuccess should not be called");
            }

            @Override
            public void onError(Request request, Exception e) {
                errorCalled.set(true);
            }
        }));

        new SpiderRunnable(spider, new Request(testRequest)).run();

        assertThat(errorCalled.get()).isTrue();
    }
}
