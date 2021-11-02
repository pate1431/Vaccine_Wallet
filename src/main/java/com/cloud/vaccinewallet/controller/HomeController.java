package com.cloud.vaccinewallet.controller;

import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.cloud.vaccinewallet.amazon.AmazonClient;

import com.cloud.vaccinewallet.beans.Email;

import com.cloud.vaccinewallet.beans.VaccineInformation;
import com.cloud.vaccinewallet.repositories.VaccineInformationRepository;
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


//username: vwallet475@gmail.com
//password: capstone1234

@Controller
@AllArgsConstructor
public class HomeController {

    /*
     * Declaration of all Private Variables
     */
    private AmazonClient amazonClient;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private VaccineInformationRepository vaccineInformationRepository;
    private Email e;

    /*
     * Method encode string password to BCrypt
     * */
    private String encodePassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    @GetMapping("/")
    public String index() {
        return "index";
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
    public String uploadPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("userName", auth.getName());
        return "user/upload";
    }

    @GetMapping("/contact")
    public String contactPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("userName", auth.getName());
        return "user/contact";
    }

    /*
     *   LOGIN CONTROLLER FOR USER
     * */

    @GetMapping("/user")
    public String userIndex(Model model, Authentication authenticationUser) {
        String name = authenticationUser.getName();

        List<String> roles = new ArrayList<String>();
        for (GrantedAuthority ga : authenticationUser.getAuthorities()) {
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
        List<String> roles = new ArrayList<String>();
        for (GrantedAuthority ga : authentication.getAuthorities()) {
            roles.add(ga.getAuthority());
        }
        model.addAttribute("name", name);
        model.addAttribute("roles", roles);
        model.addAttribute("userList", userRepository.findByUsername(name));
        model.addAttribute("userData", userRepository.findAll());
        return "admin/index";
    }



    /*ADMIN MAPPINGS*/

    /*
     *   LOGIN CONTROLLER FOR ADMIN
     * */


    @GetMapping("/admin/index")
    public String adminIndexLoad(Model model, Authentication authentication) {
        User user = new User();

        user.setUsername(authentication.getName());
        String name = authentication.getName();
        System.out.println(name);
        model.addAttribute("name", name);
        return "admin/index";
    }

    @GetMapping("/enableDisable/{username}")
    public String enableDisable(Model model, @PathVariable String username) {
        User user = userRepository.findByUsername(username);

        if (user.getEnabled() == true) {
            user.setEnabled(false);
            e.sendEmail(user.getEmail(),
                    "VaccineWallet", "Sorry your account access for the vaccine wallet application has been blocked");

        } else {
            user.setEnabled(true);
            e.sendEmail(user.getEmail(),
                    "VaccineWallet", "You account access has been approved sorry for the inconnvennce");
        }
        userRepository.save(user);
        model.addAttribute("userData", userRepository.findAll());
        return "admin/database";
    }

    @GetMapping("/admin/database/{name}")
    public String adminDatabase(Model model, @PathVariable String name) {
        System.out.println(name);
        model.addAttribute("userData", userRepository.findAll());
        return "admin/database";
    }


//show index in user on load

    @GetMapping("user/index")
    public String homePage(Model model, Authentication authentication) {
     /*   User user = new User();
        user.setUsername(authentication.getName());*/

        model.addAttribute("userName", authentication.getName());
        model.addAttribute("userList", userRepository.findByUsername(authentication.getName()));

        return "user/index";
    }

    @GetMapping("user/code")
    public String codePage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("userName", auth.getName());
        model.addAttribute("vaccineInfo", userRepository.findByUsername(auth.getName()));
        return "user/code";
    }

    @GetMapping("/addUser")
    public String addUser(Model model, @RequestParam String firstname, @RequestParam String lastname,
                            @RequestParam String middlename,
                            @RequestParam Long phoneNumber ,
                            @RequestParam Integer age, @RequestParam String email) {

        User user = new User();

        user.setFirstName(firstname);
        user.setLastName(lastname);
        user.setMiddleName(middlename);
        user.setPhoneNumber(phoneNumber);
        user.setAge(age);
        user.setEmail(email);

        userRepository.save(user);
        model.addAttribute("userList", userRepository.findAll());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("userName", auth.getName());
        return "/user/index";
    }

    @PostMapping("/register")
    public String doRegistration(Model model, @RequestParam String username, @RequestParam String password, @RequestParam String usermail) {
        User user = new User(username, encodePassword(password), usermail, Boolean.valueOf("1"));
        user.getRoles().add(roleRepository.findByRolename("ROLE_USER"));
        user.setEnabled(true);
        userRepository.save(user);
        e.sendEmail(user.getEmail(),
                "VaccineWallet", "Account Created");
        model.addAttribute("userList", userRepository.findAll());
        return "redirect:/";
    }

    @PostMapping(value = "/generateQR", consumes = "multipart/form-data")
    public String generateQR(Model model, @RequestParam(value = "vacfile") MultipartFile vacfile) throws IOException, WriterException {

        PDDocument document = PDDocument.load(vacfile.getBytes());
        PDFTextStripper pdfStripper = new PDFTextStripper();

        // Fetching PDF document into Text variable
        String text = pdfStripper.getText(document);

/**************************************************************************************************************
 * Code to Save Vaccine Information
 *************************************************************************************************************/


        String[] lines = text.split("\r\n|\r|\n");

        if (lines[0].equals("Ministry of Health") && lines[1].equals("Ministère de la Santé")) {
            String name = "", date = "", vaccine = "", dose = "";

            VaccineInformation vaccineInfo = new VaccineInformation();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userRepository.findByUsername(auth.getName());

            int count = 1;
            for (String line : lines) {
                if (count == 3) {
                    name = line.substring(10);
                    vaccineInfo.setNameOnVaccine(name);
                }
                if (count == 6) {
                    date = line.substring(0, 11);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate vacDate = LocalDate.parse(date.trim(), formatter);
                    vaccineInfo.setVaccineDate(vacDate);
                }
                if (count == 8) {
                    String[] res = line.split(" ");
                    vaccine = res[4];
                    vaccineInfo.setVaccineName(vaccine);
                }
                if (count == 14) {
                    String[] res = line.split(" ");
                    dose = res[3];
                    vaccineInfo.setNoOfDose(Integer.parseInt(dose));
                }

                System.out.println(count + " " + line);
                count++;
            }
            vaccineInformationRepository.save(vaccineInfo);
            user.setVaccine(vaccineInfo);
            userRepository.save(user);

            /**************************************************************************************************************
             * Code to Generate QR
             *************************************************************************************************************/
            //data that we want to store in the QR code
            BitMatrix matrix = new MultiFormatWriter().encode(new String(text.getBytes("UTF-8"),
                    "UTF-8"), BarcodeFormat.QR_CODE, 500, 500);


            BufferedImage bimg = MatrixToImageWriter.toBufferedImage(matrix);

            File file = new File("C:\\Users\\Sn3haL\\Downloads\\" + auth.getName() + ".png");
            ImageIO.write(bimg, "jpg", file);
            amazonClient.uploadFile(file, auth.getName());


            model.addAttribute("userName", auth.getName());
            model.addAttribute("vaccineInfo", userRepository.findByUsername(auth.getName()));
            return "user/code";
        } else {
            return "user/upload";
        }
    }

    /*
     *
     *   GET MAPPING FOR EDITING PROFILE ON PROFILE PAGE
     *
     * */
    @GetMapping("/profile/{name}")
    public String profilePage(Model model, @PathVariable String name) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("userName", auth.getName());
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
                                  @RequestParam String middleName, @RequestParam Integer age, @RequestParam String hcNumber,
                                  @RequestParam String address, @RequestParam String postalCode, @RequestParam String city,
                                  @RequestParam String province,@RequestParam String email,
                                  @RequestParam Long phoneNumber)
    {


        // List<User> userList= userRepository.findAll();
        User user = userRepository.findByUsername(name);

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setMiddleName(middleName);
        user.setPhoneNumber(phoneNumber);
        user.setHcNumber(hcNumber);
        user.setAddress(address);
        user.setPostalCode(postalCode);
        user.setCity(city);
        user.setProvince(province);
        user.setAge(age);
        user.setEmail(email);
        userRepository.save(user);
        model.addAttribute("user", new User());
        model.addAttribute("userList", userRepository.findByUsername(name));
        return "user/index";
    }


    @GetMapping("/changeUserPass/{name}")
    public String changeSigningInfo(Model model, @PathVariable String name) {
        model.addAttribute("userList", userRepository.findByUsername(name));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("userName", auth.getName());
        return "user/changeUserPass";

    }

    @PostMapping("/changePass/{name}")
    public String editProfilePassword(Model model, @PathVariable String name,
                                      @RequestParam String username, @RequestParam String password) {

        /*  List<User> userList = userRepository.findAll();*/
        if (name.equals(username)) {
            User user = userRepository.findByUsername(username);
            System.out.println(password);
            System.out.println(encodePassword(password));
            user.setEncryptedPassword(encodePassword(password));

            userRepository.save(user);
            model.addAttribute("user", new User());
            model.addAttribute("userList", userRepository.findByUsername(name));
            return "user/index";
        } else {
            return "user/changeUserPass";
        }

    }

}
