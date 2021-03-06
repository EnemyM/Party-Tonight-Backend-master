package com.partymaker.mvc.controller.functional.maker.sign.up;

import com.partymaker.mvc.model.whole.RoleEntity;
import com.partymaker.mvc.model.whole.UserEntity;
import com.partymaker.mvc.service.billing.BillingService;
import com.partymaker.mvc.service.user.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * partymaker controller
 * Created by Mostipan Anton
 */

@RestController
@RequestMapping(value = {"/maker"})
public class PartyMakerRESTSignUp {

    private static final Logger logger = Logger.getLogger(PartyMakerRESTSignUp.class);
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static Date date;
    @Autowired
    UserService userService;

    @Autowired
    @Qualifier("BillingService")
    BillingService billingService;


    @PostMapping(value = {"/signup"})
    public Callable<ResponseEntity<?>> signUp(@RequestBody UserEntity user) {
        return () -> {
            logger.info("Sign up maker = " + user);
            try {
                try {
                    if (userService.isExist(user.getEmail())) {
                        return new ResponseEntity<Object>("Promoter with such email is already exist", HttpStatus.IM_USED);
                    }
                    if (userService.isExistByName(user.getUserName())) {
                        return new ResponseEntity<Object>("Promoter with such username is already exist", HttpStatus.IM_USED);
                    }
                    if (billingService.isExist(user.getBilling())) {
                        return new ResponseEntity<Object>("Promoter with current billing info is already exist", HttpStatus.IM_USED);
                    }
                } catch (Exception e) {
                    logger.info("Checking failed " + e);
                }
                logger.info("Creating user ");
                date = new Date();
                user.setCreatedDate(dateFormat.format(date));
                // hard code
                user.setRole(new RoleEntity(2, "PARTY_MAKER"));

                logger.info("saving billing");
                billingService.saveBilling(user.getBilling());
                logger.info("saved billing");
                user.setEnable(true);

                try {
                    logger.info("try to get  billing");
                    user.setBilling(billingService.findByCard(user.getBilling().getCard_number()));
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.info("Failed to fetch billing due to " + e);
                }

                userService.saveUser(user);
                logger.info("Created maker = " + user);
                return new ResponseEntity<String>("", HttpStatus.CREATED);
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("Failed to create maker = " + user + ", due to: " + e);
                return new ResponseEntity<Void>(HttpStatus.FORBIDDEN);
            }
        };
    }

    private void validationUser() {

    }
}
