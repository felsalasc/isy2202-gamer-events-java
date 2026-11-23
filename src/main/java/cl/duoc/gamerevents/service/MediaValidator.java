package cl.duoc.gamerevents.service;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/** Valida el contenido, sin confiar en la extensión o el nombre suministrado. */
@Component
public class MediaValidator {
    public record Validated(String contentType,byte[] bytes) { }
    public Validated validate(MultipartFile file) {
        if (file==null || file.isEmpty()) throw invalid();
        String type=file.getContentType();
        boolean video="video/mp4".equals(type);
        if (!video && !"image/png".equals(type) && !"image/jpeg".equals(type)) throw invalid();
        long limit=video ? 20L*1024*1024 : 5L*1024*1024;
        if (file.getSize()>limit) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE);
        try {
            byte[] bytes=file.getBytes();
            if (video) { validateMp4(bytes); return new Validated(type,bytes); }
            return new Validated(type,cleanImage(bytes,type));
        } catch (IOException exception) { throw invalid(); }
    }
    private byte[] cleanImage(byte[] bytes,String type) throws IOException {
        try (ImageInputStream input=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers=ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw invalid();
            ImageReader reader=readers.next();
            try {
                reader.setInput(input);
                String format=reader.getFormatName();
                if (!("image/png".equals(type) && "png".equalsIgnoreCase(format))
                        && !("image/jpeg".equals(type) && "jpeg".equalsIgnoreCase(format))) throw invalid();
                int width=reader.getWidth(0),height=reader.getHeight(0);
                if (width<1 || height<1 || width>8000 || height>8000 || (long)width*height>16000000) throw invalid();
                BufferedImage image=reader.read(0);
                var output=new ByteArrayOutputStream();
                if (!ImageIO.write(image,"image/png".equals(type)?"png":"jpeg",output)) throw invalid();
                if (output.size()>5*1024*1024) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE);
                return output.toByteArray();
            } finally { reader.dispose(); }
        }
    }
    private void validateMp4(byte[] bytes) {
        // Solo contenedores ISO BMFF completos; rechaza cajas truncadas y marcas ajenas a MP4.
        int offset=0; boolean ftyp=false,moov=false,mdat=false;
        while (offset<bytes.length) {
            if (bytes.length-offset<8) throw invalid();
            long size=Integer.toUnsignedLong(ByteBuffer.wrap(bytes,offset,4).getInt());
            String box=new String(bytes,offset+4,4,StandardCharsets.US_ASCII);
            int header=8;
            if (size==1) {
                if (bytes.length-offset<16) throw invalid();
                size=ByteBuffer.wrap(bytes,offset+8,8).getLong(); header=16;
            }
            if (size==0) size=bytes.length-offset;
            if (size<header || size>bytes.length-offset) throw invalid();
            if (offset==0 && !"ftyp".equals(box)) throw invalid();
            if ("ftyp".equals(box)) {
                if (size<header+8) throw invalid();
                String brand=new String(bytes,offset+header,4,StandardCharsets.US_ASCII);
                if (!Set.of("isom","iso2","mp41","mp42","avc1","M4V ").contains(brand)) throw invalid();
                ftyp=true;
            }
            if ("moov".equals(box)) moov=hasVideoTrack(bytes,offset+header,offset+(int)size);
            if ("mdat".equals(box)) mdat=size>header;
            offset+=(int)size;
        }
        if (!ftyp || !moov || !mdat) throw invalid();
    }
    private record Box(String type,int payload,int end) { }
    private java.util.List<Box> boxes(byte[] bytes,int start,int end) {
        var result=new java.util.ArrayList<Box>();
        while(start<end) {
            if(end-start<8) throw invalid();
            long size=Integer.toUnsignedLong(ByteBuffer.wrap(bytes,start,4).getInt());
            String type=new String(bytes,start+4,4,StandardCharsets.US_ASCII);
            int header=8;
            if(size==1) {
                if(end-start<16) throw invalid();
                size=ByteBuffer.wrap(bytes,start+8,8).getLong();header=16;
            }
            if(size==0) size=end-start;
            if(size<header || size>end-start) throw invalid();
            result.add(new Box(type,start+header,start+(int)size));
            start+=(int)size;
        }
        return result;
    }
    private boolean hasVideoTrack(byte[] bytes,int start,int end) {
        for(Box track:boxes(bytes,start,end)) {
            if(!"trak".equals(track.type())) continue;
            boolean dimensions=false,video=false,samples=false;
            for(Box child:boxes(bytes,track.payload(),track.end())) {
                if("tkhd".equals(child.type())) {
                    if(child.end()-child.payload()<84) throw invalid();
                    dimensions=ByteBuffer.wrap(bytes,child.end()-8,4).getInt()>0
                            && ByteBuffer.wrap(bytes,child.end()-4,4).getInt()>0;
                }
                if(!"mdia".equals(child.type())) continue;
                for(Box mdia:boxes(bytes,child.payload(),child.end())) {
                    if("hdlr".equals(mdia.type())) {
                        if(mdia.end()-mdia.payload()<12) throw invalid();
                        video="vide".equals(new String(bytes,mdia.payload()+8,4,StandardCharsets.US_ASCII));
                    }
                    if("minf".equals(mdia.type())) {
                        for(Box minf:boxes(bytes,mdia.payload(),mdia.end())) {
                            if("stbl".equals(minf.type())) samples=hasVideoSamples(bytes,minf);
                        }
                    }
                }
            }
            if(dimensions && video && samples) return true;
        }
        return false;
    }
    private boolean hasVideoSamples(byte[] bytes,Box table) {
        boolean codec=false,samples=false;
        for(Box box:boxes(bytes,table.payload(),table.end())) {
            if("stsd".equals(box.type())) {
                if(box.end()-box.payload()<8) throw invalid();
                for(Box entry:boxes(bytes,box.payload()+8,box.end())) {
                    if(Set.of("avc1","avc3").contains(entry.type())) {
                        if(entry.end()-entry.payload()<78) throw invalid();
                        for(Box config:boxes(bytes,entry.payload()+78,entry.end()))
                            if("avcC".equals(config.type()) && config.end()-config.payload()>=7) codec=true;
                    }
                }
            }
            if("stsz".equals(box.type())) {
                if(box.end()-box.payload()<12) throw invalid();
                int count=ByteBuffer.wrap(bytes,box.payload()+8,4).getInt();
                samples=count>0;
            }
        }
        return codec && samples;
    }
    private ResponseStatusException invalid() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,"Usa una foto PNG/JPEG válida o un video MP4 válido.");
    }
}
