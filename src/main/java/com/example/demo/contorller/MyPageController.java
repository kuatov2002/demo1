package com.example.demo.contorller;

import com.example.demo.Repo.BillboardRepo;
import com.example.demo.Repo.UserRepo;
import com.example.demo.models.Billboard;
import org.apache.catalina.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MyPageController {

    @Autowired
    private BillboardRepo billboardRepo;
    @GetMapping("/cabinet")
    public String cabinet(Model model){
        Iterable<Billboard> billboards = billboardRepo.findAll();
        model.addAttribute("billboards", billboards);
        return "orders";
    }
}
