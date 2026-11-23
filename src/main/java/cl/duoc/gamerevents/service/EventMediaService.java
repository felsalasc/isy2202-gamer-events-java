package cl.duoc.gamerevents.service;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import cl.duoc.gamerevents.model.*;
import cl.duoc.gamerevents.repository.EventMediaRepository;

@Service
@Transactional(readOnly=true)
public class EventMediaService {
    private final EventMediaRepository media;
    private final EventPublicationService publication;
    private final EventService events;
    private final MediaValidator validator;
    private final EntityManager entityManager;
    public EventMediaService(EventMediaRepository media,EventPublicationService publication,EventService events,
            MediaValidator validator,EntityManager entityManager) {
        this.media=media; this.publication=publication; this.events=events; this.validator=validator; this.entityManager=entityManager;
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MediaSummary upload(long eventId,String email,MultipartFile file) {
        GamerEvent event=publication.requireOwner(eventId,email);
        var valid=validator.validate(file);
        entityManager.lock(event,LockModeType.PESSIMISTIC_WRITE);
        if (media.countByEventId(eventId)>=20) throw new ResponseStatusException(HttpStatus.CONFLICT,"El evento admite hasta 20 archivos.");
        EventMedia saved=media.save(new EventMedia(event,valid.contentType(),valid.bytes()));
        return new MediaSummary(saved.getId(),saved.getContentType());
    }
    public List<MediaSummary> list(long eventId) { events.detail(eventId); return media.summaries(eventId); }
    public EventMedia content(long eventId,long id) {
        events.detail(eventId);
        return media.findByIdAndEventId(id,eventId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
