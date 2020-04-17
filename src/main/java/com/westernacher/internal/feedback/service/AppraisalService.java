package com.westernacher.internal.feedback.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import com.westernacher.internal.feedback.controller.PersonController;
import com.westernacher.internal.feedback.domain.Appraisal;
import com.westernacher.internal.feedback.domain.AppraisalStatusType;
import com.westernacher.internal.feedback.domain.Person;
import com.westernacher.internal.feedback.repository.AppraisalRepository;
import com.westernacher.internal.feedback.repository.PersonRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.PrintWriter;
import java.util.*;

@Service
@Slf4j
public class AppraisalService {

    @Autowired
    private AppraisalRepository repository;

    @Autowired
    private EmailUtility emailUtility;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Async
    public void sendListOfMail(Set<String> idList, AppraisalStatusType position, String personId) {
        for (String userId:idList) {
            try {
                Person manager = personRepository.findById(userId).get();
                Person person = personRepository.findById(personId).get();
                String[] subjectParameters = new String[]{ person.getName() };
                String[] bodyParameters = new String[]{ person.getName(), manager.getName() };

                emailUtility.send(personRepository.findById(userId).get().getEmail(),
                        "subject_review", "body_review", subjectParameters, bodyParameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*public void parse(String json)  {
        JsonFactory factory = new JsonFactory();

        ObjectMapper mapper = new ObjectMapper(factory);
        JsonNode rootNode = mapper.readTree(json);

        Iterator<Map.Entry<String,JsonNode>> fieldsIterator = rootNode.fields();
        while (fieldsIterator.hasNext()) {

            Map.Entry<String,JsonNode> field = fieldsIterator.next();
            System.out.println("Key:"field.getKey() + "\tValue:" + field.getValue());
        }*/
    public List<CsvObject> generateReport() {
        List<CsvObject> csvObjectList = new ArrayList<>();
        List<Appraisal> appraisals = repository.findAll();
        appraisals.stream().forEach(appraisal -> {
            List<CsvObject> csvObjects = new ArrayList<>();
            Object object = null;
            try {
                object = getFlatReportData(appraisal);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            try {
                ObjectMapper mapper = new ObjectMapper();
                ArrayList myArrayList = (ArrayList) object;

                for (Object object1:myArrayList) {
                    HashMap<String, Object> map = getHashMapFromJson(mapper.writeValueAsString(object1) );
                    csvObjects.add(getCsvObject(map));
                }
            } catch (JSONException | JsonProcessingException e) {
                e.printStackTrace();
            }
            csvObjectList.addAll(csvObjects);
        });
        return csvObjectList;
    }

    public Object getFlatReportData(Appraisal appraisal) throws JsonProcessingException {
        //String customURL = baseUrl+tenantHolder.getTenantId()+"/model";
        String customURL = "https://gandalf-ioak.herokuapp.com/api/flatten";


        RequestResource requestResource = new RequestResource();
        Map<String, String> emptyMap = new HashMap<>();
        requestResource.setMeta(emptyMap);
        ObjectMapper mapper = new ObjectMapper();
        requestResource.setData(appraisal);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<String>(mapper.writeValueAsString(requestResource), headers);
        try{
            ResponseEntity<List> responseEntity = restTemplate.postForEntity(customURL, requestEntity, List.class);
            return responseEntity.getBody();
        }catch (HttpStatusCodeException exception) {
            return null;
        }catch (RestClientException exception) {
            return null;
        }
    }

    public HashMap<String, Object> getHashMapFromJson(String json) throws JSONException {
        HashMap<String, Object> map = new HashMap<String, Object>();
        JSONObject jsonObject = new JSONObject(json);
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            map.put(key, jsonObject.get(key));
        }
        return map;
    }

    private CsvObject getCsvObject(HashMap<String, Object> map) {
        CsvObject csvObject = new CsvObject();
        String pmID1 = null;
        String pmID2 = null;
        String pmID3 = null;
        String pmID4 = null;
        String pmID5 = null;

        String tlID1 = null;
        String tlID2 = null;

        String pdID1 = null;
        String pdID2 = null;


        for (Map.Entry<String,Object> entry : map.entrySet()) {
            if (entry.getKey().contains("sectiononeResponse.response.projectManagerReviews") && entry.getKey().contains("name")) {
                if (pmID1==null) {
                    pmID1 = entry.getKey().substring(entry.getKey().indexOf("projectManagerReviews") + 22, entry.getKey().indexOf("projectManagerReviews")+46);
                }else if (pmID2==null) {
                    pmID2 = entry.getKey().substring(entry.getKey().indexOf("projectManagerReviews") + 22, entry.getKey().indexOf("projectManagerReviews")+46);
                }else if (pmID3==null) {
                    pmID3 = entry.getKey().substring(entry.getKey().indexOf("projectManagerReviews") + 22, entry.getKey().indexOf("projectManagerReviews")+46);
                }else if (pmID4==null) {
                    pmID4 = entry.getKey().substring(entry.getKey().indexOf("projectManagerReviews") + 22, entry.getKey().indexOf("projectManagerReviews")+46);
                }else if (pmID5==null) {
                    pmID5 = entry.getKey().substring(entry.getKey().indexOf("projectManagerReviews") + 22, entry.getKey().indexOf("projectManagerReviews")+46);
                }
            }

            if (entry.getKey().contains("sectiononeResponse.response.teamLeadReviews") && entry.getKey().contains("name")) {
                if (tlID1==null) {
                    tlID1 = entry.getKey().substring(entry.getKey().indexOf("teamLeadReviews") + 16, entry.getKey().indexOf("teamLeadReviews")+40);
                }else if (tlID2==null) {
                    tlID2 = entry.getKey().substring(entry.getKey().indexOf("teamLeadReviews") + 16, entry.getKey().indexOf("teamLeadReviews")+40);
                }
            }

            if (entry.getKey().contains("sectiononeResponse.response.practiceDirectorReviews") && entry.getKey().contains("name")) {
                if (pdID1==null) {
                    pdID1 = entry.getKey().substring(entry.getKey().indexOf("practiceDirectorReviews") + 24, entry.getKey().indexOf("practiceDirectorReviews")+48);
                }else if (pdID2==null) {
                    pdID2 = entry.getKey().substring(entry.getKey().indexOf("practiceDirectorReviews") + 24, entry.getKey().indexOf("practiceDirectorReviews")+48);
                }
            }
        }

        for (Map.Entry<String,Object> entry : map.entrySet()) {

            if (entry.getKey().contains("root.userId")) {
                csvObject.setUserId(entry.getValue().toString());
            }

            if (entry.getKey().contains("root.status")) {
                csvObject.setStatus(entry.getValue().toString());
            }

            if (entry.getKey().contains("sectiononeResponse.response.projectManagerReviews") && entry.getKey().contains("name")) {
                if (entry.getKey().contains(pmID1)) {
                    csvObject.setProjectManagerName1(entry.getValue().toString());
                }else if (entry.getKey().contains(pmID2)) {
                    csvObject.setProjectManagerName2(entry.getValue().toString());
                }else if (entry.getKey().contains(pmID3)) {
                    csvObject.setProjectManagerName3(entry.getValue().toString());
                }else if (entry.getKey().contains(pmID4)) {
                    csvObject.setProjectManagerName4(entry.getValue().toString());
                }else if (entry.getKey().contains(pmID5)) {
                    csvObject.setProjectManagerName5(entry.getValue().toString());
                }
            }

            if (entry.getKey().contains("sectiononeResponse.response.projectManagerReviews") && entry.getKey().contains("comment")) {
                if (entry.getKey().contains(pmID1)) {
                    csvObject.setProjectManagerComment1(entry.getValue().toString());
                }else if (entry.getKey().contains(pmID2)) {
                    csvObject.setProjectManagerComment2(entry.getValue().toString());
                }else if (entry.getKey().contains(pmID3)) {
                    csvObject.setProjectManagerComment3(entry.getValue().toString());
                }else if (entry.getKey().contains(pmID4)) {
                    csvObject.setProjectManagerComment4(entry.getValue().toString());
                }else if (entry.getKey().contains(pmID5)) {
                    csvObject.setProjectManagerComment5(entry.getValue().toString());
                }
            }

            if (entry.getKey().contains("sectiononeResponse.response.projectManagerReviews") && entry.getKey().contains("rating")) {
                if (entry.getKey().contains(pmID1)) {
                    csvObject.setProjectManagerRating1(entry.getValue().toString());
                }else if (entry.getKey().contains(pmID2)) {
                    csvObject.setProjectManagerRating2(entry.getValue().toString());
                }else if (entry.getKey().contains(pmID3)) {
                    csvObject.setProjectManagerRating3(entry.getValue().toString());
                }else if (entry.getKey().contains(pmID4)) {
                    csvObject.setProjectManagerRating4(entry.getValue().toString());
                }else if (entry.getKey().contains(pmID5)) {
                    csvObject.setProjectManagerRating5(entry.getValue().toString());
                }
            }

            if (entry.getKey().contains("sectiononeResponse.response.projectManagerReviews") && entry.getKey().contains("complete")) {
                if (entry.getKey().contains(pmID1)) {
                    csvObject.setProjectManagerComplete1(entry.getValue().toString());
                }else if (entry.getKey().contains(pmID2)) {
                    csvObject.setProjectManagerComplete2(entry.getValue().toString());
                }else if (entry.getKey().contains(pmID3)) {
                    csvObject.setProjectManagerComplete3(entry.getValue().toString());
                }else if (entry.getKey().contains(pmID4)) {
                    csvObject.setProjectManagerComplete4(entry.getValue().toString());
                }else if (entry.getKey().contains(pmID5)) {
                    csvObject.setProjectManagerComplete5(entry.getValue().toString());
                }
            }

            /*Team lead construction*/

            if (entry.getKey().contains("sectiononeResponse.response.teamLeadReviews") && entry.getKey().contains("name")) {
                if (entry.getKey().contains(tlID1)) {
                    csvObject.setTeamLeadName1(entry.getValue().toString());
                }else if (entry.getKey().contains(tlID2)) {
                    csvObject.setTeamLeadName2(entry.getValue().toString());
                }
            }

            if (entry.getKey().contains("sectiononeResponse.response.teamLeadReviews") && entry.getKey().contains("comment")) {
                if (entry.getKey().contains(tlID1)) {
                    csvObject.setTeamLeadComment1(entry.getValue().toString());
                }else if (entry.getKey().contains(tlID2)) {
                    csvObject.setTeamLeadComment2(entry.getValue().toString());
                }
            }

            if (entry.getKey().contains("sectiononeResponse.response.teamLeadReviews") && entry.getKey().contains("rating")) {
                if (entry.getKey().contains(tlID1)) {
                    csvObject.setTeamLeadRating1(entry.getValue().toString());
                }else if (entry.getKey().contains(tlID2)) {
                    csvObject.setTeamLeadRating2(entry.getValue().toString());
                }
            }

            if (entry.getKey().contains("sectiononeResponse.response.teamLeadReviews") && entry.getKey().contains("complete")) {
                if (entry.getKey().contains(tlID1)) {
                    csvObject.setTeamLeadComplete1(entry.getValue().toString());
                }else if (entry.getKey().contains(tlID2)) {
                    csvObject.setTeamLeadComplete2(entry.getValue().toString());
                }
            }

            /*practice director construction*/

            if (entry.getKey().contains("sectiononeResponse.response.practiceDirectorReviews") && entry.getKey().contains("name")) {
                if (entry.getKey().contains(pdID1)) {
                    csvObject.setPracticeDirectorName1(entry.getValue().toString());
                }else if (entry.getKey().contains(pdID2)) {
                    csvObject.setPracticeDirectorName2(entry.getValue().toString());
                }
            }

            if (entry.getKey().contains("sectiononeResponse.response.practiceDirectorReviews") && entry.getKey().contains("comment")) {
                if (entry.getKey().contains(pdID1)) {
                    csvObject.setPracticeDirectorComment1(entry.getValue().toString());
                }else if (entry.getKey().contains(pdID2)) {
                    csvObject.setPracticeDirectorComment2(entry.getValue().toString());
                }
            }

            if (entry.getKey().contains("sectiononeResponse.response.practiceDirectorReviews") && entry.getKey().contains("rating")) {
                if (entry.getKey().contains(pdID1)) {
                    csvObject.setPracticeDirectorRating1(entry.getValue().toString());
                }else if (entry.getKey().contains(pdID2)) {
                    csvObject.setPracticeDirectorRating2(entry.getValue().toString());
                }
            }

            if (entry.getKey().contains("sectiononeResponse.response.practiceDirectorReviews") && entry.getKey().contains("complete")) {
                if (entry.getKey().contains(pdID1)) {
                    csvObject.setPracticeDirectorComplete1(entry.getValue().toString());
                }else if (entry.getKey().contains(pdID2)) {
                    csvObject.setPracticeDirectorComplete2(entry.getValue().toString());
                }
            }

            /*HR Construction*/

            if (entry.getKey().contains("sectiononeResponse.response.hrReviews") && entry.getKey().contains("name")) {
                csvObject.setHrName(entry.getValue().toString());
            }

            if (entry.getKey().contains("sectiononeResponse.response.hrReviews") && entry.getKey().contains("comment")) {
                csvObject.setHrComment(entry.getValue().toString());
            }

            if (entry.getKey().contains("sectiononeResponse.response.hrReviews") && entry.getKey().contains("rating")) {
                csvObject.setHrRating(entry.getValue().toString());
            }

            if (entry.getKey().contains("sectiononeResponse.response.hrReviews") && entry.getKey().contains("complete")) {
                csvObject.setHrComplete(entry.getValue().toString());
            }

        }
        return csvObject;
    }

    public static void writeDataToCsvUsingStringArray(PrintWriter writer, List<CsvObject> csvObjects) {
        String[] CSV_HEADER = { "UserId", "Status", "Project_Manager_1_Name", "Project_Manager_1_Comment", "Project_Manager_1_isComplete", "Project_Manager_1_Rating" };
        try (
                CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END);
        ){
            csvWriter.writeNext(CSV_HEADER);

            for (CsvObject csvObject : csvObjects) {
                String[] data = {
                        csvObject.getUserId(),
                        csvObject.getStatus(),
                        csvObject.getProjectManagerName1(),
                        csvObject.getProjectManagerComment1(),
                        csvObject.getProjectManagerComplete1(),
                        csvObject.getProjectManagerRating1(),

                        csvObject.getProjectManagerName2(),
                        csvObject.getProjectManagerComment2(),
                        csvObject.getProjectManagerComplete2(),
                        csvObject.getProjectManagerRating2(),

                        csvObject.getProjectManagerName3(),
                        csvObject.getProjectManagerComment3(),
                        csvObject.getProjectManagerComplete3(),
                        csvObject.getProjectManagerRating3(),

                        csvObject.getProjectManagerName4(),
                        csvObject.getProjectManagerComment4(),
                        csvObject.getProjectManagerComplete4(),
                        csvObject.getProjectManagerRating4(),

                        csvObject.getProjectManagerName5(),
                        csvObject.getProjectManagerComment5(),
                        csvObject.getProjectManagerComplete5(),
                        csvObject.getProjectManagerRating5(),

                        csvObject.getTeamLeadName1(),
                        csvObject.getTeamLeadComment1(),
                        csvObject.getTeamLeadComplete1(),
                        csvObject.getTeamLeadRating1(),

                        csvObject.getTeamLeadName2(),
                        csvObject.getTeamLeadComment2(),
                        csvObject.getTeamLeadComplete2(),
                        csvObject.getTeamLeadRating2(),

                        csvObject.getPracticeDirectorName1(),
                        csvObject.getPracticeDirectorComment1(),
                        csvObject.getPracticeDirectorComplete1(),
                        csvObject.getPracticeDirectorRating1(),

                        csvObject.getPracticeDirectorName2(),
                        csvObject.getPracticeDirectorComment2(),
                        csvObject.getPracticeDirectorComplete2(),
                        csvObject.getPracticeDirectorRating2(),

                        csvObject.getHrName(),
                        csvObject.getHrComment(),
                        csvObject.getHrComplete(),
                        csvObject.getHrRating()

                };

                csvWriter.writeNext(data);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}

@Data
class RequestResource {
    Map<String, String> meta;
    Appraisal data;
}
