package cl.duoc.gamerevents.controller;

import java.security.Principal;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import cl.duoc.gamerevents.model.*;
import cl.duoc.gamerevents.service.*;

@RestController
@RequestMapping("/api")
public class EventCommunityApiController {
    private final EventPublicationService publication;
    private final EventMediaService media;
    private final EventShareService shares;
    public EventCommunityApiController(EventPublicationService publication,EventMediaService media,EventShareService shares) {
        this.publication=publication; this.media=media; this.shares=shares;
    }
    @PostMapping("/events") @ResponseStatus(HttpStatus.CREATED)
    public EventDetail publish(Principal principal,@Valid @RequestBody EventPublicationForm form) {
        return EventDetail.from(publication.publish(principal.getName(),form));
    }
    @PostMapping(value="/events/{id}/media",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MediaSummary upload(@PathVariable long id,Principal principal,@RequestParam MultipartFile file) {
        return media.upload(id,principal.getName(),file);
    }
    @GetMapping("/events/{id}/media") public List<MediaSummary> media(@PathVariable long id) { return media.list(id); }
    @GetMapping("/events/{id}/media/{mediaId}")
    public ResponseEntity<byte[]> content(@PathVariable long id,@PathVariable long mediaId,
            @RequestHeader(value="Range",required=false) String range) {
        return MediaResponses.content(media.content(id,mediaId),range);
    }
    @PostMapping("/events/{id}/shares") public ShareResponse share(@PathVariable long id,Principal principal) {
        return shares.share(id,principal.getName());
    }
    @GetMapping("/community") public CommunityPage community(@RequestParam(defaultValue="0") int page) {
        return shares.community(page);
    }
}
