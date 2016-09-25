package com.adblockers.controllers;

import com.adblockers.AdblockersBackendApplication;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by alexandrosfilios on 24/09/16.
 */
@CrossOrigin
@RestController
@RequestMapping("resources/")
public class ResourcesController {

    @RequestMapping(value = "paper", method = RequestMethod.GET, produces = "application/pdf")
    public ResponseEntity<byte[]> getPaperAsPdf() {

        try (FileInputStream fileInputStream = new FileInputStream(AdblockersBackendApplication.RESOURCES_PATH + "paper.pdf")) {
            byte[] contents = IOUtils.toByteArray(fileInputStream);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_PDF);
            String fileName = "adblockerprivacy.pdf";
            httpHeaders.setContentDispositionFormData(fileName, fileName);
            return new ResponseEntity<>(contents, httpHeaders, HttpStatus.OK);
        } catch(IOException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
