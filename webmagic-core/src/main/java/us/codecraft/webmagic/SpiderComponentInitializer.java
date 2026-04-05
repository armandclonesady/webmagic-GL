package us.codecraft.webmagic;

import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.thread.CountableThreadPool;

public class SpiderComponentInitializer {

    private final Spider spider;

    public SpiderComponentInitializer(Spider spider) {
        this.spider = spider;
    }

    public void init() {
        initDownloader();
        initPipelines();
        initThreadPool();
        initRequests();
    }

    private void initDownloader(){
        if (spider.downloader == null) {
            spider.downloader = new HttpClientDownloader();
        }
        spider.downloader.setThread(spider.threadNum);
    }

    private void initPipelines(){
        if (spider.pipelines.isEmpty()) {
            spider.pipelines.add(new ConsolePipeline());
        }
    }

    private void initThreadPool(){
        if (spider.threadPool == null || spider.threadPool.isShutdown()) {
            if (spider.executorService != null && !spider.executorService.isShutdown()) {
                spider.threadPool = new CountableThreadPool(spider.threadNum, spider.executorService);
            } else {
                spider.threadPool = new CountableThreadPool(spider.threadNum);
            }
        }
    }
    private void initRequests(){
        if (spider.startRequests != null) {
            for (Request request : spider.startRequests) {
                spider.addRequest(request);
            }
            spider.startRequests.clear();
        }
    }

}
