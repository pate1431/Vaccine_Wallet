package com.cloud.vaccinewallet.controller;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.List;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.cloud.vaccinewallet.beans.User;
import com.cloud.vaccinewallet.repositories.UserRepository;
import com.cloud.vaccinewallet.repositories.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


@Controller
@AllArgsConstructor
public class HomeController {

    private UserRepository userRepository;
    private RoleRepository roleRepository;

    private String encodePassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/user")
    public String userIndex(Model model, Authentication authentication) {

        String name = authentication.getName();
        User user= new User();
        List<String> roles = new ArrayList<String>();

        for (GrantedAuthority ga: authentication.getAuthorities()) {
            roles.add(ga.getAuthority());
        }
        model.addAttribute("name", name);
        model.addAttribute("roles", roles);
     /*   model.addAttribute("userLista", userRepository.findAll());*/
        model.addAttribute("userList", userRepository.findByUsername(name));
         return "user/index";
    }
    @GetMapping("/admin")
    public String adminIndex(Model model, Authentication authentication) {

        String name = authentication.getName();
        User user= new User();
        List<String> roles = new ArrayList<String>();

        for (GrantedAuthority ga: authentication.getAuthorities()) {
            roles.add(ga.getAuthority());
        }
        model.addAttribute("name", name);
        model.addAttribute("roles", roles);
        /*   model.addAttribute("userLista", userRepository.findAll());*/
        model.addAttribute("userList", userRepository.findByUsername(name));
        return "admin/index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "/error/access-denied";
    }

    @GetMapping("/register")
    public String goRegistration() {
        return "register";
    }

    @GetMapping("/upload")
    public String uploadPage() {
        return "user/upload";
    }

    @GetMapping("/contact")
    public String contactPage() {
        return "user/contact";
    }

//show index in user on load

    @GetMapping("/index")
    public String homePage(Model model, Authentication authentication) {
        User user = new User();

        user.setUsername(authentication.getName());
        String name = authentication.getName();

        model.addAttribute("name", name);
        model.addAttribute("username", authentication.getName());

        model.addAttribute("userList", userRepository.findByUsername(name));

        return "user/index";
    }
    /*@GetMapping("/index")
    public String homePageAdmin(Model model, Authentication authentication) {
        User user = new User();

        user.setUsername(authentication.getName());
        String name = authentication.getName();

        model.addAttribute("name", name);
        model.addAttribute("username", authentication.getName());

        model.addAttribute("userList", userRepository.findByUsername(name));

        return "admin/index";
    }*/

    @GetMapping("/code")
    public String codePage(Model model) {
        model.addAttribute("pdfInformation");
        return "user/code";
    }

    @PostMapping("/register")
    public String doRegistration(Model model,@RequestParam String username, @RequestParam String password) {


        User user= new User(username, encodePassword(password), Byte.valueOf("1"));
        user.getRoles().add(roleRepository.findByRolename("ROLE_USER"));

        userRepository.save(user);
        model.addAttribute("userList",userRepository.findAll());
        return "redirect:/";
    }
    @PostMapping(value = "/generateQR",  consumes = "multipart/form-data")
    public String generateQR(Model model, @RequestParam(value="vacfile") MultipartFile vacfile) throws IOException, WriterException {
        String path= "C:\\Users\\Sn3haL\\Downloads\\code.png";

        PDDocument document = PDDocument.load(vacfile.getBytes());
        PDFTextStripper pdfStripper = new PDFTextStripper();
        // Fetching PDF document
        String text = pdfStripper.getText(document);
        System.out.println(text);

        //data that we want to store in the QR code
        BitMatrix matrix = new MultiFormatWriter().encode(new String(text.getBytes("UTF-8"),
                "UTF-8"), BarcodeFormat.QR_CODE,300, 300);
        MatrixToImageWriter.writeToFile(matrix, path.substring(path.lastIndexOf('.') + 1), new File(path));
        System.out.println("Create QR");

        model.addAttribute("pdfInformation", text);
        return "user/code";
    }

    @GetMapping("/profile/{name}")
    public String profilePage(Model model, @PathVariable String name) {
        model.addAttribute("userList", userRepository.findByUsername(name));
        return "user/profile";
    }
    @PostMapping("/profile/{name}")
    public String editProfileInfo(Model model,@PathVariable String name ,@RequestParam String firstName,@RequestParam String lastName,
                                  @RequestParam String middleName, @RequestParam Integer age, @RequestParam String email,
                                  @RequestParam Long phoneNumber)
    {

        List<User> userList= userRepository.findAll();
        User user = userRepository.findByUsername(name);

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setMiddleName(middleName);
        user.setPhoneNumber(phoneNumber);
        user.setAge(age);
        user.setEmail(email);
        userRepository.save(user);
        model.addAttribute("user", new User());
        model.addAttribute("userList", userRepository.findByUsername(name));
        return "user/index";
    }


}
