package spe.projectportfolio.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spe.projectportfolio.backend.mapper.ImageMapper;
import spe.projectportfolio.backend.pojo.Image;
import spe.projectportfolio.backend.pojo.User;
import spe.projectportfolio.backend.pojo.enums.ImageType;

import java.io.IOException;
import java.util.Objects;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ImageController {
    private final ImageMapper mapper;

    @GetMapping(path = "/images/{id}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> getImageById(@PathVariable Long id) throws IOException {
        // get requested image
        Image foundImage = mapper.findById(id);

        // if image cannot be found, return status code 404
        if (foundImage == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // get image data
        byte[] imageData = foundImage.getData();

        // get image type as a MediaType
        MediaType mediaType = getMediaTypeFromImageType(foundImage.getType());

        // return response entity
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(mediaType)
                .body(imageData);
    }

    @PostMapping(path = "/images", consumes = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<Long> createImage(@RequestBody byte[] imageData, @RequestHeader("Content-type") String contentType) {
        Image image = new Image(null, imageData, getImageTypeFromMediaType(contentType));
        try {
            mapper.insert(image);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(image.getImageId());
    }

    @PutMapping(path = "/images/{id}", consumes = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<Void> updateImage(@PathVariable Long id, @RequestBody byte[] imageData, @RequestHeader("Content-type") String contentType) {
        ResponseEntity<Void> badRequest = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        Image image = new Image(id, imageData, getImageTypeFromMediaType(contentType));
        try {
            if (mapper.findById(id) == null) {
                return badRequest;
            }
            mapper.update(image);
        } catch (Exception e) {
            return badRequest;
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping(path = "/images/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        // check if image exists
        if (mapper.findById(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // delete image
        mapper.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // convert ImageType to MediaType
    public static MediaType getMediaTypeFromImageType(ImageType imageType) {
        MediaType mediaType;
        if (imageType == ImageType.JPEG) {
            mediaType = MediaType.IMAGE_JPEG;
        } else {
            mediaType = MediaType.IMAGE_PNG;
        }
        return mediaType;
    }

    // convert MediaType string to ImageType
    public static ImageType getImageTypeFromMediaType(String mediaType) {
        if (Objects.equals(mediaType, "image/jpeg") || Objects.equals(mediaType, "image/jpeg;charset=UTF-8")) {
            return ImageType.JPEG;
        } else {
            return ImageType.PNG;
        }
    }
}
