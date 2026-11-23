package cl.duoc.gamerevents.model;

import java.time.LocalDateTime;

public record ShareResponse(long eventId,String title,String author,LocalDateTime createdAt,String url) { }
