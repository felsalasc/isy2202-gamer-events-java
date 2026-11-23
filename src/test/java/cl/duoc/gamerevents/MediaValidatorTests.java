package cl.duoc.gamerevents;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import cl.duoc.gamerevents.service.MediaValidator;
import static org.assertj.core.api.Assertions.*;

class MediaValidatorTests {
    private final MediaValidator validator=new MediaValidator();
    private byte[] box(String type,byte[] payload) {
        return ByteBuffer.allocate(8+payload.length).putInt(8+payload.length).put(type.getBytes(java.nio.charset.StandardCharsets.US_ASCII)).put(payload).array();
    }
    @Test void fakeMovieBoxesAreNotAPlayableVideo() {
        byte[] ftyp=box("ftyp",new byte[]{'i','s','o','m',0,0,0,0});
        byte[] moov=box("moov",new byte[]{1});
        byte[] mdat=box("mdat",new byte[]{2});
        byte[] bytes=ByteBuffer.allocate(ftyp.length+moov.length+mdat.length).put(ftyp).put(moov).put(mdat).array();
        assertThatThrownBy(()->validator.validate(new MockMultipartFile("file","fake.mp4","video/mp4",bytes)))
                .isInstanceOf(ResponseStatusException.class);
    }
    @Test void extendedFtypWithoutItsPayloadReturnsValidationError() {
        byte[] bytes=ByteBuffer.allocate(16).putInt(1).put(new byte[]{'f','t','y','p'}).putLong(16).array();
        assertThatThrownBy(()->validator.validate(new MockMultipartFile("file","broken.mp4","video/mp4",bytes)))
                .isInstanceOf(ResponseStatusException.class);
    }
}
