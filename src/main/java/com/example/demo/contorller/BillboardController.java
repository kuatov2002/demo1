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

import javax.servlet.http.HttpSession;
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
                         @RequestParam String start,
                         @RequestParam String end,
                         HttpSession session,
                         Model model) {
        if (start.length()==0||start.length()==0){
            session.setAttribute("errorMessage","Error: fill in all fields");
            return "redirect:/bulletin";
        }

        LocalDate startDate = LocalDate.of(Integer.valueOf(start.substring(0,4)), Integer.valueOf(start.substring(5,7)), 1);
        LocalDate endDate = LocalDate.of(Integer.valueOf(end.substring(0,4)), Integer.valueOf(end.substring(5,7)), 1);
        if (endDate.isBefore(startDate)){
            session.setAttribute("errorMessage","Finish date must be after start date");
            return "redirect:/bulletin";
        }

        if (startDate.isBefore(LocalDate.now())){
            session.setAttribute("errorMessage","Start date must be after current date");
            return "redirect:/bulletin";
        }





        session.setAttribute("errorMessage","");
        Iterable<Billboard> billboards = billboardRepo.findAll();
        Iterable<Prototype> prototypes = prototypeRepo.findAll();

        Optional<Prototype> prototypeOptional = prototypeRepo.findById(proId);
        Prototype prototype = prototypeOptional.get();

        for (Billboard bill:
             prototype.list) {
            System.out.println(bill.getEndDate1()+" "+bill.getStartDate1());
            if((startDate.isAfter(bill.getStartDate1())&&startDate.isBefore(bill.getEndDate1()))||(endDate.isAfter(bill.getStartDate1())&&endDate.isBefore(bill.getEndDate1()))){
                session.setAttribute("errorMessage","Your range intersects with another");
                return "redirect:/bulletin";
            }
        }

        if (prototype.list.size() >= 3) {
//            model.addAttribute("errorMessage", "Maximum number of seats reserved");
        } else {
            // Create a new billboard and add it to the prototype
            String status = "onReview";
            boolean inWork = true;
            Billboard billboard = new Billboard(address, price, status, startDate, endDate, user, inWork);
            billboardRepo.save(billboard);
            prototype.list.add(billboard);
            prototypeRepo.save(prototype);
        }

        // Get the count of billboards that are in work and have the status "online"
//        String status = "o";

        return "redirect:/bulletin";
    }

    @GetMapping("/BuyBulletin")
    public String BuyBulletin(Model model) {
        return "overall";
    }
}
