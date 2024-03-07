package spe.projectportfolio.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import spe.projectportfolio.backend.pojo.enums.ImageType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    private Long imageId;
    private byte[] data;
    private ImageType type;
}
