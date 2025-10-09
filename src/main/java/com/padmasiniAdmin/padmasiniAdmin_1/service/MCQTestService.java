package com.padmasiniAdmin.padmasiniAdmin_1.service;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.padmasiniAdmin.padmasiniAdmin_1.model.MCQTest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.MotherMCQTest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.Unit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.UnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperMCQTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MCQTestService {

    @Autowired
    private MongoClient mongoClient;

    private MongoTemplate getTemplate(String dbName) {
        return new MongoTemplate(mongoClient, dbName);
    }

    // Add / attach questions into existing test or create test if not exist
    public String addQuestion(WrapperMCQTest data) {
        // Defensive checks
        if (data == null) {
            System.out.println("❌ Received null payload");
            return null;
        }
        System.out.println("📥 addQuestion called with wrapper: " + data.getTestName() +
                " parentId: " + data.getParentId() + " db: " + data.getDbname());

        UnitRequest root = getById(data.getRootId(), data.getSubjectName(), data.getDbname());
        if (root == null) {
            System.out.println("❌ Root not found for rootId=" + data.getRootId() + " db=" + data.getDbname());
            return null;
        }

        // find test by name under parentId
        MotherMCQTest existingTest = findTestRecursive(root, data.getParentId(), data.getTestName());

        if (existingTest != null) {
            // update existing test: either add questions or update single question if quesId present
            if (data.getQuesId() != null && !data.getQuesId().isEmpty()) {
                // update matching question in this test
                boolean updated = false;
                for (MCQTest q : existingTest.getQuestionsList()) {
                    if (q.getId().equals(data.getQuesId())) {
                        updateQuestionFields(q, data);
                        updated = true;
                        System.out.println("✏️ Updated existing question " + q.getId());
                        break;
                    }
                }
                if (!updated) System.out.println("⚠️ quesId provided but not found in test");
            } else {
                // add new questionsList (if provided)
                if (data.getQuestionsList() != null && !data.getQuestionsList().isEmpty()) {
                    // ensure each question has an id & sanitized lists
                    data.getQuestionsList().forEach(this::sanitizeQuestionBeforeSave);
                    existingTest.getQuestionsList().addAll(data.getQuestionsList());
                    System.out.println("✅ Added " + data.getQuestionsList().size() + " question(s) into existing test " + existingTest.getTestName());
                } else {
                    System.out.println("⚠️ No questionsList provided to add");
                }
            }
        } else {
            // create new test under the parent unit/root
            MotherMCQTest newTest = new MotherMCQTest();
            newTest.setTestName(data.getTestName());
            newTest.setMarks(data.getMarks());
            newTest.setSubjectName(data.getSubjectName());
            newTest.setUnitName(data.getUnitName());

            List<MCQTest> questions = data.getQuestionsList() != null ? data.getQuestionsList() : new ArrayList<>();
            questions.forEach(this::sanitizeQuestionBeforeSave);
            newTest.setQuestionsList(questions);

            attachTestToParent(root, data.getParentId(), newTest);
            System.out.println("🆕 Created and attached new test '" + newTest.getTestName() + "' to parent " + data.getParentId());
        }

        saveRoot(root, data);
        return root.getUnitName();
    }

    public String updateQuestion(WrapperMCQTest data, String oldTestName) {
        System.out.println("📥 updateQuestion called: oldName=" + oldTestName + " wrapper testName=" + data.getTestName());
        UnitRequest root = getById(data.getRootId(), data.getSubjectName(), data.getDbname());
        if (root == null) {
            System.out.println("❌ Root not found");
            return null;
        }

        MotherMCQTest test = findTestRecursive(root, data.getParentId(), oldTestName);
        if (test == null) {
            System.out.println("⚠️ Test not found: " + oldTestName);
            return null;
        }

        // Update metadata
        if (data.getTestName() != null && !data.getTestName().trim().isEmpty()) {
            test.setTestName(data.getTestName());
        }
        if (data.getMarks() > 0) {
            test.setMarks(data.getMarks());
        }

        boolean updated = false;

        // 1️⃣ Update single question if quesId provided
        if (data.getQuesId() != null && !data.getQuesId().isEmpty()) {
            for (MCQTest q : test.getQuestionsList()) {
                if (q.getId().equals(data.getQuesId())) {
                    updateQuestionFields(q, data);
                    updated = true;
                    break;
                }
            }
        }

        // 2️⃣ Replace all questions if questionsList provided
        if (!updated && data.getQuestionsList() != null && !data.getQuestionsList().isEmpty()) {
            List<MCQTest> newQuestions = new ArrayList<>();
            for (MCQTest q : data.getQuestionsList()) {
                sanitizeQuestionBeforeSave(q);
                newQuestions.add(q);
            }
            test.setQuestionsList(newQuestions);
            updated = true;
        }

        if (updated) {
            saveRoot(root, data);
            return root.getUnitName();
        } else {
            System.out.println("⚠️ No question updated (quesId missing or empty, questionsList missing)");
            return null;
        }
    }


    public String deleteQuestion(WrapperMCQTest data) {
        System.out.println("📥 deleteQuestion called");
        UnitRequest root = getById(data.getRootId(), data.getSubjectName(), data.getDbname());
        if (root == null) {
            System.out.println("❌ root not found");
            return null;
        }

        boolean deleted = deleteTestRecursive(root, data.getParentId(), data);

        if (deleted) {
            saveRoot(root, data);
            return root.getUnitName();
        } else {
            System.out.println("⚠️ nothing deleted");
            return null;
        }
    }

    private void updateQuestionFields(MCQTest q, WrapperMCQTest data) {
        if (data.getQuestion() != null) q.setQuestion(data.getQuestion());
        if (data.getQuestionImages() != null) q.setQuestionImages(sanitizeList(data.getQuestionImages(), "NO_QUESTION_IMAGE"));
        if (data.getOption1() != null) q.setOption1(data.getOption1());
        if (data.getOption1Image() != null) q.setOption1Image(data.getOption1Image());
        if (data.getOption2() != null) q.setOption2(data.getOption2());
        if (data.getOption2Image() != null) q.setOption2Image(data.getOption2Image());
        if (data.getOption3() != null) q.setOption3(data.getOption3());
        if (data.getOption3Image() != null) q.setOption3Image(data.getOption3Image());
        if (data.getOption4() != null) q.setOption4(data.getOption4());
        if (data.getOption4Image() != null) q.setOption4Image(data.getOption4Image());
        if (data.getExplanation() != null) q.setExplanation(data.getExplanation());
        if (data.getSolutionImages() != null) q.setSolutionImages(sanitizeList(data.getSolutionImages(), "NO_SOLUTION_IMAGE"));
    }

    private void sanitizeQuestionBeforeSave(MCQTest q) {
        if (q.getId() == null || q.getId().trim().isEmpty()) {
            q.setId(new ObjectId().toHexString());
        }
        q.setQuestionImages(sanitizeList(q.getQuestionImages(), "NO_QUESTION_IMAGE"));
        q.setSolutionImages(sanitizeList(q.getSolutionImages(), "NO_SOLUTION_IMAGE"));
        if (q.getExplanation() == null) q.setExplanation(""); // keep explicit empty string if no solution
        if (q.getTableData() == null) q.setTableData(new ArrayList<>());
    }

    private List<String> sanitizeList(List<String> input, String defaultValue) {
        if (input == null || input.isEmpty()) {
            List<String> list = new ArrayList<>();
            list.add(defaultValue);
            return list;
        }
        return input;
    }

    private MotherMCQTest findTestRecursive(UnitRequest root, String parentId, String testName) {
        if (root == null) return null;
        if (root.getId().equals(parentId)) {
            return root.getTest().stream()
                    .filter(t -> t.getTestName().equals(testName))
                    .findFirst().orElse(null);
        }
        for (Unit u : root.getUnits()) {
            MotherMCQTest found = findTestRecursiveInUnit(u, parentId, testName);
            if (found != null) return found;
        }
        return null;
    }

    private MotherMCQTest findTestRecursiveInUnit(Unit unit, String parentId, String testName) {
        if (unit.getId().equals(parentId)) {
            return unit.getTest().stream()
                    .filter(t -> t.getTestName().equals(testName))
                    .findFirst()
                    .orElse(null);
        }
        for (Unit child : unit.getUnits()) {
            MotherMCQTest r = findTestRecursiveInUnit(child, parentId, testName);
            if (r != null) return r;
        }
        return null;
    }

    private void attachTestToParent(UnitRequest root, String parentId, MotherMCQTest test) {
        if (root.getId().equals(parentId)) {
            root.getTest().add(test);
            return;
        }
        for (Unit u : root.getUnits()) {
            if (u.getId().equals(parentId)) {
                u.getTest().add(test);
                return;
            }
            attachTestToParentRecursive(u, parentId, test);
        }
    }

    private void attachTestToParentRecursive(Unit unit, String parentId, MotherMCQTest test) {
        if (unit.getId().equals(parentId)) {
            unit.getTest().add(test);
            return;
        }
        for (Unit child : unit.getUnits()) {
            attachTestToParentRecursive(child, parentId, test);
        }
    }

    private boolean deleteTestRecursive(UnitRequest root, String parentId, WrapperMCQTest data) {
        if (root.getId().equals(parentId)) {
            if (data.getQuesId() != null && !data.getQuesId().isEmpty()) {
                return root.getTest().stream()
                        .filter(t -> t.getTestName().equals(data.getTestName()))
                        .findFirst()
                        .map(t -> t.getQuestionsList().removeIf(q -> q.getId().equals(data.getQuesId())))
                        .orElse(false);
            } else {
                return root.getTest().removeIf(t -> t.getTestName().equals(data.getTestName()));
            }
        }
        for (Unit u : root.getUnits()) {
            if (deleteTestRecursiveInUnit(u, parentId, data)) return true;
        }
        return false;
    }

    private boolean deleteTestRecursiveInUnit(Unit unit, String parentId, WrapperMCQTest data) {
        if (unit.getId().equals(parentId)) {
            if (data.getQuesId() != null && !data.getQuesId().isEmpty()) {
                return unit.getTest().stream()
                        .filter(t -> t.getTestName().equals(data.getTestName()))
                        .findFirst()
                        .map(t -> t.getQuestionsList().removeIf(q -> q.getId().equals(data.getQuesId())))
                        .orElse(false);
            } else {
                return unit.getTest().removeIf(t -> t.getTestName().equals(data.getTestName()));
            }
        }
        for (Unit child : unit.getUnits()) {
            if (deleteTestRecursiveInUnit(child, parentId, data)) return true;
        }
        return false;
    }

    private void saveRoot(UnitRequest root, WrapperMCQTest data) {
        System.out.println("📌 Saving DB: " + data.getDbname() + " | Collection: " + data.getSubjectName());
        getTemplate(data.getDbname()).save(root, data.getSubjectName());
        System.out.println("✅ Saved successfully!");
    }

    public UnitRequest getById(String id, String collectionName, String dbname) {
        MongoTemplate mongoTemplate = getTemplate(dbname);
        try {
            return mongoTemplate.findById(new ObjectId(id), UnitRequest.class, collectionName);
        } catch (IllegalArgumentException e) {
            return mongoTemplate.findById(id, UnitRequest.class, collectionName);
        }
    }
}
