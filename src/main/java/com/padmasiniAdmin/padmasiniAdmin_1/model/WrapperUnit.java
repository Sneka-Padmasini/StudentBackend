package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

/**
 * Wrapper class used for frontend requests to add/update/delete units.
 * Frontend sends URLs (not raw files) for audio, images, and AI video.
 */
public class WrapperUnit {

    // ----- Basic Info -----
    @NotBlank(message = "Parent ID is required")
    private String parentId;

    @NotBlank(message = "Root Unit ID is required")
    private String rootUnitId;

    @NotBlank(message = "Standard is required")
    private String standard;

    private String unitName;
    private String explanation;

    // ----- Database Info -----
    private String dbname;
    private String subjectName;

    // ----- Media URLs -----
    private List<String> audioFileId;     // S3 URLs of uploaded audio files
    private List<String> imageUrls;       // S3 URLs of uploaded images
    private String aiVideoUrl;            // S3 URL of generated/AI video

    // ----- Audio Management -----
    private List<String> keepAudioFileIds; // IDs to retain on update

    // ----- Optional Tests -----
    private List<MCQTest> test;

    // ----- Getters & Setters -----
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getRootUnitId() { return rootUnitId; }
    public void setRootUnitId(String rootUnitId) { this.rootUnitId = rootUnitId; }

    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getDbname() { return dbname; }
    public void setDbname(String dbname) { this.dbname = dbname; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public List<String> getAudioFileId() { return audioFileId; }
    public void setAudioFileId(List<String> audioFileId) { this.audioFileId = audioFileId; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getAiVideoUrl() { return aiVideoUrl; }
    public void setAiVideoUrl(String aiVideoUrl) { this.aiVideoUrl = aiVideoUrl; }

    public List<String> getKeepAudioFileIds() { return keepAudioFileIds; }
    public void setKeepAudioFileIds(List<String> keepAudioFileIds) { this.keepAudioFileIds = keepAudioFileIds; }

    public List<MCQTest> getTest() { return test; }
    public void setTest(List<MCQTest> test) { this.test = test; }

    @Override
    public String toString() {
        return "WrapperUnit [" +
                "parentId=" + parentId +
                ", rootUnitId=" + rootUnitId +
                ", standard=" + standard +
                ", unitName=" + unitName +
                ", explanation=" + explanation +
                ", dbname=" + dbname +
                ", subjectName=" + subjectName +
                ", audioFileId=" + audioFileId +
                ", imageUrls=" + imageUrls +
                ", aiVideoUrl=" + aiVideoUrl +
                ", keepAudioFileIds=" + keepAudioFileIds +
                ", test=" + test + "]";
    }
	public Object getId() {
		// TODO Auto-generated method stub
		return null;
	}
}
