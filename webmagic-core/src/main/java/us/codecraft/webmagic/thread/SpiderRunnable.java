package us.codecraft.webmagic.thread;

import org.apache.commons.collections4.CollectionUtils;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.SpiderListener;

import java.util.List;

public class SpiderRunnable implements Runnable {

    Spider spider;
    Request request;

    public SpiderRunnable(Spider spider, Request request) {
        super();
        this.spider = spider;
        this.request = request;

    }

    @Override
    public void run() {
        try {
            spider.processRequest(request);
            onSuccess(request);
        } catch (Exception e) {
            onError(request, e);
            spider.getLogger().error("process request {} error", request, e);
        } finally {
            spider.getPageCount().incrementAndGet();
            this.spider.getSpiderScheduler().signalNewUrl();
        }
    }

    protected void onSuccess(Request request) {
        List<SpiderListener> spiderListeners = spider.getSpiderListeners();
        if (CollectionUtils.isNotEmpty(spiderListeners)) {
            for (SpiderListener spiderListener : spiderListeners) {
                spiderListener.onSuccess(request);
            }
        }
    }

    protected void onError(Request request, Exception e) {
        List<SpiderListener> spiderListeners = spider.getSpiderListeners();
        if (CollectionUtils.isNotEmpty(spiderListeners)) {
            for (SpiderListener spiderListener : spiderListeners) {
                spiderListener.onError(request, e);
            }
        }
    }
}
