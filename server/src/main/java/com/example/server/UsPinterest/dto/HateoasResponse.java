package com.example.server.UsPinterest.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Base response class with HATEOAS support.
 * Implements Hypermedia as the Engine of Application State (HATEOAS)
 * for improved REST API design.
 *
 * @param <T> Type of the data payload
 */
public class HateoasResponse<T> {
    private T data;
    private List<Link> links = new ArrayList<>();
    private Meta meta;

    public HateoasResponse() {
        this.meta = new Meta();
    }

    public HateoasResponse(T data) {
        this.data = data;
        this.meta = new Meta();
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public HateoasResponse<T> addLink(String rel, String href, String method) {
        links.add(new Link(rel, href, method));
        return this;
    }

    public HateoasResponse<T> addSelfLink(String href) {
        return addLink("self", href, "GET");
    }

    public HateoasResponse<T> addCreateLink(String href) {
        return addLink("create", href, "POST");
    }

    public HateoasResponse<T> addUpdateLink(String href) {
        return addLink("update", href, "PUT");
    }

    public HateoasResponse<T> addDeleteLink(String href) {
        return addLink("delete", href, "DELETE");
    }

    public HateoasResponse<T> setMessage(String message) {
        this.meta.message = message;
        return this;
    }

    /**
     * HATEOAS Link representation
     */
    public static class Link {
        private String rel;
        private String href;
        private String method;

        public Link() {
        }

        public Link(String rel, String href, String method) {
            this.rel = rel;
            this.href = href;
            this.method = method;
        }

        public String getRel() {
            return rel;
        }

        public void setRel(String rel) {
            this.rel = rel;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }
    }

    /**
     * Metadata for the response
     */
    public static class Meta {
        private LocalDateTime timestamp;
        private String message;
        private String version;

        public Meta() {
            this.timestamp = LocalDateTime.now();
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}