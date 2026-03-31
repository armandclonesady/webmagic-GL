package us.codecraft.webmagic.selector;

import us.codecraft.webmagic.utils.Experimental;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Borrowed from https://code.google.com/p/cx-extractor/
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.4.1
 *
 */
@Experimental
public class SmartContentSelector implements Selector {

    public static final int DEFAULT_THRESHOLD = 86;

    public static final int DEFAULT_BLOCKS_WIDTH = 3;

    public static final int DEFAULT_MIN_LENGTH = 5;

    private int threshold;

    private ArrayList<Integer> indexDistribution;

    public SmartContentSelector() {
        this.threshold = DEFAULT_THRESHOLD;
        this.indexDistribution = new ArrayList<>();
    }

    public SmartContentSelector(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public String select(String html) {
        String cleanHtml = cleanHtml(html);
        List<String> lines = Arrays.asList(cleanHtml.split("\n"));
        populateIndexDistribution(lines);

        int start = -1;
        int end = -1;
        boolean boolstart = false;
        boolean boolend = false;
        StringBuilder text = new StringBuilder();
        text.setLength(0);

        for (int i = 0; i < indexDistribution.size() - 1; i++) {
            if (indexIsAboveThreshold(i) && !boolstart && isStart(i)) {
                boolstart = true;
                start = i;
                continue;
            }
            if (boolstart && isEnd(i)) {
                end = i;
                boolend = true;
            }
            StringBuilder tmp = new StringBuilder();
            if (boolend) {
                for (int ii = start; ii <= end; ii++) {
                    if (lines.get(ii).length() < DEFAULT_MIN_LENGTH) continue;
                    tmp.append(lines.get(ii) + "\n");
                }
                String str = tmp.toString();
                if (str.contains("Copyright")   ) continue;
                text.append(str);
                boolstart = boolend = false;
            }
        }
        return text.toString();
    }

    private String cleanHtml(String html) {
        html = html.replaceAll("(?is)<!DOCTYPE[^>]*+>", "");
        html = html.replaceAll("(?is)<!--.*?-->", "");				// remove html comment
        html = html.replaceAll("(?is)<script.*?>.*?</script>", ""); // remove javascript
        html = html.replaceAll("(?is)<style.*?>.*?</style>", "");   // remove css
        html = html.replaceAll("&.{2,5};|&#.{2,5};", " ");			// remove special char
        return html;
    }

    private void populateIndexDistribution(List<String> lines) {
        this.indexDistribution.clear();
        for (int i = 0; i < lines.size() - DEFAULT_BLOCKS_WIDTH; i++) {
            int wordsNum = 0;
            for (int j = i; j < i + DEFAULT_BLOCKS_WIDTH; j++) {
                lines.set(j, lines.get(j).replaceAll("\\s+", ""));
                wordsNum += lines.get(j).length();
            }
            indexDistribution.add(wordsNum);
        }
    }

    private boolean isStart(int index) {
        return !indexIsZero(index + 1) && !indexIsZero(index + 2) && !indexIsZero(index + 3);
    }

    private boolean isEnd(int index) {
        return indexIsZero(index) || indexIsZero(index + 1);
    }

    private boolean indexIsAboveThreshold(int index) {
        return indexDistribution.get(index) > threshold;
    }

    private boolean indexIsZero(int index) {
        return indexDistribution.get(index) != 0;
    }

    @Override
    public List<String> selectList(String text) {
        throw new UnsupportedOperationException();
    }
}
