package com.example.demo.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
class UserController {

    private final UserRepository repository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    public static String PhotosDirectory = System.getProperty("User.dir") + "/ProfilePhotos";

    UserController(UserRepository repository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.repository = repository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    //only admin can see all users
    @CrossOrigin(origins = "*")
    @GetMapping(value="/Users", produces = {MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("hasAnyRole('ADMIN')")
    List<User> all() {
        return repository.findAll();
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/Registration")
    User newUser(@RequestBody User newUser) {
        newUser.setPassword(bCryptPasswordEncoder.encode(newUser.getPassword()));
        //if username already exists
        if (repository.findByUsername(newUser.getUserName()) != null) {
            return null;
        } else {
            //new user needs to be verified first to be a host
            if (newUser.getIsHost() == true) {
                newUser.setIsHost(false);
            }
            return repository.save(newUser);
        }
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/Users/{id}")
    User getUser(@PathVariable Long id, Principal principal) {
        //if someone else asks for a user,they are returned but will null password
        if (principal == null || !UserHasRights(id, principal)) {
            User user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
            user.setPassword(null);
            return user;
        }
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @CrossOrigin(origins = "*")
    @PutMapping("/Users/{id}")
    User replaceUser(@RequestBody User newUser, @PathVariable Long id, Principal principal) {
        //if someone else wants to edit a users account, user not found exception is thrown
        if (!UserHasRights(id, principal)) {
            throw new UserNotFoundException(id);
        }


        //if user has asked to become a host, they need to be verified by admin first
        if (!repository.findByUsername(principal.getName()).getIsAdmin() &&
            !repository.findByUsername(principal.getName()).getIsHost()) {
            newUser.setIsHost(false);
        }

        return repository.findById(id).map(User -> {
            User.setEMail(newUser.getEMail());
            User.setFirstName(newUser.getFirstName());
            User.setLastName(newUser.getLastName());
            User.setUserName(newUser.getUserName());
            User.setPassword(newUser.getPassword());
            User.setTelephone(newUser.getTelephone());
            User.setPhotoPath(newUser.getPhotoPath());
            User.setIsTenant(newUser.getIsTenant());
            User.setIsHost(newUser.getIsHost());
            User.setIsAdmin(newUser.getIsAdmin());
            return repository.save(newUser);
        }).orElseThrow(() -> new UserNotFoundException(id));
    }

    //used when user wants to change their password
    @CrossOrigin(origins = "*")
    @PutMapping("/UsersNewPassword/{id}")
    User NewPassword(@RequestBody User newUser, @PathVariable Long id, Principal principal) {
        if (!UserHasRights(id, principal)) {
            throw new UserNotFoundException(id);
        }

        newUser.setPassword(bCryptPasswordEncoder.encode(newUser.getPassword()));
        return repository.findById(id).map(User -> {
            User.setEMail(newUser.getEMail());
            User.setFirstName(newUser.getFirstName());
            User.setLastName(newUser.getLastName());
            User.setUserName(newUser.getUserName());
            User.setPassword(newUser.getPassword());
            User.setTelephone(newUser.getTelephone());
            User.setPhotoPath(newUser.getPhotoPath());
            User.setIsTenant(newUser.getIsTenant());
            User.setIsHost(newUser.getIsHost());
            User.setIsAdmin(newUser.getIsAdmin());
            return repository.save(newUser);
        }).orElseThrow(() -> new UserNotFoundException(id));
    }


    //returns id given username
    @CrossOrigin(origins = "*")
    @GetMapping("/UserId/{Username}")
    Long GetId(@PathVariable String Username, Principal principal) {
        User User = repository.findByUsername(Username);//.orElseThrow(() -> new UserNotFoundException(Username));
        if (User == null) return Long.valueOf(-1);
        return User.getUserId();
    }

    /*@CrossOrigin(origins = "*")
    @GetMapping("/Username/{id}")
    String GetUsername(@PathVariable Long id){
        User User= repository.findById(id).orElse(null);//.orElseThrow(() -> new UserNotFoundException(Username));
        if( User == null ) return null;
        return User.getUserName();
    }*/

    @CrossOrigin(origins = "*")
    @Transactional
    @RequestMapping(
            value = ("/Users/Image/{username}"),
            headers = "content-type=multipart/form-data",
            method = RequestMethod.POST)
    public int PostImage(@RequestParam("file") MultipartFile Image, @PathVariable String username, Principal principal) throws IOException {
        if (!UserHasRights(repository.findByUsername(username).getUserId(), principal)) {
            throw new IOException();
        }

        User user = repository.findByUsername(username);

        if (user == null) return -1;
        if (Image.isEmpty()) return -2;

        String PhotosDirectory = System.getProperty("user.dir") + "\\images\\";

        String PhotoPath = username + PlacePhotoController.GetImageType(Image);

        Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + user.getPhotoPath()));
        Image.transferTo(new File(PhotosDirectory + PhotoPath));

        repository.setPhotoPath("\\images\\" + PhotoPath, username);

        return 0;
    }

    @GetMapping(
            value = "/Users/Image/{username}",
            produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody
    byte[] GetImage(@PathVariable String username, Principal principal) throws IOException {

        User TUser = repository.findByUsername(username);
        if (TUser == null) return null;

        if (TUser.getPhotoPath() == null) {
            Path imagePath = Paths.get(System.getProperty("user.dir") + "\\images\\ApplicationImages\\DefaultUserImage.png");
            return Files.readAllBytes(imagePath);
        }

        Path imagePath = Paths.get(System.getProperty("user.dir") + TUser.getPhotoPath());
        return Files.readAllBytes(imagePath);
    }

    //used to examine if the logged in user is the one with the given id
    private Boolean UserHasRights(@PathVariable Long id, Principal principal) {

        User user = repository.findById(id).orElse(null);

        if (user == null) {
            return false;
        }

        String requestedUser = repository.findById(id).get().getUserName();
        String loggedInUser = principal.getName();
        Boolean loggedInAdmin = repository.findByUsername(loggedInUser).getIsAdmin();


        /*If logged in User is different than the requested User and is not an Admin, then
        throw UserNotFound exception*/
        if (loggedInUser.compareTo(requestedUser) != 0 && !loggedInAdmin) {
            return false;
        }
        return true;
    }

}
