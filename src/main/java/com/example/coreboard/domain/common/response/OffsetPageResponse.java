package com.example.coreboard.domain.common.response;

import java.util.List;

public class OffsetPageResponse<T> {
    private final List<T> content;
    private final PageInfo pageInfo;

    public OffsetPageResponse(
            List<T> content,
            PageInfo pageInfo
    ) {
        this.content = content;
        this.pageInfo = pageInfo;
    }

    public List<T> getContent() {
        return content;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }
}
/* {
  "content": [
    {
      "userId": 1,
      "username": "admin01",
      "role": "ADMIN"
    }
  ],
  "pageInfo": {
    "page": 0,
    "size": 20,
    "totalElements": 53,
    "totalPages": 3
  }
}
*/