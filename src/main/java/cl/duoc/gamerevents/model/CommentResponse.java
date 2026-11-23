package cl.duoc.gamerevents.model;

import java.time.LocalDateTime;

public record CommentResponse(long id, String author, String text, LocalDateTime createdAt) {
    public static CommentResponse from(EventComment comment) {
        GamerUser user = comment.getUser();
        String author = user.getNickname().isBlank() ? user.getName() : user.getNickname();
        return new CommentResponse(comment.getId(), author, comment.getText(), comment.getCreatedAt());
    }
}
