package spe.projectportfolio.backend.service;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spe.projectportfolio.backend.mapper.CaseStudyMapper;
import spe.projectportfolio.backend.mapper.ImageMapper;
import spe.projectportfolio.backend.pojo.CaseStudy;
import spe.projectportfolio.backend.pojo.CaseStudyUpload;
import spe.projectportfolio.backend.pojo.Image;
import spe.projectportfolio.backend.pojo.enums.ImageType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CaseStudyService {
    private final CaseStudyMapper caseStudyMapper;
    private final ImageMapper imageMapper;

    @Value("${app.api-url}")
    private String url;

    public List<CaseStudy> getCaseStudiesByGlobalSearch(String search) {
        List<CaseStudy> caseStudies = caseStudyMapper.findByGlobalSearch(search);
        caseStudies.forEach(this::convertRichTextFieldImageIds);
        return caseStudies;
    }

    public List<CaseStudy> getCaseStudiesByCondition(String title, String clientName, String industry) {
        List<CaseStudy> caseStudies = caseStudyMapper.findByCondition(title, clientName, industry);
        caseStudies.forEach(this::convertRichTextFieldImageIds);
        return caseStudies;
    }

    public CaseStudy getCaseStudyById(Long id) {
        CaseStudy foundCaseStudy = caseStudyMapper.findById(id);
        if (foundCaseStudy != null) convertRichTextFieldImageIds(foundCaseStudy);
        return foundCaseStudy;
    }

    @Transactional
    public CaseStudy addCaseStudy(CaseStudyUpload data) {
        String clientLogoBase64 = data.getClientLogoBase64();
        Long imageId = null;

        // check if we are uploading an image, and insert it into the database if we are
        if ((clientLogoBase64 != null) && !clientLogoBase64.isEmpty()) {
            // insert image into database
            Image image = getImageFromBase64(clientLogoBase64);
            imageMapper.insert(image);

            // set image ID
            imageId = image.getImageId();
        }

        sanitiseRichTextFields(data);
        insertImagesFoundInRichTextFields(data);

        // insert case study into database
        CaseStudy caseStudy = getCaseStudyFromUploadData(data, imageId);
        caseStudyMapper.insert(caseStudy);

        return caseStudy;
    }

    @Transactional
    public CaseStudy updateCaseStudy(CaseStudyUpload data) {
        CaseStudy currentCaseStudyData = caseStudyMapper.findById(data.getCaseStudyId());

        // case study with provided ID does not exist, so cannot update case study so return null
        if (currentCaseStudyData == null) return null;

        String clientLogoBase64 = data.getClientLogoBase64();

        // get ID of current image
        Long imageId = currentCaseStudyData.getClientLogoId();

        // check if we are uploading a new image, and replace the current one in the database if we are
        if ((clientLogoBase64 != null) && !clientLogoBase64.isEmpty()) {
            // update image in database
            Image image = getImageFromBase64(clientLogoBase64);

            // we need to check if there was previously a logo we need to update, or if we need to insert a new one
            if (imageId == null) {
                // insert a new image and set image ID for case study
                imageMapper.insert(image);
                imageId = image.getImageId();
            } else {
                // update existing image (case study already has correct ID so no need to update it, so set to null)
                image.setImageId(imageId);
                imageMapper.update(image);
                imageId = null;
            }
        }

        sanitiseRichTextFields(data);
        updateImagesInRichTextFields(data, currentCaseStudyData);

        // update case study in database
        CaseStudy caseStudy = getCaseStudyFromUploadData(data, imageId);
        caseStudyMapper.update(caseStudy);

        CaseStudy updatedCaseStudy = caseStudyMapper.findById(data.getCaseStudyId());
        return updatedCaseStudy;
    }

    @Transactional
    public void deleteCaseStudy(Long id) {
        CaseStudy caseStudy = caseStudyMapper.findById(id);

        // delete case study
        caseStudyMapper.delete(id);

        deleteImagesFoundInRichTextFields(caseStudy);

        // if case study has a client logo, delete it
        Long clientLogoId = caseStudy.getClientLogoId();
        if (clientLogoId != null) {
            imageMapper.delete(clientLogoId);
        }
    }

    public static Image getImageFromBase64(String base64) {
        String imageType = base64.substring(5).split(";")[0];
        String imageBase64 = base64.split(",")[1];

        // decode base64 to get image bytes
        byte[] imageData = Base64.getDecoder().decode(imageBase64);

        // return image
        return new Image(null, imageData, getImageTypeFromMediaType(imageType));
    }

    public static CaseStudy getCaseStudyFromUploadData(CaseStudyUpload data, Long imageId) {
        return new CaseStudy(
                data.getCaseStudyId(),
                data.getTitle(),
                data.getProjectStatus(),
                data.getEditStatus(),
                data.getClientName(),
                data.getClientLink(),
                imageId,
                data.getIndustry(),
                data.getProjectType(),
                data.getStartDate(),
                data.getEndDate(),
                data.getSummary(),
                data.getTeamMembers(),
                data.getAdvanceLink(),
                data.getProblemDescription(),
                data.getSolutionDescription(),
                data.getOutcomes(),
                data.getToolsUsed(),
                data.getProjectLearnings()
        );
    }

    // convert MediaType string to ImageType
    public static ImageType getImageTypeFromMediaType(String mediaType) {
        if (mediaType.startsWith("image/jpeg")) {
            return ImageType.JPEG;
        } else {
            return ImageType.PNG;
        }
    }

    // convert all image IDs into image links inside all rich text fields of case study
    private void convertRichTextFieldImageIds(CaseStudy caseStudy) {
        List<String> richTextFields = Arrays.asList(caseStudy.getProblemDescription(), caseStudy.getSolutionDescription(), caseStudy.getOutcomes(), caseStudy.getToolsUsed(), caseStudy.getProjectLearnings());
        List<String> newRichTextFields = new ArrayList<>();
        for (String field : richTextFields) {
            if (field == null) {
                newRichTextFields.add(null);
                continue;
            }
            Document document = Jsoup.parse(field);
            Elements imageElements = document.select("img"); // get all image elements
            for (Element imageElement : imageElements) {
                // extract ID from image element
                String id = imageElement.attr("src");

                // create image link
                String link = url + "/api/images/" + id;

                // replace ID in field with image link
                Element newImageElement = new Element("img");
                newImageElement.attr("src", link);
                imageElement.replaceWith(newImageElement);
            }
            String newHtml = document.select("body").html();
            newRichTextFields.add(newHtml);
        }

        // replace rich text fields in data with updated ones
        caseStudy.setProblemDescription(newRichTextFields.get(0));
        caseStudy.setSolutionDescription(newRichTextFields.get(1));
        caseStudy.setOutcomes(newRichTextFields.get(2));
        caseStudy.setToolsUsed(newRichTextFields.get(3));
        caseStudy.setProjectLearnings(newRichTextFields.get(4));
    }

    // insert into database any images found in rich text fields, and replace base64 with image ID
    private void insertImagesFoundInRichTextFields(CaseStudyUpload data) {
        List<String> richTextFields = Arrays.asList(data.getProblemDescription(), data.getSolutionDescription(), data.getOutcomes(), data.getToolsUsed(), data.getProjectLearnings());
        List<String> newRichTextFields = new ArrayList<>();
        for (String field : richTextFields) {
            if (field == null) {
                newRichTextFields.add(null);
                continue;
            }
            Document document = Jsoup.parse(field);
            Elements imageElements = document.select("img"); // get all image elements
            for (Element imageElement : imageElements) {
                // extract base64 from image element
                String imgBase64 = imageElement.attr("src");

                // decode base64 and insert image into database
                Image image = getImageFromBase64(imgBase64);
                imageMapper.insert(image);

                // get ID of image
                Long id = image.getImageId();

                // replace base64 in field with image ID
                Element newImageElement = new Element("img");
                newImageElement.attr("src", id.toString());
                imageElement.replaceWith(newImageElement);
            }
            String newHtml = document.select("body").html();
            newRichTextFields.add(newHtml);
        }

        // replace rich text fields in data with updated ones
        data.setProblemDescription(newRichTextFields.get(0));
        data.setSolutionDescription(newRichTextFields.get(1));
        data.setOutcomes(newRichTextFields.get(2));
        data.setToolsUsed(newRichTextFields.get(3));
        data.setProjectLearnings(newRichTextFields.get(4));
    }

    // for all images in all rich text fields in data, if new base64 found then it is inserted into database and
    // replaced with a link, if there is an image ID in currentData that isn't in data, then the corresponding image
    // is deleted from the database
    private void updateImagesInRichTextFields(CaseStudyUpload data, CaseStudy currentData) {
        // get all image IDs from rich text fields from current case study data
        List<String> richTextFields = Arrays.asList(currentData.getProblemDescription(), currentData.getSolutionDescription(), currentData.getOutcomes(), currentData.getToolsUsed(), currentData.getProjectLearnings());
        List<Long> currentImageIDs = new ArrayList<>();
        for (String field : richTextFields) {
            if (field == null) continue;
            Document document = Jsoup.parse(field);
            Elements imageElements = document.select("img"); // get all image elements
            for (Element imageElement : imageElements) {
                // extract ID from image element
                String id = imageElement.attr("src");

                // add ID to list
                currentImageIDs.add(Long.parseLong(id));
            }
        }

        // insert into database any images found in rich text fields, and replace base64 with image ID
        richTextFields = Arrays.asList(data.getProblemDescription(), data.getSolutionDescription(), data.getOutcomes(), data.getToolsUsed(), data.getProjectLearnings());
        List<String> newRichTextFields = new ArrayList<>();
        for (String field : richTextFields) {
            if (field == null) {
                newRichTextFields.add(null);
                continue;
            }
            Document document = Jsoup.parse(field);
            Elements imageElements = document.select("img"); // get all image elements
            for (Element imageElement : imageElements) {
                // extract src from image element
                String src = imageElement.attr("src");

                // check if src is not a link (meaning that it is base64)
                String regex = "^https?://.+/api/images/\\d+$";
                boolean isBase64 = !src.matches(regex);

                // if src is base64 then it is a new image and should be decoded, inserted into database, and replaced
                if (isBase64) {
                    // decode base64 and insert image into database
                    Image image = getImageFromBase64(src);
                    imageMapper.insert(image);

                    // get ID of image
                    Long id = image.getImageId();

                    // replace base64 in field with image ID
                    Element newImageElement = new Element("img");
                    newImageElement.attr("src", id.toString());
                    imageElement.replaceWith(newImageElement);
                } else {
                    // src is a link here, meaning that it existed in database before, and still does now, so
                    // remove the corresponding ID from the list of current IDs
                    String[] tokens = src.split("/");
                    Long imgId = Long.parseLong(tokens[tokens.length - 1]);
                    currentImageIDs.removeIf(id -> id.equals(imgId));

                    // replace link with image ID
                    Element newImageElement = new Element("img");
                    newImageElement.attr("src", imgId.toString());
                    imageElement.replaceWith(newImageElement);
                }
            }
            String newHtml = document.select("body").html();
            newRichTextFields.add(newHtml);
        }

        // replace rich text fields in data with updated ones
        data.setProblemDescription(newRichTextFields.get(0));
        data.setSolutionDescription(newRichTextFields.get(1));
        data.setOutcomes(newRichTextFields.get(2));
        data.setToolsUsed(newRichTextFields.get(3));
        data.setProjectLearnings(newRichTextFields.get(4));

        // if any IDs remain in the list of current IDs, then it means that these images were in the case study
        // previously, but have been removed in the new case study data, so these images must now be deleted from
        // the database
        currentImageIDs.forEach(imageMapper::delete);
    }

    // delete from database any images found in rich text fields
    private void deleteImagesFoundInRichTextFields(CaseStudy caseStudy) {
        List<String> richTextFields = Arrays.asList(caseStudy.getProblemDescription(), caseStudy.getSolutionDescription(), caseStudy.getOutcomes(), caseStudy.getToolsUsed(), caseStudy.getProjectLearnings());
        for (String field : richTextFields) {
            if (field == null) continue;
            Document document = Jsoup.parse(field);
            Elements imageElements = document.select("img"); // get all image elements
            for (Element imageElement : imageElements) {
                // extract id from image element
                String imgId = imageElement.attr("src");

                // delete image from database
                imageMapper.delete(Long.parseLong(imgId));
            }
        }
    }

    // sanitise the html in all rich text fields
    private void sanitiseRichTextFields(CaseStudyUpload data) {
        // create a sanitised version of each rich text field
        List<String> richTextFields = Arrays.asList(data.getProblemDescription(), data.getSolutionDescription(), data.getOutcomes(), data.getToolsUsed(), data.getProjectLearnings());
        List<String> newRichTextFields = new ArrayList<>();
        for (String field : richTextFields) {
            if (field == null) {
                newRichTextFields.add(null);
                continue;
            }
            String safeHtml = Jsoup.clean(field, Safelist.basic().addTags("img").addAttributes("img", "src"));
            newRichTextFields.add(safeHtml);
        }

        // replace rich text fields in data with sanitised ones
        data.setProblemDescription(newRichTextFields.get(0));
        data.setSolutionDescription(newRichTextFields.get(1));
        data.setOutcomes(newRichTextFields.get(2));
        data.setToolsUsed(newRichTextFields.get(3));
        data.setProjectLearnings(newRichTextFields.get(4));
    }
}
