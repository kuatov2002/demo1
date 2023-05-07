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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class BillboardController {
    @Autowired
    private BillboardRepo billboardRepo;
    @Autowired
    private PrototypeRepo prototypeRepo;
    @Autowired
    HttpSession session;
    @GetMapping("/pricing")
    public String Zakaz() {
        return "pricing";
    }
    String type="small";
    @PostMapping("/pricing")
    public String goToBuy(@RequestParam String type){
        this.type=type;
        return "redirect:/bulletin";
    }

    @GetMapping("/bulletin")
    public String Bulletin(Model model) {
        Iterable<Billboard> billboards = billboardRepo.findAll();
        Iterable<Prototype> prototypes = prototypeRepo.findAll();
//        prototypeRepo.deleteAll();
        // Очищаем списки объявлений у каждого прототипа перед заполнением
//        for (Prototype proto : prototypes) {
//            proto.getList().clear();
//        }
        ArrayList<Prototype> prototypesInSite=new ArrayList<>();
        for (Prototype proto:
                prototypes) {
            if (proto.type.equals(type)){
                prototypesInSite.add(proto);
            }
        }
        // Заполняем списки объявлений по адресам
        for (Billboard billboard : billboards) {
            String address = billboard.getAddress();
            for (Prototype prototype : prototypesInSite) {
                if (!prototype.getList().contains(billboard)&&prototype.Address.equals(address)&&!billboard.getStatus().equals("reject")) {
                    prototype.getList().add(billboard);
                    break;
                }
            }
        }

        model.addAttribute("billboards", billboards);
        model.addAttribute("prototypes", prototypesInSite);

        return "map";
    }


    @PostMapping("/bulletin")
    public String toMain(@AuthenticationPrincipal User user,
                         @RequestParam String address,
                         @RequestParam String price,
                         @RequestParam Long proId,
                         @RequestParam String start,
                         @RequestParam String end,

                         Model model) {
        if (start.length()==0||end.length()==0){
            session.setAttribute("errorMessage","Error: fill in all fields");
            return "redirect:/bulletin";
        }
        System.out.println(session.getAttribute("prototypes"));
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
        Iterable<Billboard> billboards = billboardRepo.findAll();
        Iterable<Prototype> prototypes = prototypeRepo.findAll();
        ArrayList<Prototype> prototypesInSite=new ArrayList<>();
        for (Prototype proto:
                prototypes) {
            if (proto.type.equals(type)){
                prototypesInSite.add(proto);
            }
        }
        // Заполняем списки объявлений по адресам
        for (Billboard billboard : billboards) {
            String address1 = billboard.getAddress();
            for (Prototype prototype : prototypesInSite) {
                if (!prototype.getList().contains(billboard)&&prototype.Address.equals(address1)&&!billboard.getStatus().equals("reject")) {
                    prototype.getList().add(billboard);
                    break;
                }
            }
        }





        session.setAttribute("errorMessage","");

        Optional<Prototype> prototypeOptional = prototypeRepo.findById(proId);
        Prototype prototype = prototypeOptional.get();

        for (Billboard bill:
            prototype.list) {
//                System.out.println(startDate.isEqual(bill.getStartDate1()));
                if(endDate.isEqual(bill.getEndDate1())||(startDate.isEqual(bill.getStartDate1())||(startDate.isAfter(bill.getStartDate1())&&startDate.isBefore(bill.getEndDate1()))||(endDate.isAfter(bill.getStartDate1())&&endDate.isBefore(bill.getEndDate1())))){
                session.setAttribute("errorMessage","Your range intersects with another");
                return "redirect:/bulletin";
            }
        }
        long monthsBetween = ChronoUnit.MONTHS.between(
                LocalDate.parse("2016-08-31").withDayOfMonth(1),
                LocalDate.parse("2016-11-30").withDayOfMonth(1));
        System.out.println(monthsBetween);
        if (prototype.list.size() >= 11) {
            model.addAttribute("errorMessage", "Maximum number of seats reserved");
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
        System.out.println(prototype.getList().toString());
        System.out.println(prototypesInSite.get(0).getList().toString());
        return "redirect:/bulletin";
    }

    @GetMapping("/BuyBulletin")
    public String BuyBulletin(Model model) {
        return "overall";
    }
}
