package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

/**
 * Represents a single learning unit or subtopic.
 */
public class Unit {

    private String id;
    private String unitName;
    private String parentId;     // Links back to the parent unit
    private String explanation;

    // ✅ Multimedia (URLs stored, not files)
    private List<String> audioFileId = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();
    private String aiVideoUrl;

    // ✅ Nested subtopics
    private List<Unit> units = new ArrayList<>();

    // ✅ Optional MCQ tests
    private List<MotherMCQTest> test = new ArrayList<>();

    // ----- Constructors -----
    public Unit() {
        this.id = new ObjectId().toHexString();
    }

    public Unit(boolean withTest) {
        this();
        this.test = withTest ? new ArrayList<>() : null;
    }

    // ----- Getters & Setters -----
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public List<String> getAudioFileId() { return audioFileId; }
    public void setAudioFileId(List<String> audioFileId) {
        this.audioFileId = audioFileId != null ? audioFileId : new ArrayList<>();
    }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public String getAiVideoUrl() { return aiVideoUrl; }
    public void setAiVideoUrl(String aiVideoUrl) { this.aiVideoUrl = aiVideoUrl; }

    public List<Unit> getUnits() { return units; }
    public void setUnits(List<Unit> units) {
        this.units = units != null ? units : new ArrayList<>();
    }

    public List<MotherMCQTest> getTest() { return test; }
    public void setTest(List<MotherMCQTest> test) { this.test = test; }

    @Override
    public String toString() {
        return "Unit [id=" + id +
                ", unitName=" + unitName +
                ", parentId=" + parentId +
                ", explanation=" + explanation +
                ", audioCount=" + (audioFileId != null ? audioFileId.size() : 0) +
                ", imageCount=" + (imageUrls != null ? imageUrls.size() : 0) +
                ", subUnitCount=" + (units != null ? units.size() : 0) +
                ", aiVideoUrl=" + aiVideoUrl + "]";
    }
}
