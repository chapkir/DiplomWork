package com.example.server.UsPinterest.util;

import com.example.server.UsPinterest.dto.HateoasResponse;
import com.example.server.UsPinterest.dto.CursorPageResponse;
import com.example.server.UsPinterest.dto.PinResponse;
import org.springframework.stereotype.Component;

@Component
public class HateoasUtil {

    public HateoasResponse<PinResponse> buildPinDetailResponse(PinResponse pinResponse) {
        HateoasResponse<PinResponse> response = new HateoasResponse<>(pinResponse);
        Long id = pinResponse.getId();
        response.addSelfLink("/api/pins/detail/" + id);
        response.addLink("all-pins", "/api/pins", "GET");
        response.addUpdateLink("/api/pins/detail/" + id);
        response.addDeleteLink("/api/pins/detail/" + id);
        response.addLink("comments", "/api/pins/detail/" + id + "/comments", "GET");
        response.addLink("likes", "/api/pins/detail/" + id + "/likes", "GET");
        return response;
    }

    public <T> HateoasResponse<CursorPageResponse<T, String>> buildCursorPageResponse(CursorPageResponse<T, String> pageResponse, String cursor, int size) {
        HateoasResponse<CursorPageResponse<T, String>> response = new HateoasResponse<>(pageResponse);
        StringBuilder href = new StringBuilder("/api/pins");
        if (cursor != null && !cursor.isEmpty()) {
            href.append("?cursor=").append(cursor).append("&size=").append(size);
        } else {
            href.append("?size=").append(size);
        }
        response.addSelfLink(href.toString());
        response.addLink("create", "/api/pins", "POST");
        return response;
    }
}