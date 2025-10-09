package com.padmasiniAdmin.padmasiniAdmin_1.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "mcq_tests")
public class MCQTest {

    @Id
    private String id;

    private String question;
    private List<String> questionImages = new ArrayList<>();

    private String option1;
    private String option1Image;

    private String option2;
    private String option2Image;

    private String option3;
    private String option3Image;

    private String option4;
    private String option4Image;

    private String explanation;
    private List<String> solutionImages = new ArrayList<>();

    private int correctIndex;
    private List<List<String>> tableData = new ArrayList<>();

    public MCQTest() {
        // ensure unique id when created server-side
        this.id = new ObjectId().toHexString();
    }

    // getters & setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public int getCorrectIndex() { return correctIndex; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }

    public List<List<String>> getTableData() { return tableData; }
    public void setTableData(List<List<String>> tableData) { this.tableData = tableData; }
}
