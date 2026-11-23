package cl.duoc.gamerevents.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.web.server.ResponseStatusException;
import cl.duoc.gamerevents.model.*;
import cl.duoc.gamerevents.repository.EventShareRepository;

@Service
@Transactional(readOnly=true)
public class EventShareService {
    private final EventShareRepository shares;
    private final EventPublicationService publication;
    private final EntityManager entityManager;
    private final String baseUrl;
    public EventShareService(EventShareRepository shares,EventPublicationService publication,EntityManager entityManager,
            @Value("${app.public-base-url:http://localhost}") String baseUrl) {
        URI uri=URI.create(baseUrl);
        if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) || uri.getHost()==null
                || uri.getUserInfo()!=null || uri.getQuery()!=null || uri.getFragment()!=null
                || !(uri.getPath().isEmpty() || "/".equals(uri.getPath())))
            throw new IllegalArgumentException("PUBLIC_BASE_URL debe ser el origen público del sitio.");
        this.shares=shares; this.publication=publication; this.entityManager=entityManager;
        this.baseUrl=baseUrl.replaceAll("/+$","");
    }
    public String url(long id) { return baseUrl+"/eventos/"+id; }
    public String encodedUrl(long id) { return URLEncoder.encode(url(id),StandardCharsets.UTF_8); }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ShareResponse share(long eventId,String email) {
        GamerEvent event=publication.requireOwner(eventId,email);
        entityManager.lock(event,LockModeType.PESSIMISTIC_WRITE);
        EventShare saved=shares.findByEventId(eventId).orElseGet(()->shares.save(new EventShare(event)));
        return response(saved);
    }
    public CommunityPage community(int page) {
        if(page<0 || page>100000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        var result=shares.findAll(PageRequest.of(page,20,Sort.by(Sort.Direction.DESC,"createdAt","id")));
        return new CommunityPage(result.stream().map(this::response).toList(),page,result.hasNext());
    }
    private ShareResponse response(EventShare share) {
        GamerEvent event=share.getEvent();
        GamerUser user=event.getOwner();
        String author=user.getNickname().isBlank()?user.getName():user.getNickname();
        return new ShareResponse(event.getId(),event.getTitle(),author,share.getCreatedAt(),url(event.getId()));
    }
}
