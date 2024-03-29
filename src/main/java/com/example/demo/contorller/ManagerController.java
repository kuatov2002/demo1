package com.example.demo.contorller;

import com.example.demo.Repo.BillboardRepo;
import com.example.demo.Repo.UserRepo;
import com.example.demo.models.Billboard;
import com.example.demo.models.User;
import com.example.demo.service.BillboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class ManagerController {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    BillboardRepo billboardRepo;
    @Autowired
    private BillboardService billboardService;


    @GetMapping("/users")
    public String managerPage(Model model,Authentication authentication){
        List<User> users = userRepo.findAll();
        model.addAttribute("users", users);
        return "manager";
    }
    @GetMapping("/request")
    public String Request(Model model){
        billboardService.updateExpiredStatus();
        Iterable<Billboard> billboards=billboardRepo.findAll();
        ArrayList<Billboard> managerBills=new ArrayList<>();
        for (Billboard bill:
             billboards) {
            if(bill.getStatus().trim().equals("onReview")){
                managerBills.add(bill);
            }
        }
        model.addAttribute("managerBills",managerBills);
//        System.out.println(managerBills.toString());
        return "request";
    }
    @PostMapping("/request")
    public String PostRequest(@RequestParam Long id,
                              @RequestParam String button){
        Optional<Billboard> bill=billboardRepo.findById(id);
        Billboard bill1=bill.get();
        bill1.setStatus(button);
        bill1.setInteractionDay(LocalDate.now());
        billboardRepo.save(bill1);
//        System.out.println(id+" "+button);

        return "redirect:/request";
    }

    @GetMapping("/orders")
    public String Orders(Model model){
        billboardService.updateExpiredStatus();
        Iterable<Billboard> billboards=billboardRepo.findAll();
        ArrayList<Billboard> notManagerBills=new ArrayList<>();
        for (Billboard bill:
                billboards) {
            if(!bill.getStatus().trim().equals("onReview")){
                notManagerBills.add(bill);
            }
        }
        model.addAttribute("notManagerBills",notManagerBills);
        return "orders";
    }
}
