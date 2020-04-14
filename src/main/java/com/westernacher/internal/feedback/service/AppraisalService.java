package com.westernacher.internal.feedback.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
    public void generateReport() {
        List<Appraisal> appraisals = repository.findAll();
        appraisals.stream().forEach(appraisal -> {
            Object object = null;
            try {
                object = getFlatReportData(appraisal);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            try {
                ObjectMapper mapper = new ObjectMapper();
                ArrayList myArrayList = (ArrayList) object;
                HashMap<String, Object> map = getHashMapFromJson(mapper.writeValueAsString(myArrayList.get(0)) );
                CsvObject object1 = getCsvObject(map);
                log.info(""+map);
            } catch (JSONException | JsonProcessingException e) {
                e.printStackTrace();
            }
        });
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

        map.forEach((k,v)->{
            //System.out.println("Item : " + k + " Count : " + v);
            if(k.contains("root.cycleId")){
                csvObject.setCycleId(v.toString());
            }

            if (k.contains("root.userId")) {
                csvObject.setUserId(v.toString());
            }

            if (k.contains("sectiononeResponse.response.hrReviews") && k.contains("name")) {
                csvObject.setSectiononeResponseHrReviewsName(v.toString());
            }
        });

        return csvObject;
    }
}

@Data
class RequestResource {
    Map<String, String> meta;
    Appraisal data;
}
