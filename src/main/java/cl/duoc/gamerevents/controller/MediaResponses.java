package cl.duoc.gamerevents.controller;

import org.springframework.http.*;
import cl.duoc.gamerevents.model.EventMedia;

/** Contenido privado con soporte de Range para reproducir MP4 en el navegador. */
final class MediaResponses {
    private MediaResponses() { }
    static ResponseEntity<byte[]> content(EventMedia media, String range) {
        byte[] bytes=media.getContent();
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(media.getContentType()));
        headers.setCacheControl("private, no-store");
        headers.set("X-Content-Type-Options","nosniff");
        headers.set("Content-Security-Policy","default-src 'none'; sandbox");
        headers.set("Accept-Ranges","bytes");
        if(range!=null) {
            try {
                var ranges=HttpRange.parseRanges(range);
                if(ranges.size()!=1) throw new IllegalArgumentException();
                int start=Math.toIntExact(ranges.get(0).getRangeStart(bytes.length));
                int end=Math.toIntExact(ranges.get(0).getRangeEnd(bytes.length));
                if(start<0 || start>=bytes.length || end<start) throw new IllegalArgumentException();
                byte[] partial=java.util.Arrays.copyOfRange(bytes,start,end+1);
                headers.set("Content-Range","bytes "+start+"-"+end+"/"+bytes.length);
                headers.setContentLength(partial.length);
                return new ResponseEntity<>(partial,headers,HttpStatus.PARTIAL_CONTENT);
            } catch(IllegalArgumentException|ArithmeticException exception) {
                headers.set("Content-Range","bytes */"+bytes.length);
                return new ResponseEntity<>(null,headers,HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
            }
        }
        headers.setContentLength(bytes.length);
        return new ResponseEntity<>(bytes,headers,HttpStatus.OK);
    }
}
