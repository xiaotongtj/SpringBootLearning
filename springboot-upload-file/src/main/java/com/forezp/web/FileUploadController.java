package com.forezp.web;


import com.forezp.storage.StorageFileNotFoundException;
import com.forezp.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by fangzhipeng on 2017/4/19.
 */

@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {

        List<String> files = storageService
                .loadAll()
                .map(this::getServeFile)
                .collect(Collectors.toList());

        model.addAttribute("files", files);


        return "uploadForm";
    }

    private String getServeFile(Path path) {
        String serveFile = MvcUriComponentsBuilder
                .fromMethodName(FileUploadController.class, "serveFile", path.getFileName().toString())
                .build().toString();
        return serveFile;
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        storageService.store(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

//    public static void main(String[] args) {
//
//        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//        params.put("id", Collections.singletonList("1"));
//        params.put("name", Collections.singletonList("张三"));
//        String uri = UriComponentsBuilder
//                .fromHttpUrl("http://localhost:8080//hello")
//                .queryParams(params).build().toUriString(); //params是个Map
//        System.out.println(uri);
//
//
//        String uri = UriComponentsBuilder
//                .fromUriString("http://example.com/hotels/{hotel}/bookings/{booking}")
//                .build().expand("43","32").encode().toUriString();
//        URI uri1 = uriComponents.expand("42", "21").encode().toUri();
//
//
//        String uri = UriComponentsBuilder.newInstance()
//                .scheme("http").host("example.com").path("/hotels/{hotel}/bookings/{booking}").build()
//                .expand("42", "21")
//                .encode().toUriString();
//
//       /* ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest((HttpServletRequest) null)
//                .replaceQueryParam("accountId", "{id}").build()
//                .expand("123")
//                .encode();*/
//
//        UriComponents uriComponents4 = MvcUriComponentsBuilder
//                .fromMethodName(FileUploadController.class, "getBooking", 21).buildAndExpand(42);
//
//        //MvcUriComponentsBuilder.fromMappingName()
//    }

}