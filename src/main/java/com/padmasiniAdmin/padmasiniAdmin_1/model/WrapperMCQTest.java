package com.padmasiniAdmin.padmasiniAdmin_1.model;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class WrapperMCQTest {

    private List<MCQTest> questionsList;

    @NotBlank(message = "parent id is required")
    private String parentId;

    private String subjectName;
    private String testName;
    private String unitName;
    private int marks;
    private String dbname;
    private String rootId;
    private String quesId;

    // single-question fields (for updates if needed)
    private String question;
    private List<String> questionImages;

    private String option1;
    private String option1Image;

    private String option2;
    private String option2Image;

    private String option3;
    private String option3Image;

    private String option4;
    private String option4Image;

    private String explanation;
    private List<String> solutionImages;

    // getters / setters omitted for brevity in this snippet — include all in your file
    // (copy the same getters/setters from your original class)
    // ... generate or copy all getters and setters here ...
    public List<MCQTest> getQuestionsList() { return questionsList; }
    public void setQuestionsList(List<MCQTest> questionsList) { this.questionsList = questionsList; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }

    public String getDbname() { return dbname; }
    public void setDbname(String dbname) { this.dbname = dbname; }

    public String getRootId() { return rootId; }
    public void setRootId(String rootId) { this.rootId = rootId; }

    public String getQuesId() { return quesId; }
    public void setQuesId(String quesId) { this.quesId = quesId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getQuestionImages() { return questionImages; }
    public void setQuestionImages(List<String> questionImages) { this.questionImages = questionImages; }

    public String getOption1() { return option1; }
    public void setOption1(String option1) { this.option1 = option1; }

    public String getOption1Image() { return option1Image; }
    public void setOption1Image(String option1Image) { this.option1Image = option1Image; }

    public String getOption2() { return option2; }
    public void setOption2(String option2) { this.option2 = option2; }

    public String getOption2Image() { return option2Image; }
    public void setOption2Image(String option2Image) { this.option2Image = option2Image; }

    public String getOption3() { return option3; }
    public void setOption3(String option3) { this.option3 = option3; }

    public String getOption3Image() { return option3Image; }
    public void setOption3Image(String option3Image) { this.option3Image = option3Image; }

    public String getOption4() { return option4; }
    public void setOption4(String option4) { this.option4 = option4; }

    public String getOption4Image() { return option4Image; }
    public void setOption4Image(String option4Image) { this.option4Image = option4Image; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public List<String> getSolutionImages() { return solutionImages; }
    public void setSolutionImages(List<String> solutionImages) { this.solutionImages = solutionImages; }
}
