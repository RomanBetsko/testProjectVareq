package com.ua.stomat.appservices.service;

import com.ua.stomat.appservices.entity.UploadFile;
import com.ua.stomat.appservices.validator.AddClientCriteria;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Service
public interface ClientService {

    ResponseEntity<?> addClient(AddClientCriteria request, Errors errors);

    ModelAndView getClientsData();

    ModelAndView getClientPage(Integer clientId);

    ResponseEntity<?> deleteClient(Integer id, Errors errors);

    ResponseEntity<?> upload(CommonsMultipartFile[] fileUpload, String clientId);

    UploadFile downloadFile(String fileId, Errors errors);
}
