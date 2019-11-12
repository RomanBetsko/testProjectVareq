package com.ua.stomat.appservices.service.impl;

import com.google.api.services.drive.model.File;
import com.ua.stomat.appservices.dao.ClientRepository;
import com.ua.stomat.appservices.dao.UploadFileRepository;
import com.ua.stomat.appservices.entity.Client;
import com.ua.stomat.appservices.entity.UploadFile;
import com.ua.stomat.appservices.service.ClientService;
import com.ua.stomat.appservices.service.FileService;
import com.ua.stomat.appservices.validator.AddClientCriteria;
import com.ua.stomat.appservices.validator.AjaxResponseBody;
import net.sf.jmimemagic.*;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {

    private ServletContext context;
    private ClientRepository clientRepository;
    private UploadFileRepository fileRepository;
    private FileService fileService;

    public ClientServiceImpl(ServletContext context, ClientRepository clientRepository, UploadFileRepository fileRepository, FileService fileService) {
        this.context = context;
        this.clientRepository = clientRepository;
        this.fileRepository = fileRepository;
        this.fileService = fileService;
    }


    @Override
    public ResponseEntity<?> addClient(AddClientCriteria request, Errors errors) {
        AjaxResponseBody result = new AjaxResponseBody();
        if (errors.hasErrors()) {
            result.setMsg(errors.getAllErrors()
                    .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(",")));
            return ResponseEntity.badRequest().body(result);
        }
        Client client = clientRepository.save(prepareClient(request));
        result.setMsg(client.getClientId().toString());
        return ResponseEntity.ok(result);
    }


    private Client prepareClient(AddClientCriteria request) {
        Client client = new Client();
        client.setFirstName(request.getFirstName());
        client.setSecondName(request.getSecondName());
        client.setThirdName(request.getThirdName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setSex(request.getSex());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        Date dateOfBirth = null;
        try {
            dateOfBirth = simpleDateFormat.parse(request.getDateOfBirth());
        } catch (ParseException e) {

            //throw exeption
            e.printStackTrace();
        }
        client.setDateOfBirth(dateOfBirth);
        return client;
    }

    @Override
    public ModelAndView getClientsData() {
        Map<String, Object> params = new HashMap<>();
        params.put("clients", clientRepository.findAll());
        return new ModelAndView("clients", params);
    }

    @Override
    public ModelAndView getClientPage(Integer clientId) {
        Map<String, Object> params = new HashMap<>();
        Client client = clientRepository.findByClientId(clientId);
//        List<UploadFile> clientFiles = client.getFiles();
//        List<UploadFile> updatedClientFiles = new ArrayList<>();
//        for (UploadFile file : clientFiles) {
//            String fileName = file.getFileName();
//            List<File> googleFiles = fileService.getGoogleFilesByName(fileName);
//            if (googleFiles.size() != 0) {
//                File temp = googleFiles.get(0);
//                updatedClientFiles.add(new UploadFile(temp.getId(), temp.getName(), null, client));
//            }
//        }
//        client.setFiles(updatedClientFiles);
        params.put("client", client);
        return new ModelAndView("singleclient", params);
    }

    @Override
    public ResponseEntity<?> deleteClient(Integer id, Errors errors) {
        AjaxResponseBody result = new AjaxResponseBody();
        if (errors.hasErrors()) {
            result.setMsg(errors.getAllErrors()
                    .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(",")));
            return ResponseEntity.badRequest().body(result);
        }
        clientRepository.deleteByClientId(id);
        result.setMsg("Клієнта було видалено");
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<?> upload(CommonsMultipartFile[] fileUpload, String clientId) {
        AjaxResponseBody result = new AjaxResponseBody();
        Client client = clientRepository.findByClientId(Integer.valueOf(clientId));
        if (fileUpload != null && fileUpload.length > 0) {
            for (CommonsMultipartFile aFile : fileUpload) {
                String folder;

                List<com.google.api.services.drive.model.File> rootGoogleFolders = fileService.getGoogleSubFolderByName
                        (null, client.getSecondName() + client.getFirstName() + clientId);
                if (rootGoogleFolders.size() == 0) {
                    com.google.api.services.drive.model.File folderDrive = fileService.createFolder
                            (null, client.getSecondName() + client.getFirstName() + client.getClientId());
                    folder = folderDrive.getId();

                } else {
                    folder = rootGoogleFolders.get(0).getId();
                }

                try {
                    MagicMatch match = Magic.getMagicMatch(aFile.getBytes(), false);
                    com.google.api.services.drive.model.File file = fileService.createGoogleFile(folder, match.getMimeType(), aFile.getOriginalFilename(), aFile.getBytes());
                    UploadFile uploadFile = new UploadFile();
                    uploadFile.setFileId(file.getId());
                    uploadFile.setFileName(aFile.getOriginalFilename());
                    uploadFile.setClient(clientRepository.findByClientId(Integer.valueOf(clientId)));
                    fileRepository.save(uploadFile);

                    fileService.createPublicPermission(file.getId());
                } catch (MagicParseException | MagicMatchNotFoundException | MagicException e) {
                    e.printStackTrace();
                }
            }
        }
        result.setMsg("Файл було завантажено");
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<?> downloadFile(String fileId, HttpServletResponse response) {

        UploadFile uploadFile = fileRepository.findByFileId(fileId);

        List<com.google.api.services.drive.model.File> rootGoogleFiles = fileService.getGoogleFilesByName(uploadFile.getFileName());
        for (com.google.api.services.drive.model.File temp : rootGoogleFiles) {
            if (temp.getId().equals(fileId)) {
                return ResponseEntity.ok("https://drive.google.com/file/d/" + temp.getId() + "/view?usp=sharing");
            }
        }
        return null;
    }

}
