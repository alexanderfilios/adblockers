package com.adblockers.httprequestrecord;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
public class HttpRequestRecordDTO {
    private String source;
    private String target;
    private Long timestamp;
    private String contentType;
    private String cookie;
    private String sourceVisited;
    private Boolean secure;
    private String sourcePathDepth;
    private String sourceQueryDepth;
    private String sourceSub;
    private String targetSub;
    private String method;
    private String status;
    private Boolean cacheable;
    private Boolean fromPrivateMode;

    public HttpRequestRecord toHttpRequestRecord() {
        return new HttpRequestRecord(source, target);
    }

    public HttpRequestRecordDTO() {}

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getSourceVisited() {
        return sourceVisited;
    }

    public void setSourceVisited(String sourceVisited) {
        this.sourceVisited = sourceVisited;
    }

    public Boolean getSecure() {
        return secure;
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    public String getSourcePathDepth() {
        return sourcePathDepth;
    }

    public void setSourcePathDepth(String sourcePathDepth) {
        this.sourcePathDepth = sourcePathDepth;
    }

    public String getSourceQueryDepth() {
        return sourceQueryDepth;
    }

    public void setSourceQueryDepth(String sourceQueryDepth) {
        this.sourceQueryDepth = sourceQueryDepth;
    }

    public String getSourceSub() {
        return sourceSub;
    }

    public void setSourceSub(String sourceSub) {
        this.sourceSub = sourceSub;
    }

    public String getTargetSub() {
        return targetSub;
    }

    public void setTargetSub(String targetSub) {
        this.targetSub = targetSub;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getCacheable() {
        return cacheable;
    }

    public void setCacheable(Boolean cacheable) {
        this.cacheable = cacheable;
    }

    public Boolean getFromPrivateMode() {
        return fromPrivateMode;
    }

    public void setFromPrivateMode(Boolean fromPrivateMode) {
        this.fromPrivateMode = fromPrivateMode;
    }
}
