package cl.duoc.gamerevents.controller;

import java.security.Principal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import cl.duoc.gamerevents.model.*;
import cl.duoc.gamerevents.service.*;

@Controller
public class EventCommunityController {
    private final EventPublicationService publication;
    private final EventMediaService media;
    private final EventShareService shares;
    public EventCommunityController(EventPublicationService publication,EventMediaService media,EventShareService shares) {
        this.publication=publication; this.media=media; this.shares=shares;
    }
    @GetMapping("/eventos/nuevo") public String create(Model model) {
        model.addAttribute("eventForm",EventPublicationForm.empty()); return "event-new";
    }
    @PostMapping("/eventos/nuevo") public String publish(Principal principal,
            @Valid @ModelAttribute("eventForm") EventPublicationForm form,BindingResult errors) {
        if(errors.hasErrors()) return "event-new";
        return "redirect:/eventos/"+publication.publish(principal.getName(),form).getId();
    }
    @PostMapping("/eventos/{id}/multimedia") public String upload(@PathVariable long id,Principal principal,
            @RequestParam MultipartFile file,RedirectAttributes redirect) {
        try { media.upload(id,principal.getName(),file); redirect.addFlashAttribute("mediaMessage","Archivo agregado."); }
        catch(ResponseStatusException exception) {
            if(exception.getStatusCode().value()!=400 && exception.getStatusCode().value()!=413 && exception.getStatusCode().value()!=409) throw exception;
            redirect.addFlashAttribute("mediaError","No se pudo agregar el archivo. Usa PNG/JPEG de hasta 5 MB o MP4 de hasta 20 MB; máximo 20 archivos por evento.");
        }
        return "redirect:/eventos/"+id+"#multimedia";
    }
    @GetMapping("/eventos/{id}/multimedia/{mediaId}") public ResponseEntity<byte[]> content(@PathVariable long id,
            @PathVariable long mediaId,@RequestHeader(value="Range",required=false) String range) {
        return MediaResponses.content(media.content(id,mediaId),range);
    }
    @PostMapping("/eventos/{id}/compartir") public String share(@PathVariable long id,Principal principal) {
        shares.share(id,principal.getName()); return "redirect:/comunidad";
    }
    @GetMapping("/comunidad") public String community(@RequestParam(defaultValue="0") int page,Model model) {
        model.addAttribute("community",shares.community(page)); return "community";
    }
}
