package cl.duoc.gamerevents.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import cl.duoc.gamerevents.model.*;
import cl.duoc.gamerevents.repository.EventRepository;

@Service
@Transactional(readOnly=true)
public class EventPublicationService {
    private final EventService events;
    private final EventRepository repository;
    private final UserService users;
    public EventPublicationService(EventService events, EventRepository repository, UserService users) {
        this.events=events; this.repository=repository; this.users=users;
    }
    @Transactional
    public GamerEvent publish(String email, EventPublicationForm form) {
        GamerUser owner=users.findByEmail(email);
        String extra="Sin información adicional.";
        GamerEvent event = form.modality()==Modality.ONLINE
                ? GamerEvent.online(form.title(), form.category(), form.game(), form.country(), form.accessUrl(),
                    form.startsAt(), form.description(), owner.getName(), extra, extra, extra, extra,
                    "/images/events/nexus-arena.svg",false)
                : GamerEvent.presencial(form.title(), form.category(), form.game(), form.country(), form.city(),form.address(),
                    form.startsAt(), form.description(), owner.getName(), extra, extra, extra, extra,
                    "/images/events/nexus-arena.svg",false);
        event.assignOwner(owner);
        return repository.save(event);
    }
    public GamerEvent requireOwner(long id, String email) {
        GamerEvent event=events.detail(id);
        if (event.getOwner()==null || !event.getOwner().getId().equals(users.findByEmail(email).getId()))
            throw new AccessDeniedException("Solo el propietario puede realizar esta acción.");
        return event;
    }
    public boolean owns(long id, String email) {
        GamerEvent event=events.detail(id);
        return email != null && event.getOwner()!=null && event.getOwner().getEmail().equals(UserService.normalize(email));
    }
}
