package com.cloud.vaccinewallet.controller;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


import com.cloud.vaccinewallet.amazon.AmazonClient;

import com.cloud.vaccinewallet.beans.Email;
import com.cloud.vaccinewallet.beans.Role;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.cloud.vaccinewallet.beans.User;
import com.cloud.vaccinewallet.repositories.UserRepository;
import com.cloud.vaccinewallet.repositories.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;



//username: vwallet475@gmail.com
//password: capstone1234

@Controller
@AllArgsConstructor
public class HomeController {

    private AmazonClient amazonClient;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private Email e;

    private String encodePassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    @GetMapping("/")
    public String index() {


        return "index";
    }

    /*
    *   LOGIN CONTROLLER FOR USER
    * */

    @GetMapping("/user")
    public String userIndex(Model model, Authentication authenticationUser) {

        String name = authenticationUser.getName();

        User user= new User();
        List<String> roles = new ArrayList<String>();

        for (GrantedAuthority ga: authenticationUser.getAuthorities()) {
            roles.add(ga.getAuthority());
        }

        model.addAttribute("name", name);
        model.addAttribute("roles", roles);
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
        model.addAttribute("userList", userRepository.findByUsername(name));
        model.addAttribute("userData", userRepository.findAll());
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



    /*ADMIN MAPPINGS*/

    /*
     *   LOGIN CONTROLLER FOR ADMIN
     * */


    @GetMapping("/admin/index")
    public String adminIndexLoad(Model model,Authentication authentication)
    {
        User user = new User();

        user.setUsername(authentication.getName());
        String name = authentication.getName();
        System.out.println(name);
        model.addAttribute("name", name);
        return "admin/index";
    }

    @GetMapping("/enableDisable/{username}")
    public String enableDisable(Model model, @PathVariable String username)
    {
        User user = userRepository.findByUsername(username);

        if(user.getEnabled()==true)
        {

            user.setEnabled(false);
            e.sendEmail(user.getEmail(),
                    "VaccineWallet", "Sorry your account access for the vaccine wallet application has been blocked");

        }
        else{
            user.setEnabled(true);
            e.sendEmail(user.getEmail(),
                    "VaccineWallet", "You account access has been approved sorry for the inconnvennce");

        }
        userRepository.save(user);
        model.addAttribute("userData",userRepository.findAll());
        return "admin/database";
    }
    @GetMapping("/admin/database/{name}")
    public String adminDatabase(Model model,  @PathVariable String name)
    {
        System.out.println(name);
        model.addAttribute("userData",userRepository.findAll());
        return "admin/database";
    }
//show index in user on load

    @GetMapping("user/index")
    public String homePage(Model model, Authentication authentication) {
        User user = new User();

        user.setUsername(authentication.getName());
        String name = authentication.getName();

        model.addAttribute("name", name);
        model.addAttribute("username", authentication.getName());

        model.addAttribute("userList", userRepository.findByUsername(name));

        return "user/index";
    }

    @GetMapping("/code")
    public String codePage(Model model) {

        //model.addAttribute("pdfInformation");
        return "user/code";
    }

    @PostMapping("/register")
    public String doRegistration(Model model,@RequestParam String username, @RequestParam String password, @RequestParam String usermail)
    {


        User user= new User(username, encodePassword(password),usermail, Boolean.valueOf("1"));
        user.getRoles().add(roleRepository.findByRolename("ROLE_USER"));
        user.setEnabled(true);
        userRepository.save(user);

        e.sendEmail(user.getEmail(),
                "VaccineWallet", "Account Created");

        model.addAttribute("userList",userRepository.findAll());
        return "redirect:/";
    }

    @PostMapping(value = "/generateQR",  consumes = "multipart/form-data")
    public String generateQR(Model model, @RequestParam(value="vacfile") MultipartFile vacfile) throws IOException, WriterException {
        String path= "C:\\Users\\Sn3haL\\Downloads\\code.png";

        PDDocument document = PDDocument.load(vacfile.getBytes());
        PDFTextStripper pdfStripper = new PDFTextStripper();

        // Fetching PDF document into Text variable
        String text = pdfStripper.getText(document);
        System.out.println(text);

        //data that we want to store in the QR code
        BitMatrix matrix = new MultiFormatWriter().encode(new String(text.getBytes("UTF-8"),
                "UTF-8"), BarcodeFormat.QR_CODE,500, 500);

          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          BufferedImage bimg = MatrixToImageWriter.toBufferedImage(matrix);

          Authentication auth = SecurityContextHolder.getContext().getAuthentication();

          File file = new File("C:\\Users\\Sn3haL\\Downloads\\" + auth.getName() + ".png");
          ImageIO.write(bimg, "jpg", file);

          amazonClient.uploadFile(file, auth.getName());
          System.out.println("Create QR");

        User user = userRepository.findByUsername(auth.getName());


        model.addAttribute("pdfInformation", text);
        return "user/code";
    }

    /*
    *
    *   GET MAPPING FOR EDITING PROFILE ON PROFILE PAGE
    *
    * */
    @GetMapping("/profile/{name}")
    public String profilePage(Model model, @PathVariable String name) {
        model.addAttribute("userList", userRepository.findByUsername(name));
        return "user/profile";
    }

    /*
     *
     *   POST MAPPING FOR EDITING PROFILE ON PROFILE PAGE
     *
     * */
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
