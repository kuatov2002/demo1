package com.example.demo.contorller;

import com.example.demo.Repo.BillboardRepo;
import com.example.demo.Repo.PrototypeRepo;
import com.example.demo.models.Billboard;
import com.example.demo.models.Prototype;
import com.example.demo.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
//import com.example.demo.Repo.AddressRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class BillboardController {
    @Autowired
    private BillboardRepo billboardRepo;
    @Autowired
    private PrototypeRepo prototypeRepo;
    @GetMapping("/pricing")
    public String Zakaz() {
        return "pricing";
    }
    String type="";
    @PostMapping("/pricing")
    public String goToBuy(@RequestParam String type){
        this.type=type;
        return "redirect:/bulletin";
    }

    @GetMapping("/bulletin")
    public String Bulletin(Model model) {
        Iterable<Billboard> billboards = billboardRepo.findAll();
        Iterable<Prototype> prototypes = prototypeRepo.findAll();

        // Очищаем списки объявлений у каждого прототипа перед заполнением
        for (Prototype proto : prototypes) {
            proto.getList().clear();
        }
        ArrayList<Prototype> prot=new ArrayList<>();
        for (Prototype proto:
                prototypes) {
            if (proto.type.equals(type)){
                prot.add(proto);
            }
        }
        // Заполняем списки объявлений по адресам
        for (Billboard billboard : billboards) {
            String address = billboard.getAddress();
            for (Prototype proto1 : prot) {
                if (proto1.Address.equals(address)&&!billboard.getStatus().equals("reject")) {
                    proto1.getList().add(billboard);
                    break;
                }
            }
        }

        model.addAttribute("billboards", billboards);
        model.addAttribute("prototypes", prot);

        return "map";
    }


    @PostMapping("/bulletin")
    public String toMain(@AuthenticationPrincipal User user,
                         @RequestParam String address,
                         @RequestParam String price,
                         @RequestParam Long proId,
                         Model model) {

        // Find all billboards and prototypes from database
        Iterable<Billboard> billboards = billboardRepo.findAll();
        Iterable<Prototype> prototypes = prototypeRepo.findAll();

        // Count the number of billboards that belong to the current user and have the same address
        int num = 0;
        for (Billboard billboard : billboards) {
            if (billboard.getAddress().equals(address)) {
                num++;
            }
        }

        // Set the start date based on the number of existing billboards with the same address
        LocalDate startDate1 = LocalDate.now().withDayOfMonth(1);
        if (num == 1) {
            startDate1 = startDate1.plusMonths(1);
        } else if (num == 2) {
            startDate1 = startDate1.plusMonths(2);
        }

        // Set the end date based on the start date
        LocalDate startDate2 = startDate1.plusMonths(1).withDayOfMonth(1);
        LocalDate endDate2 = startDate2.plusMonths(1).withDayOfMonth(1);

        // Check if the prototype has reached its maximum capacity
        Optional<Prototype> prototypeOptional = prototypeRepo.findById(proId);
        Prototype prototype = prototypeOptional.get();
        if (prototype.list.size() >= 3) {
            model.addAttribute("errorMessage", "Максимальное количество мест зарезервировано");
        } else {
            // Create a new billboard and add it to the prototype
            String status = "manager";
            boolean inWork = true;
            Billboard billboard = new Billboard(address, price, status, startDate2, endDate2, user, inWork);
            billboardRepo.save(billboard);
            prototype.list.add(billboard);
            prototypeRepo.save(prototype);
            num++;
        }

        // Get the count of billboards that are in work and have the status "online"
        String status = "online";

        return "redirect:/bulletin";
    }

    @GetMapping("/BuyBulletin")
    public String BuyBulletin(Model model) {
        return "overall";
    }
}
