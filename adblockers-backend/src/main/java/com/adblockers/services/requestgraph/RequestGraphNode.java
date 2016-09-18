package com.adblockers.services.requestgraph;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public class RequestGraphNode<T> {

    private T content;
    private Boolean isFirstParty;

    public RequestGraphNode(T content, Boolean isFirstParty) {
        this.content = content;
        this.isFirstParty = isFirstParty;
    }

    public T getContent() {
        return content;
    }
    public Boolean getIsFirstParty() {
        return isFirstParty;
    }
}
