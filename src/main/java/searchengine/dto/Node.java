package searchengine.dto;

import lombok.Getter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Node {
    private final String url;
    private volatile Node parent;
    private volatile int depth;
    private final Set<Node> sublinks;

    public Node(String url) {
        this.url = url;
        parent = null;
        depth = 0;
        sublinks = ConcurrentHashMap.newKeySet();
    }

    private synchronized void setParent(Node node) {
        this.parent = node;
        this.depth = changeDepth();
    }

    private int changeDepth() {
        if (parent == null) {
            return 0;
        }
        return 1 + parent.getDepth();
    }

    public synchronized void addSublinks(Node link) {
        if (!sublinks.contains(link) && link.getUrl().startsWith(url)) {
            this.sublinks.add(link);
            link.setParent(this);
        }
    }

}
