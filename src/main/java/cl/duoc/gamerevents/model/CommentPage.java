package cl.duoc.gamerevents.model;

import java.util.List;

public record CommentPage(List<CommentResponse> items, int page, long total, boolean hasNext) { }
